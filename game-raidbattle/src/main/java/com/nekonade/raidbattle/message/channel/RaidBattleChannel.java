package com.nekonade.raidbattle.message.channel;

import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.raidbattle.message.ServerConfig;
import com.nekonade.raidbattle.message.rpc.RaidBattleRPCService;
import com.nekonade.raidbattle.service.GameErrorService;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RaidBattleChannel {
    private static final Logger logger = LoggerFactory.getLogger(RaidBattleChannel.class);
    private final RaidBattleIMessageSendFactory messageSendFactory; // 发送消息的工厂类接口
    private final RaidBattleChannelPipeline channelPipeline;// 处理事件的链表
    private final RaidBattleMessageEventDispatchService gameChannelService; // 事件分发管理器
    private final List<Runnable> waitTaskList = new ArrayList<>(5);// 事件等待队列，如果GameChannel还没有注册成功，这个时候又有新的消息过来了，就让事件在这个队列中等待。
    private final String raidId;
    private final RaidBattleRPCService gameRpcSendFactory;
    private final ServerConfig serverConfig;
    private volatile EventExecutor executor;// 此channel所属的线程
    private volatile boolean registered; // 标记GameChannel是否注册成功
    private int gatewayServerId;
    @Getter
    private volatile boolean isClose;
    private final GameErrorService gameErrorService;

    public RaidBattleChannel(String raidId, RaidBattleMessageEventDispatchService gameChannelService, RaidBattleIMessageSendFactory messageSendFactory, RaidBattleRPCService gameRpcSendFactory) {
        this.gameChannelService = gameChannelService;
        this.messageSendFactory = messageSendFactory;
        channelPipeline = new RaidBattleChannelPipeline(this);
        this.raidId = raidId;
        this.gameRpcSendFactory = gameRpcSendFactory;
        this.serverConfig = gameChannelService.getApplicationContext().getBean(ServerConfig.class);
        this.gameErrorService = gameChannelService.getApplicationContext().getBean(GameErrorService.class);
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public int getGatewayServerId() {
        return gatewayServerId;
    }

    public String getRaidId() {
        return raidId;
    }

    public boolean isRegistered() {
        return this.registered;
    }

    public void register(EventExecutor executor, String raidId,IGameMessage gameMessage) {
        this.executor = executor;
        RaidBattleChannelPromise promise = new DefaultRaidBattleChannelPromise(this);
        this.channelPipeline.fireRegister(raidId, promise);
        promise.addListener(future -> {
            if (future.isSuccess()) {// 注册成功的时候，设置为true
                registered = true;
                waitTaskList.forEach(task -> {
                    task.run();// 注册channel成功之后，执行等待的任务，因为此执行这些任务和判断是否注册完成是在同一个线程中，所以此处执行完之后，waitTaskList中不会再有新的任务了。
                });
                logger.info("RaidBattle {} Channel 注册成功",raidId);
            } else {
                logger.info("RaidBattle {} Channel 注册失败", raidId/*, future.cause()*/);
                IGameMessage response = gameErrorService.returnGameErrorResponse(future.cause());
                if(gameMessage != null && response != null){
                    GameMessagePackage gameMessagePackage = wrapResponseMessage(gameMessage, response);
                    messageSendFactory.sendMessage(gameMessagePackage,null);
                }
                waitTaskList.clear();
                gameChannelService.fireInactiveChannel(raidId);
            }
        });
    }

    private GameMessagePackage wrapResponseMessage(IGameMessage requestMessage,IGameMessage response) {
        GameMessageHeader responseHeader = response.getHeader();
        GameMessageHeader requestHeader = requestMessage.getHeader();
        responseHeader.setClientSendTime(requestHeader.getClientSendTime());
        responseHeader.setClientSeqId(requestHeader.getClientSeqId());
        responseHeader.setPlayerId(requestHeader.getPlayerId());
        responseHeader.setServerSendTime(System.currentTimeMillis());
        responseHeader.setToServerId(requestHeader.getFromServerId());
        responseHeader.setFromServerId(requestHeader.getToServerId());
        responseHeader.setVersion(requestHeader.getVersion());
        GameMessagePackage gameMessagePackage = new GameMessagePackage();
        gameMessagePackage.setHeader(response.getHeader());
        gameMessagePackage.setBody(response.body());
        GameMessageHeader header = response.getHeader();
        header.setFromServerId(this.serverConfig.getServerId());
        return gameMessagePackage;
    }

    public RaidBattleChannelPipeline getChannelPipeLine() {
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
            this.isClose = true;
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


    protected void unsafeSendMessage(GameMessagePackage gameMessagePackage, RaidBattleChannelPromise promise) {
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
        this.gameChannelService.fireInactiveChannel(raidId);
    }

    public RaidBattleMessageEventDispatchService getEventDispatchService() {
        return this.gameChannelService;
    }


}
