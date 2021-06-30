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

    //发布消息Kafka服务之中
    @SneakyThrows
    private void publishMessage(ChatMessage chatMessage) {
        //String json = JSON.toJSONString(chatMessage);
        String json = JacksonUtils.toJSONStringV2(chatMessage);
        byte[] message = json.getBytes(StandardCharsets.UTF_8);
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(IM_TOPIC, "IM", message);
        kafkaTemplate.send(record);
    }

    //这里需要注意的是groupId一定要不一样，因为kafka的机制是一个消息只能被同一个消费者组下的某个消费者消费一次。不同的服务实例的serverId不一样
    @SneakyThrows
    @KafkaListener(topics = {IM_TOPIC}, groupId = "IM-SERVER-" + "${game.server.config.server-id}")
    public void messageListener(ConsumerRecord<String, byte[]> record) {
        //监听聊天服务发布的信息，收到信息之后，将聊天信息转发到所有的客户端。
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
        //因为这里不再GatewayMessageContext参数，所以这里使用总的GameChannel管理类，将消息广播出去
        gatewayMessageConsumerService.getGameMessageEventDispatchService().broadcastMessage(response);
    }

    @GameMessageMapping(IMSendIMMsgRequest.class)//在这里接收客户端发送的聊天消息
    public void chatMsg(IMSendIMMsgRequest request, GatewayMessageContext<IMManager> ctx) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatMessage(request.getBodyObj().getChat());
        //chatMessage.setNickName(ctx.getPlayer().getNickName());
        long playerId = ctx.getPlayerId();
        String key = EnumRedisKey.PLAYERID_TO_NICKNAME.getKey(String.valueOf(playerId));
        String nickname = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(nickname)) {
            logger.warn("来源不明的消息{}", chatMessage);
            return;
        }
        String globalKey = EnumRedisKey.IM_ID_INCR.getKey("GLOBAL");
        Long id = redisTemplate.opsForValue().increment(globalKey);
        chatMessage.setSeqId(id);
        chatMessage.setNickName(nickname);
        chatMessage.setPlayerId(playerId);
        chatMessage.setClientSeqId(request.getHeader().getClientSeqId());
        chatMessage.setClientSendTime(request.getHeader().getClientSendTime());
        logger.info("IM服务器收到消息{}", chatMessage);
        this.publishMessage(chatMessage);//收到客户端的聊天消息之后，把消息封装，发布到Kafka之中。
    }

    @GameMessageMapping(PassConnectionStatusMsgRequest.class)
    public void clientConnectionStatus(PassConnectionStatusMsgRequest request, GatewayMessageContext<IMManager> ctx){
        long playerId = request.getHeader().getPlayerId();
        boolean connected = request.getBodyObj().isConnect();
        if(connected){
            //登录
            //1 - 广播某某登录了
            //2 - 取得历史记录

        }else{
            //掉线
        }
    }

    @GameMessageMapping(TriggerConnectionInactive.class)
    public void connectionInactive(TriggerConnectionInactive request, GatewayMessageContext<IMManager> ctx){
        long playerId = request.getHeader().getPlayerId();
        logger.info("玩家PlayerId:{}触发了TriggerConnectionInactive",playerId);
    }

}
