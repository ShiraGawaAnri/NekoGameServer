package com.nekonade.network.message.rpc;

import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.network.param.game.messagedispatcher.DispatcherMapping;
import com.nekonade.network.param.game.messagedispatcher.GameMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Service
public class DispatchRPCEventService {

    private static final Logger logger = LoggerFactory.getLogger(DispatchRPCEventService.class);

    private final Map<String, DispatcherMapping> userEventMethodCache = new HashMap<>();//数据缓存
    @Autowired
    private ApplicationContext context;//注入spring 上下文类

    @PostConstruct
    public void init() {//项目启动之后，调用此初始化方法
        Map<String, Object> beans = context.getBeansWithAnnotation(GameMessageHandler.class);//从spring 容器中获取所有被@GameMessageHandler标记的所有的类实例
        beans.values().parallelStream().forEach(c -> {//使用stream并行处理遍历这些对象
            Method[] methods = c.getClass().getMethods();
            for (Method method : methods) {//遍历每个类中的方法
                RPCEvent userEvent = method.getAnnotation(RPCEvent.class);
                if (userEvent != null) {//如果这个方法被@RPCEvent注解标记了，缓存下所有的数据
                    String key = userEvent.value().getName();
                    DispatcherMapping dispatcherMapping = new DispatcherMapping(c, method);
                    userEventMethodCache.put(key, dispatcherMapping);
                }
            }
        });
    }

    //通过反射调用处理相应事件的方法
    public void callMethod(RPCEventContext<?> ctx, IGameMessage msg) {
        String key = msg.getClass().getName();
        DispatcherMapping dispatcherMapping = this.userEventMethodCache.get(key);
        if (dispatcherMapping != null) {
            try {//通过反射调用方法
                dispatcherMapping.getTargetMethod().invoke(dispatcherMapping.getTargetObj(), ctx, msg);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                logger.error("RPC处理调用失败，消息对象:{},处理对象：{}，处理方法：{}", msg.getClass().getName(), dispatcherMapping.getTargetObj().getClass().getName(), dispatcherMapping.getTargetMethod().getName());
            }
        } else {
            logger.debug("RPC请求对象：{} 没有找到处理的方法", msg.getClass().getName());
        }
    }
}
