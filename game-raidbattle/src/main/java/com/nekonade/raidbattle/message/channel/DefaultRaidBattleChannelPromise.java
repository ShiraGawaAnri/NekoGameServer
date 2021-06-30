package com.nekonade.raidbattle.message.channel;

import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class DefaultRaidBattleChannelPromise extends DefaultPromise<Void> implements RaidBattleChannelPromise {
    private final RaidBattleChannel channel;

    /**
     * Creates a new instance.
     *
     * @param channel the {@link Channel} associated with this future
     */
    public DefaultRaidBattleChannelPromise(RaidBattleChannel channel) {
        this.channel = channel;
    }

    /**
     * Creates a new instance.
     *
     * @param channel the {@link Channel} associated with this future
     */
    public DefaultRaidBattleChannelPromise(RaidBattleChannel channel, EventExecutor executor) {
        super(executor);
        this.channel = channel;
    }

    @Override
    protected EventExecutor executor() {
        EventExecutor e = super.executor();
        if (e == null) {
            return channel().executor();
        } else {
            return e;
        }
    }

    @Override
    public RaidBattleChannel channel() {
        return channel;
    }

    @Override
    public RaidBattleChannelPromise setSuccess() {
        return setSuccess(null);
    }

    @Override
    public RaidBattleChannelPromise setSuccess(Void result) {
        super.setSuccess(result);
        return this;
    }

    @Override
    public boolean trySuccess() {
        return trySuccess(null);
    }

    @Override
    public RaidBattleChannelPromise setFailure(Throwable cause) {
        super.setFailure(cause);
        return this;
    }

    @Override
    public RaidBattleChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.addListener(listener);
        return this;
    }

    @Override
    public RaidBattleChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.addListeners(listeners);
        return this;
    }

    @Override
    public RaidBattleChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.removeListener(listener);
        return this;
    }

    @Override
    public RaidBattleChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.removeListeners(listeners);
        return this;
    }

    @Override
    public RaidBattleChannelPromise sync() throws InterruptedException {
        super.sync();
        return this;
    }

    @Override
    public RaidBattleChannelPromise syncUninterruptibly() {
        super.syncUninterruptibly();
        return this;
    }

    @Override
    public RaidBattleChannelPromise await() throws InterruptedException {
        super.await();
        return this;
    }

    @Override
    public RaidBattleChannelPromise awaitUninterruptibly() {
        super.awaitUninterruptibly();
        return this;
    }

    @Override
    protected void checkDeadLock() {
        super.checkDeadLock();
    }
}
