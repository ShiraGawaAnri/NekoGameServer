package com.nekonade.common.config;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

/**
 * @ClassName: RedisTemplate
 * @Author: Lily
 * @Description: 用于配置RedisTemplate, 不采用AutoConfigure的原因是配置中心会比RedisTemplate的AutoConfigure慢
 * @Date: 2021/6/30
 * @Version: 1.0
 */

public class RedisTemplate {


}
