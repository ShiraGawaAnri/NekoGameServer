package com.nekonade.gamegateway.server.handler;



import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import com.nekonade.gamegateway.config.WaitLinesConfig;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class EnterGameRateLimiterController {

    private final RateLimiter rateLimiter;

    //同一秒允许多少人登录
    private final double loginPermitsPerSeconds;

    //等待获取登录许可的请求个数，原则上可以通过maxPermits推算
    private final long maxWaitingRequests;

    private final long warmUpPeriodSeconds;

    private final long tryAcquireWait;

    //当前队列
    /*@Getter
    private final ConcurrentHashMap<Long, Long> waitLoginDeque;*/

    private final EventExecutor eventExecutor = new DefaultEventExecutor();

    private final LoadingCache<Long, Long> waitLoginDeque;

    public long getLineLength(){
        return waitLoginDeque.estimatedSize();
    }

    public double getRestTime(){
        return getLineLength() / (rateLimiter.getRate() != 0 ? rateLimiter.getRate() : 1);
    }

    //只允许以warm up的形式处理
    public EnterGameRateLimiterController(WaitLinesConfig waitLinesConfig) {
        this.loginPermitsPerSeconds = waitLinesConfig.getLoginPermitsPerSeconds();
        this.warmUpPeriodSeconds = waitLinesConfig.getWarmUpPeriodSeconds();
        this.maxWaitingRequests = waitLinesConfig.getMaxWaitingRequests();
        this.rateLimiter = RateLimiter.create(loginPermitsPerSeconds);
        long fakeSeconds = waitLinesConfig.getFakeSeconds();
        waitLoginDeque = Caffeine.newBuilder().maximumSize(this.maxWaitingRequests).expireAfterAccess(fakeSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public Long load(Long key) throws Exception {
                        return System.currentTimeMillis();
                    }
                });
        this.tryAcquireWait = (long)(1 / loginPermitsPerSeconds * 1000);
    }

    @SneakyThrows
    public Double acquire(long playerId) {
        if(waitLoginDeque.estimatedSize() > 0){
            Long ifPresent = waitLoginDeque.getIfPresent(playerId);
            if(ifPresent == null){
                return 10d;
            }
        }
        boolean success = rateLimiter.tryAcquire(1, tryAcquireWait,TimeUnit.MILLISECONDS);
        //当有排队人数时
        if (success) {
            waitLoginDeque.invalidate(playerId);
        }else{
            waitLoginDeque.get(playerId);
        }
        return rateLimiter.acquire(1);//可能有出入
    }
}
