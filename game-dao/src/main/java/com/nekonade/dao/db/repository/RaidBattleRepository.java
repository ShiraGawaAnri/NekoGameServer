package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.RaidBattleInstance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RaidBattleRepository extends MongoRepository<RaidBattleInstance, String> {

    Optional<RaidBattleInstance> findByRaidId(String raidId);

    Optional<RaidBattleInstance> findByRaidIdAndFinishAndExpireTimestampBefore(String raidId, Boolean finish, long now);
}
