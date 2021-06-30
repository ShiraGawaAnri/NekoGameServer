package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.data.skill.SkillDB;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SkillDBRepository extends MongoRepository<SkillDB,String> {
}
