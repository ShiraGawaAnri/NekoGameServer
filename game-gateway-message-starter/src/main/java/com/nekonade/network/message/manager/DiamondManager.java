package com.nekonade.network.message.manager;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.exceptions.GameErrorException;
import com.nekonade.dao.daos.db.GlobalConfigDao;
import com.nekonade.dao.db.entity.Diamond;
import lombok.Getter;
import org.springframework.context.ApplicationContext;

public class DiamondManager {

    private final PlayerManager playerManager;

    private final ApplicationContext context;

    private final GlobalConfigDao globalConfigDao;

    @Getter
    private final Diamond diamond;

    public DiamondManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
        this.context = playerManager.getContext();
        this.diamond = playerManager.getPlayer().getDiamond();
        this.globalConfigDao = playerManager.getContext().getBean(GlobalConfigDao.class);
    }

    public synchronized boolean checkDiamondEnough(int needAmount){
        return this.diamond.getAmount() >= needAmount;
    }

    public synchronized boolean checkDiamondMaxValue(int addAmount){
        return this.diamond.getAmount() + addAmount >= globalConfigDao.getGlobalConfig().getDiamondConfig().getMaxValue();
    }

    public synchronized void addDiamond(int amount){
        if(checkDiamondMaxValue(amount)){
            throw GameErrorException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.DiamondReachMax).build();
        }
        this.diamond.addAmount(amount);
    }

    public synchronized void subDiamond(int amount){
        this.diamond.subAmount(amount);
    }
}
