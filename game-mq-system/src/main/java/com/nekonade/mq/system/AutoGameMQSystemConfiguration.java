package com.nekonade.mq.system;

import com.nekonade.mq.system.mq.GameMQTemplate;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutoGameMQSystemConfiguration {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Bean
    public GameMQTemplate gameMQTemplate() {
        return new GameMQTemplate(rocketMQTemplate);
    }
}
