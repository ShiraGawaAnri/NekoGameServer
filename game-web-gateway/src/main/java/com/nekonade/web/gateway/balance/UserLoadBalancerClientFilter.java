package com.nekonade.web.gateway.balance;


import com.nekonade.common.utils.CommonField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.LoadBalancerClientFilter;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;


public class UserLoadBalancerClientFilter extends LoadBalancerClientFilter {

    public UserLoadBalancerClientFilter(LoadBalancerClient loadBalancer, LoadBalancerProperties properties) {
        super(loadBalancer, properties);
    }

    /**
     * TODO:当routekey为空时,基本上会全由同一个服务器来处理，这会导致根本没用上均衡负载。举个例子,由于/request/10001的注册请求中使用了synchronized(userName)来约束用户名唯一,即必须落在同一个服务器上才有效，此时需要改造synchronized或改造同一参数和请求时必须落在同一服务器上。如果需要根据传入参数作为routeKey，POST方法下需要参考ModifyRequestBodyGatewayFilterFactory或ReadBodyPredicateFactory等源码来截获RequestBody(因为RequestBody只能订阅一次)
     *
     * @param exchange
     * @return
     */
    @Override
    protected ServiceInstance choose(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String token = request.getHeaders().getFirst(CommonField.TOKEN);
        String openId = exchange.getRequest().getHeaders().getFirst(CommonField.OPEN_ID);
        String sessionId = request.getHeaders().getFirst(CommonField.SESSION_ID);
        String routekey = null;
        if (!StringUtils.isEmpty(token)) {
            routekey = token;
        }
        if (!StringUtils.isEmpty(openId)) {
            routekey = openId;
        } else if (!StringUtils.isEmpty(sessionId)) {
            routekey = sessionId;
        }
        if (routekey == null) {
            return super.choose(exchange);
        }
        if (this.loadBalancer instanceof RibbonLoadBalancerClient) {
            RibbonLoadBalancerClient client = (RibbonLoadBalancerClient) this.loadBalancer;
            String serviceId = ((URI) exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR)).getHost();
            return client.choose(serviceId, routekey);
        }
        return super.choose(exchange);
    }

}
