package com.nekonade.raidbattle.message.context;


import com.nekonade.common.gameMessage.DataManager;
import com.nekonade.raidbattle.message.channel.AbstractRaidBattleChannelHandlerContext;

public class RaidBattleEventContext<T extends DataManager> {

    private final T dataManager;
    private final AbstractRaidBattleChannelHandlerContext ctx;


    public RaidBattleEventContext(T dataManager, AbstractRaidBattleChannelHandlerContext ctx) {
        super();
        this.dataManager = dataManager;
        this.ctx = ctx;
    }

    public T getDataManager() {
        return dataManager;
    }

    public AbstractRaidBattleChannelHandlerContext getCtx() {
        return ctx;
    }


}
