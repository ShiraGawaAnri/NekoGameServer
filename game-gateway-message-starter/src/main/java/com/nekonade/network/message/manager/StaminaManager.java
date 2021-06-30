package com.nekonade.network.message.manager;


import com.nekonade.dao.db.entity.Stamina;
import com.nekonade.network.message.event.function.StaminaRecoverEvent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;

public class StaminaManager {

    private final PlayerManager playerManager;

    private final ApplicationContext context;

    @Getter
    @Setter
    private Stamina stamina;

    public StaminaManager(PlayerManager playerManager) {
        this.context = playerManager.getContext();
        this.playerManager = playerManager;
        this.stamina = playerManager.getPlayer().getStamina();
    }

    public void addStamina(int point) {
        int v = this.getStamina().getValue() + point;
        this.stamina.setValue(v);
    }

    public void checkStamina() {
        StaminaRecoverEvent event = new StaminaRecoverEvent(this, playerManager);
        context.publishEvent(event);
    }
}
