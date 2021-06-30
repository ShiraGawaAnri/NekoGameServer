package com.nekonade.game.client.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nekonade.common.utils.CommonField;
import com.nekonade.common.utils.GameHttpClient;
import com.nekonade.network.param.game.messagedispatcher.DispatchGameMessageService;
import com.nekonade.network.param.http.MessageCode;
import com.nekonade.network.param.http.request.SelectGameGatewayParam;
import com.nekonade.network.param.http.response.GameGatewayInfoMsg;
import com.nekonade.network.param.http.response.ResponseEntity;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GameClientInitService {
    private static final Logger logger = LoggerFactory.getLogger(GameClientInitService.class);
    @Autowired
    private GameClientConfig gameClientConfig;
    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        DispatchGameMessageService.scanGameMessages(applicationContext, 0, "com.nekonade");// 扫描加载要处理的消息类型
        this.selectGateway();
    }

    public void testInit(String packagePath){

        DispatchGameMessageService.scanGameMessages(applicationContext, 0,packagePath);
    }


    /**
     * isUseGameCenter为true时 使用测试账号连接游戏服务中心，从而获取游戏网关的地址
     * isUseGameCenter为false时，使用配置文件中的游戏网关地址连接
     */
    private void selectGateway() {
        if (gameClientConfig.isUseGameCenter()) {
            // 因为是测试环境，这里使用一些默认参数
            SelectGameGatewayParam param = new SelectGameGatewayParam();
            param.setOpenId("test_openId");
            param.setZoneId("1");
            GameGatewayInfoMsg gateGatewayMsg = this.selectGatewayInfoFromGameCenter(param);
            // 替换默认的游戏网关信息
            if (gateGatewayMsg != null) {
                gameClientConfig.setDefaultGameGatewayHost(gateGatewayMsg.getIp());
                gameClientConfig.setDefaultGameGatewayPort(gateGatewayMsg.getPort());
                gameClientConfig.setGatewayToken(gateGatewayMsg.getToken());
                gameClientConfig.setRsaPrivateKey(gateGatewayMsg.getRsaPrivateKey());
            } else {
                throw new IllegalArgumentException("从服务中心获取游戏网关信息失败，没有可使用的游戏网关信息");
            }
        }
    }


    /**
     * 单机测试用:默认从游戏服务中心获取游戏网关
     * 初次启动时会通过 login username password的方式请求登陆注册
     * 随后需要 手动 登陆对应账号，建立一次测试角色
     * @param selectGameGatewayParam
     * @return
     */
    public GameGatewayInfoMsg selectGatewayInfoFromGameCenter(SelectGameGatewayParam selectGameGatewayParam) {
        String username = "test_Gateway";
        String password = "test_Gateway";
        String webGatewayUrl = gameClientConfig.getGameCenterUrl() + CommonField.GAME_CENTER_PATH + MessageCode.USER_LOGIN;
        JSONObject params = new JSONObject();
        params.put("zoneId", "test_openId");
        params.put("openId", "test_openId");
        params.put("loginType", 1);
        params.put("username", username);
        params.put("password", password);
        String result = GameHttpClient.post(webGatewayUrl, params);
        JSONObject responseJson = JSONObject.parseObject(result);
        if (!responseJson.get("code").equals(0)) {
            logger.warn("获取网关时出错:{}", responseJson.getString("errorMsg"));
            return null;
        }
        String token = responseJson.getJSONObject("data").getString("token");
        //将token验证放在Http的Header里面，以后的命令地请求Http的时候，需要携带，做权限验证
        String uri = gameClientConfig.getGameCenterUrl() + CommonField.GAME_CENTER_PATH + MessageCode.SELECT_GAME_GATEWAY;
        Header head = new BasicHeader("user-token", token);
        selectGameGatewayParam.setToken(token);
        //Header head = new BasicHeader("user-token", "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxNjAzNzY5NzUwODMyIiwiaWF0IjoxNjAzNzY5NzUwLCJzdWIiOiJ7XCJwYXJhbVwiOltdLFwicGxheWVySWRcIjowLFwic2VydmVySWRcIjpcIi0xXCIsXCJ1c2VySWRcIjoxLFwidXNlcm5hbWVcIjpcInl1a2ljdXRlXCJ9IiwiZXhwIjoxNjA0Mzc0NTUwfQ.wEEbYpaBP0Bv6A9sG88MOtIU4Uv3EYGeVKM6zwWgE5s");
        String response = GameHttpClient.post(uri, selectGameGatewayParam, head);
        if (response == null) {
            logger.warn("从游戏服务中心[{}]获取游戏网关信息失败", uri);
            return null;
        }
        JSONObject responseJson2 = JSONObject.parseObject(response);
        if (!responseJson2.get("code").equals(0)) {
            logger.warn("获取网关时出错:{}", responseJson2.getString("errorMsg"));
            return null;
        }
        ResponseEntity<GameGatewayInfoMsg> responseEntity = ResponseEntity.parseObject(response, GameGatewayInfoMsg.class);
        GameGatewayInfoMsg gateGatewayMsg = responseEntity.getData();
        return gateGatewayMsg;
    }

}
