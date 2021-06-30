package com.nekonade.im.handler;

import com.nekonade.network.message.channel.AbstractGameChannelHandlerContext;
import com.nekonade.network.message.channel.GameChannelPromise;
import com.nekonade.network.message.handler.AbstractGameMessageDispatchHandler;
import com.nekonade.network.message.manager.IMManager;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.springframework.context.ApplicationContext;


public class GameIMHandler extends AbstractGameMessageDispatchHandler<IMManager> {
    private IMManager imManager;

    public GameIMHandler(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected IMManager getDataManager() {
        return imManager;
    }

    @Override
    protected Future<Boolean> updateToRedis(Promise<Boolean> promise) {
        promise.setSuccess(true);
        return promise;
    }

    @Override
    protected Future<Boolean> updateToDB(Promise<Boolean> promise) {
        promise.setSuccess(true);
        return promise;
    }

    @Override
    protected void initData(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise) {
        imManager = new IMManager();
        promise.setSuccess();
    }

}
