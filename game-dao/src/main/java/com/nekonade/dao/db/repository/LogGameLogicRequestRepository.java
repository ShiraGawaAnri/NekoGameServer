package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.LogGameLogicRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogGameLogicRequestRepository extends MongoRepository<LogGameLogicRequest, String> {
}
