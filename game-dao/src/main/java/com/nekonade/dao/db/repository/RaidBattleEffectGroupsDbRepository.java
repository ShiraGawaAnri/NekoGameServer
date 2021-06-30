package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.data.rbeffect.RaidBattleEffectGroupDB;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RaidBattleEffectGroupsDbRepository extends MongoRepository<RaidBattleEffectGroupDB, String> {
    void deleteByEffectGroupId(String effectGroupId);
}
