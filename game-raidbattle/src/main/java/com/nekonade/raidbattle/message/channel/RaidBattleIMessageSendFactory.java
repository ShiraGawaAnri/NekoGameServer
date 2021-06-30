package com.nekonade.raidbattle.message.channel;


import com.nekonade.common.gameMessage.GameMessagePackage;

public interface RaidBattleIMessageSendFactory {

    void sendMessage(GameMessagePackage gameMessagePackage, RaidBattleChannelPromise promise);
}
