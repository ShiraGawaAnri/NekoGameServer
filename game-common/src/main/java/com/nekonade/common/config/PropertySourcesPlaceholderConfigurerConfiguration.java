package com.nekonade.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

/**
 * @ClassName: PropertySourcesPlaceholderConfigurerConfiguration
 * @Author: Lily
 * @Description: 忽略Nacos Cannot Reslove placholder的问题,但实际能取到值
 * @Date: 2021/6/30
 * @Version: 1.0
 */
@Component
public class PropertySourcesPlaceholderConfigurerConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {

        PropertySourcesPlaceholderConfigurer c = new PropertySourcesPlaceholderConfigurer();

        c.setIgnoreUnresolvablePlaceholders(true);

        return c;

    }
}
