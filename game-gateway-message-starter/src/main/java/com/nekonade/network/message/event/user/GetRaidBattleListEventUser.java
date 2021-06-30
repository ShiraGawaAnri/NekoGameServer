package com.nekonade.network.message.event.user;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRaidBattleListEventUser extends BasicEventUser {

    private boolean finish;

    private int page;

    private int limit;

    private int sort;
}
