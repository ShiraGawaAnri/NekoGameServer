package com.nekonade.dao.daos;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.db.entity.LogGameRaidBattleRequest;
import com.nekonade.dao.db.repository.LogGameRaidBattleRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public class LogGameRaidBattleDao extends AbstractDao<LogGameRaidBattleRequest, String> {

    @Autowired
    private LogGameRaidBattleRequestRepository repository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return null;
    }

    @Override
    protected MongoRepository<LogGameRaidBattleRequest, String> getMongoRepository() {
        return repository;
    }

    @Override
    protected Class<LogGameRaidBattleRequest> getEntityClass() {
        return LogGameRaidBattleRequest.class;
    }

    public void saveLog(LogGameRaidBattleRequest entity) {
        this.saveOrUpdateToDB(entity);
    }
}
