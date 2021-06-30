package com.nekonade.network.param.game.bus;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorChooserFactory.EventExecutorChooser;

import java.util.concurrent.atomic.AtomicInteger;

public class BusEventExecutorService {
    private final AtomicInteger childIndex = new AtomicInteger();
    private EventExecutor[] eventExecutors;
    private EventExecutorChooser chooser;

    private static boolean isPowerOfTwo(int val) {
        return (val & -val) == val;
    }

    public void init(int threads) {//初始化一个线程池，threahds是线程数
        if (threads <= 0) {
            threads = Runtime.getRuntime().availableProcessors() * 2;//默认取cpu核数的2倍
        }
        eventExecutors = new DefaultEventExecutor[threads];
        for (int i = 0; i < threads; i++) {
            eventExecutors[i] = new DefaultEventExecutor();
        }
        if (isPowerOfTwo(eventExecutors.length)) {//初始化一个选择器，按顺序返回一个EventExecutor
            chooser = new PowerOfTwoEventExecutorChooser();
        } else {
            chooser = new GenericEventExecutorChooser();
        }
    }

    public EventExecutor next() {// 按顺序依次返回一个EventExecutor
        return chooser.next();
    }

    public EventExecutor getExecutorByKeyHash(Object selectKey) {//根据一个key的hash值获取一个单线程池，同一个key获取的始终是同一个单线程池
        int value = Math.abs(selectKey.hashCode());
        EventExecutor executor = null;
        if (value < eventExecutors.length) {
            executor = eventExecutors[value];
        } else {
            int index = value % eventExecutors.length;
            executor = eventExecutors[index];
        }
        return executor;
    }

    private final class PowerOfTwoEventExecutorChooser implements EventExecutorChooser {
        @Override
        public EventExecutor next() {
            return eventExecutors[childIndex.getAndIncrement() & eventExecutors.length - 1];
        }
    }

    private final class GenericEventExecutorChooser implements EventExecutorChooser {
        @Override
        public EventExecutor next() {
            return eventExecutors[Math.abs(childIndex.getAndIncrement() % eventExecutors.length)];
        }
    }
}
