package com.nekonade.dao.daos.db;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.daos.AbstractDao;
import com.nekonade.dao.db.entity.data.CharacterDB;
import com.nekonade.dao.db.repository.CharacterDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@CacheConfig(cacheManager = "caffeineCacheManager")
@Service
public class CharacterDBDao extends AbstractDao<CharacterDB, String> {

    @Autowired
    private CharacterDBRepository repository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.CHARACTER_DB;
    }

    @Override
    protected MongoRepository<CharacterDB, String> getMongoRepository() {
        return repository;
    }

    @Override
    protected Class<CharacterDB> getEntityClass() {
        return CharacterDB.class;
    }

    @Cacheable(cacheNames = "CHARACTER_DB", key = "#characterId", sync = true)
    public CharacterDB findChara(String characterId) {
        Query query = new Query(Criteria.where("characterId").is(characterId));
        return this.findByIdInMap(query, characterId, CharacterDB.class);
    }

    @CacheEvict(cacheNames = "CHARACTER_DB", allEntries = true)
    public void deleteCache() {

    }
}
