package com.nekonade.game.client.service.logichandler;

import com.nekonade.common.utils.GameBase64Utils;
import com.nekonade.common.utils.GameTimeUtils;
import com.nekonade.common.utils.RSAUtils;
import com.nekonade.game.client.command.IMClientCommand;
import com.nekonade.game.client.service.GameClientBoot;
import com.nekonade.game.client.service.GameClientConfig;
import com.nekonade.game.client.service.handler.GameClientChannelContext;
import com.nekonade.game.client.service.handler.HeartbeatHandler;
import com.nekonade.game.client.service.handler.codec.DecodeHandler;
import com.nekonade.game.client.service.handler.codec.EncodeHandler;
import com.nekonade.network.param.game.message.DoConfirmMsgResponse;
import com.nekonade.network.param.game.message.HeartbeatMsgResponse;
import com.nekonade.network.param.game.message.neko.DoEnterGameMsgRequest;
import com.nekonade.network.param.game.message.neko.error.GameErrorMsgResponse;
import com.nekonade.network.param.game.message.neko.error.GameGatewayErrorMsgResponse;
import com.nekonade.network.param.game.message.neko.error.GameNotificationMsgResponse;
import com.nekonade.network.param.game.messagedispatcher.GameMessageHandler;
import com.nekonade.network.param.game.messagedispatcher.GameMessageMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;

/**
 * @ClassName: SystemMessageHandler
 * @Author: Lily
 * @Description: 处理从服务端返回的系统级消息
 * @Date: 2021/6/29
 * @Version: 1.0
 */
@GameMessageHandler
public class SystemMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(SystemMessageHandler.class);

    @Autowired
    private GameClientConfig gameClientConfig;

    @Autowired
    private IMClientCommand imClientCommand;

    @Autowired
    private GameClientBoot gameClientBoot;

    @GameMessageMapping(DoConfirmMsgResponse.class)
    public void confirmResponse(DoConfirmMsgResponse response, GameClientChannelContext ctx) {
        String encryptAesKey = response.getBodyObj().getSecretKey();
        byte[] content = Base64Utils.decodeFromString(encryptAesKey);
        try {
            byte[] privateKey = GameBase64Utils.decodeFromString(gameClientConfig.getRsaPrivateKey());
            byte[] valueBytes = RSAUtils.decryptByPrivateKey(content, privateKey);
            String value = new String(valueBytes);// 得到明文的aes加密密钥
            DecodeHandler decodeHandler = (DecodeHandler) ctx.getChannel().pipeline().get("DecodeHandler");
            decodeHandler.setAesSecretKey(value);// 把密钥给解码Handler
            EncodeHandler encodeHandler = (EncodeHandler) ctx.getChannel().pipeline().get("EncodeHandler");
            encodeHandler.setAesSecretKey(value);// 把密钥给编码Handler
            HeartbeatHandler heartbeatHandler = (HeartbeatHandler) ctx.getChannel().pipeline().get("HeartbeatHandler");
            heartbeatHandler.setConfirmSuccess(true);
            logger.debug("连接认证成功,channelId:{} 等待1.5s进行连接", ctx.getChannel().id().asShortText());
            Thread.sleep(1500);//这里睡眠1s是由于gateway定义了限流器的类型导致
            DoEnterGameMsgRequest request = new DoEnterGameMsgRequest();
            gameClientBoot.getChannel().writeAndFlush(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GameMessageMapping(HeartbeatMsgResponse.class)
    public void heartbeatResponse(HeartbeatMsgResponse response, GameClientChannelContext ctx) {
        logger.trace("服务器心跳返回，当前服务器时间：{}", GameTimeUtils.getStringDate(response.getBodyObj().getServerTime()));
    }

    @GameMessageMapping(GameGatewayErrorMsgResponse.class)
    public void gameGatewayErrorMsgResponse(GameGatewayErrorMsgResponse response, GameClientChannelContext ctx) {
        logger.warn("网关返回报错{}", response.bodyToString());
    }

    @GameMessageMapping(GameErrorMsgResponse.class)
    public void gameErrorMsgResponse(GameErrorMsgResponse response, GameClientChannelContext ctx) {
        logger.warn("服务器返回报错{}", response.bodyToString());
    }

    @GameMessageMapping(GameNotificationMsgResponse.class)
    public void gameNotificationMsgResponse(GameNotificationMsgResponse response, GameClientChannelContext ctx) {
        logger.warn("弹框提醒{}", response.bodyToString());
    }
}
