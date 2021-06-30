package com.nekonade.game.client.service.logichandler;

import com.nekonade.game.client.service.handler.GameClientChannelContext;
import com.nekonade.network.param.game.message.FirstMsgResponse;
import com.nekonade.network.param.game.message.SecondMsgResponse;
import com.nekonade.network.param.game.message.ThirdMsgResponse;
import com.nekonade.network.param.game.messagedispatcher.GameMessageHandler;
import com.nekonade.network.param.game.messagedispatcher.GameMessageMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: TestMessageHandler
 * @Author: Lily
 * @Description: 测试用
 * @Date: 2021/6/29
 * @Version: 1.0
 */
@GameMessageHandler
public class TestMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(TestMessageHandler.class);

    @GameMessageMapping(FirstMsgResponse.class)
    public void firstMessage(FirstMsgResponse response, GameClientChannelContext ctx) {
        logger.info("收到服务器响应:{}", response.getServerTime());
    }

    @GameMessageMapping(SecondMsgResponse.class)
    public void secondMessage(SecondMsgResponse response, GameClientChannelContext ctx) {
        logger.info("second msg response :{}", response.getBodyObj().getResult1());
    }

    @GameMessageMapping(ThirdMsgResponse.class)
    public void thirdMessage(ThirdMsgResponse response, GameClientChannelContext ctx) {
        logger.info("third msg response:{}", response.getResponseBody().getValue1());
    }
}
