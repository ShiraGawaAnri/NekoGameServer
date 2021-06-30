package com.nekonade.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching //开启缓存
public class CaffeineConfig {

    public static final int DEFAULT_MAXSIZE = 10000;

    public static final int DEFAULT_TTL = -1;

    public enum Caches {
        CARD_DB(),
        ITEM_DB(),
        CHARACTER_DB(),
        ENEMY_DB(),
        CONFIG_GLOBAL(),
        TASK_DB(),
        findTaskIdsByType(),
        findTaskIdsByStageId(),
        ;

        Caches() {
        }

        Caches(int ttl) {
            this.ttl = ttl;
        }

        Caches(int ttl, int maxSize) {
            this.ttl = ttl;
            this.maxSize = maxSize;
        }

        private int maxSize = DEFAULT_MAXSIZE;    //最大數量
        private int ttl = DEFAULT_TTL;        //过期时间（秒）

        public int getMaxSize() {
            return maxSize;
        }

        public int getTtl() {
            return ttl;
        }
    }

    @Bean
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<CaffeineCache> caches = new ArrayList<>();
        for (Caches c : Caches.values()) {
            Caffeine<Object, Object> builder = Caffeine.newBuilder();
            builder.recordStats();
            if (c.getTtl() > -1){
                builder.expireAfterAccess(c.getTtl(), TimeUnit.SECONDS);
            }
            caches.add(new CaffeineCache(c.name(), builder.build()));
        }
        cacheManager.setCaches(caches);
        return cacheManager;
    }

}
