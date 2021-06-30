package com.nekonade.raidbattle.message.channel;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

public interface RaidBattleChannelPromise extends RaidBattleChannelFuture, Promise<Void> {
    @Override
    RaidBattleChannel channel();

    @Override
    RaidBattleChannelPromise setSuccess(Void result);

    RaidBattleChannelPromise setSuccess();

    boolean trySuccess();

    @Override
    RaidBattleChannelPromise setFailure(Throwable cause);

    @Override
    RaidBattleChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    RaidBattleChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    RaidBattleChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    RaidBattleChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    RaidBattleChannelPromise sync() throws InterruptedException;

    @Override
    RaidBattleChannelPromise syncUninterruptibly();

    @Override
    RaidBattleChannelPromise await() throws InterruptedException;

    @Override
    RaidBattleChannelPromise awaitUninterruptibly();
}
