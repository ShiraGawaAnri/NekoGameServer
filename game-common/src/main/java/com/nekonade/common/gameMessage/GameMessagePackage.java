package com.nekonade.common.gameMessage;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameMessagePackage {

    private GameMessageHeader header;

    private byte[] body;

}
