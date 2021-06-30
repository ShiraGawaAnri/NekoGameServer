package com.nekonade.raidbattle.message.channel;

import com.nekonade.common.gameMessage.IGameMessage;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PromiseNotificationUtil;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public abstract class AbstractRaidBattleChannelHandlerContext {
    static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultChannelPipeline.class);
    final EventExecutor executor;
    private final boolean inbound;
    private final boolean outbound;
    private final RaidBattleChannelPipeline pipeline;
    private final String name;
    volatile AbstractRaidBattleChannelHandlerContext next;
    volatile AbstractRaidBattleChannelHandlerContext prev;

    public AbstractRaidBattleChannelHandlerContext(RaidBattleChannelPipeline pipeline, EventExecutor executor, String name, boolean inbound, boolean outbound) {

        this.name = ObjectUtil.checkNotNull(name, "name");
        this.pipeline = pipeline;
        this.executor = executor;
        this.inbound = inbound;
        this.outbound = outbound;

    }

    static void invokeChannelInactive(final AbstractRaidBattleChannelHandlerContext next) {
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeChannelInactive();
        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    next.invokeChannelInactive();
                }
            });
        }
    }

    static void invokeExceptionCaught(final AbstractRaidBattleChannelHandlerContext next, final Throwable cause) {
        ObjectUtil.checkNotNull(cause, "cause");
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeExceptionCaught(cause);
        } else {
            try {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        next.invokeExceptionCaught(cause);
                    }
                });
            } catch (Throwable t) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to submit an exceptionCaught() event.", t);
                    logger.warn("The exceptionCaught() event that was failed to submit was:", cause);
                }
            }
        }
    }

    static void invokeChannelRegistered(final AbstractRaidBattleChannelHandlerContext next, String raidId, RaidBattleChannelPromise promise) {
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeChannelRegistered(raidId, promise);
        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    next.invokeChannelRegistered(raidId, promise);
                }
            });
        }
    }

    static void invokeUserEventTriggered(final AbstractRaidBattleChannelHandlerContext next, final Object event, Promise<Object> promise) {
        ObjectUtil.checkNotNull(event, "event");
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeUserEventTriggered(event, promise);
        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    next.invokeUserEventTriggered(event, promise);
                }
            });
        }
    }

    static void invokeChannelRead(final AbstractRaidBattleChannelHandlerContext next, final Object msg) {
        ObjectUtil.checkNotNull(msg, "msg");
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeChannelRead(msg);
        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    next.invokeChannelRead(msg);
                }
            });
        }
    }

    static void invokeChannelReadRPCRequest(final AbstractRaidBattleChannelHandlerContext next, final IGameMessage msg) {
        ObjectUtil.checkNotNull(msg, "msg");
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeChannelReadRPCRequest(msg);
        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    next.invokeChannelReadRPCRequest(msg);
                }
            });
        }
    }

    private static boolean inExceptionCaught(Throwable cause) {
        do {
            StackTraceElement[] trace = cause.getStackTrace();
            if (trace != null) {
                for (StackTraceElement t : trace) {
                    if (t == null) {
                        break;
                    }
                    if ("exceptionCaught".equals(t.getMethodName())) {
                        return true;
                    }
                }
            }

            cause = cause.getCause();
        } while (cause != null);

        return false;
    }

    private static void notifyOutboundHandlerException(Throwable cause, Promise<?> promise) {
        PromiseNotificationUtil.tryFailure(promise, cause, logger);

    }

    private static boolean safeExecute(EventExecutor executor, Runnable runnable, RaidBattleChannelPromise promise, Object msg) {
        try {
            executor.execute(runnable);
            return true;
        } catch (Throwable cause) {
            try {
                promise.setFailure(cause);
            } finally {
                if (msg != null) {
                    ReferenceCountUtil.release(msg);
                }
            }
            return false;
        }
    }

    public String name() {
        return name;
    }

    private AbstractRaidBattleChannelHandlerContext findContextInbound() {
        AbstractRaidBattleChannelHandlerContext ctx = this;
        do {
            ctx = ctx.next;
        } while (!ctx.inbound);
        return ctx;
    }

    private AbstractRaidBattleChannelHandlerContext findContextOutbound() {
        AbstractRaidBattleChannelHandlerContext ctx = this;
        do {
            ctx = ctx.prev;
        } while (!ctx.outbound);
        return ctx;
    }

    public RaidBattleChannel gameChannel() {
        return pipeline.gameChannel();
    }

    public RaidBattleChannelPipeline pipeline() {
        return pipeline;
    }

    public EventExecutor executor() {
        if (executor == null) {
            return gameChannel().executor();
        } else {
            return executor;
        }
    }

    public AbstractRaidBattleChannelHandlerContext fireChannelInactive() {
        invokeChannelInactive(findContextInbound());
        return this;
    }

    private void invokeChannelInactive() {
        try {
            ((RaidBattleChannelInboundHandler) handler()).channelInactive(this);
        } catch (Throwable t) {
            notifyHandlerException(t);
        }
    }

    public AbstractRaidBattleChannelHandlerContext fireExceptionCaught(final Throwable cause) {
        invokeExceptionCaught(next, cause);
        return this;
    }

    private void invokeExceptionCaught(final Throwable cause) {
        try {
            handler().exceptionCaught(this, cause);
        } catch (Throwable error) {
            if (logger.isDebugEnabled()) {
                logger.debug("An exception {}" + "was thrown by a user handler's exceptionCaught() " + "method while handling the following exception:", ThrowableUtil.stackTraceToString(error), cause);
            } else if (logger.isWarnEnabled()) {
                logger.warn("An exception '{}' [enable DEBUG level for full stacktrace] " + "was thrown by a user handler's exceptionCaught() " + "method while handling the following exception:", error, cause);
            }
        }

    }

    public AbstractRaidBattleChannelHandlerContext fireChannelRegistered(String raidId, RaidBattleChannelPromise promise) {
        invokeChannelRegistered(findContextInbound(), raidId, promise);
        return this;
    }

    private void invokeChannelRegistered(String raidId, RaidBattleChannelPromise promise) {

        try {
            ((RaidBattleChannelInboundHandler) handler()).channelRegister(this, raidId, promise);
        } catch (Throwable t) {
            notifyHandlerException(t);
        }
    }

    public AbstractRaidBattleChannelHandlerContext fireUserEventTriggered(final Object event, Promise<Object> promise) {
        invokeUserEventTriggered(findContextInbound(), event, promise);
        return this;
    }

    private void invokeUserEventTriggered(Object event, Promise<Object> promise) {
        try {
            ((RaidBattleChannelInboundHandler) handler()).userEventTriggered(this, event, promise);
        } catch (Throwable t) {
            notifyHandlerException(t);
        }
    }

    public AbstractRaidBattleChannelHandlerContext fireChannelRead(final Object msg) {
        invokeChannelRead(findContextInbound(), msg);
        return this;
    }

    private void invokeChannelRead(Object msg) {
        try {
            ((RaidBattleChannelInboundHandler) handler()).channelRead(this, msg);
        } catch (Throwable t) {
            notifyHandlerException(t);
        }

    }

    public AbstractRaidBattleChannelHandlerContext fireChannelReadRPCRequest(final IGameMessage msg) {
        invokeChannelReadRPCRequest(findContextInbound(), msg);
        return this;
    }

    private void invokeChannelReadRPCRequest(IGameMessage msg) {
        try {
            ((RaidBattleChannelInboundHandler) handler()).channelReadRPCRequest(this, msg);
        } catch (Throwable t) {
            notifyHandlerException(t);
        }

    }

    private void notifyHandlerException(Throwable cause) {
        if (inExceptionCaught(cause)) {
            if (logger.isWarnEnabled()) {
                logger.warn("An exception was thrown by a user handler " + "while handling an exceptionCaught event", cause);
            }
            return;
        }

        invokeExceptionCaught(cause);
    }

    public RaidBattleChannelFuture writeAndFlush(IGameMessage msg) {
        return writeAndFlush(msg, newPromise());
    }

    public RaidBattleChannelPromise newPromise() {
        return new DefaultRaidBattleChannelPromise(gameChannel(), this.executor());
    }

    public RaidBattleChannelFuture writeAndFlush(IGameMessage msg, RaidBattleChannelPromise promise) {
        AbstractRaidBattleChannelHandlerContext next = findContextOutbound();
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeWrite(msg, promise);
        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    next.invokeWrite(msg, promise);
                }
            });
        }
        return promise;
    }

    private void invokeWrite(IGameMessage msg, RaidBattleChannelPromise promise) {
        try {
            ((RaidBattleChannelOutboundHandler) handler()).writeAndFlush(this, msg, promise);
        } catch (Throwable t) {
            notifyOutboundHandlerException(t, promise);
        }
    }

    public void writeRPCMessage(IGameMessage msg, Promise<IGameMessage> promise) {
        AbstractRaidBattleChannelHandlerContext next = findContextOutbound();
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeWriteRPCMessage(msg, promise);
        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    next.invokeWriteRPCMessage(msg, promise);
                }
            });
        }
    }

    private void invokeWriteRPCMessage(IGameMessage msg, Promise<IGameMessage> callback) {
        try {
            ((RaidBattleChannelOutboundHandler) handler()).writeRPCMessage(this, msg, callback);
        } catch (Throwable t) {
            notifyOutboundHandlerException(t, callback);
        }
    }

    public RaidBattleChannelFuture close() {
        return this.close(new DefaultRaidBattleChannelPromise(this.gameChannel()));
    }

    public RaidBattleChannelFuture close(final RaidBattleChannelPromise promise) {


        final AbstractRaidBattleChannelHandlerContext next = findContextOutbound();
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeClose(promise);
        } else {
            safeExecute(executor, new Runnable() {
                @Override
                public void run() {
                    next.invokeClose(promise);
                }
            }, promise, null);
        }

        return promise;
    }

    private void invokeClose(RaidBattleChannelPromise promise) {

        try {
            ((RaidBattleChannelOutboundHandler) handler()).close(this, promise);
        } catch (Throwable t) {
            notifyOutboundHandlerException(t, promise);
        }

    }

    public abstract RaidBattleChannelHandler handler();


}
