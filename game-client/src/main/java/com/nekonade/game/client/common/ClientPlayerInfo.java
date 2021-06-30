package com.nekonade.game.client.common;

import com.nekonade.network.param.http.response.GameGatewayInfoMsg;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class ClientPlayerInfo {

    private String userName;
    private String password;
    private long playerId;
    private String token;
    private long userId;
    private GameGatewayInfoMsg gameGatewayInfoMsg;


}
