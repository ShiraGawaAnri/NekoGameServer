package com.nekonade.network.message.manager;

import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.dao.db.entity.Experience;
import com.nekonade.network.message.event.function.ExperienceAddEvent;
import com.nekonade.network.message.event.function.ExperienceCheckEvent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;

public class ExperienceManager {

    private final PlayerManager playerManager;
    private final ApplicationContext context;
    @Getter
    @Setter
    private Experience experience;

    public ExperienceManager(PlayerManager playerManager) {
        this.context = playerManager.getContext();
        this.playerManager = playerManager;
        this.experience = playerManager.getPlayer().getExperience();
    }

    public void addExperience(long exp) {
        /*experience.setExp(experience.getExp() + exp);
        ExperienceCheckEvent event = new ExperienceCheckEvent(this, playerManager);
        context.publishEvent(event);*/
        ExperienceAddEvent event = new ExperienceAddEvent(this,playerManager, exp);
        context.publishEvent(event);
    }
}
