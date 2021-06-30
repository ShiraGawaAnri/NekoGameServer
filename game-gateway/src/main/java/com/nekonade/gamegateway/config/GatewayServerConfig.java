package com.nekonade.gamegateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "game.gateway.server.config")
@Getter
@Setter
public class GatewayServerConfig {

    /**
     * 服务器ID
     */
    private int serverId;
    private int serviceId;
    private int port;
    private int bossThreadCount;
    private int workThreadCount;
    private long recBufSize;
    private long sendBufSize;
    //到达压缩的消息最小大小
    private int compressMessageSize = 1024 * 2;
    //等待认证的超时时间
    private int waitConfirmTimeoutSecond = 600;
    /**
     * 单个用户的限流请允许的每秒请求数量
     */
    private double requestPerSecond = 20;//此数值 /2 基本是每秒处理的所有请求
    /**
     * 全局流量限制请允许每秒请求数量
     */
    private double globalRequestPerSecond = 2000;
    /**
     * channel读取空闲时间
     */
    private int readerIdleTimeSeconds = 300;
    /**
     * channel写出空闲时间
     */
    private int writerIdleTimeSeconds = 12;
    private boolean enableHeartbeat = true;
    /**
     * 读写空闲时间
     */
    private int allIdleTimeSeconds = 15;
    private String businessGameMessageTopic = "business-game-message-topic";
    private String gatewayGameMessageTopic = "gateway-game-message-topic";
    private String rbBusinessGameMessageTopic = "rb-business-game-message-topic";
    private String rbGatewayGameMessageTopic = "rb-gateway-game-message-topic";
}
