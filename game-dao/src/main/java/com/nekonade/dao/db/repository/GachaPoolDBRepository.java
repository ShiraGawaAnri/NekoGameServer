package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.data.GachaPoolDB;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GachaPoolDBRepository extends MongoRepository<GachaPoolDB,String> {
}
