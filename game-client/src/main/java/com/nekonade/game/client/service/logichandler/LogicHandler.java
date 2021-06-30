package com.nekonade.game.client.service.logichandler;


import com.google.protobuf.InvalidProtocolBufferException;
import com.nekonade.common.dto.*;
import com.nekonade.common.dto.raidbattle.vo.RaidBattleDamageVo;
import com.nekonade.common.dto.raidbattle.vo.RaidBattleVo;
import com.nekonade.common.model.PageResult;
import com.nekonade.common.utils.GameBeanUtils;
import com.nekonade.game.client.common.PlayerInfo;
import com.nekonade.game.client.common.RaidBattleInfo;
import com.nekonade.game.client.service.handler.GameClientChannelContext;
import com.nekonade.network.param.game.message.battle.JoinRaidBattleMsgResponse;
import com.nekonade.network.param.game.message.battle.RaidBattleAttackMsgResponse;
import com.nekonade.network.param.game.message.battle.RaidBattleBoardCastMsgResponse;
import com.nekonade.network.param.game.message.neko.*;
import com.nekonade.network.param.game.messagedispatcher.GameMessageHandler;
import com.nekonade.network.param.game.messagedispatcher.GameMessageMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: LogicHandler
 * @Author: Lily
 * @Description: 主要处理从服务端收到的消息
 * @Date: 2021/6/29
 * @Version: 1.0
 */
@GameMessageHandler
public class LogicHandler {

    private static final Logger logger = LoggerFactory.getLogger(LogicHandler.class);

    @Autowired
    private PlayerInfo playerInfo;

    @Autowired
    private RaidBattleInfo raidBattleVo;

    @GameMessageMapping(GetPlayerByIdMsgResponse.class)
    public void getPlayerByIdResponse(GetPlayerByIdMsgResponse response, GameClientChannelContext ctx) {
        GetPlayerByIdMsgResponse.ResponseBody bodyObj = response.getBodyObj();
        logger.info("查询指定PlayerId信息{}", response.bodyToString());
    }

    @GameMessageMapping(GetPlayerSelfMsgResponse.class)
    public void getPlayerSelfMsgResponse(GetPlayerSelfMsgResponse response, GameClientChannelContext ctx) {
        GetPlayerSelfMsgResponse.ResponseBody bodyObj = response.getBodyObj();
        BeanUtils.copyProperties(bodyObj,playerInfo);
        logger.info("查询自身信息{}", response.bodyToString());
    }

    @GameMessageMapping(GetInventoryMsgResponse.class)
    public void getInventoryMsgResponse(GetInventoryMsgResponse response, GameClientChannelContext ctx) {
        logger.info("查询仓库{}", response.bodyToString());
    }

    @GameMessageMapping(GetStaminaMsgResponse.class)
    public void getStaminaMsgResponse(GetStaminaMsgResponse response, GameClientChannelContext ctx) {
        logger.info("查询疲劳{}", response.bodyToString());
    }

    @GameMessageMapping(GetArenaPlayerListMsgResponse.class)
    public void getArenaPlayerListMsgResponse(GetArenaPlayerListMsgResponse response, GameClientChannelContext ctx) {
        logger.info("查询战场玩家列表{}", response.bodyToString());
    }

    @GameMessageMapping(TriggerPlayerLevelUpMsgResponse.class)
    public void levelUpdateMsgResponse(TriggerPlayerLevelUpMsgResponse response, GameClientChannelContext ctx) {
        logger.info("玩家升级消息{}", response.bodyToString());
    }

    @GameMessageMapping(GetMailBoxMsgResponse.class)
    public void getMailBoxMsgResponse(GetMailBoxMsgResponse response, GameClientChannelContext ctx) {
        GetMailBoxMsgResponse.PageResult responseBodyObj = response.getBodyObj();
        PageResult<MailVo> mail = new PageResult<>();
        BeanUtils.copyProperties(responseBodyObj, mail);
        logger.info("玩家邮件信息{}", mail);
    }

    @GameMessageMapping(DoCreateBattleMsgResponse.class)
    public void createBattleMsgResponse(DoCreateBattleMsgResponse response, GameClientChannelContext ctx) {
        long endNano = System.nanoTime();
        DoCreateBattleMsgResponse.ResponseBody bodyObj = response.getBodyObj();
        BeanUtils.copyProperties(bodyObj, raidBattleVo);
        logger.info("战斗信息返回 \r\nRaidId {} \r\n{}", raidBattleVo.getRaidId(), response.bodyToString());
    }

    @GameMessageMapping(JoinRaidBattleMsgResponse.class)
    public void joinRaidBattleMsgResponse(JoinRaidBattleMsgResponse response,GameClientChannelContext ctx){
        RaidBattleVo raidBattleDTO = new RaidBattleVo();
        BeanUtils.copyProperties(response.getBodyObj(), raidBattleDTO);
        raidBattleVo.setRaidId(raidBattleDTO.getRaidId());
        logger.info("加入战斗结果返回 \r\nRaidId {} \r\n{}", raidBattleDTO.getRaidId(), response.bodyToString());
    }

    @GameMessageMapping(RaidBattleAttackMsgResponse.class)
    public void raidBattleAttackMsgResponse(RaidBattleAttackMsgResponse response, GameClientChannelContext ctx) throws InvalidProtocolBufferException {
        RaidBattleDamageVo raidBattleDamageDTO = new RaidBattleDamageVo();
        BeanUtils.copyProperties(response.getBodyObj(), raidBattleDamageDTO);
        logger.info("战斗攻击信息返回 \r\nRaidId {} \r\n{}", raidBattleDamageDTO.getRaidId(), response.getBodyObj().toString());
        /*
        //Protobuf version
        RaidBattleAttackMsgBody.RaidBattleAttackMsgResponseBody responseBody = response.getResponseBody();
        JsonFormat.TypeRegistry typeRegistry = JsonFormat.TypeRegistry.newBuilder()
                .add(RaidBattleAttackMsgBody.Contribution.getDescriptor())
                .add(RaidBattleAttackMsgBody.Attack.getDescriptor())
                .build();
        String print = JsonFormat.printer().usingTypeRegistry(typeRegistry).print(responseBody);
        RaidBattleDamageDTO raidBattleDamageDTO = JacksonUtils.parseObjectV2(print, RaidBattleDamageDTO.class);
        logger.info("战斗攻击信息返回 \r\nRaidId {} \r\n{}", responseBody.getRaidId(), raidBattleDamageDTO.toString());*/
    }

    @GameMessageMapping(GetRaidBattleListMsgResponse.class)
    public void getRaidBattleListMsgResponse(GetRaidBattleListMsgResponse response, GameClientChannelContext ctx) {
        PageResult<RaidBattleVo> list = new PageResult<>();
        BeanUtils.copyProperties(response.getBodyObj(), list);
        if(response.getBodyObj().isFinish()){
            logger.info("战斗历史记录{}", list);
        }else{
            logger.info("当前战斗可参与/已参与列表{}", list);
        }

    }

    @GameMessageMapping(GetRaidBattleRewardListMsgResponse.class)
    public void getRaidBattleRewardListMsgResponse(GetRaidBattleRewardListMsgResponse response, GameClientChannelContext ctx) {
        PageResult<RaidBattleVo> list = new PageResult<>();
        BeanUtils.copyProperties(response.getBodyObj(), list);
        logger.info("玩家报酬列表{}", list);
    }

    @GameMessageMapping(DoReceiveMailMsgResponse.class)
    public void receiveMailMsgResponse(DoReceiveMailMsgResponse response,GameClientChannelContext ctx){
        List origin = response.getBodyObj().getList();
        origin.forEach(ori -> {
            //MailDTO mailDTO = JSON.parseObject(JSON.toJSONString(ori), MailDTO.class);
            MailVo mailDTO = GameBeanUtils.deepCopyByJsonV2(ori, MailVo.class);
            logger.info("玩家领取邮件{}的道具", mailDTO.getId());
            mailDTO.getGifts().forEach(gift->{
                logger.info("领取了邮件中的道具{} 数量:{}",gift.getItemId(),gift.getAmount());
            });
        });
    }

    @GameMessageMapping(DoClaimRaidBattleRewardMsgResponse.class)
    public void claimRaidBattleRewardMsgResponse(DoClaimRaidBattleRewardMsgResponse response,GameClientChannelContext ctx){
        String raidId = response.getBodyObj().getRaidId();
        List<ItemDTO> items = response.getBodyObj().getItems();
        logger.info("RaidBattle {} 领取报酬",raidId);
        if(items != null){
            items.forEach(itemDTO -> {
                logger.info("领取了道具{} 数量:{}",itemDTO.getItemId(),itemDTO.getAmount());
            });
        }
    }

    @GameMessageMapping(GetPlayerCharacterListMsgResponse.class)
    public void getPlayerCharacterListMsgResponse(GetPlayerCharacterListMsgResponse response, GameClientChannelContext ctx) {
        Map<String, CharacterVo> characterMap = response.getBodyObj().getCharacterMap();
        logger.info("拥有角色列表{}", characterMap.toString());
    }

    @GameMessageMapping(DoDiamondGachaMsgResponse.class)
    public void diamondGachaMsgResponse(DoDiamondGachaMsgResponse response, GameClientChannelContext ctx) {
        List<CharacterVo> list = response.getBodyObj().getResult();
        logger.info("抽奖列表{}", list);
    }

    @GameMessageMapping(RaidBattleBoardCastMsgResponse.class)
    public void raidBattleBoardCastMsgResponse(RaidBattleBoardCastMsgResponse response, GameClientChannelContext ctx){
        logger.info("服务器广播信息返回 \r\nRaidId {} \r\n{}",response.getBodyObj().getRaidId(),response.bodyToString());

    }
}
