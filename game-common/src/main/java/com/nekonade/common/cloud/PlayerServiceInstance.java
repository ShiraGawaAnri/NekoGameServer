package com.nekonade.common.cloud;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.exceptions.GameErrorException;
import com.nekonade.common.eventsystem.event.GameChannelCloseEvent;
import com.nekonade.common.model.ServerInfo;
import com.nekonade.common.redis.EnumRedisKey;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerServiceInstance implements BasicServiceInstance<Long, GameChannelCloseEvent> {
    /**
     * 缓存PlayerID对应的所有的服务的实例的id,最外层的key是playerId，里面的Map的key是serviceId，value是serverId
     */
    private final Map<Long, Map<Integer, Integer>> serviceInstanceMap = new ConcurrentHashMap<>();

    private final EventExecutor eventExecutor = new DefaultEventExecutor();// 创建一个事件线程，操作redis的时候，使用异步
    @Autowired
    private BusinessServerService businessServerService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Set<Integer> getAllServiceId() {
        return businessServerService.getAllServiceId();
    }

    public Promise<Integer> selectServerId(Long playerId, int serviceId, Promise<Integer> promise) {
        Map<Integer, Integer> instanceMap = this.serviceInstanceMap.get(playerId);
        Integer serverId = null;
        if (instanceMap != null) {// 如果在缓存中已存在，直接获取对应的serverId
            serverId = instanceMap.get(serviceId);
        } else {// 如果不存在，创建缓存对象
            instanceMap = new ConcurrentHashMap<>();
            this.serviceInstanceMap.put(playerId, instanceMap);
        }
        if (serverId != null) {
            if (businessServerService.isEnableServer(serviceId, serverId)) {// 检测目前这个缓存的serverId的实例是否还有效，如果有效，直接返回
                promise.setSuccess(serverId);
            } else {
                serverId = null;// 如果无效，设置为空，下面再重新获取
            }
        }
        if (serverId == null) {// 重新获取一个新的服务实例serverId
            eventExecutor.execute(() -> {
                try {
                    String key = this.getRedisKey(playerId);// 从redis查找一下，是否已由别的服务计算好
                    Object value = redisTemplate.opsForHash().get(key, String.valueOf(serviceId));
                    boolean flag = true;
                    if (value != null) {
                        int serverIdOfRedis = Integer.parseInt((String) value);
                        flag = businessServerService.isEnableServer(serviceId, serverIdOfRedis);
                        if (flag) {// 如果redis中已缓存且是有效的服务实例serverId，直接返回
                            promise.setSuccess(serverIdOfRedis);
                            this.addLocalCache(playerId, serviceId, serverIdOfRedis);
                        }
                    }
                    if (value == null || !flag) {// 如果Redis中没有缓存，或实例已失效，重新获取一个新的服务实例Id
                        Integer serverId2 = this.selectServerIdAndSaveRedis(playerId, serviceId);
                        this.addLocalCache(playerId, serviceId, serverId2);
                        promise.setSuccess(serverId2);
                    }
                } catch (Throwable e) {
                    promise.setFailure(e);
                }
            });
        }
        return promise;
    }

    private void addLocalCache(long playerId, int serviceId, int serverId) {
        Map<Integer, Integer> instanceMap = this.serviceInstanceMap.get(playerId);
        instanceMap.put(serviceId, serverId);// 添加到本地缓存
    }

    private String getRedisKey(Long playerId) {
        return EnumRedisKey.SERVICE_INSTANCE.getKey(playerId.toString());
    }
    private Duration getRedisKeyExpire() {
        return EnumRedisKey.SERVICE_INSTANCE.getTimeout();
    }

    private Integer selectServerIdAndSaveRedis(Long playerId, int serviceId) {
        ServerInfo serverInfo = businessServerService.selectServerInfo(serviceId, playerId);
        if (serverInfo == null) {
            //throw new Error("警告:未检测到服务ID为[{"+serviceId+"}]的服务器在线");
            EnumCollections.CodeMapper.GameGatewayError error = EnumCollections.CodeMapper.GameGatewayError.GAME_GATEWAY_ERROR;
            EnumCollections.CodeMapper.GameGatewayError[] values = EnumCollections.CodeMapper.GameGatewayError.values();
            for (EnumCollections.CodeMapper.GameGatewayError tempError : values) {
                if (tempError.getErrorCode() == serviceId) {
                    error = tempError;
                    break;
                }
            }
            throw GameErrorException.newBuilder(error).build();
        }
        Integer serverId = serverInfo.getServerId();
        this.eventExecutor.execute(() -> {
            try {
                String key = this.getRedisKey(playerId);
                this.redisTemplate.opsForHash().put(key, String.valueOf(serviceId), String.valueOf(serverId));
                this.redisTemplate.opsForHash().getOperations().expire(key,getRedisKeyExpire());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return serverId;
    }

    @Override
    public void onApplicationEvent(GameChannelCloseEvent event) {
        this.serviceInstanceMap.remove(event.getPlayerId());
    }


}
