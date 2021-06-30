package com.nekonade.dao.daos.db;


import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.daos.AbstractDao;
import com.nekonade.dao.db.entity.data.rbeffect.RaidBattleEffectGroupDB;
import com.nekonade.dao.db.repository.RaidBattleEffectGroupsDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public class RaidBattleEffectGroupDBDao extends AbstractDao<RaidBattleEffectGroupDB, String> {

    @Autowired
    private RaidBattleEffectGroupsDbRepository repository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.RAIDBATTLE_EFFECT_GROUP_DB;
    }

    @Override
    protected MongoRepository<RaidBattleEffectGroupDB, String> getMongoRepository() {
        return repository;
    }

    @Override
    protected Class<RaidBattleEffectGroupDB> getEntityClass() {
        return RaidBattleEffectGroupDB.class;
    }
}
