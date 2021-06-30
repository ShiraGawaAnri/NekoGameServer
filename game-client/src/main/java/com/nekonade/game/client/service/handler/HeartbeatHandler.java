package com.nekonade.game.client.service.handler;

import com.nekonade.network.param.game.message.HeartbeatMsgRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    private boolean confirmSuccess;

    public void setConfirmSuccess(boolean confirmSuccess) {
        this.confirmSuccess = confirmSuccess;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {// 接收写出空闲事件，说明一定时间内没有向服务器发送消息了。
                if (confirmSuccess) {//连接认证成功之后再发送
                    HeartbeatMsgRequest request = new HeartbeatMsgRequest();
                    ctx.writeAndFlush(request);// 发送心跳事件
                }
            }
        }
    }
}
