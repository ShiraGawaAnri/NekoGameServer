package com.nekonade.network.param.game.messagedispatcher;


import com.nekonade.common.gameMessage.IGameMessage;

public interface IGameChannelContext {

    void sendMessage(IGameMessage gameMessage);

    <T> T getRequest();

    String getRemoteHost();

    long getPlayerId();

}
