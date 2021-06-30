package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.RaidBattleReward;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RaidBattleRewardRepository extends MongoRepository<RaidBattleReward,String> {

    void deleteByPlayerIdAndRaidId(long playerId,String raidId);

    Optional<RaidBattleReward> findByPlayerIdAndRaidId(long playerId,String raidId);

    Optional<RaidBattleReward> findByPlayerIdAndRaidIdAndClaimed(long playerId,String raidId,int claimed);
}
