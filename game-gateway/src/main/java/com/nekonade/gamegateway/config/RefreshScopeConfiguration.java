package com.nekonade.gamegateway.config;


import com.nekonade.gamegateway.config.RequestConfigs;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(RequestConfigs.class)
public class RefreshScopeConfiguration {

}
