package com.nekonade.common.cloud;

import io.netty.util.concurrent.Promise;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.Set;

public interface BasicServiceInstance<ID,R extends ApplicationEvent> extends ApplicationListener<R>{

    Set<Integer> getAllServiceId();

    Promise<Integer> selectServerId(ID id, int serviceId, Promise<Integer> promise);

}
