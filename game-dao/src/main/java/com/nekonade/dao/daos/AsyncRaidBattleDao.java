package com.nekonade.dao.daos;

import com.nekonade.common.concurrent.GameEventExecutorGroup;
import com.nekonade.common.constcollections.RedisConstants;
import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.db.entity.RaidBattleInstance;
import io.netty.util.concurrent.Promise;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AsyncRaidBattleDao extends AbstractAsyncDao {

    private final RaidBattleDao raidBattleDao;

    private final StringRedisTemplate redisTemplate;

    public AsyncRaidBattleDao(GameEventExecutorGroup executorGroup, RaidBattleDao raidBattleDao, StringRedisTemplate redisTemplate) {
        super(executorGroup);
        this.raidBattleDao = raidBattleDao;
        this.redisTemplate = redisTemplate;
    }

    public CompletableFuture<Optional<RaidBattleInstance>> findRaidBattle(String raidId) {
        AsyncRaidBattleDao that = this;
        return CompletableFuture.supplyAsync(() -> that.raidBattleDao.findByRaidId(raidId));
    }

    public Promise<Optional<RaidBattleInstance>> findRaidBattle(String raidId, Promise<Optional<RaidBattleInstance>> promise) {
        this.execute(raidId, promise, () -> {
            Optional<RaidBattleInstance> op = raidBattleDao.findByRaidId(raidId);
            promise.setSuccess(op);
        });
        return promise;
    }

    public CompletableFuture<Optional<RaidBattleInstance>> findByRaidIdWhichIsBattling(String raidId) {
        AsyncRaidBattleDao that = this;
        return CompletableFuture.supplyAsync(() -> that.raidBattleDao.findByRaidIdWhichIsBattling(raidId));
    }

    public void updateToRedis(String raidId, RaidBattleInstance raidBattle, Promise<Boolean> promise) {
        this.execute(raidId, promise, () -> {
            raidBattleDao.saveOrUpdateToRedis(raidBattle, raidId);
            promise.setSuccess(true);
        });
    }

    public void updateToDB(String raidId, RaidBattleInstance raidBattle, Promise<Boolean> promise) {
        this.execute(raidId, promise, () -> {
            raidBattleDao.saveOrUpdateToRedis(raidBattle, raidId);
            promise.setSuccess(true);
        });
    }

    public String findRaidBattleFromRedis(String raidId) {
        return this.raidBattleDao.findRaidBattleFromRedis(raidId);
    }

    public Boolean setThisRaidBattleNotFound(String raidId) {
        String key = EnumRedisKey.RAIDBATTLE_NOT_FOUND.getKey(raidId);
        return redisTemplate.opsForValue().setIfAbsent(key, RedisConstants.RedisDefaultValue, EnumRedisKey.RAIDBATTLE_NOT_FOUND.getTimeout());
    }

    public Boolean getThisRaidBattleNotFound(String raidId) {
        String key = EnumRedisKey.RAIDBATTLE_NOT_FOUND.getKey(raidId);
        return RedisConstants.RedisDefaultValue.equals(redisTemplate.opsForValue().get(key));
    }

    public CompletableFuture<Boolean> saveOrUpdateRaidBattleToRedis(RaidBattleInstance raidBattle) {
        AsyncRaidBattleDao that = this;
        return CompletableFuture.supplyAsync(() -> {
            long now = System.currentTimeMillis();
            long dt = raidBattle.getExpireTimestamp() - now;
            raidBattle.setRemainTime(dt);
            dt = Math.max(1, dt);
            that.raidBattleDao.saveOrUpdateToRedis(raidBattle, raidBattle.getRaidId(), Duration.ofMillis(dt));
            return true;
        }, this.getEventExecutor(raidBattle.getRaidId()));
    }

    public CompletableFuture<Boolean> saveOrUpdateRaidBattleToDB(RaidBattleInstance raidBattle) {
        AsyncRaidBattleDao that = this;
        return CompletableFuture.supplyAsync(() -> {
            that.raidBattleDao.saveOrUpdateToDB(raidBattle);
            return true;
        }, this.getEventExecutor(raidBattle.getRaidId()));
    }

    public void syncFlushRaidBattle(RaidBattleInstance raidBattle) {
        this.raidBattleDao.saveOrUpdate(raidBattle, raidBattle.getRaidId());
    }

    public void removeRaidBattleFromRedis(RaidBattleInstance raidBattle) {
        this.raidBattleDao.removeRaidBattleFromRedis(raidBattle.getRaidId());
    }

    public String getServerIdByRaidId(String raidId) {
        return this.raidBattleDao.getServerIdByRaidId(raidId);
    }
}
