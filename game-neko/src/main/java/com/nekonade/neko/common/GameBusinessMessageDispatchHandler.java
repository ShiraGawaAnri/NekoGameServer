package com.nekonade.neko.common;

import com.nekonade.common.constcollections.RedisConstants;
import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.common.utils.JacksonUtils;
import com.nekonade.dao.daos.AsyncPlayerDao;
import com.nekonade.dao.db.entity.Player;
import com.nekonade.network.message.channel.AbstractGameChannelHandlerContext;
import com.nekonade.network.message.channel.GameChannelPromise;
import com.nekonade.network.message.config.ServerConfig;
import com.nekonade.network.message.context.DispatchUserEventService;
import com.nekonade.network.message.context.GatewayMessageContext;
import com.nekonade.network.message.context.UserEventContext;
import com.nekonade.network.message.handler.AbstractGameMessageDispatchHandler;
import com.nekonade.network.message.manager.PlayerManager;
import com.nekonade.network.param.game.messagedispatcher.DispatchGameMessageService;
import io.netty.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GameBusinessMessageDispatchHandler extends AbstractGameMessageDispatchHandler<PlayerManager> {

    private static final Logger logger = LoggerFactory.getLogger(GameBusinessMessageDispatchHandler.class);
    private final DispatchGameMessageService dispatchGameMessageService;
    private final DispatchUserEventService dispatchUserEventService;
    // 暂时注释掉，换成异步AsyncPlayerDao
    // private PlayerDao playerDao;
    private final AsyncPlayerDao playerDao;
    private final ServerConfig serverConfig;
    private final ApplicationContext context;
    private Player player;
    private PlayerManager playerManager;
    private ScheduledFuture<?> flushToRedisScheduleFuture;
    private ScheduledFuture<?> flushToDBScheduleFuture;

    public GameBusinessMessageDispatchHandler(ApplicationContext applicationContext, ServerConfig serverConfig, DispatchGameMessageService dispatchGameMessageService, DispatchUserEventService dispatchUserEventService, AsyncPlayerDao playerDao) {
        super(applicationContext);
        this.context = applicationContext;
        this.dispatchGameMessageService = dispatchGameMessageService;
        this.playerDao = playerDao;
        this.serverConfig = serverConfig;
        this.dispatchUserEventService = dispatchUserEventService;
    }

    @Override
    public void channelRegister(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise) {
        String playerFromRedis = playerDao.findPlayerFromRedis(playerId);
        if (!StringUtils.isEmpty(playerFromRedis) && !playerFromRedis.equals(RedisConstants.RedisDefaultValue)) {
            try {
                //player = JSON.parseObject(playerFromRedis, Player.class);
                player = JacksonUtils.parseObjectV2(playerFromRedis,Player.class);
                playerManager = new PlayerManager(player, context, ctx.gameChannel());
                promise.setSuccess();
                fixTimerFlushPlayer(ctx);
                return;
            } catch (Exception e) {
                logger.error("channel注册时,redis转换失败,从MongoDb查找 player {}", playerId);
            }
        }
        // 在用户GameChannel注册的时候，对用户的数据进行初始化
        playerDao.findPlayer(playerId, new DefaultPromise<>(ctx.executor())).addListener((GenericFutureListener<Future<Optional<Player>>>) future -> {
            Optional<Player> playerOp = future.get();
            boolean foundPlayerFromDb = playerOp.isPresent();
            if (foundPlayerFromDb) {
                player = playerOp.get();
                playerManager = new PlayerManager(player, context, ctx.gameChannel());
                promise.setSuccess();
                fixTimerFlushPlayer(ctx);// 启动定时持久化数据到数据库
            } else {
                logger.error("player {} 不存在", playerId);
                promise.setFailure(new IllegalArgumentException("找不到Player数据，playerId:" + playerId));
            }
        });

    }

    private void fixTimerFlushPlayer(AbstractGameChannelHandlerContext ctx) {
        int flushRedisDelay = serverConfig.getFlushRedisDelaySecond();// 获取定时器执行的延迟时间，单位是秒
        int flushDBDelay = serverConfig.getFlushDBDelaySecond();
        flushToRedisScheduleFuture = ctx.executor().scheduleWithFixedDelay(() -> {// 创建持久化数据到redis的定时任务
            long start = System.currentTimeMillis();// 任务开始执行的时间
            Promise<Boolean> promise = new DefaultPromise<>(ctx.executor());
            playerDao.saveOrUpdatePlayerToRedis(player, promise).addListener((GenericFutureListener<Future<Boolean>>) future -> {
                if (future.isSuccess()) {
                    if (logger.isDebugEnabled()) {
                        long end = System.currentTimeMillis();
                        logger.debug("player {} 同步数据到redis成功,耗时:{} ms", player.getPlayerId(), (end - start));
                    }
                } else {
                    logger.error("player {} 同步数据到Redis失败", player.getPlayerId());
                    // 这个时候应该报警
                }
            });
        }, flushRedisDelay, flushRedisDelay, TimeUnit.SECONDS);
        flushToDBScheduleFuture = ctx.executor().scheduleWithFixedDelay(() -> {
            long start = System.currentTimeMillis();// 任务开始执行时间
            Promise<Boolean> promise = new DefaultPromise<>(ctx.executor());
            playerDao.saveOrUpdatePlayerToDB(player, promise).addListener((GenericFutureListener<Future<Boolean>>) future -> {
                if (future.isSuccess()) {
                    if (logger.isDebugEnabled()) {
                        long end = System.currentTimeMillis();
                        logger.debug("player {} 同步数据到MongoDB成功,耗时:{} ms", player.getPlayerId(), (end - start));
                    }
                } else {
                    logger.error("player {} 同步数据到MongoDB失败", player.getPlayerId());
                    // 这个时候应该报警,将数据同步到日志中，以待恢复
                }
            });
        }, flushDBDelay, flushDBDelay, TimeUnit.SECONDS);
    }

    @Override
    public void exceptionCaught(AbstractGameChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("服务器异常,playerId:{}", ctx.gameChannel().getPlayerId(), cause);
    }

    @Override
    public void channelInactive(AbstractGameChannelHandlerContext ctx) throws Exception {
        if (flushToDBScheduleFuture != null) {// 取消定时器
            flushToDBScheduleFuture.cancel(true);
        }
        if (flushToRedisScheduleFuture != null) {
            flushToRedisScheduleFuture.cancel(true);
        }
        this.playerDao.syncFlushPlayer(player);// GameChannel移除的时候，强制更新一次数据
        logger.debug("强制flush player {} 成功", player.getPlayerId());
        logger.debug("game channel 移除，playerId:{}", ctx.gameChannel().getPlayerId());
        ctx.fireChannelInactive();// 向下一个Handler发送channel失效事件
    }

    @Override
    public void channelRead(AbstractGameChannelHandlerContext ctx, Object msg) throws Exception {
        IGameMessage gameMessage = (IGameMessage) msg;
        GatewayMessageContext<PlayerManager> stx = new GatewayMessageContext<>(playerManager, gameMessage, ctx);
        playerManager.seqIncr();
        dispatchGameMessageService.callMethod(gameMessage, stx);
    }

    @Override
    public void userEventTriggered(AbstractGameChannelHandlerContext ctx, Object evt, Promise<Object> promise) throws Exception {
        UserEventContext<PlayerManager> utx = new UserEventContext<>(playerManager, ctx);
        dispatchUserEventService.callMethod(utx, evt, promise);
    }

    @Override
    protected PlayerManager getDataManager() {
        return playerManager;
    }

    @Override
    protected Future<Boolean> updateToRedis(Promise<Boolean> promise) {
        return null;
    }

    @Override
    protected Future<Boolean> updateToDB(Promise<Boolean> promise) {
        return null;
    }

    @Override
    protected void initData(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise) {

    }

}
