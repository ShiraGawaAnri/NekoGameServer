package com.nekonade.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"com.nekonade"})
@EnableMongoRepositories(basePackages = {"com.nekonade"})
@EnableDiscoveryClient
public class WebGameCenterServerMain {

    public static void main(String[] args) {
        SpringApplication.run(WebGameCenterServerMain.class, args);
    }
}
