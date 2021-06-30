package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.Player;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PlayerRepository extends MongoRepository<Player, Long> {

    Optional<Player> findByNickName(String nickname);

}
