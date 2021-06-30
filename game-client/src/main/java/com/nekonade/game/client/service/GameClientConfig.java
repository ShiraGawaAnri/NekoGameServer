package com.nekonade.game.client.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "game.client.config")
@Getter
@Setter
public class GameClientConfig {
    /**
     * 客户端处理数据的线程数。
     */
    private int workThreads = 32;
    /**
     * 连接超时时间，单位秒
     */
    private int connectTimeout = 10;
    /**
     * 默认提供的游戏网关地址:localhost
     */
    private String defaultGameGatewayHost = "localhost";
    /**
     * 网关认证需要的token
     */
    private String gatewayToken;
    /**
     * 默认提供的游戏网关的端口:6001
     */
    private int defaultGameGatewayPort = 6001;
    /**
     * 是否使用服务中心，如果false，则使用默认游戏网关，不从服务中心获取网关信息，true为从服务中心 获取网关信息
     */
    private boolean useGameCenter = false;
    /**
     * 游戏服务中心地址,默认是：http://localhost:5003，可以配置为网关的地址
     */
    private String gameCenterUrl = "http://localhost:5003";
    /**
     * 消息需要压缩的大小，如果消息包大于这个值，则需要对消息进压缩。
     */
    private int messageCompressSize = 1024 * 2;
    /**
     * 客户端加密rsa私钥
     */
    private String rsaPrivateKey;
    /**
     * 客户端版本
     */
    private int version = 1;

}
