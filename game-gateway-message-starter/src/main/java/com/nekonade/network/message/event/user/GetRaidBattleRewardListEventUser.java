package com.nekonade.network.message.event.user;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRaidBattleRewardListEventUser extends BasicEventUser {

    private int claimed;

    private int page;

    private int limit;

    private int sort;
}
