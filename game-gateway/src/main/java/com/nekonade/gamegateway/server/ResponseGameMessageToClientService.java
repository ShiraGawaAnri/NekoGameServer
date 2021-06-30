package com.nekonade.gamegateway.server;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nekonade.common.concurrent.GameEventExecutorGroup;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.gamegateway.config.GatewayServerConfig;
import com.nekonade.network.param.game.bus.GameMessageInnerDecoder;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.network.param.game.message.battle.RaidBattleBoardCastMsgResponse;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName: ResponseGameMessageToClientService
 * @Author: Lily
 * @Description: 接收业务服务返回的消息，并发送到客户端
 * @Date: 2021/6/28
 * @Version: 1.0
 */
@Service
public class ResponseGameMessageToClientService {

    private static final Logger logger = LoggerFactory.getLogger(ResponseGameMessageToClientService.class);

    @Autowired
    private GatewayServerConfig gatewayServerConfig;
    @Autowired
    private ChannelService channelService;

    private LoadingCache<String, Boolean> idempotenceCache;

    private LoadingCache<String, Boolean> idempotenceCache2;



    @PostConstruct
    public void init() {
        logger.info("监听消息接收业务消息topic:{}", gatewayServerConfig.getGatewayGameMessageTopic());
        idempotenceCache = Caffeine.newBuilder().maximumSize(20000).expireAfterAccess(Duration.ofSeconds(100)).build(key -> true);
        idempotenceCache2 = Caffeine.newBuilder().maximumSize(20000).expireAfterAccess(Duration.ofSeconds(100)).build(key -> true);
    }

    @KafkaListener(topics = {"${game.gateway.server.config.gateway-game-message-topic}"}, groupId = "${game.gateway.server.config.server-id}",containerFactory = "batchContainerFactory")
    public void receiver(List<ConsumerRecord<String, byte[]>> records, Acknowledgment ack) {
        logger.debug("Player Request Receiver:{}",records.size());
        ack.acknowledge();
        records.forEach(record->{
            String key = record.key();
            Boolean ifPresent = idempotenceCache.getIfPresent(key);
            if(ifPresent != null){
                logger.warn("传来相同的key,表示最近已接收到相同的消息,请检查kafkaKeyCreate方法的入参,拦截消息暂时不操作");
                return;
            }
            idempotenceCache.get(key);
            GameMessagePackage gameMessagePackage = GameMessageInnerDecoder.readGameMessagePackageV2(record.value());
            //logger.info("接到Player{}的 MessageId{} Time:{}",gameMessagePackage.getHeader().getPlayerId(),gameMessagePackage.getHeader().getMessageId(),System.currentTimeMillis());
            GameMessageHeader header = gameMessagePackage.getHeader();
            Long playerId = header.getPlayerId();//从包头中获取这个消息包归属的playerId
            Channel channel = channelService.getChannel(playerId);//根据playerId找到这个客户端的连接Channel
            if (channel != null) {
                channel.writeAndFlush(gameMessagePackage);//给客户端返回消息
            }
        });
    }

    @KafkaListener(topics = {"${game.gateway.server.config.rb-gateway-game-message-topic}"}, groupId = "${game.gateway.server.config.server-id}",containerFactory = "batchContainerFactory")
    public void raidBattleReceiver(List<ConsumerRecord<String, byte[]>> records, Acknowledgment ack) {
        logger.debug("RaidBattleReceiver Records:{}",records.size());
        ack.acknowledge();
        records.forEach(record->{
            String key = record.key();
            Boolean ifPresent = idempotenceCache.getIfPresent(key);
            if(ifPresent != null){
                logger.warn("传来相同的key,表示最近已接收到相同的消息,请检查kafkaKeyCreate方法的入参,拦截消息暂时不操作");
                return;
            }
            idempotenceCache.get(key);
            GameMessagePackage gameMessagePackage = GameMessageInnerDecoder.readGameMessagePackageV2(record.value());
            GameMessageHeader header = gameMessagePackage.getHeader();
            Long playerId = header.getPlayerId();//从包头中获取这个消息包归属的playerId
            header.getAttribute().addLog();
            Channel channel = channelService.getChannel(playerId);
            if (channel != null) {
                channel.writeAndFlush(gameMessagePackage);
            }
        });
    }



    /*@KafkaListener(topics = {"RaidBattle-Status"}, groupId = "${game.gateway.server.config.server-id}")
    public void raidBattleStatusReceiver(ConsumerRecord<String, byte[]> record) {
        GameMessagePackage gameMessagePackage = GameMessageInnerDecoder.readGameMessagePackageV2(record.value());

        List<?> broadIds = gameMessagePackage.getHeader().getAttribute().getBroadIds();
        List<Long> playerIds = broadIds.stream().map(each -> Long.valueOf(each.toString())).collect(Collectors.toList());
        channelService.broadcast(gameMessagePackage,playerIds);
    }*/

    @KafkaListener(topics = {"RaidBattle-Status"}, groupId = "${game.gateway.server.config.server-id}",containerFactory = "batchContainerFactory")
    public void raidBattleStatusReceiver(List<ConsumerRecord<String, byte[]>> records, Acknowledgment ack) {
        logger.debug("BattleStatusReceiver Records:{}",records.size());
        ack.acknowledge();
        records.forEach(record->{
            String key = record.key();
            logger.info("Record Key {}",key);
            Boolean ifPresent = idempotenceCache2.getIfPresent(key);
            if(ifPresent != null){
                logger.warn("传来相同的key,表示最近已接收到相同的消息,请检查kafkaKeyCreate方法的入参,拦截消息暂时不操作");
                return;
            }
            idempotenceCache2.get(key);
            GameMessagePackage gameMessagePackage = GameMessageInnerDecoder.readGameMessagePackageV2(record.value());
            GameMessageHeader header = gameMessagePackage.getHeader();
            int messageId = header.getMessageId();
            header.getAttribute().addLog();
            List<Long> playerIds = gameMessagePackage.getHeader().getAttribute().getBroadIds();
            if((playerIds == null || playerIds.size() == 0) && messageId == EnumCollections.CodeMapper.GatewayMessageCode.RaidBattleBoardCastMsgResponse.getErrorCode()){
                //解包
                RaidBattleBoardCastMsgResponse entity = new RaidBattleBoardCastMsgResponse();
                entity.read(gameMessagePackage.getBody());
                playerIds = entity.getBodyObj().getPlayers().entrySet().stream().filter(entry->!entry.getValue().isRetreated()).map(Map.Entry::getKey).collect(Collectors.toList());
                playerIds.remove(header.getPlayerId());
            }
            channelService.broadcast(gameMessagePackage,playerIds);
        });
    }
}
