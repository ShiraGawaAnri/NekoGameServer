package com.nekonade.network.message.channel;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

public interface GameChannelPromise extends GameChannelFuture, Promise<Void> {
    @Override
    GameChannel channel();

    @Override
    GameChannelPromise setSuccess(Void result);

    GameChannelPromise setSuccess();

    boolean trySuccess();

    @Override
    GameChannelPromise setFailure(Throwable cause);

    @Override
    GameChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    GameChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    GameChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    GameChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    GameChannelPromise sync() throws InterruptedException;

    @Override
    GameChannelPromise syncUninterruptibly();

    @Override
    GameChannelPromise await() throws InterruptedException;

    @Override
    GameChannelPromise awaitUninterruptibly();
}
