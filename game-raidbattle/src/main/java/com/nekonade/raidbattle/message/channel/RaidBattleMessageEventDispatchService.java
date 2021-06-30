package com.nekonade.raidbattle.message.channel;

import com.nekonade.common.cloud.RaidBattleChannelCloseEvent;
import com.nekonade.common.concurrent.GameEventExecutorGroup;
import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.raidbattle.message.ServerConfig;
import com.nekonade.raidbattle.message.rpc.RaidBattleRPCService;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: RaidBattleMessageEventDispatchService
 * @Author: Lily
 * @Description: RaidBattleGameChannel的管理类，主要负责管理用户id与GameChannel的映射关系，一个玩家始终只有一个GameChannel,并负责请求消息的分发
 * @Date: 2021/6/28
 * @Version: 1.0
 */
public class RaidBattleMessageEventDispatchService {
    private static final Logger logger = LoggerFactory.getLogger(RaidBattleMessageEventDispatchService.class);
    private final Map<String, RaidBattleChannel> gameChannelGroup = new HashMap<>();// 管理raidId与GameChannel的集合

    private final GameEventExecutorGroup workerGroup;// 业务处理线程池组
    private final EventExecutor executor;// 当前管理gameChannelGroup集合的事件线程池
    private final RaidBattleIMessageSendFactory messageSendFactory; // 向客户端发送消息的接口类，可以根据需求，有不同的实现，这里默认是发送到kafka的消息总线服务中。
    private final RaidBattleChannelInitializer channelInitializer;
    private final RaidBattleRPCService gameRpcSendFactory;
    private final ApplicationContext context;
    private final ServerConfig serverConfig;

    public RaidBattleMessageEventDispatchService(ApplicationContext context, GameEventExecutorGroup workerGroup, RaidBattleIMessageSendFactory messageSendFactory, RaidBattleRPCService gameRpcSendFactory, RaidBattleChannelInitializer channelInitializer) {
        this.executor = workerGroup.next();
        this.workerGroup = workerGroup;
        this.messageSendFactory = messageSendFactory;
        this.channelInitializer = channelInitializer;
        this.gameRpcSendFactory = gameRpcSendFactory;
        this.context = context;
        this.serverConfig = context.getBean(ServerConfig.class);
    }

    public ApplicationContext getApplicationContext() {
        return context;
    }

    // 此方法保证所有操作gameChannelGroup集合的行为都在同一个线程中执行，避免跨线程操作。
    private void safeExecute(Runnable task) {// 将方法的请求变成事件，在此类所属的事件线程池中执行
        if (this.executor.inEventLoop()) {// 如果当前调用这个方法的线程和此类所属的线程是同一个线程，则可以立刻执行执行。
            try {
                task.run();
            } catch (Throwable e) {
                logger.error("服务器内部错误", e);
            }
        } else {
            this.executor.execute(() -> {// 如果当前调用这个方法的线程和此类所属的线程不是同一个线程，将此任务提交到线程池中等待执行。
                try {
                    task.run();
                } catch (Throwable e) {
                    logger.error("服务器内部错误", e);
                }
            });
        }
    }

    private RaidBattleChannel getGameChannel(String raidId,IGameMessage gameMessage) {
        RaidBattleChannel raidBattleChannel = this.gameChannelGroup.get(raidId);
        if (raidBattleChannel == null) {// 从集合中获取一个GameChannel，如果这个GameChannel为空，则重新创建，并初始化注册这个Channel，完成GameChannel的初始化。
            raidBattleChannel = new RaidBattleChannel(raidId, this, messageSendFactory, gameRpcSendFactory);
            this.gameChannelGroup.put(raidId, raidBattleChannel);
            this.channelInitializer.initChannel(raidBattleChannel);// 初始化Channel，可以通过这个接口向GameChannel中添加处理消息的Handler.
            raidBattleChannel.register(workerGroup.select(raidId), raidId,gameMessage);// 发注册GameChannel的事件。
        }
        return raidBattleChannel;
    }

    public void fireReadMessage(String raidId, IGameMessage message) {// 发送接收到的消息事件
        this.safeExecute(() -> {
            RaidBattleChannel raidBattleChannel = this.getGameChannel(raidId,message);
            raidBattleChannel.fireReadGameMessage(message);
        });
    }

    public void fireReadRPCRequest(IGameMessage gameMessage) {
        this.safeExecute(() -> {
            RaidBattleChannel raidBattleChannel = this.getGameChannel(gameMessage.getHeader().getAttribute().getRaidId(),gameMessage);
            raidBattleChannel.fireChannelReadRPCRequest(gameMessage);
        });
    }

    public void fireUserEvent(String raidId, Object msg, Promise<Object> promise) {// 发送用户定义的事件
        this.safeExecute(() -> {
            RaidBattleChannel raidBattleChannel = this.getGameChannel(raidId,null);
            raidBattleChannel.fireUserEvent(msg, promise);
        });
    }

    public void fireInactiveChannel(String raidId) {// 发送GameChannel失效的事件，在这个事件中可以处理一些数据落地的操作
        this.safeExecute(() -> {
            RaidBattleChannel raidBattleChannel = this.gameChannelGroup.remove(raidId);
            if (raidBattleChannel != null) {
                raidBattleChannel.fireChannelInactive();
                // 发布GameChannel失效事件
                RaidBattleChannelCloseEvent event = new RaidBattleChannelCloseEvent(this, raidId,serverConfig.getServiceId());

                context.publishEvent(event);
            }
        });
    }

    public void broadcastMessage(IGameMessage gameMessage, String... raidIds) {// 发送消息广播事件，客多个客户端发送消息。
        if (raidIds == null || raidIds.length == 0) {
            logger.debug("广播的对象集合为空，直接返回");
            return;
        }
        this.safeExecute(() -> {
            for (String raidId : raidIds) {
                if (this.gameChannelGroup.containsKey(raidId)) {
                    RaidBattleChannel raidBattleChannel = this.getGameChannel(raidId,null);
                    raidBattleChannel.pushMessage(gameMessage);
                }
            }
        });
    }

    public void broadcastMessage(IGameMessage gameMessage) {
        this.safeExecute(() -> {
            this.gameChannelGroup.values().forEach(channel -> {
                channel.pushMessage(gameMessage);
            });
        });
    }


}
