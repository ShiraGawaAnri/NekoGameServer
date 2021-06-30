package com.nekonade.dao.daos;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.db.entity.RaidBattleReward;
import com.nekonade.dao.db.repository.RaidBattleRewardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RaidBattleRewardDao extends AbstractDao<RaidBattleReward, String> {

    @Autowired
    private RaidBattleRewardRepository raidBattleRewardRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.RAIDBATTLE_REWARD;
    }

    @Override
    protected MongoRepository<RaidBattleReward, String> getMongoRepository() {
        return raidBattleRewardRepository;
    }

    @Override
    protected Class<RaidBattleReward> getEntityClass() {
        return RaidBattleReward.class;
    }


    public Optional<RaidBattleReward> findByPlayerIdAndRaidId(long playerId, String raidId) {
        return this.raidBattleRewardRepository.findByPlayerIdAndRaidId(playerId, raidId);
    }

    public Optional<RaidBattleReward> findByEntity(RaidBattleReward example) {
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
        Example<RaidBattleReward> queryEntity = Example.of(example, matcher);
        return this.raidBattleRewardRepository.findOne(queryEntity);
    }

    public Optional<RaidBattleReward> findUnclaimedRewardByRaidId(long playerId, String raidId, int claimed) {
        return this.raidBattleRewardRepository.findByPlayerIdAndRaidIdAndClaimed(playerId, raidId, claimed);
    }
}
