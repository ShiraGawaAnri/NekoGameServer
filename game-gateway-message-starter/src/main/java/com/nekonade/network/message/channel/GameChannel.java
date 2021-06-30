package com.nekonade.network.message.channel;

import com.nekonade.network.message.config.ServerConfig;
import com.nekonade.network.message.rpc.GameRPCService;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.common.gameMessage.IGameMessage;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GameChannel {
    private static final Logger logger = LoggerFactory.getLogger(GameChannel.class);
    private final IMessageSendFactory messageSendFactory; // 发送消息的工厂类接口
    private final GameChannelPipeline channelPipeline;// 处理事件的链表
    private final GameMessageEventDispatchService gameChannelService; // 事件分发管理器
    private final List<Runnable> waitTaskList = new ArrayList<>(5);// 事件等待队列，如果GameChannel还没有注册成功，这个时候又有新的消息过来了，就让事件在这个队列中等待。
    private final long playerId;
    private final GameRPCService gameRpcSendFactory;
    private final ServerConfig serverConfig;
    private volatile EventExecutor executor;// 此channel所属的线程
    private volatile boolean registered; // 标记GameChannel是否注册成功
    private int gatewayServerId;
    private boolean isClose;

    public GameChannel(long playerId, GameMessageEventDispatchService gameChannelService, IMessageSendFactory messageSendFactory, GameRPCService gameRpcSendFactory) {
        this.gameChannelService = gameChannelService;
        this.messageSendFactory = messageSendFactory;
        channelPipeline = new GameChannelPipeline(this);
        this.playerId = playerId;
        this.gameRpcSendFactory = gameRpcSendFactory;
        this.serverConfig = gameChannelService.getApplicationContext().getBean(ServerConfig.class);
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public int getGatewayServerId() {
        return gatewayServerId;
    }

    public long getPlayerId() {
        return playerId;
    }

    public boolean isRegistered() {
        return this.registered;
    }

    public void register(EventExecutor executor, long playerId) {
        this.executor = executor;
        GameChannelPromise promise = new DefaultGameChannelPromise(this);
        this.channelPipeline.fireRegister(playerId, promise);
        promise.addListener(new GenericFutureListener<Future<? super Void>>() {

            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {// 注册成功的时候，设置为true
                    registered = true;
                    waitTaskList.forEach(task -> {
                        task.run();// 注册channel成功之后，执行等待的任务，因为此执行这些任务和判断是否注册完成是在同一个线程中，所以此处执行完之后，waitTaskList中不会再有新的任务了。
                    });
                } else {
                    gameChannelService.fireInactiveChannel(playerId);
                    logger.error("player {} channel 注册失败", playerId, future.cause());
                }
            }
        });
    }

    public GameChannelPipeline getChannelPiple() {
        return channelPipeline;
    }

    public EventExecutor executor() {
        return executor;
    }

    private void safeExecute(Runnable task) {
        if (this.executor.inEventLoop()) {
            this.safeExecute0(task);
        } else {
            this.executor.execute(() -> {
                this.safeExecute0(task);
            });
        }
    }

    private void safeExecute0(Runnable task) {
        try {
            if (!this.registered) {
                waitTaskList.add(task);
            } else {
                task.run();
            }
        } catch (Throwable e) {
            logger.error("服务器异常", e);
        }
    }

    public void fireChannelInactive() {
        this.safeExecute(() -> {
            this.channelPipeline.fireChannelInactive();
        });
    }

    public void fireReadGameMessage(IGameMessage gameMessage) {
        this.safeExecute(() -> {
            if (isClose) {
                return;// channel已关闭，不再接收消息
            }
            this.gatewayServerId = gameMessage.getHeader().getFromServerId();
            this.channelPipeline.fireChannelRead(gameMessage);
        });
    }


    public void fireUserEvent(Object message, Promise<Object> promise) {
        this.safeExecute(() -> {
            this.channelPipeline.fireUserEventTriggered(message, promise);
        });
    }

    public void fireChannelReadRPCRequest(IGameMessage gameMessage) {
        this.safeExecute(() -> {
            this.channelPipeline.fireChannelReadRPCRequest(gameMessage);
        });
    }

    public void pushMessage(IGameMessage gameMessage) {
        this.safeExecute(() -> {
            this.channelPipeline.writeAndFlush(gameMessage);
        });
    }


    protected void unsafeSendMessage(GameMessagePackage gameMessagePackage, GameChannelPromise promise) {
        this.messageSendFactory.sendMessage(gameMessagePackage, promise);
    }

    protected void unsafeSendRpcMessage(IGameMessage gameMessage, Promise<IGameMessage> callback) {
        if (gameMessage.getHeader().getMessageType() == EnumMessageType.RPC_REQUEST) {
            this.gameRpcSendFactory.sendRPCRequest(gameMessage, callback);
        } else if (gameMessage.getHeader().getMessageType() == EnumMessageType.RPC_RESPONSE) {
            this.gameRpcSendFactory.sendRPCResponse(gameMessage);
        }
    }

    public void unsafeClose() {
        this.gameChannelService.fireInactiveChannel(playerId);
    }

    public GameMessageEventDispatchService getEventDispatchService() {
        return this.gameChannelService;
    }


}
