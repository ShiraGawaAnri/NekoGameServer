package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserAccountRepository extends MongoRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);
}
