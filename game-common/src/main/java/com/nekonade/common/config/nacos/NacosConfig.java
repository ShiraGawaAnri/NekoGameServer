package com.nekonade.common.config.nacos;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "spring.cloud.nacos.discovery")
@Getter
@Setter
public class NacosConfig {

    private String ip;

    private String serverAddr;

    private String namespace;

    private String group = "DEFAULT_GROUP";

    private String service;

    private Map<String,Object> metadata;

}
