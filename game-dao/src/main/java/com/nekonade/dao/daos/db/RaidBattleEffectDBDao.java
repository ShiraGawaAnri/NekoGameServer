package com.nekonade.dao.daos.db;


import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.daos.AbstractDao;
import com.nekonade.dao.db.entity.data.rbeffect.RaidBattleEffectDB;
import com.nekonade.dao.db.repository.RaidBattleEffectsDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public class RaidBattleEffectDBDao extends AbstractDao<RaidBattleEffectDB, String> {

    @Autowired
    private RaidBattleEffectsDbRepository repository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.RAIDBATTLE_EFFECT_DB;
    }

    @Override
    protected MongoRepository<RaidBattleEffectDB, String> getMongoRepository() {
        return repository;
    }

    @Override
    protected Class<RaidBattleEffectDB> getEntityClass() {
        return RaidBattleEffectDB.class;
    }
}
