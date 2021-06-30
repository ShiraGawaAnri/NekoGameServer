package com.nekonade.network.message.event.user;

import lombok.Getter;

@Getter
public class GetPlayerInfoEventUser extends BasicEventUser {

    private final Long playerId;

    public GetPlayerInfoEventUser(Long playerId) {
        super();
        this.playerId = playerId;
    }

}
