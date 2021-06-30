package com.nekonade.raidbattle.service;

import com.nekonade.common.utils.MessageUtils;
import com.nekonade.network.param.game.bus.GameMessageInnerDecoder;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.network.param.game.message.battle.RaidBattleBoardCastMsgResponse;
import com.nekonade.raidbattle.message.channel.RaidBattleChannelConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class BroadCastMessageService {

    @Autowired
    private RaidBattleChannelConfig serverConfig;

    @Resource(name = "CustomKafkaTemplate")
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    private void broadcast(RaidBattleBoardCastMsgResponse gameMessage, String topic, List<Long> broadIds) {
        GameMessageHeader header = gameMessage.getHeader();
        String raidId = gameMessage.getBodyObj().getRaidId();
        if(broadIds.size() <= 30){
            header.getAttribute().setBroadIds(broadIds);
        }
        header.getAttribute().setRaidId(raidId);
        GameMessagePackage gameMessagePackage = new GameMessagePackage();
        gameMessagePackage.setHeader(header);
        gameMessagePackage.setBody(gameMessage.body());
        byte[] value = GameMessageInnerDecoder.sendMessageV2(gameMessagePackage);
        String keyId = MessageUtils.kafkaKeyCreate(header);
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, keyId, value);
        kafkaTemplate.send(record);
    }


    public void broadCastRaidBattleStatus(RaidBattleBoardCastMsgResponse gameMessage, Long[] playerIds) {
        String topic = "RaidBattle-Status";
        List<Long> broadIds = Arrays.asList(playerIds);
        broadcast(gameMessage, topic, broadIds);
    }

    public void broadCastRaidBattleStatus(RaidBattleBoardCastMsgResponse gameMessage, long playerId) {
        String topic = "RaidBattle-Status";
        List<Long> broadIds = Collections.singletonList(playerId);
        broadcast(gameMessage, topic, broadIds);
    }

    public void broadCastRaidBattleStatus(RaidBattleBoardCastMsgResponse gameMessage, List<Long> playerIds) {
        String topic = "RaidBattle-Status";
        broadcast(gameMessage, topic, playerIds);
    }


}
