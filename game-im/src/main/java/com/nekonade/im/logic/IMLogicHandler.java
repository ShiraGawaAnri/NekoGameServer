package com.nekonade.im.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.common.utils.JacksonUtils;
import com.nekonade.dao.daos.PlayerDao;
import com.nekonade.network.message.context.GatewayMessageConsumerService;
import com.nekonade.network.message.context.GatewayMessageContext;
import com.nekonade.network.message.manager.IMManager;
import com.nekonade.network.param.game.message.im.IMSendIMMsgRequest;
import com.nekonade.network.param.game.message.im.IMSendIMMsgeResponse;
import com.nekonade.network.param.game.message.neko.PassConnectionStatusMsgRequest;
import com.nekonade.network.param.game.message.neko.TriggerConnectionInactive;
import com.nekonade.network.param.game.messagedispatcher.GameMessageHandler;
import com.nekonade.network.param.game.messagedispatcher.GameMessageMapping;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@GameMessageHandler
public class IMLogicHandler {

    private static final Logger logger = LoggerFactory.getLogger(IMLogicHandler.class);
    private final static String IM_TOPIC = "game-im-topic";
    @Resource
    private KafkaTemplate<String, byte[]> kafkaTemplate;
    @Autowired
    private GatewayMessageConsumerService gatewayMessageConsumerService;
    @Autowired
    private PlayerDao playerDao;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private final CopyOnWriteArrayList<ChatMessage> history = new CopyOnWriteArrayList<>();

    //????????????Kafka????????????
    @SneakyThrows
    private void publishMessage(ChatMessage chatMessage) {
        //String json = JSON.toJSONString(chatMessage);
        String json = JacksonUtils.toJSONStringV2(chatMessage);
        byte[] message = json.getBytes(StandardCharsets.UTF_8);
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(IM_TOPIC, "IM", message);
        kafkaTemplate.send(record);
    }

    //????????????????????????groupId???????????????????????????kafka??????????????????????????????????????????????????????????????????????????????????????????????????????????????????serverId?????????
    @SneakyThrows
    @KafkaListener(topics = {IM_TOPIC}, groupId = "IM-SERVER-" + "${game.server.config.server-id}")
    public void messageListener(ConsumerRecord<String, byte[]> record) {
        //??????????????????????????????????????????????????????????????????????????????????????????????????????
        byte[] value = record.value();
        String json = new String(value, StandardCharsets.UTF_8);
        //ChatMessage chatMessage = JSON.parseObject(json, ChatMessage.class);
        ChatMessage chatMessage = JacksonUtils.parseObjectV2(json, ChatMessage.class);
        history.addIfAbsent(chatMessage);
        if(history.size() > 100){
            List<ChatMessage> chatMessages = history.subList(history.size() - 10, history.size());
            history.clear();
            history.addAll(chatMessages);
            redisTemplate.expire(EnumRedisKey.IM_ID_INCR.getKey("GLOBAL"),EnumRedisKey.IM_ID_INCR.getTimeout());
        }
        IMSendIMMsgeResponse response = new IMSendIMMsgeResponse();
        response.getBodyObj().setChat(chatMessage.getChatMessage());
        response.getBodyObj().setSender(chatMessage.getNickName());
        response.getHeader().setClientSeqId(chatMessage.getClientSeqId());
        response.getHeader().setClientSendTime(chatMessage.getClientSendTime());
        //??????????????????GatewayMessageContext?????????????????????????????????GameChannel?????????????????????????????????
        gatewayMessageConsumerService.getGameMessageEventDispatchService().broadcastMessage(response);
    }

    @GameMessageMapping(IMSendIMMsgRequest.class)//?????????????????????????????????????????????
    public void chatMsg(IMSendIMMsgRequest request, GatewayMessageContext<IMManager> ctx) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatMessage(request.getBodyObj().getChat());
        //chatMessage.setNickName(ctx.getPlayer().getNickName());
        long playerId = ctx.getPlayerId();
        String key = EnumRedisKey.PLAYERID_TO_NICKNAME.getKey(String.valueOf(playerId));
        String nickname = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(nickname)) {
            logger.warn("?????????????????????{}", chatMessage);
            return;
        }
        String globalKey = EnumRedisKey.IM_ID_INCR.getKey("GLOBAL");
        Long id = redisTemplate.opsForValue().increment(globalKey);
        chatMessage.setSeqId(id);
        chatMessage.setNickName(nickname);
        chatMessage.setPlayerId(playerId);
        chatMessage.setClientSeqId(request.getHeader().getClientSeqId());
        chatMessage.setClientSendTime(request.getHeader().getClientSendTime());
        logger.info("IM?????????????????????{}", chatMessage);
        this.publishMessage(chatMessage);//??????????????????????????????????????????????????????????????????Kafka?????????
    }

    @GameMessageMapping(PassConnectionStatusMsgRequest.class)
    public void clientConnectionStatus(PassConnectionStatusMsgRequest request, GatewayMessageContext<IMManager> ctx){
        long playerId = request.getHeader().getPlayerId();
        boolean connected = request.getBodyObj().isConnect();
        if(connected){
            //??????
            //1 - ?????????????????????
            //2 - ??????????????????

        }else{
            //??????
        }
    }

    @GameMessageMapping(TriggerConnectionInactive.class)
    public void connectionInactive(TriggerConnectionInactive request, GatewayMessageContext<IMManager> ctx){
        long playerId = request.getHeader().getPlayerId();
        logger.info("??????PlayerId:{}?????????TriggerConnectionInactive",playerId);
    }

}
