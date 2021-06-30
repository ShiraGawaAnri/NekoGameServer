package com.nekonade.gamegateway.server.handler;

import com.nekonade.common.cloud.PlayerServiceInstance;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.utils.AESUtils;
import com.nekonade.common.utils.JWTUtil;
import com.nekonade.common.utils.NettyUtils;
import com.nekonade.common.utils.RSAUtils;
import com.nekonade.gamegateway.config.GatewayServerConfig;
import com.nekonade.gamegateway.server.ChannelService;
import com.nekonade.gamegateway.server.handler.codec.DecodeHandler;
import com.nekonade.gamegateway.server.handler.codec.EncodeHandler;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.network.param.game.message.DoConfirmMsgRequest;
import com.nekonade.network.param.game.message.DoConfirmMsgResponse;
import com.nekonade.network.param.game.message.neko.PassConnectionStatusMsgRequest;
import com.nekonade.network.param.game.message.neko.TriggerConnectionInactive;
import io.jsonwebtoken.ExpiredJwtException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ConfirmHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ConfirmHandler.class);
    private final PlayerServiceInstance businessServerService;// 注入业务服务管理类，从这里获取负载均衡的服务器信息
    private final GatewayServerConfig serverConfig;// 注入服务端配置
    private final ChannelService channelService;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;// 其实将来可以将消息的发送封装到自己的类里面，这样就可以屏蔽底层消息队列的使用了
    private boolean confirmSuccess = false;// 标记连接是否认证成功
    private ScheduledFuture<?> future;// 定时器的返回值
    private JWTUtil.TokenBody tokenBody;

    public ConfirmHandler(GatewayServerConfig serverConfig, ChannelService channelService, KafkaTemplate<String, byte[]> kafkaTemplate, ApplicationContext applicationContext) {
        this.serverConfig = serverConfig;
        this.channelService = channelService;
        businessServerService = applicationContext.getBean(PlayerServiceInstance.class);
        this.kafkaTemplate = kafkaTemplate;
    }

    public JWTUtil.TokenBody getTokenBody() {
        return tokenBody;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {// 此方法会在连接建立成功channel注册之后调用
        logger.debug("客户端 {} 连接成功，channelId:{}", NettyUtils.getRemoteIP(ctx.channel()), ctx.channel().id().asShortText());
        int delay = serverConfig.getWaitConfirmTimeoutSecond();// 从配置中获取延迟时间
        future = ctx.channel().eventLoop().schedule(() -> {
            if (!confirmSuccess) {// 如果没有认证成功，则关闭连接。
                logger.debug("连接认证超时，断开连接，channelId:{}", ctx.channel().id().asShortText());
                ctx.close();
            }
        }, delay, TimeUnit.SECONDS);
        ctx.fireChannelActive();
    }

    private void repeatedConnect() {
        if (tokenBody != null) {
            Channel existChannel = this.channelService.getChannel(tokenBody.getPlayerId());
            if (existChannel != null) {
                // 如果检测到同一个账号创建了多个连接，则把旧连接关闭，保留新连接。
                DoConfirmMsgResponse response = new DoConfirmMsgResponse();
                response.getHeader().setErrorCode(EnumCollections.CodeMapper.GameGatewayError.REPEATED_CONNECT.getErrorCode());
                GameMessagePackage returnPackage = new GameMessagePackage();
                returnPackage.setHeader(response.getHeader());
                returnPackage.setBody(response.body());
                existChannel.writeAndFlush(returnPackage);// 在关闭之后，给这个连接返回一条提示信息，告诉客户端账号可能异地登陆了。
                existChannel.close();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (future != null) {
            future.cancel(true);// 如果连接关闭了，取消息定时检测任务。
        }
        if (tokenBody != null) { // 连接断开之后，移除连接
            long playerId = tokenBody.getPlayerId();
            this.channelService.removeChannel(playerId, ctx.channel());// 调用移除，否则出现内存泄漏的问题。
            String ip = NettyUtils.getRemoteIP(ctx.channel());
            this.sendConnectStatusMsg(false, ctx, ip);
            this.sendClientInactive(ctx, ip);
        }
        ctx.fireChannelInactive();// 接着告诉下面的Handler

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        GameMessagePackage gameMessagePackage = (GameMessagePackage) msg;
        int messageId = gameMessagePackage.getHeader().getMessageId();
        ChannelId id = ctx.channel().id();
        if (messageId == EnumCollections.CodeMapper.GatewayMessageCode.ConnectConfirm.getMessageId()) {// 如果是认证消息，在这里处理
            DoConfirmMsgRequest request = new DoConfirmMsgRequest();
            request.read(gameMessagePackage.getBody());// 反序列化消息内容
            String token = request.getBodyObj().getToken();
            DoConfirmMsgResponse response = new DoConfirmMsgResponse();
            if (StringUtils.isEmpty(token)) {// 检测token
                logger.error("token为空，直接关闭连接");
                ctx.close();
            } else {
                try {
                    tokenBody = JWTUtil.getTokenBodyV2(token);// 解析token里面的内容，如果解析失败，会抛出异常
                    this.confirmSuccess = true;// 标记认证成功
                    this.repeatedConnect();// 检测重复连接
                    channelService.addChannel(tokenBody.getPlayerId(), ctx.channel());// 加入连接管理
                    String aesSecretKey = AESUtils.createSecret(tokenBody.getUserId(), tokenBody.getServerId());// 生成此连接的AES密钥
                    // 将对称加密密钥分别设置到编码和解码的handler中。
                    DecodeHandler decodeHandler = ctx.channel().pipeline().get(DecodeHandler.class);
                    decodeHandler.setAesSecretKey(aesSecretKey);
                    EncodeHandler encodeHandler = ctx.channel().pipeline().get(EncodeHandler.class);
                    encodeHandler.setAesSecret(aesSecretKey);
                    byte[] clientPublicKey = this.getClientRsaPublickKey();
                    byte[] encryptAesKey = RSAUtils.encryptByPublicKey(aesSecretKey.getBytes(), clientPublicKey);// 使用客户端的公钥加密对称加密密钥
                    //logger.info("给Player{}发送了秘钥{}",tokenBody.getPlayerId(),aesSecretKey);
                    response.getBodyObj().setSecretKey(Base64Utils.encodeToString(encryptAesKey));// 返回给客户端
                    response.getHeader().setPlayerId(tokenBody.getPlayerId());
                    response.getHeader().setClientSendTime(gameMessagePackage.getHeader().getClientSendTime());

                    GameMessagePackage returnPackage = new GameMessagePackage();
                    returnPackage.setHeader(response.getHeader());
                    returnPackage.setBody(response.body());
                    ctx.writeAndFlush(returnPackage);

                    // 通知各个服务，某个用户连接成功
                    String ip = NettyUtils.getRemoteIP(ctx.channel());
                    this.sendConnectStatusMsg(true, ctx, ip);
                } catch (Exception e) {
                    if (e instanceof ExpiredJwtException) {// 告诉客户端token过期，它客户端重新获取并重新连接
                        response.getHeader().setErrorCode(EnumCollections.CodeMapper.GameGatewayError.TOKEN_EXPIRE.getErrorCode());
                        ctx.writeAndFlush(response);
                        ctx.close();
                        logger.warn("token过期，关闭连接");
                    } else {
                        logger.error("token解析异常，直接关闭连接", e);
                        ctx.close();
                    }
                }
            }
        } else {
            if (!confirmSuccess) {
                logger.warn("连接未认证，不处理任务消息，关闭连接，channelId:{}", id.asShortText());
                ctx.close();
                return;
            }
            ((GameMessagePackage) msg).getHeader().setPlayerId(tokenBody.getPlayerId());
            ctx.fireChannelRead(msg);// 如果不是认证消息，则向下发送消息，让后面的Handler去处理，如果不下发，后面的Handler将接收不到消息。
        }
    }

    private void sendConnectStatusMsg(boolean connect, ChannelHandlerContext ctx, String clientIp) {
        PassConnectionStatusMsgRequest request = new PassConnectionStatusMsgRequest();
        request.getBodyObj().setConnect(connect);
        long playerId = tokenBody.getPlayerId();
        request.getHeader().setClientSendTime(System.currentTimeMillis());
        request.getHeader().setServerSendTime(System.currentTimeMillis());
        broadcastMsgRequest(ctx, clientIp, playerId, request.body(), request.getHeader());
    }

    private void sendClientInactive(ChannelHandlerContext ctx, String clientIp) {
        TriggerConnectionInactive request = new TriggerConnectionInactive();
        long playerId = tokenBody.getPlayerId();
        request.getBodyObj().setPlayerId(playerId);
        request.getBodyObj().setServerId(this.serverConfig.getServerId());
        request.getHeader().setClientSendTime(System.currentTimeMillis());
        request.getHeader().setServerSendTime(System.currentTimeMillis());
        broadcastMsgRequest(ctx, clientIp, playerId, request.body(), request.getHeader(), true);
    }


    private void broadcastMsgRequest(ChannelHandlerContext ctx, String clientIp, long playerId, byte[] body, GameMessageHeader header) {
        broadcastMsgRequest(ctx, clientIp, playerId, body, header, false);
    }

    private void broadcastMsgRequest(ChannelHandlerContext ctx, String clientIp, long playerId, byte[] body, GameMessageHeader header, Boolean ignoreServiceId) {
        Set<Integer> allServiceId = businessServerService.getAllServiceId();
        for (Integer serviceId : allServiceId) {
            //通知所有的服务，该用户已掉线
            if (!ignoreServiceId) {
                if (!serviceId.equals(header.getServiceId())) {
                    continue;
                }
            }
            GameMessagePackage gameMessagePackage = new GameMessagePackage();
            gameMessagePackage.setBody(body);
            gameMessagePackage.setHeader(header);
            DispatchGameMessageHandler.dispatchMessage(kafkaTemplate, ctx, businessServerService,playerId, playerId, serviceId, clientIp, gameMessagePackage, serverConfig);
        }
    }

    // 从token中获取客户端的公钥
    private byte[] getClientRsaPublickKey() {
        String publickKey = tokenBody.getParam()[1];// 获取客户端的公钥字符串。
        return Base64Utils.decodeFromString(publickKey);
    }


}
