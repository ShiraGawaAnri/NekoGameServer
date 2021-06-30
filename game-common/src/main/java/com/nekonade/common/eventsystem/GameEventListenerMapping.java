package com.nekonade.common.eventsystem;

import lombok.Getter;

import java.lang.reflect.Method;

@Getter
public class GameEventListenerMapping {

    private final Object bean;//处理事件方法所在的bean类

    private final Method method;//处理事件的方法

    public GameEventListenerMapping(Object bean, Method method) {
        super();
        this.bean = bean;
        this.method = method;
    }

}
