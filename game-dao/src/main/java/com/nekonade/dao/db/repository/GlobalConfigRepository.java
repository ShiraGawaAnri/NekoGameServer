package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.config.GlobalConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GlobalConfigRepository extends MongoRepository<GlobalConfig, Long> {

}
