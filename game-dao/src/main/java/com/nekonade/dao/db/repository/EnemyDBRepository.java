package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.data.EnemyDB;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @ClassName: EnemyDBRepository
 * @Author: Lily
 * @Description: 对应的MongoDB接口
 * @Date: 2021/6/27
 * @Version: 1.0
 */
public interface EnemyDBRepository extends MongoRepository<EnemyDB,String> {

    void deleteByCharacterId(String characterId);
}
