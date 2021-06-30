package com.nekonade.neko.service.test;


import com.nekonade.dao.db.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class PlayerService {

    private final ConcurrentHashMap<Long, Player> playerCache = new ConcurrentHashMap<Long, Player>();

    public Player getPlayer(Long playerId) {
        return playerCache.get(playerId);
    }

    public void addPlayer(Player player) {
        this.playerCache.putIfAbsent(player.getPlayerId(), player);
    }

}
