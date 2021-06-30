package com.nekonade.dao.db.entity;

import com.nekonade.common.basePojo.BaseRaidBattle;
import com.nekonade.common.dto.raidbattle.RaidBattleEnemy;
import com.nekonade.common.dto.raidbattle.RaidBattlePlayer;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName: RaidBattle
 * @Author: Lily
 * @Description: 战斗时的副本类，入库的版本
 * @Date: 2021/6/28
 * @Version: 1.0
 */
@Getter
@Setter
@Document(collection = "RaidBattleInstance")
public class RaidBattleInstance extends BaseRaidBattle implements Cloneable{

    @Id
    private String raidId;//唯一的RaidId

    private List<RaidBattleEnemy> enemies = new CopyOnWriteArrayList<>();

    private Map<Long, RaidBattlePlayer> players = new ConcurrentHashMap<>();

}
