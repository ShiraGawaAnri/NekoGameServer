package com.nekonade.dao.daos.db;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.daos.AbstractDao;
import com.nekonade.dao.db.entity.data.skill.SkillDB;
import com.nekonade.dao.db.repository.SkillDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@CacheConfig(cacheManager = "caffeineCacheManager")
@Service
public class SkillDBDao extends AbstractDao<SkillDB, String> {

    @Autowired
    private SkillDBRepository repository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.CARD_DB;
    }

    @Override
    protected MongoRepository<SkillDB, String> getMongoRepository() {
        return repository;
    }

    @Override
    protected Class<SkillDB> getEntityClass() {
        return SkillDB.class;
    }

    @Cacheable(cacheNames = "SKILL_DB", key = "#cardId", sync = true)
    public SkillDB findCardsDB(String cardId) {
        Query query = new Query(Criteria.where("cardId").is(cardId));
        return this.findByIdInMap(query, cardId, SkillDB.class);
    }

    //@Cacheable(cacheNames = "CARDS_DB_ALL")
    public Map<String, SkillDB> findAllCardsDB() {
        return this.findAllInMap();
    }
}
