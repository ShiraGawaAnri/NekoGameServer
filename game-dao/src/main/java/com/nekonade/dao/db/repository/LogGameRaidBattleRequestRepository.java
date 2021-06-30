package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.LogGameRaidBattleRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogGameRaidBattleRequestRepository extends MongoRepository<LogGameRaidBattleRequest, String> {
}
