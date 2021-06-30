package com.nekonade.dao.daos;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.db.entity.MailBox;
import com.nekonade.dao.db.repository.MailBoxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public class MailBoxDao extends AbstractDao<MailBox, Long> {

    @Autowired
    private MailBoxRepository repository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.MAIL_ID_INCR;
    }

    @Override
    protected MongoRepository<MailBox, Long> getMongoRepository() {
        return repository;
    }

    @Override
    protected Class<MailBox> getEntityClass() {
        return MailBox.class;
    }
}
