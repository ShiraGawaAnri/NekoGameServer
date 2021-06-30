package com.nekonade.network.param.game;

import com.nekonade.common.gameMessage.*;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameMessageService {
    private final Logger logger = LoggerFactory.getLogger(GameMessageService.class);
    private final Map<String, Class<? extends IGameMessage>> gameMessageClassMap = new ConcurrentHashMap<>();
    private final Map<Integer, List<String>> gameMessageGroupMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 初始化的时候，将每个请求的响应的Message的class和messageId对应起来
        Reflections reflections = new Reflections("com.nekonade");
        Set<Class<? extends AbstractGameMessage>> classSet = reflections.getSubTypesOf(AbstractGameMessage.class);
        classSet.forEach(c -> {
            GameMessageMetadata messageMetadata = c.getAnnotation(GameMessageMetadata.class);
            if (messageMetadata != null) {
                this.checkGameMessageMetadata(messageMetadata, c);
                int messageId = messageMetadata.messageId();
                int groupId = messageMetadata.groupId();
                EnumMessageType messageType = messageMetadata.messageType();
                String key = this.getMessageClassCacheKey(messageType, messageId);
                gameMessageClassMap.put(key, c);
                gameMessageGroupMap.computeIfAbsent(groupId,value->{
                    List<String> list = new ArrayList<>();
                    list.add(key);
                    return list;
                });
                gameMessageGroupMap.computeIfPresent(groupId,(k,v)->{
                    if(!v.contains(key)){
                        v.add(key);
                    }
                    return v;
                });
            }
        });
    }

    private String getMessageClassCacheKey(EnumMessageType type, int messageId) {
        return messageId + ":" + type.name();
    }

    //获取响应数据包的实例
    public IGameMessage getResponseInstanceByMessageId(int messageId) {
        return this.getMessageInstance(EnumMessageType.RESPONSE, messageId);
    }

    //获取请求数据包的实例
    public IGameMessage getRequestInstanceByMessageId(int messageId) {
        return this.getMessageInstance(EnumMessageType.REQUEST, messageId);
    }

    public boolean inTargetGroup(EnumMessageType type, int messageId, int groupId){
        String key = this.getMessageClassCacheKey(type, messageId);
        return gameMessageGroupMap.entrySet().stream().anyMatch(target-> target.getKey().equals(groupId) && target.getValue().contains(key));
    }

    public int inWhichGroup(EnumMessageType type,int messageId){
        String key = this.getMessageClassCacheKey(type, messageId);
        Optional<Map.Entry<Integer, List<String>>> op = gameMessageGroupMap.entrySet().stream().filter(target -> target.getValue().contains(key)).findFirst();
        if(op.isEmpty()){
            return ConstMessageGroup.NONE;
        }
        Map.Entry<Integer, List<String>> entry = op.get();
        return entry.getKey();
    }

    //获取传数据反序列化的对象实例
    public IGameMessage getMessageInstance(EnumMessageType messageType, int messageId) {
        String key = this.getMessageClassCacheKey(messageType, messageId);
        Class<? extends IGameMessage> clazz = this.gameMessageClassMap.get(key);
        if (clazz == null) {
            this.throwMetadataException("找不到messageId:" + key + "对应的响应数据对象Class");
        }
        IGameMessage gameMessage = null;
        try {
            //gameMessage = clazz.newInstance();
            gameMessage = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            String msg = "实例化响应参数出现," + "messageId:" + key + ", class:" + clazz.getName();
            logger.error(msg, e);
            this.throwMetadataException(msg);
        }
        return gameMessage;
    }

    //检测数据对象的正确性
    private void checkGameMessageMetadata(GameMessageMetadata messageMetadata, Class<?> c) {
        int messageId = messageMetadata.messageId();
        if (messageId == 0) {
            this.throwMetadataException("messageId未设置:" + c.getName());
        }
        int serviceId = messageMetadata.serviceId();
        if (serviceId == 0) {
            this.throwMetadataException("serviceId未设置：" + c.getName());
        }
        EnumMessageType messageType = messageMetadata.messageType();
        if (messageType == null) {
            this.throwMetadataException("messageType未设置:" + c.getName());
        }

    }

    private void throwMetadataException(String msg) {
        throw new IllegalArgumentException(msg);
    }
}
