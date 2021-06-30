package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.data.RaidBattleDB;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RaidBattleDbRepository extends MongoRepository<RaidBattleDB, String> {
    void deleteByStageId(String stageId);
}
