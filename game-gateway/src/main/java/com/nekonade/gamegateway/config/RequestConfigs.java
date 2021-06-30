package com.nekonade.gamegateway.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@RefreshScope
@ConfigurationProperties(prefix = "request.config")
@Configuration
public class RequestConfigs {

    private List<RequestConfigLimiters> limiters;


    private boolean allServerMaintenance = false;


    private long maintenanceStartTime;


    private long maintenanceEndTime;
}
