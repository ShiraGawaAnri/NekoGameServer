package com.nekonade.dao.daos.db;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.daos.AbstractDao;
import com.nekonade.dao.db.entity.data.ItemDB;
import com.nekonade.dao.db.repository.ItemDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@CacheConfig(cacheManager = "caffeineCacheManager")
@Service
public class ItemDBDao extends AbstractDao<ItemDB, String> {

    @Autowired
    private ItemDBRepository itemsDbRepository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.ITEM_DB;
    }

    @Override
    protected MongoRepository<ItemDB, String> getMongoRepository() {
        return itemsDbRepository;
    }

    @Override
    protected Class<ItemDB> getEntityClass() {
        return ItemDB.class;
    }

    @Cacheable(cacheNames = "ITEM_DB", key = "#itemId", sync = true)
    public ItemDB findItemDb(String itemId) {
        Query query = new Query(Criteria.where("itemId").is(itemId));
        return findByIdInMap(query, itemId, ItemDB.class);
    }

}
