package com.nekonade.web.gateway.filter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.IServerError;
import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.common.utils.CommonField;
import com.nekonade.web.gateway.config.FilterConfig;
import com.nekonade.web.gateway.exception.WebGatewayException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: RequestRateLimiterFilter
 * @Author: Lily
 * @Description: 请求限流器
 * @Date: 2021/6/28
 * @Version: 1.0
 */
@Service
public class RequestRateLimiterFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestRateLimiterFilter.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private FilterConfig filterConfig;
    private RateLimiter globalRateLimiter;
    private LoadingCache<String, RateLimiter> userRateLimiterCache;


    @PostConstruct
    public void init() {// 初始化
        double permitsPerSecond = filterConfig.getGlobalRequestRateCount();
        globalRateLimiter = RateLimiter.create(permitsPerSecond);
        // 创建用户cache
        long maximumSize = filterConfig.getCacheUserMaxCount();
        long duration = filterConfig.getCacheUserTimeout();
        userRateLimiterCache = CacheBuilder.newBuilder().maximumSize(maximumSize).expireAfterAccess(duration, TimeUnit.MILLISECONDS).build(new CacheLoader<>() {
            @Override
            public RateLimiter load(String key) throws Exception {
                // 不存在限流器就创建一个。
                double permitsPerSecond = filterConfig.getUserRequestRateCount();
                RateLimiter newRateLimiter = RateLimiter.create(permitsPerSecond,1,TimeUnit.SECONDS);
                return newRateLimiter;
            }
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 2;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(CommonField.TOKEN);
        String sessionId = exchange.getRequest().getHeaders().getFirst(CommonField.SESSION_ID);
        String symbol = null;
        boolean sessionCheck = false;
        String ipAddress = getIpAddress(exchange.getRequest());
        if (!StringUtils.isEmpty(token)) {
            symbol = token;
        } else if (!StringUtils.isEmpty(sessionId)) {
            symbol = sessionId;
        }
        try {
            if (StringUtils.isEmpty(symbol)) {
                Long id = redisTemplate.opsForValue().increment(EnumRedisKey.SESSION_ID_INCR.getKey());
                symbol = DigestUtils.md5Hex(UUID.randomUUID().toString() + id.toString());
                ResponseCookie cookie = ResponseCookie.from(CommonField.SESSION_ID, symbol).path("/").maxAge(Duration.ofDays(7)).build();
                exchange.getResponse().addCookie(cookie);
                sessionCheck = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("sessionId 操作失败 - {}",symbol, e);
            symbol = ipAddress;
        }
        if (!StringUtils.isEmpty(symbol)) {
            try {
                RateLimiter userRateLimiter = userRateLimiterCache.get(symbol);
                if (!userRateLimiter.tryAcquire(1,1,TimeUnit.SECONDS)) {// 获取令牌失败，触发限流
                    logger.warn("限流器触发 — Symbol:{},Uri:{}", symbol, exchange.getRequest().getURI());
                    this.tooManyRequest(exchange, chain, EnumCollections.CodeMapper.WebGatewayError.TOO_MANY_USER_REQUEST);
                }
                //应对Cookie无效或不传sessionId时,以ip方式强制处理
                userRateLimiter.acquire();
                if (sessionCheck) {
                    RateLimiter ipRateLimiter = userRateLimiterCache.get(ipAddress);
                    if (!(ipRateLimiter.tryAcquire(1,1,TimeUnit.SECONDS))) {
                        logger.warn("限流器触发 — Symbol:{},Uri:{}", symbol, exchange.getRequest().getURI());
                        return this.tooManyRequest(exchange, chain, EnumCollections.CodeMapper.WebGatewayError.TOO_MANY_USER_REQUEST);
                    }
                    //以新IP访问时添加一次全局
                    if (!globalRateLimiter.tryAcquire(1,1,TimeUnit.SECONDS)) {
                        return this.tooManyRequest(exchange, chain, EnumCollections.CodeMapper.WebGatewayError.TOO_MANY_GLOBAL_REQUEST);
                    }
                    globalRateLimiter.acquire();
                }
            } catch (ExecutionException e) {
                logger.error("限流器异常 — Symbol:{},Uri:{}", symbol, exchange.getRequest().getURI(), e);
                return this.tooManyRequest(exchange, chain);
            }
        }
//        if (!globalRateLimiter.tryAcquire()) {
//            return this.tooManyRequest(exchange, chain);
//        }
        return chain.filter(exchange);// 成功获取令牌，放行
    }

    private Mono<Void> tooManyRequest(ServerWebExchange exchange, GatewayFilterChain chain) {
        return tooManyRequest(exchange, chain, EnumCollections.CodeMapper.WebGatewayError.UNKNOWN);
    }

    private Mono<Void> tooManyRequest(ServerWebExchange exchange, GatewayFilterChain chain, IServerError serverError) {
        logger.debug("请求太多，触发限流");
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);// 请求失败，返回请求太多
        throw new WebGatewayException.Builder(serverError).build();
    }


    private String getIpAddress(ServerHttpRequest request) {
        String ipAddress = null;
        HttpHeaders headers = request.getHeaders();
        try {
            ipAddress = headers.getFirst("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = headers.getFirst("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = headers.getFirst("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddress().getAddress().toString();
                if (ipAddress != null) {
                    ipAddress = ipAddress.replaceAll("/", "");
                }
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                        ipAddress = inet.getHostAddress();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        ipAddress = request.getRemoteAddress().toString();
                    }
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        // ipAddress = this.getRequest().getRemoteAddr();

        return ipAddress;
    }
}
