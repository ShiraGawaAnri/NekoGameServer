package com.nekonade.web.gateway.filter;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.exceptions.TokenException;
import com.nekonade.common.utils.CommonField;
import com.nekonade.common.utils.JWTUtil;
import com.nekonade.web.gateway.config.FilterConfig;
import com.nekonade.web.gateway.exception.WebGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @ClassName: TokenVerifyFilter
 * @Author: Lily
 * @Description: token验证的filter，用户登陆成功之后，以后再访问服务需要对token进行验证
 * @Date: 2021/6/28
 * @Version: 1.0
 */
@Service
public class TokenVerifyFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(TokenVerifyFilter.class);
    @Autowired
    private FilterConfig filterConfig;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestUri = exchange.getRequest().getURI().getPath();
        List<String> whiteRequestUris = filterConfig.getWhiteRequestUri();
        if (whiteRequestUris.contains(requestUri)) {
            return chain.filter(exchange);// 如果请求的uri在白名单中，则跳过验证。
        }

        String token = exchange.getRequest().getHeaders().getFirst(CommonField.TOKEN);
        if (StringUtils.isEmpty(token)) {
            logger.debug("{} 请求验证失败,token为空", requestUri);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            //需要构造全局Exception捕获
            throw new WebGatewayException.Builder(EnumCollections.CodeMapper.WebGatewayError.TOKEN_EMPTY).build();
            //return exchange.getResponse().setComplete();
        }

        try {
            JWTUtil.TokenBody tokenBody = JWTUtil.getTokenBodyV2(token);
            // 把token中的openId和userId添加到Header中，转发到后面的服务。
            ServerHttpRequest request = exchange.getRequest().mutate().header(CommonField.OPEN_ID, tokenBody.getOpenId()).header(CommonField.USER_ID, String.valueOf(tokenBody.getUserId())).header(CommonField.USERNAME, tokenBody.getUsername()).build();
            ServerWebExchange newExchange = exchange.mutate().request(request).build();
            return chain.filter(newExchange);
        } catch (TokenException e) {
            logger.debug("{} 请求验证失败,token非法",token,e);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

    }

}
