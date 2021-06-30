package com.nekonade.dao.daos;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.db.entity.UserAccount;
import com.nekonade.dao.db.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserAccountDao extends AbstractDao<UserAccount, Long> {

    @Autowired
    private UserAccountRepository repository;

    public long getNextUserId() {
        String key = EnumRedisKey.USER_ID_INCR.getKey();
        redisTemplate.opsForValue().setIfAbsent(key, "10000000");
        long userId = redisTemplate.opsForValue().increment(key);
        return userId;
    }

    public Optional<UserAccount> findByUsername(String username) {
        return this.repository.findByUsername(username);
    }

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.USER_ACCOUNT;
    }

    @Override
    protected MongoRepository<UserAccount, Long> getMongoRepository() {

        return repository;
    }

    @Override
    protected Class<UserAccount> getEntityClass() {
        return UserAccount.class;
    }
}
