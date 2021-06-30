package com.nekonade.gamegateway.server.handler;


import com.nekonade.common.cloud.PlayerServiceInstance;
import com.nekonade.common.cloud.RaidBattleServerInstance;
import com.nekonade.common.cloud.BasicServiceInstance;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.model.ErrorResponseEntity;
import com.nekonade.common.error.exceptions.GameErrorException;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.utils.JWTUtil;
import com.nekonade.common.utils.MessageUtils;
import com.nekonade.common.utils.NettyUtils;
import com.nekonade.common.utils.TopicUtil;
import com.nekonade.gamegateway.config.GatewayServerConfig;
import com.nekonade.network.param.game.GameMessageService;
import com.nekonade.network.param.game.bus.GameMessageInnerDecoder;
import com.nekonade.common.gameMessage.ConstMessageGroup;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.network.param.game.message.neko.error.GameGatewayErrorMsgResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.kafka.core.KafkaTemplate;

public class DispatchGameMessageHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DispatchGameMessageHandler.class);
    private final PlayerServiceInstance playerServiceInstance;// 注入业务服务管理类，从这里获取负载均衡的服务器信息
    private final RaidBattleServerInstance raidBattleServerInstance;
    private final GatewayServerConfig gatewayServerConfig; // 注入游戏网关服务配置信息。
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private JWTUtil.TokenBody tokenBody;
    private final GameMessageService gameMessageService;

    public DispatchGameMessageHandler(KafkaTemplate<String, byte[]> kafkaTemplate, PlayerServiceInstance playerServiceInstance, RaidBattleServerInstance raidBattleServerInstance, GatewayServerConfig gatewayServerConfig, GameMessageService gameMessageService) {
        this.playerServiceInstance = playerServiceInstance;
        this.raidBattleServerInstance = raidBattleServerInstance;
        this.gatewayServerConfig = gatewayServerConfig;
        this.kafkaTemplate = kafkaTemplate;
        this.gameMessageService = gameMessageService;
    }

    public static <E extends BasicServiceInstance<ID,G>,ID,G extends ApplicationEvent> void dispatchMessage(KafkaTemplate<String, byte[]> kafkaTemplate, ChannelHandlerContext ctx, E serviceInstance, long playerId,ID id, int serviceId, String clientIp, GameMessagePackage gameMessagePackage, GatewayServerConfig gatewayServerConfig) {
        String specificTopic = gatewayServerConfig.getBusinessGameMessageTopic();
        dispatchMessage(kafkaTemplate,ctx,serviceInstance,playerId,id,serviceId,clientIp,gameMessagePackage,gatewayServerConfig,specificTopic);
    }

    public static <E extends BasicServiceInstance<ID,G>,ID,G extends ApplicationEvent> void dispatchMessage(KafkaTemplate<String, byte[]> kafkaTemplate, ChannelHandlerContext ctx, E serviceInstance,long playerId, ID id, int serviceId, String clientIp, GameMessagePackage gameMessagePackage, GatewayServerConfig gatewayServerConfig,String specificTopic) {
        EventExecutor executor = ctx.executor();
        Promise<Integer> promise = new DefaultPromise<>(executor);
        GameMessageHeader header = gameMessagePackage.getHeader();
        int serverId = gatewayServerConfig.getServerId();
        serviceInstance.selectServerId(id, serviceId, promise).addListener((GenericFutureListener<Future<Integer>>) future -> {
            if (future.isSuccess()) {
                Integer toServerId = future.get();
                header.setToServerId(toServerId);
                header.setFromServerId(serverId);
                header.setPlayerId(playerId);
                header.getAttribute().setClientIp(clientIp);
                String topic = TopicUtil.generateTopic(specificTopic, toServerId);// 动态创建与业务服务交互的消息总线Topic
                byte[] value = GameMessageInnerDecoder.sendMessageV2(gameMessagePackage);// 向消息总线服务发布客户端请求消息。
                String key = MessageUtils.kafkaKeyCreate(header);
                ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, value);
                kafkaTemplate.send(record);
                //logger.info("消息发送成功 {}\r\n", header);
            } else {
                Throwable cause = future.cause();
                GameErrorException exception;
                if (cause instanceof GameErrorException) {
                    exception = (GameErrorException) cause;
                } else {
                    exception = GameErrorException.newBuilder(EnumCollections.CodeMapper.GameGatewayError.GAME_GATEWAY_ERROR).build();
                }

                GameMessagePackage returnPackage = new GameMessagePackage();
                GameGatewayErrorMsgResponse response = new GameGatewayErrorMsgResponse();
                ErrorResponseEntity errorEntity = new ErrorResponseEntity();
                errorEntity.setErrorCode(exception.getError().getErrorCode());
                errorEntity.setErrorMsg(exception.getError().getErrorDesc());
                response.getBodyObj().setError(errorEntity);
//                response.getBodyObj().setErrorCode(exception.getError().getErrorCode());
//                response.getBodyObj().setErrorMessage(exception.getError().getErrorDesc());
                returnPackage.setHeader(response.getHeader());
                returnPackage.setBody(response.body());
                ctx.writeAndFlush(returnPackage);
                logger.error("消息发送失败", cause);
            }
        });
    }




    /*public static void dispatchMessage(KafkaTemplate<String, byte[]> kafkaTemplate, ChannelHandlerContext ctx, PlayerServiceInstance playerServiceInstance, long playerId, int serviceId, String clientIp, GameMessagePackage gameMessagePackage, GatewayServerConfig gatewayServerConfig) {
        EventExecutor executor = ctx.executor();
        Promise<Integer> promise = new DefaultPromise<>(executor);
        GameMessageHeader header = gameMessagePackage.getHeader();
        playerServiceInstance.selectServerId(playerId, serviceId, promise).addListener((GenericFutureListener<Future<Integer>>) future -> {
            if (future.isSuccess()) {
                Integer toServerId = future.get();
                header.setToServerId(toServerId);
                header.setFromServerId(gatewayServerConfig.getServerId());
                header.getAttribute().setClientIp(clientIp);
                header.setPlayerId(playerId);
                String topic = TopicUtil.generateTopic(gatewayServerConfig.getBusinessGameMessageTopic(), toServerId);// 动态创建与业务服务交互的消息总线Topic
                byte[] value = GameMessageInnerDecoder.sendMessageV2(gameMessagePackage);// 向消息总线服务发布客户端请求消息。
                StringBuffer key = new StringBuffer();
                key.append(playerId).append("_").append(header.getClientSeqId()).append("_").append(header.getClientSendTime());
                ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key.toString(), value);
                kafkaTemplate.send(record);
                //logger.info("消息发送成功 {}\r\n", header);
            } else {
                Throwable cause = future.cause();
                GameErrorException exception;
                if (cause instanceof GameErrorException) {
                    exception = (GameErrorException) cause;
                } else {
                    exception = GameErrorException.newBuilder(EnumCollections.CodeMapper.GameGatewayError.GAME_GATEWAY_ERROR).build();
                }

                GameMessagePackage returnPackage = new GameMessagePackage();
                GameGatewayErrorMsgResponse response = new GameGatewayErrorMsgResponse();
                ErrorResponseEntity errorEntity = new ErrorResponseEntity();
                errorEntity.setErrorCode(exception.getError().getErrorCode());
                errorEntity.setErrorMsg(exception.getError().getErrorDesc());
                response.getBodyObj().setError(errorEntity);
//                response.getBodyObj().setErrorCode(exception.getError().getErrorCode());
//                response.getBodyObj().setErrorMessage(exception.getError().getErrorDesc());
                returnPackage.setHeader(response.getHeader());
                returnPackage.setBody(response.body());
                ctx.writeAndFlush(returnPackage);
                logger.error("消息发送失败", cause);
            }
        });
    }

    public static void raidBattleDispatchMessage(KafkaTemplate<String, byte[]> kafkaTemplate, ChannelHandlerContext ctx, RaidBattleServerInstance raidBattleServerInstance, long playerId, int serviceId, String clientIp,String raidId, GameMessagePackage gameMessagePackage, GatewayServerConfig gatewayServerConfig) {
        EventExecutor executor = ctx.executor();
        Promise<Integer> promise = new DefaultPromise<>(executor);
        raidBattleServerInstance.selectRaidBattleServerId(raidId, serviceId, promise).addListener((GenericFutureListener<Future<Integer>>) future -> {
            if (future.isSuccess()) {
                Integer toServerId = future.get();
                GameMessageHeader header = gameMessagePackage.getHeader();
                header.setToServerId(toServerId);
                header.setFromServerId(gatewayServerConfig.getServerId());
                header.getAttribute().setClientIp(clientIp);
                header.setPlayerId(playerId);
                String topic = TopicUtil.generateTopic(gatewayServerConfig.getRbBusinessGameMessageTopic(), toServerId);// 动态创建与业务服务交互的消息总线Topic
                byte[] value = GameMessageInnerDecoder.sendMessageV2(gameMessagePackage);// 向消息总线服务发布客户端请求消息。

                StringBuffer key = new StringBuffer();
                key.append(playerId).append("_").append(header.getClientSeqId()).append("_").append(header.getClientSendTime());
                ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key.toString(), value);
                header.getAttribute().addLog();
                kafkaTemplate.send(record);
                //logger.info("消息发送成功 {}\r\n", header);
            } else {
                Throwable cause = future.cause();
                GameErrorException exception;
                if (cause instanceof GameErrorException) {
                    exception = (GameErrorException) cause;
                } else {
                    exception = GameErrorException.newBuilder(EnumCollections.CodeMapper.GameGatewayError.GAME_GATEWAY_ERROR).build();
                }

                GameMessagePackage returnPackage = new GameMessagePackage();
                GameGatewayErrorMsgResponse response = new GameGatewayErrorMsgResponse();
                ErrorResponseEntity errorEntity = new ErrorResponseEntity();
                errorEntity.setErrorCode(exception.getError().getErrorCode());
                errorEntity.setErrorMsg(exception.getError().getErrorDesc());
                response.getBodyObj().setError(errorEntity);
//                response.getBodyObj().setErrorCode(exception.getError().getErrorCode());
//                response.getBodyObj().setErrorMessage(exception.getError().getErrorDesc());
                returnPackage.setHeader(response.getHeader());
                returnPackage.setBody(response.body());
                ctx.writeAndFlush(returnPackage);
                logger.error("消息发送失败", cause);
            }
        });
    }*/

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        GameMessagePackage gameMessagePackage = (GameMessagePackage) msg;
        GameMessageHeader header = gameMessagePackage.getHeader();
        int serviceId = header.getServiceId();
        if (tokenBody == null) {// 如果首次通信，获取验证信息
            ConfirmHandler confirmHandler = (ConfirmHandler) ctx.channel().pipeline().get("ConfirmHandler");
            tokenBody = confirmHandler.getTokenBody();
        }
        String clientIp = NettyUtils.getRemoteIP(ctx.channel());
        String raidId = header.getAttribute().getRaidId();
        int messageId = header.getMessageId();
        int requestGroup = gameMessageService.inWhichGroup(EnumMessageType.REQUEST, messageId);
        header.getAttribute().addLog();
        /*if(StringUtils.isEmpty(raidId)){
            dispatchMessage(kafkaTemplate, ctx, playerServiceInstance, tokenBody.getPlayerId(), serviceId, clientIp, gameMessagePackage, gatewayServerConfig);
        }else if(StringUtils.isNotEmpty(raidId) && requestGroup == ConstMessageGroup.RAIDBATTLE){
            raidBattleDispatchMessage(kafkaTemplate, ctx, raidBattleServerInstance, tokenBody.getPlayerId(), serviceId, clientIp,raidId,gameMessagePackage, gatewayServerConfig);
        }*/
        if(StringUtils.isEmpty(raidId)){
            dispatchMessage(kafkaTemplate,ctx,playerServiceInstance,tokenBody.getPlayerId(),tokenBody.getPlayerId(),serviceId,clientIp,gameMessagePackage,gatewayServerConfig);
        }else if(StringUtils.isNotEmpty(raidId) && requestGroup == ConstMessageGroup.RAIDBATTLE){
            dispatchMessage(kafkaTemplate,ctx,raidBattleServerInstance,tokenBody.getPlayerId(),raidId,serviceId,clientIp,gameMessagePackage,gatewayServerConfig,gatewayServerConfig.getRbBusinessGameMessageTopic());
        }
        ctx.fireChannelRead(msg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        //logger.error("服务器异常，连接{}断开", ctx.channel().id().asShortText(), cause);
    }
}
