package com.nekonade.gamegateway.server.handler;


import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.exceptions.BasicException;
import com.nekonade.common.model.ErrorResponseEntity;
import com.nekonade.common.error.exceptions.GameNotifyException;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.common.utils.GameTimeUtils;
import com.nekonade.gamegateway.config.GatewayServerConfig;
import com.nekonade.gamegateway.config.RequestConfigLimiters;
import com.nekonade.gamegateway.config.RequestConfigs;
import com.nekonade.network.param.game.message.neko.error.GameNotificationMsgResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class RequestRateLimiterHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RequestRateLimiterHandler.class);
    //测试使用封装过的RateLimiter
    //private final RateLimiterController rateLimiterController;
    private final RateLimiter globalRateLimiter; // 全局限制器
    private int lastClientSeqId = 0;
    private final EnterGameRateLimiterController waitingLinesController;
    private final RequestConfigs requestConfigs;
    private final GatewayServerConfig gatewayServerConfig;
    private final LoadingCache<Long, RateLimiter> userRateLimiterCache;

    public RequestRateLimiterHandler(RateLimiter globalRateLimiter, EnterGameRateLimiterController waitingLinesController, GatewayServerConfig gatewayServerConfig, RequestConfigs requestConfigs) {
        this.globalRateLimiter = globalRateLimiter;
        this.waitingLinesController = waitingLinesController;
        this.gatewayServerConfig = gatewayServerConfig;
        this.requestConfigs = requestConfigs;


        this.userRateLimiterCache = Caffeine.newBuilder().maximumSize(20000).expireAfterAccess(2, TimeUnit.HOURS)
                .build(new CacheLoader<>() {
                    @Override
                    public RateLimiter load(Long key) throws Exception {
                        return RateLimiter.create(gatewayServerConfig.getRequestPerSecond());
                    }
                });
    }

    private boolean enteredGame = false;

    private GameNotificationMsgResponse buildResponse(BasicException error) {
        ErrorResponseEntity errorEntity = new ErrorResponseEntity();
        errorEntity.setErrorCode(error.getError().getErrorCode());
        errorEntity.setErrorMsg(error.getError().getErrorDesc());
        errorEntity.setData(error.getData());
        GameNotificationMsgResponse response;
        response = new GameNotificationMsgResponse();
        response.getBodyObj().setError(errorEntity);
        return response;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        GameMessagePackage gameMessagePackage = (GameMessagePackage) msg;
        GameMessageHeader header = gameMessagePackage.getHeader();
        int messageId = header.getMessageId();
        boolean isEnterGameRequest = messageId == EnumCollections.CodeMapper.GatewayMessageCode.EnterGame.getMessageId();
        boolean isHeartBeatRequest = messageId == EnumCollections.CodeMapper.GatewayMessageCode.Heartbeat.getMessageId();
        long playerId = header.getPlayerId();
        int clientSeqId = header.getClientSeqId();
        RateLimiter userRateLimiter = userRateLimiterCache.get(playerId);
        //检查请求是否被配置文件拒绝
        if (requestConfigs != null) {
            long startTime = 0;
            long endTime = 0;
            boolean allServerMaintenance = requestConfigs.isAllServerMaintenance();
            boolean triggered = false;
            boolean maintenanceNotify = false;
            if (allServerMaintenance) {
                startTime = requestConfigs.getMaintenanceStartTime();
                endTime = requestConfigs.getMaintenanceEndTime();
                maintenanceNotify = true;
                triggered = GameTimeUtils.checkTimeIsBetween(startTime, endTime);
            } else {
                List<RequestConfigLimiters> limiters = requestConfigs.getLimiters();
                if (limiters != null && limiters.size() > 0) {
                    Optional<RequestConfigLimiters> op = limiters.stream().filter(requestLimiter -> requestLimiter.getMessageId() == messageId).findAny();
                    if (op.isPresent()) {
                        RequestConfigLimiters requestLimiter = op.get();
                        logger.info("模拟拒绝已拦截请求:{}", requestLimiter);
                        startTime = requestLimiter.getStartTime();
                        endTime = requestLimiter.getEndTime();
                        maintenanceNotify = requestLimiter.isMaintenance();
                        boolean switchOn = requestLimiter.isSwitchOn();
                        if (switchOn) {
                            triggered = GameTimeUtils.checkTimeIsBetween(startTime, endTime);
                        }
                    }
                }
            }
            if (triggered) {
                GameNotifyException error;
                if (maintenanceNotify) {
                    Map<String, Object> map = new HashMap<>();
                    if (startTime != 0) {
                        map.put("startTime", startTime);
                    }
                    if (endTime != 0) {
                        map.put("endTime", endTime);
                    }
                    error = GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameGatewayError.RequestFunctionMaintenance).data(map).build();
                } else {
                    error = GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameGatewayError.RequestRefuse).build();
                }
                GameNotificationMsgResponse response = buildResponse(error);
                GameMessagePackage returnPackage = new GameMessagePackage();
                returnPackage.setHeader(response.getHeader());
                returnPackage.setBody(response.body());
                ctx.writeAndFlush(returnPackage);
                return;
            }

        }

        if (isEnterGameRequest && !enteredGame) {
            Double acquire = waitingLinesController.acquire(playerId);
            if (acquire != null && acquire > 0d) {
                long lineLength = waitingLinesController.getLineLength();
                logger.info("channel {} 的Player {} 正在排队中 总人数{}", ctx.channel().id().asShortText(), playerId, lineLength);
                //ctx.close();
                Map<String, Double> map = new HashMap<>();
                map.put("lines", (double) lineLength);
                map.put("time", acquire * (double) lineLength);
                GameNotifyException error = GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameGatewayError.RateLimiterWaitLines).data(map).build();
                GameNotificationMsgResponse response = buildResponse(error);
                GameMessagePackage returnPackage = new GameMessagePackage();
                returnPackage.setHeader(response.getHeader());
                returnPackage.setBody(response.body());
                ctx.writeAndFlush(returnPackage);
                return;
            }
            logger.info("channel {} 的Player {} 被允许登陆", ctx.channel().id().asShortText(), playerId);
            this.enteredGame = true;
        } else {
            if (!this.enteredGame && !isHeartBeatRequest) {
                logger.warn("channel {} 的Player {} 未经排队但直接进行其他请求,已驳回", ctx.channel().id().asShortText(), playerId);
                return;
            }
        }
        if (!userRateLimiter.tryAcquire(1)) {// 获取令牌失败，触发限流
            double acquire = userRateLimiter.acquire(1);
            logger.warn("channel {} 的Player {} 请求过多,驳回请求 MessageId:{} SeqId:{} RateLimiter:{}", ctx.channel().id().asShortText(), playerId, messageId, clientSeqId, userRateLimiter.hashCode());
            GameNotifyException error = GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameGatewayError.RequestTooFastUser).data(acquire).build();
            GameNotificationMsgResponse response = buildResponse(error);
            GameMessagePackage returnPackage = new GameMessagePackage();
            returnPackage.setHeader(response.getHeader());
            returnPackage.setBody(response.body());
            ctx.writeAndFlush(returnPackage);
            //ctx.close();
            return;
        }
        userRateLimiter.acquire(1);
        if (!isHeartBeatRequest) {
            if (!globalRateLimiter.tryAcquire(1)) {// 获取全局令牌失败，触发限流
                logger.warn("全局请求超载，channel {} 的Player {} 断开", ctx.channel().id().asShortText(), playerId);
                GameNotifyException error = GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameGatewayError.RequestTooFastGlobal).build();
                GameNotificationMsgResponse response = buildResponse(error);
                GameMessagePackage returnPackage = new GameMessagePackage();
                returnPackage.setHeader(response.getHeader());
                returnPackage.setBody(response.body());
                ctx.writeAndFlush(returnPackage);
                ctx.close();
                return;
            }
            globalRateLimiter.acquire(1);
        }
        if (lastClientSeqId > 0) {
            if (clientSeqId <= lastClientSeqId) {
                logger.warn("Player{} 延迟消息 ClientSeqId {} {} ", playerId, clientSeqId, gameMessagePackage);
                return;//直接返回，不再处理。
            }
        }
        this.lastClientSeqId = clientSeqId;
        ctx.fireChannelRead(msg);//不要忘记添加这个，要不然后面的handler收不到消息
    }
}
