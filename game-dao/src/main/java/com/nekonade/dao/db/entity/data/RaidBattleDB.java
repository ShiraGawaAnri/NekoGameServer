package com.nekonade.dao.db.entity.data;


import com.nekonade.common.basePojo.BaseRaidBattle;
import com.nekonade.common.dto.raidbattle.RaidBattleEnemy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Document("RaidBattleDB")
@CompoundIndexes({
        @CompoundIndex(name = "episode_idx", def = "{'episode':1}"),
        @CompoundIndex(name = "chapter_idx", def = "{'chapter':1}"),
        @CompoundIndex(name = "stage_idx", def = "{'stage':1}"),
        @CompoundIndex(name = "difficulty_idx", def = "{'difficulty':1}"),
        @CompoundIndex(name = "area_idx", def = "{'area':1}"),
})
public class RaidBattleDB extends BaseRaidBattle {

    @Id
    private String stageId;

    @DBRef
    private List<EnemyDB> enemyList = new ArrayList<>();

    private List<String> enemyIds = new ArrayList<>();

    @DBRef
    private RewardDB reward = new RewardDB();
}
