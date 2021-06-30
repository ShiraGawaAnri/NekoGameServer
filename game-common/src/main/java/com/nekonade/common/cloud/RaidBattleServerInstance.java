package com.nekonade.common.cloud;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.exceptions.GameErrorException;
import com.nekonade.common.model.ServerInfo;
import com.nekonade.common.redis.EnumRedisKey;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RaidBattleServerInstance implements BasicServiceInstance<String, RaidBattleChannelCloseEvent> {


    private final Map<String, Map<Integer, Integer>> raidBattleServiceInstanceMap = new ConcurrentHashMap<>();

    private final EventExecutor eventExecutor = new DefaultEventExecutor();//

    private static final Interner<String> pool = Interners.newWeakInterner();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BusinessServerService businessServerService;


    public Set<Integer> getAllServiceId() {
        return businessServerService.getAllServiceId();
    }

    @Override
    public Promise<Integer> selectServerId(String raidId, int serviceId, Promise<Integer> promise) {
        Map<Integer, Integer> instanceMap = this.raidBattleServiceInstanceMap.get(raidId);
        Integer serverId = null;
        if (instanceMap != null) {// 如果在缓存中已存在，直接获取对应的serverId
            serverId = instanceMap.get(serviceId);
        } else {// 如果不存在，创建缓存对象
            instanceMap = new ConcurrentHashMap<>();
            this.raidBattleServiceInstanceMap.put(raidId, instanceMap);
        }
        if (serverId != null) {
            String key = this.getRaidBattleRedisKey(raidId);
            if (businessServerService.isEnableServer(serviceId, serverId)) {// 检测目前这个缓存的serverId的实例是否还有效
                //检查是否缓存的一致
                String id = this.redisTemplate.opsForValue().get(key);
                if(!serverId.toString().equals(id)){
                    serverId = null;
                    instanceMap.remove(serviceId);
                }else{
                    promise.setSuccess(serverId);
                }
            } else {
                serverId = null;// 如果无效，设置为空，下面再重新获取
                instanceMap.remove(serviceId);
                //很可能是失效了,尝试取得备用服务
                String raidBattleBackUpRedisKey = this.getRaidBattleBackUpRedisKey(raidId);
                String backUpServerId = redisTemplate.opsForValue().get(raidBattleBackUpRedisKey);
                if(!StringUtils.isEmpty(backUpServerId) && businessServerService.isEnableServer(serviceId, Integer.valueOf(backUpServerId))){
                    //篡夺原先的管理的服务器
                    redisTemplate.opsForValue().set(key,backUpServerId,EnumRedisKey.RAIDBATTLE_RAIDID_TO_SERVERID.getTimeout());
                    redisTemplate.delete(raidBattleBackUpRedisKey);
                    //随机出下一个
                    ServerInfo serverInfo = businessServerService.selectRaidBattleServerInfoWithOut(serviceId, raidId, Integer.valueOf(backUpServerId));
                    if(serverInfo != null){
                        redisTemplate.opsForValue().set(raidBattleBackUpRedisKey,String.valueOf(serverInfo.getServerId()),EnumRedisKey.RAIDBATTLE_RAIDID_TO_SERVERID_BACKUP.getTimeout());
                    }
                    serverId = Integer.valueOf(backUpServerId);
                    promise.setSuccess(serverId);
                }
            }
        }
        if (serverId == null) {// 重新获取一个新的服务实例serverId
            eventExecutor.execute(() -> {
                try {
                    String key = this.getRaidBattleRedisKey(raidId);// 从redis查找一下，是否已由别的服务计算好
                    synchronized (pool.intern(key)){
                        Object value = redisTemplate.opsForValue().get(key);
                        boolean flag = true;
                        if (value != null) {
                            int serverIdOfRedis = Integer.parseInt((String) value);
                            flag = businessServerService.isEnableServer(serviceId, serverIdOfRedis);
                            if (flag) {// 如果redis中已缓存且是有效的服务实例serverId，直接返回
                                promise.setSuccess(serverIdOfRedis);
                                this.addRaidBattleLocalCache(raidId, serviceId, serverIdOfRedis);
                            }
                        }
                        if (value == null || !flag) {// 如果Redis中没有缓存，或实例已失效，重新获取一个新的服务实例Id
                            Integer serverId2 = this.selectRaidBattleServerIdAndSaveRedis(raidId, serviceId);
                            this.addRaidBattleLocalCache(raidId, serviceId, serverId2);
                            promise.setSuccess(serverId2);
                        }
                    }
                } catch (Throwable e) {
                    promise.setFailure(e);
                }
            });
        }
        return promise;
    }

    /**
     * 创建战斗时调用 取得管理对应Raid的ID
     * @param raidId RaidBattle的Id
     * @param serviceId 处理的ServiceId,一般是102
     * @return serverId
     */
    public Integer selectRaidBattleServerId(String raidId, int serviceId) {
        Map<Integer, Integer> instanceMap = this.raidBattleServiceInstanceMap.get(raidId);
        String key = this.getRaidBattleRedisKey(raidId);
        Integer serverId = null;
        if (instanceMap != null) {// 如果在缓存中已存在，直接获取对应的serverId
            serverId = instanceMap.get(serviceId);
        } else {// 如果不存在，创建缓存对象
            instanceMap = new ConcurrentHashMap<>();
            this.raidBattleServiceInstanceMap.put(raidId, instanceMap);
        }
        if (serverId != null) {
            if (businessServerService.isEnableServer(serviceId, serverId)) {// 检测目前这个缓存的serverId的实例是否还有效，如果有效，直接返回
                return serverId;
            } else {
                serverId = null;// 如果无效，设置为空，下面再重新获取
            }
        }
        if (serverId == null) {// 重新获取一个新的服务实例serverId
            try {
                // 从redis查找一下，是否已由别的服务计算好
                synchronized (pool.intern(key)){
                    Object value = redisTemplate.opsForValue().get(key);
                    boolean flag = true;
                    if (value != null) {
                        int serverIdOfRedis = Integer.parseInt((String) value);
                        flag = businessServerService.isEnableServer(serviceId, serverIdOfRedis);
                        if (flag) {// 如果redis中已缓存且是有效的服务实例serverId，直接返回
                            this.addRaidBattleLocalCache(raidId, serviceId, serverIdOfRedis);
                            return serverIdOfRedis;
                        }
                    }
                    if (value == null || !flag) {// 如果Redis中没有缓存，或实例已失效，重新获取一个新的服务实例Id
                        serverId = this.selectRaidBattleServerIdAndSaveRedis(raidId, serviceId);
                        this.addRaidBattleLocalCache(raidId, serviceId, serverId);
                    }
                }
            } catch (Throwable e) {
                throw e;
            }
        }
        ServerInfo serverInfo = businessServerService.selectRaidBattleServerInfoWithOut(serviceId, raidId, serverId);
        //选另外一个服务器ID作为备用服务
        if(serverInfo != null){
            String backUpKey = this.getRaidBattleBackUpRedisKey(raidId);
            int backUpServerId = serverInfo.getServerId();
            this.redisTemplate.opsForValue().set(backUpKey,String.valueOf(backUpServerId), EnumRedisKey.RAIDBATTLE_RAIDID_TO_SERVERID_BACKUP.getTimeout());
        }
        return serverId;
    }


    private void addRaidBattleLocalCache(String raidId, int serviceId, int serverId) {
        Map<Integer, Integer> instanceMap = this.raidBattleServiceInstanceMap.get(raidId);
        instanceMap.put(serviceId, serverId);// 添加到本地缓存
    }

    private String getRaidBattleRedisKey(String raidId) {
        return EnumRedisKey.RAIDBATTLE_RAIDID_TO_SERVERID.getKey(raidId);
    }

    private String getRaidBattleBackUpRedisKey(String raidId) {
        return EnumRedisKey.RAIDBATTLE_RAIDID_TO_SERVERID_BACKUP.getKey(raidId);
    }

    private Integer selectRaidBattleServerIdAndSaveRedis(String raidId, int serviceId) {
        ServerInfo serverInfo = businessServerService.selectRaidBattleServerInfo(serviceId, raidId);
        if (serverInfo == null) {
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
                String key = this.getRaidBattleRedisKey(raidId);
                this.redisTemplate.opsForValue().set(key,String.valueOf(serverId), EnumRedisKey.RAIDBATTLE_RAIDID_TO_SERVERID.getTimeout());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return serverId;
    }

    @Override
    public void onApplicationEvent(RaidBattleChannelCloseEvent event) {
        String raidId = event.getRaidId();
        int serviceId = event.getServiceId();
        String key = this.getRaidBattleRedisKey(raidId);
        //game channel移除时 从redis移除相应的 raidId:serverId 映射
        Map<Integer, Integer> instanceMap = this.raidBattleServiceInstanceMap.get(raidId);
        if(instanceMap != null){
            Integer serverId = instanceMap.get(serviceId);
            String id = this.redisTemplate.opsForValue().get(key);
            if(serverId != null && serverId.toString().equals(id)){
                this.redisTemplate.delete(key);
            }
        }
        this.raidBattleServiceInstanceMap.remove(raidId);
    }
}
