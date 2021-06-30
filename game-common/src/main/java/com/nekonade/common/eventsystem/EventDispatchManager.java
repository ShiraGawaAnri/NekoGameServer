package com.nekonade.common.eventsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EventDispatchManager {
    //缓存监听的事件与事件监听器的映射，由于一个事件对应多个监听器，所以value是一个数组
    private final Map<String, List<IGameEventListener>> eventListenerMap = new HashMap<>();

    //向事件分发管理器中注册一个监听类
    public void registerListener(Class<? extends IGameEventMessage> eventClass, IGameEventListener listener) {
        String key = eventClass.getName();
        List<IGameEventListener> listeners = this.eventListenerMap.computeIfAbsent(key, k -> new ArrayList<>());
        listeners.add(listener);
    }

    public void sendGameEvent(Object origin, IGameEventMessage gameEventMessage) {
        String key = gameEventMessage.getClass().getName();
        List<IGameEventListener> listeners = this.eventListenerMap.get(key);
        if (listeners != null) {
            listeners.forEach(listener -> {
                listener.update(origin, gameEventMessage);
            });
        }
    }
}
