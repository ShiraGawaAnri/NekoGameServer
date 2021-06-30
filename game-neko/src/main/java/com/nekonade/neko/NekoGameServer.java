package com.nekonade.neko;


import com.nekonade.dao.daos.AsyncPlayerDao;
import com.nekonade.neko.common.GameBusinessMessageDispatchHandler;
import com.nekonade.network.message.config.ServerConfig;
import com.nekonade.network.message.context.DispatchUserEventService;
import com.nekonade.network.message.context.GatewayMessageConsumerService;
import com.nekonade.network.message.handler.GameChannelIdleStateHandler;
import com.nekonade.network.param.game.messagedispatcher.DispatchGameMessageService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"com.nekonade"})
@EnableMongoRepositories(basePackages = {"com.nekonade"}) // 负责连接数据库
public class NekoGameServer {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(NekoGameServer.class, args);//初始化spring boot环境
        ServerConfig serverConfig = context.getBean(ServerConfig.class);//获取配置的实例
        DispatchGameMessageService.scanGameMessages(context, serverConfig.getServiceId(), "com.nekonade");// 扫描此服务可以处理的消息
        GatewayMessageConsumerService gatewayMessageConsumerService = context.getBean(GatewayMessageConsumerService.class);//获取网关消息监听实例
        //PlayerDao playerDao = context.getBean(PlayerDao.class);//获取Player数据操作类实例
        AsyncPlayerDao playerDao = context.getBean(AsyncPlayerDao.class);
        DispatchGameMessageService dispatchGameMessageService = context.getBean(DispatchGameMessageService.class);
        DispatchUserEventService dispatchUserEventService = context.getBean(DispatchUserEventService.class);
        //启动网关消息监听，并初始化GameChannelHandler
        gatewayMessageConsumerService.start((gameChannel) -> {
            // 初始化channel
            gameChannel.getChannelPiple().addLast(new GameChannelIdleStateHandler(300, 300, 60));
            gameChannel.getChannelPiple().addLast(new GameBusinessMessageDispatchHandler(context, serverConfig, dispatchGameMessageService, dispatchUserEventService, playerDao));
        }, serverConfig.getServerId());
    }
}
