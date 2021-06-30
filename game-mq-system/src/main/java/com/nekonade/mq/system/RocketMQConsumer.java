package com.nekonade.mq.system;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQListener;

//@Service
//@RocketMQMessageListener(topic = "rocketmq-test-my-topic", consumerGroup = "${spring.application.name}-message-consumer")
public class RocketMQConsumer implements RocketMQListener<MessageExt> {

    @Override
    public void onMessage(MessageExt message) {
        System.out.printf("------- MessageExtConsumer received message, msgId:%s, body:%s %n ", message.getMsgId(), new String(message.getBody()));

    }

}
