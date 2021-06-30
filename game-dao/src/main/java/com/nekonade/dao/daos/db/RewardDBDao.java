package com.nekonade.dao.daos.db;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.daos.AbstractDao;
import com.nekonade.dao.db.entity.data.RewardDB;
import com.nekonade.dao.db.repository.RewardDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public class RewardDBDao extends AbstractDao<RewardDB, String> {

    @Autowired
    private RewardDbRepository repository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.REWARD_DB;
    }

    @Override
    protected MongoRepository<RewardDB, String> getMongoRepository() {
        return repository;
    }

    @Override
    protected Class<RewardDB> getEntityClass() {
        return RewardDB.class;
    }
}
