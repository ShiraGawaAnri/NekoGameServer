package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.data.RewardDB;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RewardDbRepository extends MongoRepository<RewardDB,String> {

    void deleteByRewardId(String rewardId);
}
