package com.nekonade.dao.daos;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.db.entity.RaidBattleInstance;
import com.nekonade.dao.db.repository.RaidBattleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RaidBattleDao extends AbstractDao<RaidBattleInstance, String> {

    @Autowired
    private RaidBattleRepository raidBattleRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.RAIDBATTLE_RAIDID_DETAILS;
    }

    @Override
    protected MongoRepository<RaidBattleInstance, String> getMongoRepository() {
        return raidBattleRepository;
    }

    @Override
    protected Class<RaidBattleInstance> getEntityClass() {
        return RaidBattleInstance.class;
    }


    public String findRaidBattleFromRedis(String raidId) {
        String key = this.getRedisKey().getKey(String.valueOf(raidId));
        return key.equals("") ? null : redisTemplate.opsForValue().get(key);
    }

    public Optional<RaidBattleInstance> findByRaidId(String raidId) {
        return raidBattleRepository.findByRaidId(raidId);
    }

    public Optional<RaidBattleInstance> findByRaidIdWhichIsBattling(String raidId) {
        return raidBattleRepository.findByRaidIdAndFinishAndExpireTimestampBefore(raidId, false, System.currentTimeMillis());
    }

    public void removeRaidBattleFromRedis(String raidId) {
        String key = EnumRedisKey.RAIDBATTLE_RAIDID_DETAILS.getKey(raidId);
        redisTemplate.delete(key);
    }

    @Override
    public void saveOrUpdateToDB(RaidBattleInstance raidBattle) {
        String raidId = raidBattle.getRaidId();
        Query query = new Query(Criteria.where("raidId").is(raidId).and("finish").is(false));
        Update update = new Update();
        update.set("enemies", raidBattle.getEnemies());
        update.set("finish", raidBattle.getFinish());
        update.set("failed", raidBattle.getFailed());
        update.set("remainTime", System.currentTimeMillis() - raidBattle.getExpireTimestamp());
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(false);
        options.returnNew(true);
        RaidBattleInstance andModify = mongoTemplate.findAndModify(query, update, options, RaidBattleInstance.class);
        if (andModify == null) {
            raidBattleRepository.save(raidBattle);
        }
    }

    public String getServerIdByRaidId(String raidId) {
        String key = EnumRedisKey.RAIDBATTLE_RAIDID_TO_SERVERID.getKey(raidId);
        return redisTemplate.opsForValue().get(key);
    }
}
