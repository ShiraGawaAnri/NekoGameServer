package com.nekonade.log.GameLog.service;

import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.dao.daos.AsyncLogDao;
import com.nekonade.dao.db.entity.LogGameLogicRequest;
import com.nekonade.dao.db.entity.LogGameRaidBattleRequest;
import com.nekonade.log.GameLog.config.ServerConfig;
import com.nekonade.network.param.game.GameMessageService;
import com.nekonade.network.param.game.bus.GameMessageInnerDecoder;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.network.param.log.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ReceiveGameLogicLog {

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private GameMessageService gameMessageService;

    @Autowired
    private AsyncLogDao asyncLogDao;

    private static final Logger logger = LoggerFactory.getLogger(ReceiveGameLogicLog.class);

    @PostConstruct
    public void init() {
        logger.info("监听消息接收业务消息topic:{}", serverConfig.toString());
    }

    @KafkaListener(topics = {"${log.server.config.game-logic}"}, groupId = "${log.server.config.topic-group-id}",containerFactory = "batchContainerFactory",concurrency = "4")
    public void GameLogicRequestLogReceiver(List<ConsumerRecord<String, byte[]>> records, Acknowledgment ack) {
        logger.info("Server Log Records:{}",records.size());
        ack.acknowledge();
        records.forEach(record->{
            GameMessagePackage gameMessagePackage = GameMessageInnerDecoder.readGameMessagePackageV2(record.value());
            GameMessageHeader header = gameMessagePackage.getHeader();
            IGameMessage targetClass = gameMessageService.getRequestInstanceByMessageId(header.getMessageId());
            String key = record.key();
            logger.info("Record Key {}",key);
            LogTable logTable = LogTable.readBody(gameMessagePackage.getBody());
            LogGameLogicRequest logGameLogicRequest = new LogGameLogicRequest();
            BeanUtils.copyProperties(logTable, logGameLogicRequest);

            byte[] gameMessage = logTable.getGameMessage();

            BeanUtils.copyProperties(header,targetClass.getHeader());
            targetClass.read(gameMessage);
            logGameLogicRequest.setGameMessage(targetClass);

            asyncLogDao.saveGameRequestLog(logGameLogicRequest);
        });


    }

    @KafkaListener(topics = {"${log.server.config.game-raid-battle}"}, groupId = "${log.server.config.topic-group-id}",containerFactory = "batchContainerFactory",concurrency = "4")
    public void GameRaidBattleRequestLogReceiver(List<ConsumerRecord<String, byte[]>> records, Acknowledgment ack) {
        logger.info("Rb Log Records:{}",records.size());
        ack.acknowledge();
        records.forEach(record->{
            GameMessagePackage gameMessagePackage = GameMessageInnerDecoder.readGameMessagePackageV2(record.value());
            GameMessageHeader header = gameMessagePackage.getHeader();
            IGameMessage targetClass = gameMessageService.getRequestInstanceByMessageId(header.getMessageId());
            String key = record.key();
            logger.info("Record Key {}",key);
            LogTable logTable = LogTable.readBody(gameMessagePackage.getBody());
            LogGameRaidBattleRequest logGameLogicRequest = new LogGameRaidBattleRequest();
            BeanUtils.copyProperties(logTable, logGameLogicRequest);

            byte[] gameMessage = logTable.getGameMessage();

            BeanUtils.copyProperties(header,targetClass.getHeader());
            targetClass.read(gameMessage);
            logGameLogicRequest.setGameMessage(targetClass);

            asyncLogDao.saveGameRequestLog(logGameLogicRequest);
        });

    }
}
