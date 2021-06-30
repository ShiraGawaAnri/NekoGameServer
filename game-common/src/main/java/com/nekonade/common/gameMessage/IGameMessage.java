package com.nekonade.common.gameMessage;

public interface IGameMessage {

    GameMessageHeader getHeader();

    void setHeader(GameMessageHeader header);

    void read(byte[] body);

    byte[] body();

}
