package com.nekonade.neko.service;


import com.nekonade.common.dto.ItemDTO;
import com.nekonade.common.dto.MailVo;
import com.nekonade.common.model.PageResult;
import com.nekonade.common.utils.FunctionMapper;
import com.nekonade.dao.db.entity.MailBox;
import com.nekonade.dao.db.repository.MailBoxRepository;
import com.nekonade.dao.helper.MongoPageHelper;
import com.nekonade.dao.helper.SortParam;
import com.nekonade.network.message.event.function.TriggerSystemSendMailEvent;
import com.nekonade.network.message.manager.PlayerManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
public class MailBoxService {

    private final static Logger logger = LoggerFactory.getLogger(MailBoxService.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoPageHelper mongoPageHelper;

    @Autowired
    private MailBoxRepository mailBoxRepository;
    
    @EventListener
    public void systemSendMailEvent(TriggerSystemSendMailEvent event){
        List<ItemDTO> gifts = event.getGifts();
        long playerId = event.getPlayerManager().getPlayer().getPlayerId();
        String systemName = "[System GM]";
        String senderName = event.getSenderName();
        if(StringUtils.isNotEmpty(senderName)){
            systemName = "["+senderName+"]";
        }
        MailBox mailBox = new MailBox();
        mailBox.setReceived(0);
        mailBox.setReceiverId(playerId);
        mailBox.setSenderId(1L);
        mailBox.setSenderName(systemName);
        mailBox.setExpired(System.currentTimeMillis() + Duration.ofDays(30).toMillis());
        mailBox.setTitle("Item OverFlow");
        mailBox.setContent("Please receive!");
        mailBox.setGifts(gifts);
        mailBoxRepository.save(mailBox);
    }

    public PageResult<MailVo> findByPage(long playerId, int type, Integer page, Integer limit, SortParam sortParam) {
        final Query query = new Query(Criteria.where("receiverId").is(playerId).and("received").is(0));
        Function<MailBox, MailVo> mapper = FunctionMapper.Mapper(MailBox.class, MailVo.class);
        return mongoPageHelper.pageQuery(query, MailBox.class, limit, page, sortParam, mapper);
    }

    public List<MailBox> findMailById(PlayerManager dataManager, String mailId){
        long playerId = dataManager.getPlayer().getPlayerId();
        Criteria criteria = Criteria.where("receiverId").is(playerId).and("id").is(mailId).and("received").is(0);
        final Query query = new Query(criteria);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(false);
        options.returnNew(true);
        Update update = new Update();
        update.set("received",1);
        MailBox mailBox = mongoTemplate.findOne(query, MailBox.class);
        List<MailBox> mailBoxes = new ArrayList<>();
        if(mailBox != null){
            mailBoxes.add(mailBox);
        }
        return mailBoxes;
        /*MailBox returnMail = mongoTemplate.findAndModify(query, update, options, MailBox.class);
        List<ItemDTO> failedList = new ArrayList<>();
        if(returnMail != null){
            List<ItemDTO> receivedList = returnMail.getGifts().stream().filter(gift -> {
                boolean isSuccess = dataManager.getInventoryManager().produceItem(gift.getItemId(), gift.getAmount());
                if (!isSuccess) {
                    failedList.add(gift);
                    logger.info("PlayerId:{} 溢出了道具{} 数量:{}",playerId,gift.getItemId(),gift.getAmount());
                    return false;
                }
                logger.debug("PlayerId:{} 领取道具{} 数量:{}",playerId,gift.getItemId(),gift.getAmount());
                return true;
            }).collect(Collectors.toList());
            if(receivedList.size() > 0){
                MailDTO mailDTO = new MailDTO();
                BeanUtils.copyProperties(returnMail,mailDTO);
                mailDTO.setGifts(receivedList);
                list.add(mailDTO);
            }
        }
        if(failedList.size() > 0){
            TriggerSystemSendMailEvent event = new TriggerSystemSendMailEvent(this, dataManager, failedList);
            context.publishEvent(event);
        }*/
    }

    public List<MailBox> findMailAllPages(PlayerManager dataManager){
        long playerId = dataManager.getPlayer().getPlayerId();
        Criteria criteria = Criteria.where("receiverId").is(playerId).and("received").is(0);
        final Query query = new Query(criteria);
        List<MailBox> mailBoxes = mongoTemplate.find(query, MailBox.class);
        return mailBoxes;

        /*
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(false);
        options.returnNew(true);
        Update update = new Update();
        update.set("received",1);
        mailBoxes.forEach(mailBox -> {
            List<ItemDTO> failedList = new ArrayList<>();
            List<ItemDTO> receivedList = mailBox.getGifts().stream().filter(gift -> {
                boolean isSuccess = dataManager.getInventoryManager().produceItem(gift.getItemId(), gift.getAmount());
                if (!isSuccess) {
                    failedList.add(gift);
                    logger.info("PlayerId:{} 溢出了道具{} 数量:{}",playerId,gift.getItemId(),gift.getAmount());
                    return false;
                }
                logger.debug("PlayerId:{} 领取道具{} 数量:{}",playerId,gift.getItemId(),gift.getAmount());
                return true;
            }).collect(Collectors.toList());
            if(receivedList.size() > 0){
                final Query updateQuery = new Query(Criteria.where("id").is(mailBox.getId()));
                MailBox returnMail = mongoTemplate.findAndModify(updateQuery, update, options, MailBox.class);
                if(returnMail != null && returnMail.getReceived() == 1){
                    MailDTO mailDTO = new MailDTO();
                    BeanUtils.copyProperties(returnMail,mailDTO);
                    mailDTO.setGifts(receivedList);
                    list.add(mailDTO);
                }
                if(failedList.size() > 0){
                    TriggerSystemSendMailEvent event = new TriggerSystemSendMailEvent(this, dataManager, failedList);
                    context.publishEvent(event);
                }
            }

        });
        return list;*/
    }

    public List<MailBox> findMailByPage(PlayerManager dataManager, int page, int type, String lastId){
        long playerId = dataManager.getPlayer().getPlayerId();
        SortParam sortParam = new SortParam();
        sortParam.setSortDirection(Sort.Direction.DESC);
        Criteria criteria = Criteria.where("receiverId").is(playerId).and("received").is(0);
        final Query query = new Query(criteria);
        if(type != -1){
            criteria.and("type").is(type);
        }
        PageResult<MailBox> mailBoxPageResult = mongoPageHelper.pageQuery(query, MailBox.class, 10, page, sortParam, lastId, MailBox.class);
        //List<MailBox> mailBoxes = mongoTemplate.find(query, MailBox.class);
        List<MailBox> mailBoxes = mailBoxPageResult.getList();
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(false);
        options.returnNew(true);
        Update update = new Update();
        update.set("received",1);
        return mailBoxes;
    }

    public MailBox receiveMailBox(MailBox mailBox){
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(false);
        options.returnNew(true);
        Update update = new Update();
        update.set("received",1);
        final Query updateQuery = new Query(Criteria.where("id").is(mailBox.getId()));
        MailBox returnMail = mongoTemplate.findAndModify(updateQuery, update, options, MailBox.class);
        return returnMail;
    }
}
