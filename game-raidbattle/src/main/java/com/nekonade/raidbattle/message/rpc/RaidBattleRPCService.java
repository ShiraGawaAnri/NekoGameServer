package com.nekonade.raidbattle.message.rpc;

import com.nekonade.common.cloud.PlayerServiceInstance;
import com.nekonade.common.utils.MessageUtils;
import com.nekonade.common.utils.TopicUtil;
import com.nekonade.network.param.game.bus.GameMessageInnerDecoder;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.raidbattle.service.GameErrorService;
import io.netty.util.concurrent.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.atomic.AtomicInteger;

public class RaidBattleRPCService {

    private static final Logger logger = LoggerFactory.getLogger(RaidBattleRPCService.class);
    private final AtomicInteger seqId = new AtomicInteger();// 自增的唯一序列Id
    private final int localServerId;// 本地服务实例ID
    private final PlayerServiceInstance playerServiceInstance;
    private final EventExecutorGroup eventExecutorGroup;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final RaidBattleRPCCallbackService raidBattleRpcCallbackService;
    private final String requestTopic;
    private final String responseTopic;
    private final GameErrorService gameErrorService;

    public RaidBattleRPCService(String requestTopic, String responseTopic, int localServerId, PlayerServiceInstance playerServiceInstance, GameErrorService gameErrorService, EventExecutorGroup eventExecutorGroup, KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.localServerId = localServerId;
        this.requestTopic = requestTopic;
        this.responseTopic = responseTopic;
        this.playerServiceInstance = playerServiceInstance;
        this.eventExecutorGroup = eventExecutorGroup;
        this.kafkaTemplate = kafkaTemplate;
        this.raidBattleRpcCallbackService = new RaidBattleRPCCallbackService(eventExecutorGroup);
        this.gameErrorService = gameErrorService;
    }

    public void sendRPCResponse(IGameMessage gameMessage) {
        GameMessagePackage gameMessagePackage = new GameMessagePackage();
        gameMessagePackage.setHeader(gameMessage.getHeader());
        gameMessagePackage.setBody(gameMessage.body());
        String sendTopic = TopicUtil.generateTopic(responseTopic, gameMessage.getHeader().getToServerId());
        byte[] value = GameMessageInnerDecoder.sendMessageV2(gameMessagePackage);
        ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(sendTopic, String.valueOf(gameMessage.getHeader().getPlayerId()), value);
        kafkaTemplate.send(record);
    }

    public void receiveResponse(IGameMessage gameMessage) {
        raidBattleRpcCallbackService.callback(gameMessage);
    }

    public void sendRPCRequest(IGameMessage gameMessage, Promise<IGameMessage> promise) {
        GameMessagePackage gameMessagePackage = new GameMessagePackage();
        gameMessagePackage.setHeader(gameMessage.getHeader());
        gameMessagePackage.setBody(gameMessage.body());
        GameMessageHeader header = gameMessage.getHeader();
        header.setClientSeqId(seqId.incrementAndGet());
        header.setFromServerId(localServerId);
        header.setClientSendTime(System.currentTimeMillis());
        long playerId = header.getPlayerId();
        int serviceId = header.getServiceId();

        Promise<Integer> selectServerIdPromise = new DefaultPromise<>(this.eventExecutorGroup.next());
        playerServiceInstance.selectServerId(playerId, serviceId, selectServerIdPromise).addListener((GenericFutureListener<Future<Integer>>) future -> {
            if (future.isSuccess()) {
                header.setToServerId(future.get());
                // 动态创建游戏网关监听消息的topic
                String keyId = MessageUtils.kafkaKeyCreate(header);
                String sendTopic = TopicUtil.generateTopic(requestTopic, gameMessage.getHeader().getToServerId());
                byte[] value = GameMessageInnerDecoder.sendMessageV2(gameMessagePackage);
                ProducerRecord<String, byte[]> record = new ProducerRecord<>(sendTopic, keyId, value);
                kafkaTemplate.send(record);
            } else {
                logger.error("获取目标服务实例ID出错", future.cause());
                if(promise != null){
                    promise.setFailure(future.cause());
                }
            }
            raidBattleRpcCallbackService.addCallback(header.getClientSeqId(), promise);
        });
    }

}
