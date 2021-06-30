package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.data.ItemDB;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ItemDBRepository extends MongoRepository<ItemDB, String> {

    void deleteByItemId(String itemId);

    Optional<ItemDB> findByItemId(String itemId);
}
