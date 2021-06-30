package com.nekonade.center.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nekonade.center.dataconfig.GameGatewayInfo;
import com.nekonade.common.config.nacos.NacosConfig;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.exceptions.GameErrorException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class GameGatewayService implements ApplicationListener<HeartbeatEvent> {

    private static final Logger logger = LoggerFactory.getLogger(GameGatewayService.class);

    private final String SUBSCRIBE_SERVICE_NAME = "game-gateway";

    private List<GameGatewayInfo> gameGatewayInfoList;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private NacosConfig nacosConfig;

    private LoadingCache<Long, GameGatewayInfo> userGameGatewayCache;// 用户分配到的网关缓存

    @PostConstruct
    public void init() throws NacosException {// 游戏服务中心启动之后，向Consul/Nacos获取注册的游戏网关信息
        this.refreshGameGatewayInfo();
        // 初始化用户分配的游戏网关信息缓存。
        // 当调用userGameGatewayCache.get(Long id)不存在时，会进入load方法
        userGameGatewayCache = Caffeine.newBuilder().recordStats()
                .maximumSize(20000)
                .expireAfterAccess(2, TimeUnit.HOURS)
                .build(new CacheLoader<>() {
                    @Override
                    public GameGatewayInfo load(Long key) throws Exception {
                        GameGatewayInfo gameGatewayInfo = selectGameGateway(key);
                        return gameGatewayInfo;
                    }
                });
       /* userGameGatewayCache = CacheBuilder.newBuilder().maximumSize(20000).expireAfterAccess(2, TimeUnit.HOURS)
                .build(new CacheLoader<>() {
                    @Override
                    public GameGatewayInfo load(Long key) throws Exception {
                        GameGatewayInfo gameGatewayInfo = selectGameGateway(key);
                        return gameGatewayInfo;
                    }
                });*/
        this.subscribeRefresh();
    }

    private void subscribeRefresh() throws NacosException {
        String serviceName = SUBSCRIBE_SERVICE_NAME;
        Properties properties = new Properties();
        properties.setProperty("serverAddr", nacosConfig.getServerAddr());
        properties.setProperty("namespace", nacosConfig.getNamespace());
        properties.setProperty("group", nacosConfig.getGroup());
        NamingService namingService = NamingFactory.createNamingService(properties);
        //暂不限定Group
        namingService.subscribe(serviceName/*,nacosConfig.getGroup()*/, event -> {
            if (event instanceof NamingEvent) {
                List<GameGatewayInfo> initGameGatewayInfoList = new ArrayList<>();
                List<Instance> instances = ((NamingEvent) event).getInstances();
                AtomicInteger gameGatewayId = new AtomicInteger(1);
                instances.forEach(each -> {
                    Map<String, String> metadata = each.getMetadata();
                    String serverId = metadata.get("serverId");
                    String serviceId = metadata.get("serviceId");
                    if (StringUtils.isAllEmpty(serverId, serviceId)) {
                        return;
                    }
                    String wh = metadata.get("weight");
                    int weight = StringUtils.isEmpty(wh) ? 1 : Integer.parseInt(wh);
                    for (int i = 0; i < weight; i++) {
                        int id = gameGatewayId.getAndIncrement();
                        GameGatewayInfo gameGatewayInfo = this.newGameGatewayInfo(id, each);// 构造游戏网关信息类
                        if (gameGatewayInfo != null) {
                            initGameGatewayInfoList.add(gameGatewayInfo);
                        }
                    }

                });
                logger.debug("刷新游戏网关：{}", initGameGatewayInfoList);
                Collections.shuffle(initGameGatewayInfoList);
                this.gameGatewayInfoList = initGameGatewayInfoList;
            }
        });
    }

    private void refreshGameGatewayInfo() {// 刷新游戏网关列表信息。
        // 根据serviceId根据服务信息，这里的serviceId就是在application.yml中配置的service-name，它会注册到Consul中。
        List<ServiceInstance> gameGatewayServiceInstances = discoveryClient.getInstances(SUBSCRIBE_SERVICE_NAME);
        List<GameGatewayInfo> initGameGatewayInfoList = new ArrayList<>();
        AtomicInteger gameGatewayId = new AtomicInteger(1);// Id自增
        gameGatewayServiceInstances.forEach(instance -> {
            int weight = this.getGameGatewayWeight(instance);
            for (int i = 0; i < weight; i++) {// 根据权重初始化游戏网关数量。
                int id = gameGatewayId.getAndIncrement();
                GameGatewayInfo gameGatewayInfo = this.newGameGatewayInfo(id, instance);// 构造游戏网关信息类
                if (gameGatewayInfo != null) {
                    initGameGatewayInfoList.add(gameGatewayInfo);
                    logger.debug("刷新游戏网关：{}", gameGatewayInfo);
                }
            }
        });
        Collections.shuffle(initGameGatewayInfoList);
        this.gameGatewayInfoList = initGameGatewayInfoList;
    }

    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        this.refreshGameGatewayInfo();// 根据心跳事件，刷新游戏网关列表信息。
    }

    private GameGatewayInfo newGameGatewayInfo(int id, Instance instance) {
        GameGatewayInfo gameGatewayInfo = new GameGatewayInfo();
        gameGatewayInfo.setId(id);
        // 网关服务注册的地址
        String ip = instance.getIp();
        // 网关中手动配置的长连接端口
        String portStr = instance.getMetadata().get("gamePort");
        if (StringUtils.isEmpty(portStr)) {
            portStr = "6000";
        }
        int port = Integer.valueOf(portStr);
        if (port == 0) {
            return null;
        }
        // 获取网关服务注册的http端口
        int httpPort = instance.getPort();
        gameGatewayInfo.setIp(ip);
        gameGatewayInfo.setPort(port);
        gameGatewayInfo.setHttpPort(httpPort);
        String gameProxyHost = instance.getMetadata().get("gameProxyHost");
        if (gameProxyHost != null) {
            gameGatewayInfo.setIp(gameProxyHost);
            String gameProxyPort = instance.getMetadata().get("gameProxyPort");
            if (gameProxyPort != null) {
                gameGatewayInfo.setPort(Integer.parseInt(gameProxyPort));
                logger.info("使用网关代理地址：{}:{}", gameProxyHost, gameProxyPort);
            }

        }
        return gameGatewayInfo;
    }

    private GameGatewayInfo newGameGatewayInfo(int id, ServiceInstance instance) {
        GameGatewayInfo gameGatewayInfo = new GameGatewayInfo();
        gameGatewayInfo.setId(id);
        // 网关服务注册的地址
        String ip = instance.getHost();
        // 网关中手动配置的长连接端口
        int port = this.getGameGatewayPort(instance);
        if (port == 0) {
            return null;
        }
        // 获取网关服务注册的http端口
        int httpPort = instance.getPort();
        gameGatewayInfo.setIp(ip);
        gameGatewayInfo.setPort(port);
        gameGatewayInfo.setHttpPort(httpPort);
        String gameProxyHost = instance.getMetadata().get("gameProxyHost");
        if (gameProxyHost != null) {
            gameGatewayInfo.setIp(gameProxyHost);
            String gameProxyPort = instance.getMetadata().get("gameProxyPort");
            if (gameProxyPort != null) {
                gameGatewayInfo.setPort(Integer.parseInt(gameProxyPort));
                logger.info("使用网关代理地址：{}:{}", gameProxyHost, gameProxyPort);
            }

        }

        return gameGatewayInfo;
    }

    private int getGameGatewayPort(ServiceInstance instance) {
        String value = instance.getMetadata().get("gamePort");
        if (value == null) {
            logger.warn("游戏网关{}未配置长连接端口，使用默认端口6000", instance.getServiceId());
            return 0;
        }
        return Integer.parseInt(value);
    }

    private int getGameGatewayWeight(ServiceInstance instance) {
        String value = instance.getMetadata().get("weight");
        if (value == null) {
            value = "1";
        }
        return Integer.parseInt(value);
    }

    public GameGatewayInfo getGameGatewayInfo(Long id) throws ExecutionException {// 向客户端提供可以使用的游戏网关信息
        GameGatewayInfo gameGatewayInfo = userGameGatewayCache.get(id);
        if (gameGatewayInfo != null) {
            List<GameGatewayInfo> gameGatewayInfos = this.gameGatewayInfoList;
            // 检测缓存的网关是否还有效，如果已被移除，从缓存中删除，并重新分配一个游戏网关信息。
            if (!gameGatewayInfos.contains(gameGatewayInfo)) {
                userGameGatewayCache.invalidate(id);
                gameGatewayInfo = userGameGatewayCache.get(id);// 这时，缓存中已不存在playerId对应的值，会重新初始化。
            }
        }
        return gameGatewayInfo;
    }

    private GameGatewayInfo selectGameGateway(Long playerId) {// 从游戏网关列表中选择一个游戏网关信息返回。
        // 再次声明一下，防止游戏网关列表发生变化，导致数据不一致。
        List<GameGatewayInfo> temGameGatewayInfoList = this.gameGatewayInfoList;
        if (temGameGatewayInfoList == null || temGameGatewayInfoList.size() == 0) {
            throw GameErrorException.newBuilder(EnumCollections.CodeMapper.GameCenterError.NO_GAME_GATEWAY_INFO).build();
        }
        int hashCode = Math.abs(playerId.hashCode());
        int gatewayCount = temGameGatewayInfoList.size();
        int index = hashCode % gatewayCount;
        return temGameGatewayInfoList.get(index);
    }

}
