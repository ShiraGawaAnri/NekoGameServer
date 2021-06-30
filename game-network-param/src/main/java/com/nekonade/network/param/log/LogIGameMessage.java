package com.nekonade.network.param.log;


import com.nekonade.common.gameMessage.IGameMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LogIGameMessage {

    private IGameMessage gameMessage;

    public void readIGameMessage(IGameMessage gameMessage){
        this.gameMessage = gameMessage;
    }


}
