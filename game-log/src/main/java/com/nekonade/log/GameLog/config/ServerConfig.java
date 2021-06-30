package com.nekonade.log.GameLog.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@ConfigurationProperties(prefix = "log.server.config")
@Getter
@Setter
@ToString
public class ServerConfig {

    private int dbThreads = 16;

    private int serverId;

    private int serviceId;

    private String gameLogic;

    private String gameRaidBattle;

    private String gameWebCenter;

    private String gameManagerCenter;

    private String topicGroupId = "defaultGroupId:" + UUID.randomUUID();


}
