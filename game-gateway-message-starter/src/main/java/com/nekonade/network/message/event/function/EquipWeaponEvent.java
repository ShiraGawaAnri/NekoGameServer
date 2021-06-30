package com.nekonade.network.message.event.function;

import org.springframework.context.ApplicationEvent;

public class EquipWeaponEvent extends ApplicationEvent {
    private String heroId;
    private String weaponId;
    public EquipWeaponEvent(Object source) {
        super(source);
    }

    public String getHeroId() {
        return heroId;
    }

    public void setHeroId(String heroId) {
        this.heroId = heroId;
    }

    public String getWeaponId() {
        return weaponId;
    }

    public void setWeaponId(String weaponId) {
        this.weaponId = weaponId;
    }


}
