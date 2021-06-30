package com.nekonade.network.message.context;

import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.utils.MessageUtils;
import com.nekonade.common.utils.TopicUtil;
import com.nekonade.network.message.channel.GameChannelPromise;
import com.nekonade.network.message.channel.IMessageSendFactory;
import com.nekonade.network.param.game.bus.GameMessageInnerDecoder;
import com.nekonade.common.gameMessage.GameMessagePackage;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

public class GameGatewayMessageSendFactory implements IMessageSendFactory {
    private final String topic;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public GameGatewayMessageSendFactory(KafkaTemplate<String, byte[]> kafkaTemplate, String topic) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendMessage(GameMessagePackage gameMessagePackage, GameChannelPromise promise) {
        GameMessageHeader header = gameMessagePackage.getHeader();
        header.getAttribute().addLog();
        int toServerId = header.getToServerId();
        long playerId = header.getPlayerId();
        int clientSeqId = header.getClientSeqId();
        String key = MessageUtils.kafkaKeyCreate(header);
        // 动态创建游戏网关监听消息的topic
        String sendTopic = TopicUtil.generateTopic(topic, toServerId);
        byte[] value = GameMessageInnerDecoder.sendMessageV2(gameMessagePackage);
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(sendTopic, key, value);
        kafkaTemplate.send(record);
        promise.setSuccess();
    }


}
