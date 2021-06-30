package com.nekonade.network.message.channel;


import com.nekonade.common.gameMessage.GameMessagePackage;

public interface IMessageSendFactory {

    void sendMessage(GameMessagePackage gameMessagePackage, GameChannelPromise promise);
}
