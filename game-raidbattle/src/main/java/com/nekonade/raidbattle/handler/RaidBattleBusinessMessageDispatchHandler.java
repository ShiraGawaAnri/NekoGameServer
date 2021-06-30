package com.nekonade.raidbattle.handler;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.constcollections.RedisConstants;
import com.nekonade.common.error.exceptions.GameNotifyException;
import com.nekonade.common.utils.JacksonUtils;
import com.nekonade.dao.daos.AsyncRaidBattleDao;
import com.nekonade.dao.db.entity.RaidBattleInstance;
import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.network.param.game.messagedispatcher.DispatchGameMessageService;
import com.nekonade.raidbattle.manager.RaidBattleManager;
import com.nekonade.raidbattle.message.ServerConfig;
import com.nekonade.raidbattle.message.channel.AbstractRaidBattleChannelHandlerContext;
import com.nekonade.raidbattle.message.channel.RaidBattleChannelPromise;
import com.nekonade.raidbattle.message.context.DispatchRaidBattleEventService;
import com.nekonade.raidbattle.message.context.RaidBattleEventContext;
import com.nekonade.raidbattle.message.context.RaidBattleMessageContext;
import com.nekonade.raidbattle.message.handler.AbstractRaidBattleMessageDispatchHandler;
import com.nekonade.raidbattle.service.GameErrorService;
import io.netty.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RaidBattleBusinessMessageDispatchHandler extends AbstractRaidBattleMessageDispatchHandler<RaidBattleManager> {

    private final static Logger logger = LoggerFactory.getLogger(RaidBattleBusinessMessageDispatchHandler.class);

    private final DispatchGameMessageService dispatchGameMessageService;

    private final DispatchRaidBattleEventService dispatchRaidBattleEventService;

    private final AsyncRaidBattleDao raidBattleDao;

    private final ServerConfig serverConfig;

    private final ApplicationContext context;

    private RaidBattleInstance raidBattle;

    private RaidBattleManager raidBattleManager;

    private ScheduledFuture<?> flushToRedisScheduleFuture;
    private ScheduledFuture<?> flushToDBScheduleFuture;

    private final GameErrorService gameErrorService;

    public RaidBattleBusinessMessageDispatchHandler(ApplicationContext applicationContext) {
        super(applicationContext);
        this.context = applicationContext;
        this.raidBattleDao = applicationContext.getBean(AsyncRaidBattleDao.class);
        this.dispatchGameMessageService = applicationContext.getBean(DispatchGameMessageService.class);
        this.serverConfig = applicationContext.getBean(ServerConfig.class);
        this.dispatchRaidBattleEventService = applicationContext.getBean(DispatchRaidBattleEventService.class);
        this.gameErrorService = applicationContext.getBean(GameErrorService.class);
    }

    @Override
    protected RaidBattleManager getDataManager() {
        return raidBattleManager;
    }

    @Override
    protected Future<Boolean> updateToRedis(Promise<Boolean> promise) {
        raidBattleDao.updateToRedis(raidId, raidBattleManager.getRaidBattle(), promise);
        return null;
    }

    @Override
    protected Future<Boolean> updateToDB(Promise<Boolean> promise) {
        raidBattleDao.updateToDB(raidId, raidBattleManager.getRaidBattle(), promise);
        return null;
    }

    @Override
    public void channelRegister(AbstractRaidBattleChannelHandlerContext ctx, String raidId, RaidBattleChannelPromise promise) {
        GameNotifyException raidBattleNotFoundError = GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.RaidBattleHasGone).build();

        String result = raidBattleDao.findRaidBattleFromRedis(raidId);
        if (raidBattleDao.getThisRaidBattleNotFound(raidId)) {
            promise.setFailure(raidBattleNotFoundError);
            return;
        }
        if (!StringUtils.isEmpty(result) && !RedisConstants.RedisDefaultValue.equals(result)) {
            try {
                //raidBattle = JSON.parseObject(result, RaidBattle.class);
                raidBattle = JacksonUtils.parseObjectV2(result, RaidBattleInstance.class);
                this.raidId = this.raidBattle.getRaidId();
                raidBattleManager = new RaidBattleManager(raidBattle, context, ctx.gameChannel());
                //由攻击方面清理
                /*if (this.raidBattle.isFinish()) {
                    promise.setFailure(raidBattleNotFoundError);
                    raidBattleDao.setThisRaidBattleNotFound(raidId);
                    return;
                }
                if (this.raidBattle.getExpired() < System.currentTimeMillis()) {
                    promise.setFailure(GameNotifyException.newBuilder(GameErrorCode.RaidBattleHasExpired).build());
                    raidBattleDao.setThisRaidBattleNotFound(raidId);
                    return;
                }*/
                promise.setSuccess();
                fixTimerFlushRaidBattle(ctx);
                return;
            } catch (Exception e) {
                logger.error("channel注册时,redis转换失败,从MongoDb查找 RaidBattle {}", raidId);
            }
        }
        Promise<Optional<RaidBattleInstance>> raidBattlePromise = new DefaultPromise<>(ctx.executor());
        raidBattleDao.findRaidBattle(raidId, raidBattlePromise).addListener((GenericFutureListener<Future<Optional<RaidBattleInstance>>>) future -> {
            if (future.isSuccess()) {
                Optional<RaidBattleInstance> r = future.get();
                if (r.isEmpty()) {
                    logger.error("RaidBattle {} 不存在", raidId);
                    promise.setFailure(raidBattleNotFoundError);
                    raidBattleDao.setThisRaidBattleNotFound(raidId);
                    return;
                }
                RaidBattleInstance temp = r.get();
                this.raidBattle = temp;
                this.raidId = raidBattle.getRaidId();
                raidBattleManager = new RaidBattleManager(this.raidBattle, context, ctx.gameChannel());
                if (this.raidBattle.getFailed()) {
                    promise.setFailure(raidBattleNotFoundError);
                    raidBattleDao.setThisRaidBattleNotFound(raidId);
                    return;
                }
                if (this.raidBattle.getExpireTimestamp() < System.currentTimeMillis()) {
                    promise.setFailure(GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.RaidBattleHasExpired).build());
                    raidBattleDao.setThisRaidBattleNotFound(raidId);
                    return;
                }
                promise.setSuccess();
                fixTimerFlushRaidBattle(ctx);
            } else {
                Throwable cause = future.cause();
                cause.printStackTrace();
                promise.setFailure(cause);
                raidBattleDao.setThisRaidBattleNotFound(raidId);
            }
        });
        /*CompletableFuture<Optional<RaidBattle>> findFromDb = raidBattleDao.findRaidBattle(raidId);
        findFromDb.whenComplete((r, e) -> {
            if (e == null) {
                if (r.isPresent()) {
                    RaidBattle temp = r.get();
                    if(temp.isFinish()){
                        if(!temp.isFailed()){

                            promise.setFailure(GameNotifyException.newBuilder(GameErrorCode.RaidBattleHasGone).build());
                        } else {
                            promise.setFailure(GameNotifyException.newBuilder(GameErrorCode.RaidBattleHasGone).build());
                        }
                        return;
                    }
                    if(temp.getExpired() < System.currentTimeMillis()){
                        promise.setFailure(GameNotifyException.newBuilder(GameErrorCode.RaidBattleHasExpired).build());
                        return;
                    }
                    this.raidBattle = temp;
                    this.raidId = raidBattle.getRaidId();
                    raidBattleManager = new RaidBattleManager(this.raidBattle, context, ctx.gameChannel());
                    promise.setSuccess();
                    fixTimerFlushRaidBattle(ctx);
                } else {
                    logger.error("RaidBattle {} 不存在", raidId);
                    promise.setFailure(GameNotifyException.newBuilder(GameErrorCode.RaidBattleHasGone).build());
                }
            } else {
                promise.setFailure(e);
            }
        })*/
    }

    //写入到数据库前 检查是否由本服务器负担的战斗
    private Boolean isThisServiceDealing(String raidId) {
        String serverIdByRaidId = getServerIdByRaidId(raidId);
        return serverIdByRaidId != null && serverIdByRaidId.equals(String.valueOf(serverConfig.getServerId()));
    }

    private String getServerIdByRaidId(String raidId) {
        return this.raidBattleDao.getServerIdByRaidId(raidId);
    }

    private void cancelChannelAndNotSave(AbstractRaidBattleChannelHandlerContext ctx) {
        if (flushToDBScheduleFuture != null) {
            flushToDBScheduleFuture.cancel(true);
        }
        if (flushToRedisScheduleFuture != null) {
            flushToRedisScheduleFuture.cancel(true);
        }
        logger.warn("RaidBattle {} 非本服务器{}处理,已取消定时器", raidId, serverConfig.getServerId());
        ctx.fireChannelInactive();
        ctx.gameChannel().unsafeClose();
    }

    @Override
    public void channelInactive(AbstractRaidBattleChannelHandlerContext ctx) throws Exception {
        if (flushToDBScheduleFuture != null) {// 取消定时器
            flushToDBScheduleFuture.cancel(true);
        }
        if (flushToRedisScheduleFuture != null) {
            flushToRedisScheduleFuture.cancel(true);
        }
        //this.raidBattleDao.syncFlushRaidBattle(raidBattle);
        //logger.debug("强制flush RaidBattle {} 成功", raidBattle.getRaidId());
        logger.debug("raid channel 移除，raidId:{}", ctx.gameChannel().getRaidId());
        ctx.fireChannelInactive();
        ctx.gameChannel().unsafeClose();
        //不安全的清空引用
        raidBattleManager = null;
    }

    @Override
    public void channelRead(AbstractRaidBattleChannelHandlerContext ctx, Object msg) throws Exception {
        IGameMessage gameMessage = (IGameMessage) msg;
        RaidBattleMessageContext<RaidBattleManager> stx = new RaidBattleMessageContext<>(raidBattleManager, gameMessage, ctx);
        dispatchGameMessageService.callMethod(gameMessage, stx);
    }

    @Override
    public void userEventTriggered(AbstractRaidBattleChannelHandlerContext ctx, Object evt, Promise<Object> promise) throws Exception {
        RaidBattleEventContext<RaidBattleManager> utx = new RaidBattleEventContext<>(raidBattleManager, ctx);
        dispatchRaidBattleEventService.callMethod(utx, evt, promise);
    }

    @Override
    public void channelReadRPCRequest(AbstractRaidBattleChannelHandlerContext ctx, IGameMessage msg) throws Exception {
        super.channelReadRPCRequest(ctx, msg);
    }

    @Override
    protected void initData(AbstractRaidBattleChannelHandlerContext ctx, String raidId, RaidBattleChannelPromise promise) {
        promise.setSuccess();
    }

    private void raidBattleTimeout(AbstractRaidBattleChannelHandlerContext ctx) throws Exception {
        raidBattle.setFinish(true);
        raidBattle.setFailed(true);
        channelInactive(ctx);
        this.raidBattleDao.syncFlushRaidBattle(raidBattle);
        this.raidBattleDao.removeRaidBattleFromRedis(raidBattle);
    }

    private void fixTimerFlushRaidBattle(AbstractRaidBattleChannelHandlerContext ctx) {
        logger.debug("RaidBattle {} 生成定时器", raidId);
        int flushRedisDelay = serverConfig.getFlushRedisDelaySecond();// 获取定时器执行的延迟时间，单位是秒
        int flushDBDelay = serverConfig.getFlushDBDelaySecond();
        flushToRedisScheduleFuture = ctx.executor().scheduleWithFixedDelay(() -> {// 创建持久化数据到redis的定时任务
            long start = System.currentTimeMillis();// 任务开始执行的时间
            if (!isThisServiceDealing(raidId)) {
                cancelChannelAndNotSave(ctx);
                return;
            }
            CompletableFuture<Boolean> future = raidBattleDao.saveOrUpdateRaidBattleToRedis(raidBattle);
            future.whenComplete((r, e) -> {
                if (e == null) {
                    if (logger.isDebugEnabled()) {
                        long end = System.currentTimeMillis();
                        logger.debug("RaidBattle {} 同步数据到redis成功,耗时:{} ms", raidBattle.getRaidId(), (end - start));
                    }
                } else {
                    logger.error("RaidBattle {} 同步数据到Redis失败", raidBattle.getRaidId());
                }
                if (raidBattle.getExpireTimestamp() < start) {
                    try {
                        raidBattleTimeout(ctx);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            });
        }, flushRedisDelay, flushRedisDelay, TimeUnit.SECONDS);
        flushToDBScheduleFuture = ctx.executor().scheduleWithFixedDelay(() -> {
            long start = System.currentTimeMillis();
            if (!isThisServiceDealing(raidId)) {
                cancelChannelAndNotSave(ctx);
                return;
            }
            CompletableFuture<Boolean> future = raidBattleDao.saveOrUpdateRaidBattleToDB(raidBattle);
            future.whenComplete((r, e) -> {
                if (e == null) {
                    if (logger.isDebugEnabled()) {
                        long end = System.currentTimeMillis();
                        logger.debug("RaidBattle {} 同步数据到MongoDb成功,耗时:{} ms", raidBattle.getRaidId(), (end - start));
                    }
                } else {
                    logger.error("RaidBattle {} 同步数据到MongoDb失败", raidBattle.getRaidId());
                }
            });
        }, flushDBDelay, flushDBDelay, TimeUnit.SECONDS);

    }

}
