package com.nekonade.game.client.service.handler;

import com.nekonade.game.client.command.IMClientCommand;
import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.network.param.game.messagedispatcher.DispatchGameMessageService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: DispatchGameMessageHandler
 * @Author: Lily
 * @Description: 接收服务器响应的消息，并将消息分发到业务处理方法中
 * @Date: 2021/6/28
 * @Version: 1.0
 */
public class DispatchGameMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(DispatchGameMessageHandler.class);

    private final DispatchGameMessageService dispatchGameMessageService;

    public DispatchGameMessageHandler(DispatchGameMessageService dispatchGameMessageService) {
        this.dispatchGameMessageService = dispatchGameMessageService;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("连接断开，channelId:{}", ctx.channel().id().asShortText());
        //IMClientCommand.enteredGame = false;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        IGameMessage gameMessage = (IGameMessage) msg;
        GameClientChannelContext gameClientChannelContext = new GameClientChannelContext(ctx.channel(), gameMessage);//构造消息处理的上下文信息
        dispatchGameMessageService.callMethod(gameMessage, gameClientChannelContext);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("服务异常", cause);
    }
}
