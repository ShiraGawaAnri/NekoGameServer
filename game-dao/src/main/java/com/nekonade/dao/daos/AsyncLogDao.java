package com.nekonade.dao.daos;

import com.nekonade.common.concurrent.GameEventExecutorGroup;
import com.nekonade.common.model.LogRequest;
import com.nekonade.dao.db.entity.LogGameLogicRequest;
import com.nekonade.dao.db.entity.LogGameRaidBattleRequest;
import org.springframework.data.redis.core.StringRedisTemplate;

public class AsyncLogDao extends AbstractAsyncDao {

    private final LogGameLogicDao logGameLogicDao;

    private final LogGameRaidBattleDao logGameRaidBattleDao;

    private final StringRedisTemplate redisTemplate;

    public AsyncLogDao(GameEventExecutorGroup executorGroup, LogGameLogicDao logGameLogicDao, LogGameRaidBattleDao logGameRaidBattleDao, StringRedisTemplate redisTemplate) {
        super(executorGroup);
        this.logGameLogicDao = logGameLogicDao;
        this.logGameRaidBattleDao = logGameRaidBattleDao;


        this.redisTemplate = redisTemplate;

    }

    public <T extends LogRequest> void saveGameRequestLog(T entity) {
        String operatorId = entity.getOperatorId();
        this.execute(operatorId, null, () -> {
            if (entity instanceof LogGameRaidBattleRequest) {
                logGameRaidBattleDao.saveLog((LogGameRaidBattleRequest) entity);
            } else {
                logGameLogicDao.saveLog((LogGameLogicRequest) entity);
            }
        });
    }
}
