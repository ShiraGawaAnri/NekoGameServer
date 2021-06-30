package com.nekonade.gamegateway.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "game.gateway.waitlines.config")
@Getter
@Setter
public class WaitLinesConfig {

    private double loginPermitsPerSeconds;

    private long warmUpPeriodSeconds;

    private long maxWaitingRequests;

    private long fakeSeconds;
}
