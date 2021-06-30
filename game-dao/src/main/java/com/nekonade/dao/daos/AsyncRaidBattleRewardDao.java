package com.nekonade.dao.daos;

import com.nekonade.common.concurrent.GameEventExecutorGroup;
import com.nekonade.dao.db.entity.RaidBattleReward;

public class AsyncRaidBattleRewardDao extends AbstractAsyncDao {

    private final RaidBattleRewardDao dao;

    public AsyncRaidBattleRewardDao(GameEventExecutorGroup executorGroup, RaidBattleRewardDao dao) {
        super(executorGroup);
        this.dao = dao;
    }

    public void updateToDB(RaidBattleReward entity) {
        this.execute(entity.getPlayerId(), null, () -> {
            dao.saveOrUpdateToDB(entity);
        });
    }

}
