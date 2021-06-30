package com.nekonade.web.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "gateway.filter")
@Getter
@Setter
public class FilterConfig {
    /**
     * 请求权限验证白名单，在这个白名单中的所有配置不需要进行权限验证
     */
    private List<String> whiteRequestUri;
    /**
     * 全局限流器每秒钟产生的令牌数
     */
    private double globalRequestRateCount;
    /**
     * 单个用户限流器每秒产生的令牌数
     */
    private double userRequestRateCount;
    /**
     * 最大用户缓存数量
     */
    private int cacheUserMaxCount;
    /**
     * 每个用户缓存的超时时间，超过个时间，从缓存中清除。单位毫秒
     */
    private int cacheUserTimeout;


}
