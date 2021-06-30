package com.nekonade.dao.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: MongoProperties
 * @Author: Lily
 * @Description: 重写配置
 * @Date: 2021/6/30
 * @Version: 1.0
 */

@Data
@RefreshScope
@ConfigurationProperties(prefix = "spring.data.mongodb")
@Configuration
public class MyMongoProperties {

    @Value("${host}")
    private String host;

    @Value("${database}")
    private String database;

    @Value("${username}")
    private String username;

    @Value("${password}")
    private String password;

    @Value("${authenticationDatabase}")
    private String authenticationDatabase;

}
