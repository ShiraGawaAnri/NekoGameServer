package com.nekonade.network.message.rpc;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.exceptions.GameErrorException;
import com.nekonade.common.gameMessage.IGameMessage;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GameRPCCallbackService {

    private final Map<Integer, Promise<IGameMessage>> callbackMap = new ConcurrentHashMap<>();

    private final EventExecutorGroup eventExecutorGroup;

    private final int timeout = 30;// 超时时间，30s;

    public GameRPCCallbackService(EventExecutorGroup eventExecutorGroup) {
        this.eventExecutorGroup = eventExecutorGroup;
    }

    public void addCallback(Integer seqId, Promise<IGameMessage> promise) {
        if (promise == null) {
            return;
        }
        callbackMap.put(seqId, promise);
        // 启动一个延时任务，如果到达时间还没有收到返回，超抛出超时异常
        eventExecutorGroup.schedule(() -> {
            Promise<?> value = callbackMap.remove(seqId);
            if (value != null) {
                value.setFailure(GameErrorException.newBuilder(EnumCollections.CodeMapper.GameRPCError.TIME_OUT).build());
            }
        }, timeout, TimeUnit.SECONDS);
    }

    public void callback(IGameMessage gameMessage) {
        int seqId = gameMessage.getHeader().getClientSeqId();
        Promise<IGameMessage> promise = this.callbackMap.remove(seqId);
        if (promise != null) {
            promise.setSuccess(gameMessage);
        }
    }
}
