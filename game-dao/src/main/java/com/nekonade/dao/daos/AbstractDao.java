package com.nekonade.dao.daos;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.nekonade.common.constcollections.RedisConstants;
import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.common.utils.JacksonUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractDao<Entity, ID> {

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    protected abstract EnumRedisKey getRedisKey();

    protected abstract MongoRepository<Entity, ID> getMongoRepository();

    protected abstract Class<Entity> getEntityClass();

    private static final Interner<String> pool = Interners.newWeakInterner();

    @SneakyThrows
    public Optional<Entity> findById(ID id) {
        String key = this.getRedisKey().getKey(id.toString());
        String value = redisTemplate.opsForValue().get(key);
        Entity entity = null;
        if (value == null) {// 说明redis中没有用户信息
            synchronized (pool.intern(key)) {// 这里对openId加锁，防止并发操作，导致缓存击穿。
                value = redisTemplate.opsForValue().get(key);// 这里二次获取一下
                if (value == null) {//如果redis中，还是没有值，再从数据库取
                    Optional<Entity> op = this.getMongoRepository().findById(id);
                    if (op.isPresent()) {// 如果数据库中不为空，存储到redis中。
                        entity = op.get();
                        this.updateRedis(entity, id);
                    } else {
                        this.setRedisDefaultValue(key);//设置默认值，防止缓存穿透
                    }
                } else if (value.equals(RedisConstants.RedisDefaultValue)) {
                    value = null;//如果取出来的是默认值，还是返回空
                }
            }
        } else if (value.equals(RedisConstants.RedisDefaultValue)) {//如果是默认值，也返回空，表示不存在。
            value = null;
        }
        if (value != null) {
            //entity = JSON.parseObject(value, this.getEntityClass());
            entity = JacksonUtils.parseObjectV2(value, this.getEntityClass());
        }
        return Optional.ofNullable(entity);
    }

    @SneakyThrows
    protected Entity findByIdInMap(Query query, ID id, Class<Entity> clazz) {
        String key = this.getRedisKey().getKey();
        Object value = redisTemplate.opsForHash().get(key, id.toString());
        Entity entity = null;
        if (value == null) {// 说明redis中没有用户信息
            synchronized (pool.intern(key)) {// 这里对openId加锁，防止并发操作，导致缓存击穿。
                value = redisTemplate.opsForHash().get(key, id.toString());// 这里二次获取一下
                if (value == null) {//如果redis中，还是没有值，再从数据库取
                    Entity one = mongoTemplate.findOne(query, clazz);
                    if (one != null) {// 如果数据库中不为空，存储到redis中。
                        entity = one;
                        this.updateRedisMap(entity, id);
                    }
                } else if (value.equals(RedisConstants.RedisDefaultValue)) {
                    value = null;//如果取出来的是默认值，还是返回空
                }
            }
        } else if (value.equals(RedisConstants.RedisDefaultValue)) {//如果是默认值，也返回空，表示不存在。
            value = null;
        }
        if (value != null) {
            //entity = JSON.parseObject((String) value, this.getEntityClass());
            entity = JacksonUtils.parseObjectV2((String) value, this.getEntityClass());
        }
        return entity;
    }

    @SneakyThrows
    protected Optional<Entity> findByIdInMap(Entity example, ID id) {
        String key = this.getRedisKey().getKey();
        Object value = redisTemplate.opsForHash().get(key, id.toString());
        Entity entity = null;
        if (value == null) {// 说明redis中没有用户信息
            synchronized (pool.intern(key)) {// 这里对openId加锁，防止并发操作，导致缓存击穿。
                value = redisTemplate.opsForHash().get(key, id.toString());// 这里二次获取一下
                if (value == null) {//如果redis中，还是没有值，再从数据库取
                    ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
                    Example<Entity> queryEntity = Example.of(example, matcher);
                    Optional<Entity> op = this.getMongoRepository().findOne(queryEntity);
                    if (op.isPresent()) {// 如果数据库中不为空，存储到redis中。
                        entity = op.get();
                        this.updateRedisMap(entity, id);
                    } else {
                        //this.setRedisDefaultValue(key);//设置默认值，防止缓存穿透
                    }
                } else if (value.equals(RedisConstants.RedisDefaultValue)) {
                    value = null;//如果取出来的是默认值，还是返回空
                }
            }
        } else if (value.equals(RedisConstants.RedisDefaultValue)) {//如果是默认值，也返回空，表示不存在。
            value = null;
        }
        if (value != null) {
            //entity = JSON.parseObject((String) value, this.getEntityClass());
            entity = JacksonUtils.parseObjectV2((String) value, this.getEntityClass());
        }
        return Optional.ofNullable(entity);
    }

    private void setRedisDefaultValue(String key) {
        Duration duration = Duration.ofMinutes(1);
        redisTemplate.opsForValue().set(key, RedisConstants.RedisDefaultValue, duration);
    }

    private void updateRedis(Entity entity, ID id) {
        this.updateRedis(entity, id, null);
    }

    @SneakyThrows
    private void updateRedis(Entity entity, ID id, Duration duration) {
        String key;
        if (id == null) {
            key = this.getRedisKey().getKey();
        } else {
            key = this.getRedisKey().getKey(id.toString());
        }
        //String value = JSON.toJSONString(entity);
        String value = JacksonUtils.toJSONStringV2(entity);
        Duration realDuration;
        if (duration == null) {
            realDuration = this.getRedisKey().getTimeout();
        } else {
            realDuration = duration;
        }
        if (realDuration != null) {
            redisTemplate.opsForValue().set(key, value, realDuration);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    public void saveOrUpdate(Entity entity, ID id) {
        this.updateRedis(entity, id);
        this.getMongoRepository().save(entity);
    }

    public void saveOrUpdateToDB(Entity entity) {
        this.getMongoRepository().save(entity);
    }

    public void saveOrUpdateToRedis(Entity entity, ID id) {
        this.updateRedis(entity, id);
    }

    public void saveOrUpdateToRedis(Entity entity, ID id, Duration duration) {
        this.updateRedis(entity, id, duration);
    }

    public List<Entity> findAll() {
        return this.getMongoRepository().findAll();
    }

    public Map<String, Entity> findAllInMap() {
        String key = this.getRedisKey().getKey();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        Map<String, Entity> map = new HashMap<>();
        entries.forEach((key1, value) -> {
            String newKey = key1.toString();
            String newJson = value.toString();
            Entity entity = JacksonUtils.parseObjectV2(newJson, this.getEntityClass());
            map.put(newKey, entity);
        });
        return map;
    }

    @SneakyThrows
    private void updateRedisMap(Entity entity, ID id) {
        //String value = JSON.toJSONString(entity);
        String value = JacksonUtils.toJSONStringV2(entity);
        Map<String, String> map = new HashMap<>();
        map.put(id.toString(), value);
        this.updateRedisMap(map);
    }

    private void updateRedisMap(Map<String, String> map) {
        String key = this.getRedisKey().getKey();
        redisTemplate.opsForHash().putAll(key, map);
    }

    public void saveOrUpdateMap(Entity entity, ID id) {
        this.updateRedisMap(entity, id);
        this.getMongoRepository().save(entity);
    }
}
