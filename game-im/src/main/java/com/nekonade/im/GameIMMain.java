package com.nekonade.im;


import com.nekonade.im.handler.GameIMHandler;
import com.nekonade.network.message.context.GatewayMessageConsumerService;
import com.nekonade.network.message.config.ServerConfig;
import com.nekonade.network.message.handler.GameChannelIdleStateHandler;
import com.nekonade.network.param.game.messagedispatcher.DispatchGameMessageService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"com.nekonade"})
@EnableMongoRepositories(basePackages = {"com.nekonade"}) // 负责连接数据库
public class GameIMMain {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(GameIMMain.class, args);//初始化spring boot环境
        ServerConfig serverConfig = context.getBean(ServerConfig.class);//获取配置的实例
        DispatchGameMessageService.scanGameMessages(context, serverConfig.getServiceId(), "com.nekonade");// 扫描此服务可以处理的消息
        GatewayMessageConsumerService gatewayMessageConsumerService = context.getBean(GatewayMessageConsumerService.class);//获取网关消息监听实例
        gatewayMessageConsumerService.start((gameChannel) -> {//启动网关消息监听，并初始化GameChannelHandler
            // 初始化channel
            gameChannel.getChannelPiple().addLast(new GameChannelIdleStateHandler(300, 300, 300));
            gameChannel.getChannelPiple().addLast(new GameIMHandler(context));
        }, serverConfig.getServerId());
    }
}
