package com.nekonade.neko.service;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.exceptions.GameNotifyException;
import com.nekonade.dao.daos.db.GlobalConfigDao;
import com.nekonade.dao.db.entity.Stamina;
import com.nekonade.dao.db.entity.config.GlobalConfig;
import com.nekonade.network.message.event.function.EnterGameEvent;
import com.nekonade.network.message.event.function.StaminaAddPointEvent;
import com.nekonade.network.message.event.function.StaminaRecoverEvent;
import com.nekonade.network.message.event.function.StaminaSubPointEvent;
import com.nekonade.network.message.manager.PlayerManager;
import com.nekonade.network.message.manager.StaminaManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class StaminaService {

    @Autowired
    private DataConfigService dataConfigService;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private GlobalConfigDao globalConfigDao;


    @EventListener
    public void EnterGameEvent(EnterGameEvent event) {
        PlayerManager playerManager = event.getPlayerManager();
        this.checkStamina(playerManager);
    }

    private void addPointStamina(Stamina stamina, int point) {
        GlobalConfig globalConfig = globalConfigDao.getGlobalConfig();
        stamina.setValue(Math.min(globalConfig.getStaminaConfig().getMaxValue(), stamina.getValue() + point));
    }

    private void subPointStamina(Stamina stamina, int point) {
        stamina.setValue(Math.max(0, stamina.getValue() - point));
    }

    private void recoverStaminaByAuto(Stamina stamina) {
        Integer value = stamina.getValue();
        GlobalConfig globalConfig = globalConfigDao.getGlobalConfig();
        GlobalConfig.StaminaConfig settingStaminaConfig = globalConfig.getStaminaConfig();
        long now = System.currentTimeMillis();
        int maxValue = globalConfig.getStaminaConfig().getMaxValue();
        long cTime = settingStaminaConfig.getRecoverTime();
        int recoverValue = settingStaminaConfig.getRecoverValue();
        Long nextRecoverTimestamp = stamina.getNextRecoverTimestamp();
        if (now > nextRecoverTimestamp) {
            long capTime = now - nextRecoverTimestamp;
            long v = capTime / cTime * recoverValue + 1;
            long remainTime = capTime - (capTime / cTime) * cTime;
            value += (int) v;
            value = Math.min(value, maxValue);
            stamina.setPreQueryTime(now);
            stamina.setValue(value);
            stamina.setNextRecoverTimestamp(stamina.getPreQueryTime() + cTime - remainTime);
            stamina.setNextRecoverTime(cTime - remainTime);
        } else {
            stamina.setNextRecoverTime(nextRecoverTimestamp - now);
        }
        //达到最大时 下次回复时间永远是查询时刻 + 回复时间
        if (value == maxValue) {
            stamina.setNextRecoverTimestamp(stamina.getPreQueryTime() + cTime);
            stamina.setNextRecoverTime(cTime);
        }
    }


    @EventListener
    public void addStamina(StaminaAddPointEvent event) {
        PlayerManager playerManager = event.getPlayerManager();
        int point = event.getPoint();
        this.addPointStamina(playerManager.getStaminaManager().getStamina(), point);
    }

    @EventListener
    public void subStamina(StaminaSubPointEvent event) {
        PlayerManager playerManager = event.getPlayerManager();
        int point = event.getPoint();
        this.subPointStamina(playerManager.getStaminaManager().getStamina(), point);
    }

    @EventListener
    public void checkStamina(StaminaRecoverEvent event) {
        PlayerManager playerManager = event.getPlayerManager();
        this.checkStamina(playerManager);
    }

    public void checkStamina(PlayerManager playerManager) {
        StaminaManager staminaManager = playerManager.getStaminaManager();
        Stamina stamina = staminaManager.getStamina();
        if (stamina == null) {
            throw GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.StaminaNoEntity).build();
        }
        this.recoverStaminaByAuto(stamina);
        playerManager.getPlayer().setStamina(stamina);
        staminaManager.setStamina(stamina);
    }
}
