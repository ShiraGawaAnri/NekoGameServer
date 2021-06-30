package com.nekonade.raidbattle.message.handler;

import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.raidbattle.message.channel.AbstractRaidBattleChannelHandlerContext;
import com.nekonade.raidbattle.message.channel.RaidBattleChannelInboundHandler;
import com.nekonade.raidbattle.message.channel.RaidBattleChannelOutboundHandler;
import com.nekonade.raidbattle.message.channel.RaidBattleChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RaidBattleChannelIdleStateHandler implements RaidBattleChannelInboundHandler, RaidBattleChannelOutboundHandler {
    private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);// 延迟事件的延迟时间的最小值
    private final long readerIdleTimeNanos;// 读取消息的空闲时间，单位纳秒
    private final long writerIdleTimeNanos;// 写出消息的空闲时间，单位纳秒
    private final long allIdleTimeNanos; // 读取和写出消息的空闲时间，单位纳秒

    private ScheduledFuture<?> readerIdleTimeout; // 读取消息的超时延时检测事件
    private long lastReadTime; // 最近一次读取消息的时间
    private ScheduledFuture<?> writerIdleTimeout; // 写出消息的超时延时检测事件
    private long lastWriteTime; // 最近一次写出消息的时间
    private ScheduledFuture<?> allIdleTimeout; // 读写消息的超时检测事件
    private byte state; // 0 - none, 1 - initialized, 2 - destroyed
    // 要不然会有这样的情况，虽然RaidBattleChannel已被移除，但是当定时事件执行时，又会创建一个新的定时事件，导致这个对象不会被回收

    public RaidBattleChannelIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {

        this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS);
    }

    public RaidBattleChannelIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        if (readerIdleTime <= 0) {
            readerIdleTimeNanos = 0;
        } else {
            readerIdleTimeNanos = Math.max(unit.toNanos(readerIdleTime), MIN_TIMEOUT_NANOS);
        }
        if (writerIdleTime <= 0) {
            writerIdleTimeNanos = 0;
        } else {
            writerIdleTimeNanos = Math.max(unit.toNanos(writerIdleTime), MIN_TIMEOUT_NANOS);
        }
        if (allIdleTime <= 0) {
            allIdleTimeNanos = 0;
        } else {
            allIdleTimeNanos = Math.max(unit.toNanos(allIdleTime), MIN_TIMEOUT_NANOS);
        }
    }

    @Override
    public void exceptionCaught(AbstractRaidBattleChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void writeAndFlush(AbstractRaidBattleChannelHandlerContext ctx, IGameMessage msg, RaidBattleChannelPromise promise) throws Exception {
        if (writerIdleTimeNanos > 0 || allIdleTimeNanos > 0) {
            this.lastWriteTime = this.ticksInNanos();
        }
        ctx.writeAndFlush(msg, promise);
    }

    @Override
    public void channelRegister(AbstractRaidBattleChannelHandlerContext ctx, String raidId, RaidBattleChannelPromise promise) {
        initialize(ctx);
        ctx.fireChannelRegistered(raidId, promise);
    }

    @Override
    public void channelInactive(AbstractRaidBattleChannelHandlerContext ctx) throws Exception {
        destroy();
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(AbstractRaidBattleChannelHandlerContext ctx, Object msg) throws Exception {
        if (readerIdleTimeNanos > 0 || allIdleTimeNanos > 0) {
            this.lastReadTime = this.ticksInNanos();// 记录最后一次读取操作的时间
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void userEventTriggered(AbstractRaidBattleChannelHandlerContext ctx, Object evt, Promise<Object> promise) throws Exception {
        ctx.fireUserEventTriggered(evt, promise);
    }

    private void initialize(AbstractRaidBattleChannelHandlerContext ctx) {
        switch (state) {
            case 1:
            case 2:
                return;
        }
        state = 1;
        lastReadTime = lastWriteTime = ticksInNanos();
        if (readerIdleTimeNanos > 0) {// 初始化创建读取消息事件检测延时任务
            readerIdleTimeout = schedule(ctx, new ReaderIdleTimeoutTask(ctx), readerIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
        if (writerIdleTimeNanos > 0) {// 初始化创建写出消息事件检测延时任务
            writerIdleTimeout = schedule(ctx, new WriterIdleTimeoutTask(ctx), writerIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
        if (allIdleTimeNanos > 0) {// 初始化创建读取和写出消息事件检测延时任务
            allIdleTimeout = schedule(ctx, new AllIdleTimeoutTask(ctx), allIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
    }

    long ticksInNanos() {
        return System.nanoTime();// 获取当前时间的纳秒
    }

    ScheduledFuture<?> schedule(AbstractRaidBattleChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit) {
        return ctx.executor().schedule(task, delay, unit);// 创建延时任务
    }

    private void destroy() {// 销毁定时事件任务
        state = 2;
        if (readerIdleTimeout != null) {
            readerIdleTimeout.cancel(false);
            readerIdleTimeout = null;
        }
        if (writerIdleTimeout != null) {
            writerIdleTimeout.cancel(false);
            writerIdleTimeout = null;
        }
        if (allIdleTimeout != null) {
            allIdleTimeout.cancel(false);
            allIdleTimeout = null;
        }
    }

    protected void channelIdle(AbstractRaidBattleChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        ctx.fireUserEventTriggered(evt, null);// 发送空闲事件
    }

    protected IdleStateEvent newIdleStateEvent(IdleState state) {// 获取空闲事件类型
        switch (state) {
            case ALL_IDLE:
                return IdleStateEvent.ALL_IDLE_STATE_EVENT;
            case READER_IDLE:
                return IdleStateEvent.READER_IDLE_STATE_EVENT;
            case WRITER_IDLE:
                return IdleStateEvent.WRITER_IDLE_STATE_EVENT;
            default:
                throw new IllegalArgumentException("Unhandled: state=" + state);
        }
    }

    @Override
    public void close(AbstractRaidBattleChannelHandlerContext ctx, RaidBattleChannelPromise promise) {
        ctx.close(promise);
    }

    @Override
    public void writeRPCMessage(AbstractRaidBattleChannelHandlerContext ctx, IGameMessage gameMessage, Promise<IGameMessage> callback) {
        ctx.writeRPCMessage(gameMessage, callback);
    }

    @Override
    public void channelReadRPCRequest(AbstractRaidBattleChannelHandlerContext ctx, IGameMessage msg) throws Exception {
        ctx.fireChannelReadRPCRequest(msg);

    }

    private abstract static class AbstractIdleTask implements Runnable {// 公共抽象任务

        private final AbstractRaidBattleChannelHandlerContext ctx;

        AbstractIdleTask(AbstractRaidBattleChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!ctx.gameChannel().isRegistered()) {
                return;
            }

            run(ctx);
        }

        protected abstract void run(AbstractRaidBattleChannelHandlerContext ctx);
    }

    private final class ReaderIdleTimeoutTask extends AbstractIdleTask {// 读取消息检测任务

        ReaderIdleTimeoutTask(AbstractRaidBattleChannelHandlerContext ctx) {
            super(ctx);
        }

        @Override
        protected void run(AbstractRaidBattleChannelHandlerContext ctx) {
            long nextDelay = readerIdleTimeNanos;
            nextDelay -= ticksInNanos() - lastReadTime;
            if (nextDelay <= 0) {// 说明读取事件超时，发送空闲事件，并创建新的延迟任务，用于下次超时检测
                readerIdleTimeout = schedule(ctx, this, readerIdleTimeNanos, TimeUnit.NANOSECONDS);
                try {
                    IdleStateEvent event = newIdleStateEvent(IdleState.READER_IDLE);
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // 没有超时，从上次读取的时间起，计时计算下次超时检测
                readerIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }

    private final class WriterIdleTimeoutTask extends AbstractIdleTask {

        WriterIdleTimeoutTask(AbstractRaidBattleChannelHandlerContext ctx) {
            super(ctx);
        }

        @Override
        protected void run(AbstractRaidBattleChannelHandlerContext ctx) {

            long lastWriteTime = RaidBattleChannelIdleStateHandler.this.lastWriteTime;
            long nextDelay = writerIdleTimeNanos - (ticksInNanos() - lastWriteTime);
            if (nextDelay <= 0) {
                // Writer is idle - set a new timeout and notify the callback.
                writerIdleTimeout = schedule(ctx, this, writerIdleTimeNanos, TimeUnit.NANOSECONDS);

                try {
                    IdleStateEvent event = newIdleStateEvent(IdleState.WRITER_IDLE);
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                writerIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }

    private final class AllIdleTimeoutTask extends AbstractIdleTask {

        AllIdleTimeoutTask(AbstractRaidBattleChannelHandlerContext ctx) {
            super(ctx);
        }

        @Override
        protected void run(AbstractRaidBattleChannelHandlerContext ctx) {

            long nextDelay = allIdleTimeNanos;
            nextDelay -= ticksInNanos() - Math.max(lastReadTime, lastWriteTime);
            if (nextDelay <= 0) {
                // Both reader and writer are idle - set a new timeout and
                // notify the callback.
                allIdleTimeout = schedule(ctx, this, allIdleTimeNanos, TimeUnit.NANOSECONDS);
                try {
                    IdleStateEvent event = newIdleStateEvent(IdleState.ALL_IDLE);
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                allIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }


}
