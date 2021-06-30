package com.nekonade.common.basePojo;

import com.nekonade.common.dto.raidbattle.RaidBattleCharacter;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: BaseRaidBattlePlayer
 * @Author: Lily
 * @Description: 战斗时的玩家基础类
 * @Date: 2021/6/28
 * @Version: 1.0
 */
@Getter
@Setter
public abstract class BaseRaidBattlePlayer extends BasePlayer {

    protected Map<String, RaidBattleCharacter> party = new HashMap<>();

    protected int contributePoint;

    protected int turn = 1;

    protected int joinedTime;

    protected boolean retreated = false;
}
