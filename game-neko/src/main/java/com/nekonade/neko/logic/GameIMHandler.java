package com.nekonade.neko.logic;


import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.network.message.config.ServerConfig;
import com.nekonade.network.message.context.GatewayMessageContext;
import com.nekonade.network.message.manager.PlayerManager;
import com.nekonade.network.param.game.message.im.SendIMMsgRequest;
import com.nekonade.network.param.game.message.im.SendIMMsgeResponse;
import com.nekonade.network.param.game.messagedispatcher.GameMessageHandler;
import com.nekonade.network.param.game.messagedispatcher.GameMessageMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@GameMessageHandler
public class GameIMHandler {

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final CopyOnWriteArrayList<SendIMMsgeResponse.IMMsgBody> history = new CopyOnWriteArrayList<>();

    @GameMessageMapping(SendIMMsgRequest.class)
    public void sendMsg(SendIMMsgRequest request, GatewayMessageContext<PlayerManager> ctx) {
        String chat = request.getBodyObj().getChat();
        String sender = ctx.getPlayerManager().getPlayer().getNickName();
        SendIMMsgeResponse response = new SendIMMsgeResponse();
        SendIMMsgeResponse.IMMsgBody bodyObj = response.getBodyObj();
        response.wrapResponse(request);
        String key = EnumRedisKey.IM_ID_INCR.getKey(String.valueOf(serverConfig.getServerId()));
        Long id = redisTemplate.opsForValue().increment(key);
        bodyObj.setSeqId(id);
        bodyObj.setText(chat);
        bodyObj.setSender(sender);
        history.addIfAbsent(bodyObj);
        if(history.size() > 100){
            List<SendIMMsgeResponse.IMMsgBody> chatMessages = history.subList(history.size() - 10, history.size());
            history.clear();
            history.addAll(chatMessages);
            redisTemplate.expire(key,EnumRedisKey.IM_ID_INCR.getTimeout());
        }
        ctx.broadcastMessage(response);
    }
}
