package com.nekonade.dao.daos.db;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.common.utils.JacksonUtils;
import com.nekonade.dao.daos.AbstractDao;
import com.nekonade.dao.db.entity.config.GlobalConfig;
import com.nekonade.dao.db.repository.GlobalConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@CacheConfig(cacheManager = "caffeineCacheManager")
@Service
public class GlobalConfigDao extends AbstractDao<GlobalConfig, Long> {

    private final String GlobalConfigKey = EnumRedisKey.CONFIG_GLOBAL.getKey().intern();


    @Autowired
    private GlobalConfigRepository globalConfigRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.CONFIG_GLOBAL;
    }

    @Override
    protected MongoRepository<GlobalConfig, Long> getMongoRepository() {
        return globalConfigRepository;
    }

    @Override
    protected Class<GlobalConfig> getEntityClass() {
        return GlobalConfig.class;
    }

    @Resource(name = "caffeineCacheManager")
    private CacheManager cacheManager;

    @Cacheable(cacheNames = "CONFIG_GLOBAL", sync = true)
    public GlobalConfig getGlobalConfig() {
        GlobalConfig result;
        String settingJson = redisTemplate.opsForValue().get(GlobalConfigKey);
        if (!StringUtils.isEmpty(settingJson)) {
            //result = JSON.parseObject(settingJson, GlobalConfig.class);
            result = JacksonUtils.parseObjectV2(settingJson, GlobalConfig.class);
        } else {
            Query query = new Query();
            query.with(Sort.by(Sort.Direction.DESC, "_id")).limit(1);
            result = mongoTemplate.findOne(query, GlobalConfig.class);
            if (result == null) {
                result = new GlobalConfig();
            }
        }
        this.saveOrUpdate(result, null);
        return result;
    }

    @CachePut(cacheNames = "CONFIG_GLOBAL")
    public GlobalConfig updateGlobalConfig() {
        GlobalConfig result;
        String settingJson = redisTemplate.opsForValue().get(GlobalConfigKey);
        if (!StringUtils.isEmpty(settingJson)) {
            //result = JSON.parseObject(settingJson, GlobalConfig.class);
            result = JacksonUtils.parseObjectV2(settingJson, GlobalConfig.class);
        } else {
            Query query = new Query();
            query.with(Sort.by(Sort.Direction.DESC, "_id")).limit(1);
            result = mongoTemplate.findOne(query, GlobalConfig.class);
            if (result == null) {
                result = new GlobalConfig();
            }
        }
        this.saveOrUpdate(result, null);
        return result;
    }
}
