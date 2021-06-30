package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.data.rbeffect.RaidBattleEffectDB;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RaidBattleEffectsDbRepository extends MongoRepository<RaidBattleEffectDB, String> {
    void deleteByEffectId(String effectId);
}
