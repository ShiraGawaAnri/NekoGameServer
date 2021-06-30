package com.nekonade.common.dto.raidbattle.vo;


import com.nekonade.common.basePojo.BaseRaidBattle;
import com.nekonade.common.dto.raidbattle.RaidBattleEnemy;
import com.nekonade.common.dto.raidbattle.RaidBattlePlayer;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class RaidBattleVo extends BaseRaidBattle {

    private String raidId;

    private List<RaidBattleEnemy> enemies = new CopyOnWriteArrayList<>();

    private Map<Long, RaidBattlePlayer> players = new ConcurrentHashMap<>();

}
