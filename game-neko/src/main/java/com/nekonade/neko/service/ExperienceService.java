package com.nekonade.neko.service;

import com.nekonade.dao.daos.db.GlobalConfigDao;
import com.nekonade.dao.db.entity.Experience;
import com.nekonade.dao.db.entity.Player;
import com.nekonade.dao.db.entity.config.GlobalConfig;
import com.nekonade.network.message.event.function.ExperienceAddEvent;
import com.nekonade.network.message.event.function.ExperienceCheckEvent;
import com.nekonade.network.message.event.user.TriggerPlayerLevelUpEventUser;
import com.nekonade.network.message.manager.PlayerManager;
import com.nekonade.network.param.game.message.neko.TriggerPlayerLevelUpMsgResponse;
import io.netty.util.concurrent.DefaultPromise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class ExperienceService {

    @Autowired
    private DataConfigService dataConfigService;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private GlobalConfigDao globalConfigDao;

    @EventListener
    public void addExperience(ExperienceAddEvent event){
        PlayerManager playerManager = event.getPlayerManager();
        Long exp = event.getExp();
        Experience experience = playerManager.getPlayer().getExperience();
        experience.addExp(exp);
        ExperienceCheckEvent experienceCheckEvent = new ExperienceCheckEvent(this, playerManager);
        context.publishEvent(experienceCheckEvent);
    }


    @EventListener
    public void checkExperience(ExperienceCheckEvent event) {
        PlayerManager playerManager = event.getPlayerManager();
        Player player = playerManager.getPlayer();
        int playerLevel = player.getLevel();
        Experience experience = player.getExperience();
        GlobalConfig globalConfig = globalConfigDao.getGlobalConfig();
        GlobalConfig.LevelConfig levelConfig = globalConfig.getLevelConfig();
        long nextLevelUpPoint = levelConfig.getNextLevelUpPoint(playerLevel);
        int playerLevelUpCount = 0;
        while (experience.getExp() >= nextLevelUpPoint && playerLevel < levelConfig.getMaxValue()) {
            playerLevel++;
            playerLevelUpCount++;
            player.setLevel(Math.min(playerLevel, levelConfig.getMaxValue()));
            nextLevelUpPoint = levelConfig.getNextLevelUpPoint(playerLevel);
        }
        if (playerLevel >= levelConfig.getMaxValue()) {
            long maxLevelExperience = levelConfig.getNextLevelUpPoint(Math.max(levelConfig.getMaxValue() - 1, 1));
            experience.setExp(maxLevelExperience);
            experience.setNextLevelExp(maxLevelExperience);
        }
        if (playerLevelUpCount > 0) {
            int addStamina = playerLevelUpCount * globalConfig.getStaminaConfig().getEachLevelAddPoint();
            playerManager.getStaminaManager().addStamina(addStamina);
            TriggerPlayerLevelUpEventUser triggerPlayerLevelUpEventUser = new TriggerPlayerLevelUpEventUser();
            triggerPlayerLevelUpEventUser.setAfterLevel(playerLevel);
            triggerPlayerLevelUpEventUser.setBeforeLevel(playerLevel - playerLevelUpCount);
            triggerPlayerLevelUpEventUser.setNowStamina(playerManager.getStaminaManager().getStamina().getValue());
            triggerPlayerLevelUpEventUser.setBeforeStamina(playerManager.getStaminaManager().getStamina().getValue() - addStamina);
            triggerPlayerLevelUpEventUser.setNextLevelExperience(experience.getNextLevelExp());
            DefaultPromise<Object> promise = new DefaultPromise<>(playerManager.getGameChannel().executor());
            promise.addListener(future->{
                if(future.isSuccess()){
                    TriggerPlayerLevelUpMsgResponse response = (TriggerPlayerLevelUpMsgResponse)future.get();
                    playerManager.getGameChannel().pushMessage(response);
                }
            });
            playerManager.getGameChannel().getEventDispatchService().fireUserEvent(player.getPlayerId(), triggerPlayerLevelUpEventUser, promise);
        }
    }
}
