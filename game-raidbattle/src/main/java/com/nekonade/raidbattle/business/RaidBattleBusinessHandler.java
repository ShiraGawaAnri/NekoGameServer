package com.nekonade.raidbattle.business;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.dto.PlayerVo;
import com.nekonade.common.dto.raidbattle.vo.RaidBattleDamageVo;
import com.nekonade.common.dto.raidbattle.RaidBattleCharacter;
import com.nekonade.common.dto.raidbattle.RaidBattlePlayer;
import com.nekonade.common.error.exceptions.BasicException;
import com.nekonade.common.error.exceptions.GameErrorException;
import com.nekonade.common.error.exceptions.GameNotifyException;
import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.common.model.ErrorResponseEntity;
import com.nekonade.dao.daos.db.SkillDBDao;
import com.nekonade.dao.db.entity.RaidBattleInstance;
import com.nekonade.network.param.game.message.battle.JoinRaidBattleMsgRequest;
import com.nekonade.network.param.game.message.battle.JoinRaidBattleMsgResponse;
import com.nekonade.network.param.game.message.battle.RaidBattleCardAttackMsgRequest;
import com.nekonade.network.param.game.message.battle.rpc.JoinRaidBattleRPCRequest;
import com.nekonade.network.param.game.message.battle.rpc.JoinRaidBattleRPCResponse;
import com.nekonade.network.param.game.message.neko.error.GameErrorMsgResponse;
import com.nekonade.network.param.game.message.neko.error.GameNotificationMsgResponse;
import com.nekonade.network.param.game.messagedispatcher.GameMessageHandler;
import com.nekonade.network.param.game.messagedispatcher.GameMessageMapping;
import com.nekonade.network.param.game.messagedispatcher.IGameChannelContext;
import com.nekonade.raidbattle.event.function.PushRaidBattleEvent;
import com.nekonade.raidbattle.event.function.RaidBattleShouldBeFinishEvent;
import com.nekonade.raidbattle.event.user.PushRaidBattleDamageDTOEventUser;
import com.nekonade.raidbattle.event.user.PushRaidBattleToSinglePlayerEventUser;
import com.nekonade.raidbattle.manager.RaidBattleManager;
import com.nekonade.raidbattle.message.context.RaidBattleEvent;
import com.nekonade.raidbattle.message.context.RaidBattleEventContext;
import com.nekonade.raidbattle.message.context.RaidBattleMessageContext;
import com.nekonade.raidbattle.service.CalcRaidBattleService;
import com.nekonade.raidbattle.service.GameErrorService;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

@GameMessageHandler
public class RaidBattleBusinessHandler {

    private final static Logger logger = LoggerFactory.getLogger(RaidBattleBusinessHandler.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private GameErrorService gameErrorService;

    @Autowired
    private CalcRaidBattleService calcRaidBattleService;

    @Autowired
    private SkillDBDao cardsDbDao;

    @RaidBattleEvent(IdleStateEvent.class)
    public void idleStateEvent(RaidBattleEventContext<RaidBattleManager> ctx, IdleStateEvent event, Promise<Object> promise) {
        IdleState state = event.state();
        logger.debug("RB收到空闲事件：{} state : {}", event.getClass().getName(), state.name());
        switch (state) {
            case READER_IDLE:
            case WRITER_IDLE:
            default:
                break;
            case ALL_IDLE:
//                检查该战斗的Enemy的状态
//                ctx.getCtx().close();
                RaidBattleShouldBeFinishEvent raidBattleShouldBeFinishEvent = new RaidBattleShouldBeFinishEvent(this, ctx.getDataManager(), true);
                context.publishEvent(raidBattleShouldBeFinishEvent);
                break;
        }
    }

    @GameMessageMapping(JoinRaidBattleMsgRequest.class)
    public void joinRaidBattleMsgRequest(JoinRaidBattleMsgRequest request, RaidBattleMessageContext<RaidBattleManager> ctx) {
        JoinRaidBattleRPCRequest joinRaidBattleMsgRequest = new JoinRaidBattleRPCRequest();
        joinRaidBattleMsgRequest.getHeader().setPlayerId(ctx.getPlayerId());
        Promise<IGameMessage> promise = ctx.newPromise();
        promise.addListener((GenericFutureListener<Future<IGameMessage>>) future -> {
            try {
                if (future.isSuccess()) {
                    JoinRaidBattleRPCResponse rpcResponse = (JoinRaidBattleRPCResponse) future.get();
                    String raidId = rpcResponse.getHeader().getAttribute().getRaidId();
                    int errorCode = rpcResponse.getHeader().getErrorCode();
                    if (errorCode == 0) {
                        logger.info("由RB服务器处理加入RaidBattle {} 的请求", raidId);
                        RaidBattleInstance raidBattle = ctx.getDataManager().getRaidBattle();
                        PlayerVo playerDTO = rpcResponse.getBodyObj().getPlayer();
                        DefaultPromise<Object> promise1 = ctx.newPromise();
                        ctx.getDataManager().playerJoinRaidBattle(playerDTO, promise1).addListener(future1 -> {
                            if (future1.isSuccess()) {
                                JoinRaidBattleMsgResponse response = new JoinRaidBattleMsgResponse();
                                BeanUtils.copyProperties(raidBattle, response.getBodyObj());
                                if(raidBattle.getPlayers().size() > 30){
                                    response.getBodyObj().setPlayers(null);
                                }
                                ctx.sendMessage(response);
                            } else {
                                Throwable e = future1.cause();
                                gameErrorService.returnGameErrorResponse(e, ctx);
                            }
                        });
                        /*promise1.addListener(future1 -> {
                            if (future1.isSuccess()) {
                                JoinRaidBattleMsgResponse response = new JoinRaidBattleMsgResponse();
                                BeanUtils.copyProperties(raidBattle, response.getBodyObj());
                                ctx.sendMessage(response);
                            } else {
                                Throwable e = future1.cause();
                                gameErrorService.returnGameErrorResponse(e, ctx);
                            }

                        });*/
                    } else {
                        //暂时处理
                        throw GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.RaidBattleJoinWithEmptyParty).build();
                    }
                } else {
                    logger.error("加入战斗失败", future.cause());
                    throw future.cause();
                }
            } catch (Throwable e) {
                gameErrorService.returnGameErrorResponse(e, ctx);
            }

        });
        ctx.sendRPCMessage(joinRaidBattleMsgRequest, promise);
    }



    @GameMessageMapping(RaidBattleCardAttackMsgRequest.class)
    public void raidBattleCardAttackMsgRequest(RaidBattleCardAttackMsgRequest request, RaidBattleMessageContext<RaidBattleManager> ctx) {
        GameMessageHeader header = request.getHeader();
        long playerId = header.getPlayerId();
        EventExecutor select = ctx.getDataManager().getRbExecutorGroup().select(playerId);
        select.execute(()->{
            try{
                RaidBattleCardAttackMsgRequest.RequestBody param = request.getBodyObj();
                int charaPos = param.getCharaPos();
                String charaId = param.getCharaId();
                String cardId = param.getCardId();
                int targetPos = param.getTargetPos();
                List<Integer> selectCharaPos = param.getSelectCharaPos();
                long turn = param.getTurn();
                RaidBattleManager dataManager = ctx.getDataManager();
                String raidId = dataManager.getRaidBattle().getRaidId();
                //检查是否在Players里
                RaidBattlePlayer actionPlayer = dataManager.getPlayerByPlayerId(playerId);
                if (actionPlayer.isRetreated()) {
                    return;
                }
                if (dataManager.checkPlayerCharacterAllDead(actionPlayer)) {
                    return;
                }
//        int cardId = request.getBodyObj().getCardId();
//        int chara = request.getBodyObj().getChara();
//        long turn = request.getBodyObj().getTurn();
                if (dataManager.isRaidBattleFinishOrFailed()) {
                    //若战斗结束,则
                    //详细数据用于战斗画面的结束
                    PushRaidBattleToSinglePlayerEventUser event = new PushRaidBattleToSinglePlayerEventUser(ctx, request);
                    ctx.sendUserEvent(event, null, raidId);

                    if (dataManager.isRaidBattleChannelActive()) {
                        dataManager.closeRaidBattleChannel();
                    }
                    //也可以再返回一个提醒
                    throw GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.RaidBattleHasBeenFinished).build();
                    //return;
                }
                //攻击
                RaidBattleCharacter character = actionPlayer.getParty().get(charaId);
                if (character == null || character.isAlive()) {
                    //由于是测试 所以简单随机化就行
                    character = actionPlayer.getParty().values().stream().findFirst().get();
                    /*throw GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.RaidBattleAttackInvalidParam).build();*/
                }
                RaidBattleDamageVo raidBattleDamageDTO = calcRaidBattleService.calcDamage(dataManager, actionPlayer, character, cardId, targetPos, selectCharaPos, turn);
                //RaidBattleDamageDTO raidBattleDamageDTO = new RaidBattleDamageDTO();

                Promise<Object> promise = select.newPromise();
                PushRaidBattleDamageDTOEventUser pushRaidBattleDamageDTOEventUser = new PushRaidBattleDamageDTOEventUser(request, raidBattleDamageDTO);
                ctx.sendUserEvent(pushRaidBattleDamageDTOEventUser, promise, raidId).addListener(future -> {
                    RaidBattleShouldBeFinishEvent raidBattleShouldBeFinishEvent = new RaidBattleShouldBeFinishEvent(this, dataManager);
                    context.publishEvent(raidBattleShouldBeFinishEvent);
                });
                //广播消息 - 除了自己
                PushRaidBattleEvent pushRaidBattleEvent = new PushRaidBattleEvent(this, dataManager, request);
                dataManager.setEvent(pushRaidBattleEvent);
            } catch (Throwable e){
                catchException(e,ctx);
            }
        });


    }

    private void catchException(Throwable e, IGameChannelContext ctx){
        ErrorResponseEntity errorEntity = new ErrorResponseEntity();
        BasicException exception;
        int type = 0;
        if (e instanceof GameErrorException) {
            exception = (GameErrorException) e;
            type = 1;
        } else if (e instanceof GameNotifyException) {
            exception = (GameNotifyException) e;
            type = 2;
        } else {
            exception = GameErrorException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.RaidBattleLogicError).build();
        }
        errorEntity.setErrorCode(exception.getError().getErrorCode());
        errorEntity.setErrorMsg(exception.getError().getErrorDesc());
        errorEntity.setData(exception.getData());
        AbstractJsonGameMessage<?> response;
        switch (type){
            case 0:
            case 1:
                response = new GameErrorMsgResponse();
                ((GameErrorMsgResponse)response).getBodyObj().setError(errorEntity);
                ctx.sendMessage(response);
                break;
            case 2:
                response = new GameNotificationMsgResponse();
                ((GameNotificationMsgResponse)response).getBodyObj().setError(errorEntity);
                ctx.sendMessage(response);
                break;
            default:
                break;
        }

    }
}
