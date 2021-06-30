package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.data.CharacterDB;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CharacterDBRepository extends MongoRepository<CharacterDB,String> {

    void deleteByCharacterId(String characterId);
}
