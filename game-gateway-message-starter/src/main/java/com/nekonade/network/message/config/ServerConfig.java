package com.nekonade.network.message.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "game.server.config")
@Getter
@Setter
public class ServerConfig {
    /**
     * 游戏服务id
     */
    private int serviceId;
    /**
     * 游戏服务所在的服务器id
     */
    private int serverId;
    private String businessGameMessageTopic = "business-game-message-topic";//业务服务监听消息的topic
    private String gatewayGameMessageTopic = "gateway-game-message-topic";//网关接收消息监听的topic
    //private int workerThreads = 1024;//业务处理线程数
    private int dbThreads = 16;//db处理线程数
    private int flushRedisDelaySecond = 60;
    private int flushDBDelaySecond = 60;
}
