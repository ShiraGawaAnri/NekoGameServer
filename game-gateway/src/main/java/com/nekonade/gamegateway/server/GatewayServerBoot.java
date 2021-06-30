package com.nekonade.gamegateway.server;


import com.google.common.util.concurrent.RateLimiter;
import com.nekonade.common.cloud.PlayerServiceInstance;
import com.nekonade.common.cloud.RaidBattleServerInstance;
import com.nekonade.gamegateway.config.GatewayServerConfig;
import com.nekonade.gamegateway.config.RequestConfigs;
import com.nekonade.gamegateway.config.WaitLinesConfig;
import com.nekonade.gamegateway.server.handler.*;
import com.nekonade.gamegateway.server.handler.codec.DecodeHandler;
import com.nekonade.gamegateway.server.handler.codec.EncodeHandler;
import com.nekonade.network.param.game.GameMessageService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GatewayServerBoot {

    private static final Logger logger = LoggerFactory.getLogger(GatewayServerBoot.class);

    @Autowired
    private GatewayServerConfig serverConfig;// 注入网关服务配置
    @Autowired
    private WaitLinesConfig waitLinesConfig;
    @Autowired
    private PlayerServiceInstance playerServiceInstance;
    @Autowired
    private RaidBattleServerInstance raidBattleServerInstance;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private ApplicationContext applicationContext;
    @Resource(name = "CustomKafkaTemplate")
    private KafkaTemplate<String, byte[]> kafkaTemplate;
    @Autowired
    private RequestConfigs requestConfigs;
    @Autowired
    private GameMessageService gameMessageService;

    private NioEventLoopGroup bossGroup = null;
    private NioEventLoopGroup workerGroup = null;
    private RateLimiter globalRateLimiter;
    private EnterGameRateLimiterController waitingLinesController;

    public void startServer() {
        //创建全局限流器
        globalRateLimiter = RateLimiter.create(serverConfig.getGlobalRequestPerSecond());
        //创建排队限流器
        waitingLinesController = new EnterGameRateLimiterController(waitLinesConfig);
        bossGroup = new NioEventLoopGroup(serverConfig.getBossThreadCount());
        // 业务逻辑线程组
        workerGroup = new NioEventLoopGroup(serverConfig.getWorkThreadCount());
        int port = serverConfig.getPort();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR,new AdaptiveRecvByteBufAllocator())
                    .childHandler(createChannelInitializer());
            logger.info("开始启动服务,端口:{}", port);
            ChannelFuture future = bootstrap.bind(port);
            future.channel().closeFuture().sync();
            logger.info("服务器关闭成功");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    // 连接channel初始化的时候调用
    private ChannelHandler createChannelInitializer() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                try {
                    int readerIdleTimeSeconds = serverConfig.getReaderIdleTimeSeconds();
                    int writerIdleTimeSeconds = serverConfig.getWriterIdleTimeSeconds();
                    int allIdleTimeSeconds = serverConfig.getAllIdleTimeSeconds();
                    //利用Nio已实现的检查空闲
                    if (serverConfig.isEnableHeartbeat()) {
                        pipeline.addLast(new IdleStateHandler(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds));
                    }
                    pipeline
                            .addLast("EncodeHandler", new EncodeHandler(serverConfig,applicationContext))// 添加编码Handler
                            .addLast(new LengthFieldBasedFrameDecoder(1024 * 8, 0, 4, -4, 0))// 添加拆包
                            .addLast("DecodeHandler", new DecodeHandler(applicationContext))// 添加解码
                            .addLast("ConfirmHandler", new ConfirmHandler(serverConfig, channelService, kafkaTemplate, applicationContext))
                            //添加限流handler&幕等处理
                            .addLast("RequestLimit",
                                    new RequestRateLimiterHandler(globalRateLimiter,waitingLinesController, serverConfig,requestConfigs))
                            .addLast("HeartbeatHandler", new HeartbeatHandler())
                            .addLast("IdempotenceInHandler",new IdempotenceHandler())
                            .addLast(new DispatchGameMessageHandler(kafkaTemplate, playerServiceInstance,raidBattleServerInstance, serverConfig,gameMessageService))
                    ;
                } catch (Exception e) {
                    pipeline.close();
                    logger.error("创建Channel失败", e);
                }
            }
        };
    }
}
