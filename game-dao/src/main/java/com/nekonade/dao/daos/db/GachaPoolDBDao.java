package com.nekonade.dao.daos.db;

import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.daos.AbstractDao;
import com.nekonade.dao.db.entity.data.GachaPoolDB;
import com.nekonade.dao.db.repository.GachaPoolDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public class GachaPoolDBDao extends AbstractDao<GachaPoolDB, String> {

    @Autowired
    private GachaPoolDBRepository repository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.GACHAPOOL_DB;
    }

    @Override
    protected MongoRepository<GachaPoolDB, String> getMongoRepository() {
        return repository;
    }

    @Override
    protected Class<GachaPoolDB> getEntityClass() {
        return GachaPoolDB.class;
    }

    public GachaPoolDB findGachaPoolsDB(String gachaPoolsId) {
        Query query = new Query(Criteria.where("gachaPoolsId").is(gachaPoolsId));
        return findByIdInMap(query, gachaPoolsId, GachaPoolDB.class);
    }
}
