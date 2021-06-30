package com.nekonade.network.message.context;


import com.nekonade.common.gameMessage.DataManager;
import com.nekonade.network.message.channel.AbstractGameChannelHandlerContext;

public class UserEventContext<T extends DataManager> {

    private final T dataManager;
    private final AbstractGameChannelHandlerContext ctx;


    public UserEventContext(T dataManager, AbstractGameChannelHandlerContext ctx) {
        super();
        this.dataManager = dataManager;
        this.ctx = ctx;
    }

    public T getDataManager() {
        return dataManager;
    }

    public AbstractGameChannelHandlerContext getCtx() {
        return ctx;
    }


}
