package com.nekonade.mq.system.mq;

import com.nekonade.mq.system.exception.GameMQSendFailedException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class GameMQTemplate {

    private static final Logger logger = LoggerFactory.getLogger(GameMQTemplate.class);
    private final RocketMQTemplate mqTemplate;

    public GameMQTemplate(RocketMQTemplate rocketMQTemplate) {
        this.mqTemplate = rocketMQTemplate;
    }

    public void syncSendOrderly(String topic, byte[] data, Object selector) throws GameMQSendFailedException {
        Message<byte[]> message = MessageBuilder.withPayload(data).build();
        SendResult sendResult = mqTemplate.syncSendOrderly(topic, message, selector.toString());
        if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
            logger.error("RocketMQ发送消息失败，{}", sendResult);
            throw new GameMQSendFailedException(sendResult.toString());
        }
    }


}
