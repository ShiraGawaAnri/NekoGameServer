package com.nekonade.network.message.event.user;

public class GetArenaPlayerEventUser extends BasicEventUser {

    private final Long playerId;

    public GetArenaPlayerEventUser(Long playerId) {
        super();
        this.playerId = playerId;
    }

    public Long getPlayerId() {
        return playerId;
    }

}
