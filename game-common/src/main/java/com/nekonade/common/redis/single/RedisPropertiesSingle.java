package com.nekonade.common.redis.single;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @ClassName: RedisPropertiesSingle
 * @Author: Lily
 * @Description: 动态加载配置中心的内容获取redis属性,单机版
 * @Date: 2021/6/30
 * @Version: 1.0
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "spring.redis")
@Configuration
public class RedisPropertiesSingle {

    @Value("${host}")
    private String host;

    @Value("${username}")
    private String username;

    @Value("${password}")
    private String password;

    @Value("${port}")
    private String port;

    @Value("${timeout}")
    private String timeout;

    private Integer commandTimeout;

    private Integer maxAttempts;

    private Integer maxRedirects;

    private Integer maxActive;

    private Integer maxWait;

    private Integer maxIdle;

    private Integer minIdle;

    private boolean testOnBorrow;
}
