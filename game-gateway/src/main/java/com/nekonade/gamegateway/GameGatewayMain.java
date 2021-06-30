package com.nekonade.gamegateway;

import com.nekonade.gamegateway.server.GatewayServerBoot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = {"com.nekonade.gamegateway", "com.nekonade", "com.nekonade.common"})
public class GameGatewayMain {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(GameGatewayMain.class);
        GatewayServerBoot serverBoot = context.getBean(GatewayServerBoot.class);
        serverBoot.startServer();
    }
}
