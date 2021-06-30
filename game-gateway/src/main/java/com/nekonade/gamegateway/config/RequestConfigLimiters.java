package com.nekonade.gamegateway.config;

import lombok.Data;

@Data
public class RequestConfigLimiters {

    private int messageId;

    private long startTime;

    private long endTime;

    private boolean maintenance;

    private boolean switchOn;
}
