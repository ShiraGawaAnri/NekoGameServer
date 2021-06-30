package com.nekonade.sync;

import com.nekonade.network.message.config.ServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.nekonade"})
@EnableMongoRepositories(basePackages = {"com.nekonade"}) // 负责
@EnableScheduling
public class GameSyncDb {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(GameSyncDb.class, args);//初始化spring boot环境
        ServerConfig serverConfig = context.getBean(ServerConfig.class);//获取配置的实例
    }
}
