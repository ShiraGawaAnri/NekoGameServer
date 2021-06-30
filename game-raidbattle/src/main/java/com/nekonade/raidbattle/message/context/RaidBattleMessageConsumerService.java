package com.nekonade.raidbattle.message.context;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.nekonade.common.cloud.PlayerServiceInstance;
import com.nekonade.common.concurrent.GameEventExecutorGroup;
import com.nekonade.network.param.game.GameMessageService;
import com.nekonade.network.param.game.bus.GameMessageInnerDecoder;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.raidbattle.message.channel.RaidBattleChannelConfig;
import com.nekonade.raidbattle.message.channel.RaidBattleChannelInitializer;
import com.nekonade.raidbattle.message.channel.RaidBattleIMessageSendFactory;
import com.nekonade.raidbattle.message.channel.RaidBattleMessageEventDispatchService;
import com.nekonade.raidbattle.message.rpc.RaidBattleRPCService;
import com.nekonade.raidbattle.service.GameErrorService;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.Null;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class RaidBattleMessageConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(RaidBattleMessageConsumerService.class);
    private final EventExecutorGroup rpcWorkerGroup = new DefaultEventExecutorGroup(4);
    private LoadingCache<String, Boolean> idempotenceCache;
    private RaidBattleMessageEventDispatchService gameChannelService;// 消息事件分类发，负责将用户的消息发到相应的GameChannel之中。
    private RaidBattleIMessageSendFactory gameGatewayMessageSendFactory;// 默认实现的消息发送接口，GameChannel返回的消息通过此接口发送到kafka中
    private RaidBattleRPCService gameRpcSendFactory;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private RaidBattleChannelConfig serverConfig;// GameChannel的一些配置信息
    @Autowired
    private GameMessageService gameMessageService; // 消息管理类，负责管理根据消息id，获取对应的消息类实例
    private GameEventExecutorGroup workerGroup;// 业务处理的线程池
    @Autowired
    private PlayerServiceInstance playerServiceInstance;
    @Autowired
    private GameErrorService gameErrorService;
    @Resource
    private KafkaTemplate<String, byte[]> kafkaTemplate; // kafka客户端类
    @Resource
    private KafkaListenerEndpointRegistry registry;

    public void start(RaidBattleChannelInitializer gameChannelInitializer, int localServerId) {
        workerGroup = new GameEventExecutorGroup(serverConfig.getWorkerThreads());
        gameGatewayMessageSendFactory = new RaidBattleGatewayMessageSendFactory(kafkaTemplate, serverConfig.getGatewayGameMessageTopic());
        gameRpcSendFactory = new RaidBattleRPCService(serverConfig.getRpcRequestGameMessageTopic(), serverConfig.getRpcResponseGameMessageTopic(), localServerId, playerServiceInstance,gameErrorService, rpcWorkerGroup, kafkaTemplate);
        gameChannelService = new RaidBattleMessageEventDispatchService(context, workerGroup, gameGatewayMessageSendFactory, gameRpcSendFactory, gameChannelInitializer);

        if(!registry.getListenerContainer("default-request").isRunning()){
            registry.getListenerContainer("default-request").start();
        }
        if(!registry.getListenerContainer("rpc-request").isRunning()){
            registry.getListenerContainer("rpc-request").start();
        }
        if(!registry.getListenerContainer("rpc-response").isRunning()){
            registry.getListenerContainer("rpc-response").start();
        }
        idempotenceCache = Caffeine.newBuilder().maximumSize(1000).expireAfterAccess(Duration.ofSeconds(100)).build(key -> true);
    }


    /*@KafkaListener(id="default-request",topics = {"${game.channel.business-game-message-topic}" + "-" + "${game.server.config.server-id}"}, groupId = "${game.channel.topic-group-id}",containerFactory = "delayContainerFactory")
    public void consume(ConsumerRecord<byte[], byte[]> record) {
        String key = new String(record.key());
        Boolean flag = consumeKeys.putIfAbsent(key, true);
        if(flag != null){
            return;
        }
        IGameMessage gameMessage = this.getGameMessage(EnumMessageType.REQUEST, record.value());
        GameMessageHeader header = gameMessage.getHeader();
        String raidId = header.getAttribute().getRaidId();
        gameChannelService.fireReadMessage(raidId, gameMessage);
    }*/

    @KafkaListener(id="default-request",topics = {"${game.channel.business-game-message-topic}" + "-" + "${game.server.config.server-id}"}, groupId = "${game.channel.topic-group-id}",containerFactory = "delayBatchContainerFactory")
    public void consume(List<ConsumerRecord<String, byte[]>> records, Acknowledgment ack) {
        logger.info("Rb Records Length:{}",records.size());
        ack.acknowledge();
        records.forEach((record)->{
            String key = record.key();
            Boolean ifPresent = idempotenceCache.getIfPresent(key);
            if(ifPresent != null){
                logger.warn("传来相同的key,表示最近已接收到相同的消息,请检查kafkaKeyCreate方法的入参,拦截消息暂时不操作");
                return;
            }
            idempotenceCache.get(key);
            IGameMessage gameMessage = this.getGameMessage(EnumMessageType.REQUEST, record.value());
            GameMessageHeader header = gameMessage.getHeader();
            String raidId = header.getAttribute().getRaidId();
            gameChannelService.fireReadMessage(raidId, gameMessage);
        });
    }

    @KafkaListener(id="rpc-request",topics = {"${game.channel.rpc-request-game-message-topic}" + "-" + "${game.server.config.server-id}"}, groupId = "rpc-${game.channel.topic-group-id}",containerFactory = "delayContainerFactory")
    public void consumeRPCRequestMessage(ConsumerRecord<byte[], byte[]> record) {
        IGameMessage gameMessage = this.getGameMessage(EnumMessageType.RPC_REQUEST, record.value());
        gameChannelService.fireReadRPCRequest(gameMessage);
    }

    @KafkaListener(id="rpc-response",topics = {"${game.channel.rpc-response-game-message-topic}" + "-" + "${game.server.config.server-id}"}, groupId = "rpc-request-${game.channel.topic-group-id}",containerFactory = "delayContainerFactory")
    public void consumeRPCResponseMessage(ConsumerRecord<byte[], byte[]> record) {
        IGameMessage gameMessage = this.getGameMessage(EnumMessageType.RPC_RESPONSE, record.value());
        this.gameRpcSendFactory.receiveResponse(gameMessage);
    }

    private IGameMessage getGameMessage(EnumMessageType messageType, byte[] data) {
        GameMessagePackage gameMessagePackage = GameMessageInnerDecoder.readGameMessagePackageV2(data);
        logger.debug("RB收到消息,类型 {} - Header: {}", messageType, gameMessagePackage.getHeader());
        GameMessageHeader header = gameMessagePackage.getHeader();
        IGameMessage gameMessage = gameMessageService.getMessageInstance(messageType, header.getMessageId());
        gameMessage.read(gameMessagePackage.getBody());
        gameMessage.setHeader(header);
        gameMessage.getHeader().setMessageType(messageType);
        header.getAttribute().addLog();
        return gameMessage;
    }


}
