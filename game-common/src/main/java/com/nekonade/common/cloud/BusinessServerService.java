package com.nekonade.common.cloud;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.nekonade.common.config.nacos.NacosConfig;
import com.nekonade.common.model.ServerInfo;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Service
public class BusinessServerService implements ApplicationListener<HeartbeatEvent> {

    private static final Logger logger = LoggerFactory.getLogger(BusinessServerService.class);

    private final String SUBSCRIBE_SERVICE_NAME = "game-logic";

    @Autowired
    private NacosConfig nacosConfig;
    @Autowired
    private DiscoveryClient discoveryClient;

    /*@Resource(name = "CustomKafkaTemplate")
    private KafkaTemplate<String, byte[]> kafkaTemplate;*/

    private Map<Integer, List<ServerInfo>> serverInfos; // serviceId对应的服务器集合，一个服务可能部署到多台服务器上面，实现负载均衡

    @PostConstruct
    public void init() throws NacosException {
        this.refreshBusinessServerInfo();
        this.subscribeRefresh();
    }

    private void subscribeRefresh() throws NacosException {
        Properties properties = new Properties();
        properties.setProperty("serverAddr",nacosConfig.getServerAddr());
        properties.setProperty("namespace",nacosConfig.getNamespace());
        properties.setProperty("group",nacosConfig.getGroup());
        NamingService namingService = NamingFactory.createNamingService(properties);
        //暂不限定Group
        namingService.subscribe(SUBSCRIBE_SERVICE_NAME/*,nacosConfig.getGroup()*/,event -> {
            if(event instanceof NamingEvent){
                Map<Integer, List<ServerInfo>> tempServerInfoMap = new ConcurrentHashMap<>();
                List<Instance> instances = ((NamingEvent) event).getInstances();
                //CopyOnWriteArrayList<ServerInfo> serverList = new CopyOnWriteArrayList<>();
                instances.forEach(each->{
                    Map<String, String> metadata = each.getMetadata();
                    String serverId = metadata.get("serverId");
                    String serviceId = metadata.get("serviceId");
                    if(StringUtils.isAllEmpty(serverId,serviceId)){
                        return;
                    }
                    String wh = metadata.get("weight");
                    int weight = StringUtils.isEmpty(wh) ? 1 : Integer.parseInt(wh);
                    for (int i = 0;i < weight;i++){
                        int port = each.getPort();
                        String ip = each.getIp();
                        ServerInfo serverInfo = new ServerInfo();
                        serverInfo.setServiceId(Integer.parseInt(serviceId));
                        serverInfo.setServerId(Integer.parseInt(serverId));
                        serverInfo.setHost(ip);
                        serverInfo.setPort(port);
                        //serverList.addIfAbsent(serverInfo);
                        List<ServerInfo> serverList = tempServerInfoMap.get(serverInfo.getServiceId());
                        if(serverList == null){
                            serverList = new CopyOnWriteArrayList<>();
                            tempServerInfoMap.put(serverInfo.getServiceId(), serverList);
                        }
                        serverList.add(serverInfo);
                    }

                });
                logger.debug("订阅服务更新,{}", tempServerInfoMap.toString());
                this.serverInfos = tempServerInfoMap;
            }
        });
    }

    /*public KafkaTemplate<String, byte[]> getKafkaTemplate() {
        return kafkaTemplate;
    }*/

    public Set<Integer> getAllServiceId() {
        return serverInfos.keySet();
    }

    //由于nacos问题,各组件得到的结果未必是最新的 - 最迟可能1分钟差距
    private void refreshBusinessServerInfo() {// 刷新网关后面的服务列表
        Map<Integer, List<ServerInfo>> tempServerInfoMap = new ConcurrentHashMap<>();
        List<ServiceInstance> businessServiceInstances = discoveryClient.getInstances(SUBSCRIBE_SERVICE_NAME);//网取网关后面的服务实例
        businessServiceInstances.forEach(instance -> {
            int weight = this.getServerInfoWeight(instance);
            for (int i = 0; i < weight; i++) {
                ServerInfo serverInfo = this.newServerInfo(instance);
                List<ServerInfo> serverList = tempServerInfoMap.computeIfAbsent(serverInfo.getServiceId(), k -> new ArrayList<>());
                serverList.add(serverInfo);
            }
        });
        //logger.debug("抓取游戏服务配置成功,{}", tempServerInfoMap.toString());
        this.serverInfos = tempServerInfoMap;
    }

    public ServerInfo selectServerInfo(Integer serviceId, Long playerId) {
        Map<Integer, List<ServerInfo>> serverInfoMap = this.serverInfos;
        List<ServerInfo> serverList = serverInfoMap.get(serviceId);
        if (serverList == null || serverList.size() == 0) {
            logger.info("选择Service失败 serviceId : {},playerId:{},serverInfoMap:{}",serviceId,playerId,serverInfoMap);
            return null;
        }
        int hashCode = Math.abs(playerId.hashCode());
        int gatewayCount = serverList.size();
        int index = hashCode % gatewayCount;
        if (index >= gatewayCount) {
            index = gatewayCount - 1;
        }
        return serverList.get(index);
    }


    /**
     * 返回特定serverId以外的一个随机ServerInfo
     * @param serviceId 服务ID,一般102
     * @param raidId RaidBattleID
     * @param serverId 指定除外的服务器ID
     * @return ServerInfo
     */
    public ServerInfo selectRaidBattleServerInfoWithOut(Integer serviceId,String raidId,Integer serverId){
        Map<Integer, List<ServerInfo>> serverInfoMap = new HashMap<>();
        this.serverInfos.forEach(serverInfoMap::put);
        List<ServerInfo> serverList = serverInfoMap.get(serviceId);
        List<ServerInfo> collect = serverList.stream().filter(each -> each.getServerId() != serverId).collect(Collectors.toList());
        if (collect.size() == 0) {
            return null;
        }
        int hashCode = Math.abs(raidId.hashCode());
        int gatewayCount = collect.size();
        int index = hashCode % gatewayCount;
        if (index >= gatewayCount) {
            index = gatewayCount - 1;
        }
        return collect.get(index);
    }

    public ServerInfo selectRaidBattleServerInfo(Integer serviceId,String raidId){
        Map<Integer, List<ServerInfo>> serverInfoMap = this.serverInfos;
        List<ServerInfo> serverList = serverInfoMap.get(serviceId);
        if (serverList == null || serverList.size() == 0) {
            return null;
        }
        int hashCode = Math.abs(raidId.hashCode());
        int gatewayCount = serverList.size();
        int index = hashCode % gatewayCount;
        if (index >= gatewayCount) {
            index = gatewayCount - 1;
        }
        return serverList.get(index);
    }

    public boolean isEnableServer(Integer serviceId, Integer serverId) {
        Map<Integer, List<ServerInfo>> serverInfoMap = this.serverInfos;
        List<ServerInfo> serverInfoList = serverInfoMap.get(serviceId);
        if (serverInfoList != null) {
            return serverInfoList.stream().anyMatch(c -> c.getServerId() == serverId);
        }
        return false;

    }

    private ServerInfo newServerInfo(ServiceInstance instance) {
        String serviceId = instance.getMetadata().get("serviceId");
        String serverId = instance.getMetadata().get("serverId");
        if (StringUtils.isEmpty(serviceId)) {
            throw new IllegalArgumentException(instance.getHost() + "的服务未配置serviceId");
        }

        if (StringUtils.isEmpty(serverId)) {
            throw new IllegalArgumentException(instance.getHost() + "的服务未配置serverId");
        }
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setServiceId(Integer.parseInt(serviceId));
        serverInfo.setServerId(Integer.parseInt(serverId));
        serverInfo.setHost(instance.getHost());
        serverInfo.setPort(instance.getPort());

        return serverInfo;
    }

    private int getServerInfoWeight(ServiceInstance instance) {
        String value = instance.getMetadata().get("weight");
        if (value == null) {
            value = "1";
        }
        return Integer.parseInt(value);
    }

    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        //this.refreshBusinessServerInfo();
    }
}
