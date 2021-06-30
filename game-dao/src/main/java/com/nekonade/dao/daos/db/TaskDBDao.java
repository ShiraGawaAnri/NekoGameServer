package com.nekonade.dao.daos.db;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.daos.AbstractDao;
import com.nekonade.dao.db.entity.data.task.TaskDB;
import com.nekonade.dao.db.repository.TaskDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@CacheConfig(cacheManager = "caffeineCacheManager")
@Service
public class TaskDBDao extends AbstractDao<TaskDB, String> {

    @Autowired
    private TaskDBRepository repository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.TASK_DB;
    }

    @Override
    protected MongoRepository<TaskDB, String> getMongoRepository() {
        return repository;
    }

    @Override
    protected Class<TaskDB> getEntityClass() {
        return TaskDB.class;
    }

    @Cacheable(cacheNames = "TASK_DB",key = "#taskId",sync = true)
    public TaskDB findTasksDb(String taskId){
        Query query = new Query(Criteria.where("taskId").is(taskId));
        return this.findByIdInMap(query, taskId, TaskDB.class);
    }
}
