package com.nekonade.log.GameLog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"com.nekonade"})
@EnableMongoRepositories(basePackages = {"com.nekonade"})
public class GameLogMain {

    public static void main(String[] args) {
        SpringApplication.run(GameLogMain.class, args);
    }
}
