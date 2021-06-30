package com.nekonade.dao.daos.db;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.daos.AbstractDao;
import com.nekonade.dao.db.entity.data.EnemyDB;
import com.nekonade.dao.db.repository.EnemyDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

/**
 * @ClassName: EnemyDBDAo
 * @Author: Lily
 * @Description: DB类对应的Dao
 * @Date: 2021/6/27
 * @Version: 1.0
 */

@CacheConfig(cacheManager = "caffeineCacheManager")
@Service
public class EnemyDBDao extends AbstractDao<EnemyDB, String> {

    @Autowired
    private EnemyDBRepository repository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.ENEMY_DB;
    }

    @Override
    protected MongoRepository<EnemyDB, String> getMongoRepository() {
        return repository;
    }

    @Override
    protected Class<EnemyDB> getEntityClass() {
        return EnemyDB.class;
    }

    @Cacheable(cacheNames = "ENEMY_DB", key = "#characterId", sync = true)
    public EnemyDB findEnemy(String characterId) {
        Query query = new Query(Criteria.where("characterId").is(characterId));
        return this.findByIdInMap(query, characterId, EnemyDB.class);
    }

    @CacheEvict(cacheNames = "ENEMY_DB", allEntries = true)
    public void deleteCache() {

    }
}
