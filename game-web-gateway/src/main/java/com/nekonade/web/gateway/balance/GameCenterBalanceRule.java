package com.nekonade.web.gateway.balance;


import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.Server;
import org.apache.commons.lang.math.RandomUtils;

import java.util.List;

/**
 * @ClassName: GameCenterBalanceRule
 * @Description: 根据某个key的hashCode对服务进行负载均衡。同一个用户id的请求，都转发到同一个服务实例上面。
 */
public class GameCenterBalanceRule extends AbstractLoadBalancerRule {

    @Override
    public Server choose(Object key) {

        List<Server> servers = this.getLoadBalancer().getReachableServers();
        if (servers.isEmpty()) {
            return null;
        }
        if (servers.size() == 1) {
            return servers.get(0);
        }
        if (key == null) {
            return randomChoose(servers);
        }
        return hashKeyChoose(servers, key);
    }

    /**
     * 随机返回一个服务实例
     * @param servers 服务器实例列表
     * @return 随机返回的结果
     */
    private Server randomChoose(List<Server> servers) {
        int randomIndex = RandomUtils.nextInt(servers.size());
        return servers.get(randomIndex);
    }

    /**
     * 使用key的hash值，和服务实例数量求余，选择一个服务实例
     * @param servers 服务器实例列表
     * @param key 指定的key
     * @return 服务器实例
     */
    private Server hashKeyChoose(List<Server> servers, Object key) {
        int hashCode = Math.abs(key.hashCode());
        int index = hashCode % servers.size();
        return servers.get(index);

    }

    @Override
    public void initWithNiwsConfig(IClientConfig config) {

    }

}
