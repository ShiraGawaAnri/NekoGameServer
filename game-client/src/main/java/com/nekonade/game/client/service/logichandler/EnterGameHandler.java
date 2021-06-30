package com.nekonade.game.client.service.logichandler;

import com.nekonade.game.client.command.IMClientCommand;
import com.nekonade.game.client.service.GameClientBoot;
import com.nekonade.game.client.service.handler.GameClientChannelContext;
import com.nekonade.network.param.game.message.im.IMSendIMMsgeResponse;
import com.nekonade.network.param.game.message.im.SendIMMsgeResponse;
import com.nekonade.network.param.game.message.neko.DoBuyArenaChallengeTimesMsgResponse;
import com.nekonade.network.param.game.message.neko.DoEnterGameMsgResponse;
import com.nekonade.network.param.game.message.neko.GetPlayerSelfMsgRequest;
import com.nekonade.network.param.game.messagedispatcher.GameMessageHandler;
import com.nekonade.network.param.game.messagedispatcher.GameMessageMapping;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GameMessageHandler
public class EnterGameHandler {

    @Autowired
    private GameClientBoot gameClientBoot;

    private static final Logger logger = LoggerFactory.getLogger(EnterGameHandler.class);

    @SneakyThrows
    @GameMessageMapping(DoEnterGameMsgResponse.class)
    public void enterGameResponse(DoEnterGameMsgResponse response, GameClientChannelContext ctx) {
        logger.debug("进入游戏成功：{}", response.getBodyObj().getNickname());
        IMClientCommand.enteredGame = true;
        Thread.sleep(1000);
        GetPlayerSelfMsgRequest getPlayerSelfMsgRequest = new GetPlayerSelfMsgRequest();
        gameClientBoot.getChannel().writeAndFlush(getPlayerSelfMsgRequest);
//        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for(int i = 0;i < 10 ; i++){
//            executorService.execute(()-> gameClientBoot.getChannel().writeAndFlush(getPlayerSelfMsgRequest));
//            executorService.execute(()-> gameClientBoot.getChannel().writeAndFlush(enterGameMsgRequest));
        }
    }

    @GameMessageMapping(DoBuyArenaChallengeTimesMsgResponse.class)
    public void buyArenaChallengeTimes(DoBuyArenaChallengeTimesMsgResponse response, GameClientChannelContext ctx) {
        logger.debug("购买竞技场挑战次数成功");
    }

    @GameMessageMapping(SendIMMsgeResponse.class)
    public void chatMsg(SendIMMsgeResponse response, GameClientChannelContext ctx) {
        logger.info("聊天信息-{}说：{}", response.getBodyObj().getSender(), response.getBodyObj().getText());
    }

    @GameMessageMapping(IMSendIMMsgeResponse.class)
    public void chatMsgIM(IMSendIMMsgeResponse response, GameClientChannelContext ctx) {
        logger.info("聊天信息-{}说：{}", response.getBodyObj().getSender(), response.getBodyObj().getChat());
    }
}
