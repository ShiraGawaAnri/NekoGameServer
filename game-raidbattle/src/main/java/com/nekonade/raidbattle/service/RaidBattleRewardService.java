package com.nekonade.raidbattle.service;


import com.nekonade.dao.daos.AsyncRaidBattleRewardDao;
import com.nekonade.dao.db.entity.RaidBattleReward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RaidBattleRewardService {

    private final static Logger logger = LoggerFactory.getLogger(RaidBattleRewardService.class);

    @Autowired
    private AsyncRaidBattleRewardDao asyncRaidBattleRewardDao;


    public void asyncSaveRaidBattleReward(RaidBattleReward raidBattleReward) {
        /*CompletableFuture<Boolean> future = asyncRaidBattleRewardDao.saveOrUpdateRaidBattleRewardToDB(raidBattleReward);
        future.whenComplete((r,e)->{
            if(e != null){
                logger.warn("存入报酬失败{}",raidBattleReward,e);
            }
            logger.info("存入状况 {} | {} | {}",raidBattleReward,r,System.currentTimeMillis());
        });*/
        asyncRaidBattleRewardDao.updateToDB(raidBattleReward);
    }
}
