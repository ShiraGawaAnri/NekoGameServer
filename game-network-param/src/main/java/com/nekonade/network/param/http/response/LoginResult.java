package com.nekonade.network.param.http.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResult {

    private long userId;
    private String token;


}
