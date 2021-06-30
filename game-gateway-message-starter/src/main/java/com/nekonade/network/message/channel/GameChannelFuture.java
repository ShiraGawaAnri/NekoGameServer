package com.nekonade.network.message.channel;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public interface GameChannelFuture extends Future<Void> {

    GameChannel channel();

    @Override
    GameChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    GameChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    GameChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    GameChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    GameChannelFuture sync() throws InterruptedException;

    @Override
    GameChannelFuture syncUninterruptibly();

    @Override
    GameChannelFuture await() throws InterruptedException;

    @Override
    GameChannelFuture awaitUninterruptibly();
}
