package com.nekonade.dao.daos;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.db.entity.LogGameLogicRequest;
import com.nekonade.dao.db.repository.LogGameLogicRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public class LogGameLogicDao extends AbstractDao<LogGameLogicRequest, String> {

    @Autowired
    private LogGameLogicRequestRepository repository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return null;
    }

    @Override
    protected MongoRepository<LogGameLogicRequest, String> getMongoRepository() {
        return repository;
    }

    @Override
    protected Class<LogGameLogicRequest> getEntityClass() {
        return LogGameLogicRequest.class;
    }

    public void saveLog(LogGameLogicRequest entity) {
        this.saveOrUpdateToDB(entity);
    }
}
