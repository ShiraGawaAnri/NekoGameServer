package com.nekonade.network.message.event.user;

import com.nekonade.network.param.game.message.neko.DoReceiveMailMsgRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoReceiveMailEventUser extends BasicEventUser {

    private DoReceiveMailMsgRequest.RequestBody request;

    public DoReceiveMailEventUser(DoReceiveMailMsgRequest.RequestBody request) {
        this.request = request;
    }
}
