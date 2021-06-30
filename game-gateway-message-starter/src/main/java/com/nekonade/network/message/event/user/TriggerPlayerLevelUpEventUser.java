package com.nekonade.network.message.event.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TriggerPlayerLevelUpEventUser extends BasicEventUser {

    private int beforeLevel;

    private int afterLevel;

    private long nextLevelExperience;

    private int beforeStamina;

    private int nowStamina;
}
