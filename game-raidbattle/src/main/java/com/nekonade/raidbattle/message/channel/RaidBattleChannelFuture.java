package com.nekonade.raidbattle.message.channel;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public interface RaidBattleChannelFuture extends Future<Void> {

    RaidBattleChannel channel();

    @Override
    RaidBattleChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    RaidBattleChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    RaidBattleChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    RaidBattleChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    RaidBattleChannelFuture sync() throws InterruptedException;

    @Override
    RaidBattleChannelFuture syncUninterruptibly();

    @Override
    RaidBattleChannelFuture await() throws InterruptedException;

    @Override
    RaidBattleChannelFuture awaitUninterruptibly();
}
