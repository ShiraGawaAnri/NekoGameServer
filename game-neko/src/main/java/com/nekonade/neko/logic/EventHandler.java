package com.nekonade.neko.logic;

import com.nekonade.common.cloud.RaidBattleServerInstance;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.dto.CharacterVo;
import com.nekonade.common.dto.ItemDTO;
import com.nekonade.common.dto.MailVo;
import com.nekonade.common.dto.RaidBattleRewardVo;
import com.nekonade.common.dto.raidbattle.RaidBattleCharacter;
import com.nekonade.common.dto.raidbattle.RaidBattleEnemy;
import com.nekonade.common.dto.raidbattle.RaidBattlePlayer;
import com.nekonade.common.dto.raidbattle.vo.RaidBattleVo;
import com.nekonade.common.error.exceptions.GameErrorException;
import com.nekonade.common.error.exceptions.GameNotifyException;
import com.nekonade.common.model.PageResult;
import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.common.utils.CalcCoolDownUtils;
import com.nekonade.common.utils.DrawUtils;
import com.nekonade.common.utils.GameTimeUtils;
import com.nekonade.common.utils.JacksonUtils;
import com.nekonade.dao.daos.db.CharacterDBDao;
import com.nekonade.dao.daos.db.EnemyDBDao;
import com.nekonade.dao.daos.db.GachaPoolDBDao;
import com.nekonade.dao.daos.db.RaidBattleDbDao;
import com.nekonade.dao.db.entity.Character;
import com.nekonade.dao.db.entity.*;
import com.nekonade.dao.db.entity.data.CharacterDB;
import com.nekonade.dao.db.entity.data.EnemyDB;
import com.nekonade.dao.db.entity.data.GachaPoolDB;
import com.nekonade.dao.helper.SortParam;
import com.nekonade.neko.service.MailBoxService;
import com.nekonade.neko.service.RaidBattleService;
import com.nekonade.neko.service.StaminaService;
import com.nekonade.network.message.channel.GameChannel;
import com.nekonade.network.message.config.ServerConfig;
import com.nekonade.network.message.context.UserEvent;
import com.nekonade.network.message.context.UserEventContext;
import com.nekonade.network.message.event.function.StaminaSubPointEvent;
import com.nekonade.network.message.event.function.TriggerSystemSendMailEvent;
import com.nekonade.network.message.event.user.*;
import com.nekonade.network.message.manager.CharacterManager;
import com.nekonade.network.message.manager.DiamondManager;
import com.nekonade.network.message.manager.InventoryManager;
import com.nekonade.network.message.manager.PlayerManager;
import com.nekonade.network.param.game.message.battle.JoinRaidBattleMsgRequest;
import com.nekonade.network.param.game.message.neko.*;
import com.nekonade.network.param.game.messagedispatcher.GameMessageHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@GameMessageHandler
public class EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(PlayerLogicHandler.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RaidBattleServerInstance raidBattleServerInstance;

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private StaminaService staminaService;

    @Autowired
    private MailBoxService mailBoxService;

    @Autowired
    private RaidBattleDbDao raidBattleDbDao;

    @Autowired
    private RaidBattleService raidBattleService;

    @Autowired
    private GachaPoolDBDao gachaPoolsDbDao;

    @Autowired
    private CharacterDBDao charactersDbDao;

    @Autowired
    private EnemyDBDao enemyDBDao;

    @UserEvent(IdleStateEvent.class)
    public void idleStateEvent(UserEventContext<PlayerManager> ctx, IdleStateEvent event, Promise<Object> promise) {
        IdleState state = event.state();
        logger.debug("收到空闲事件：{} state : {}", event.getClass().getName(), state.name());
        switch (state) {
            case READER_IDLE:
            case WRITER_IDLE:
            default:
                break;
            case ALL_IDLE:
                //ctx.getCtx().gameChannel().unsafeClose();
                break;
        }
    }

    @UserEvent(TriggerPlayerLevelUpEventUser.class)
    public void playerLevelUpEventUser(UserEventContext<PlayerManager> utx, TriggerPlayerLevelUpEventUser event, Promise<Object> promise) {
        TriggerPlayerLevelUpMsgResponse response = new TriggerPlayerLevelUpMsgResponse();
        response.getBodyObj().setData(event);
        promise.setSuccess(response);
    }

    @UserEvent(GetSelfInfoEventUser.class)
    public void getSelfInfoEvent(UserEventContext<PlayerManager> utx, GetSelfInfoEventUser event, Promise<Object> promise) {
        GetPlayerSelfMsgResponse response = new GetPlayerSelfMsgResponse();
        utx.getDataManager().getStaminaManager().checkStamina();
        Player player = utx.getDataManager().getPlayer();
        GetPlayerSelfMsgResponse.ResponseBody body = response.getBodyObj();
        body.setCreateTime(player.getCreateTime());
        body.setLastLoginTime(player.getLastLoginTime());
        body.setLevel(player.getLevel());
        body.setNickname(player.getNickName());
        body.setPlayerId(player.getPlayerId());
        body.setZoneId(player.getZoneId());
        promise.setSuccess(response);
    }

    @UserEvent(GetPlayerInfoEventUser.class)
    public void getPlayerInfoEvent(UserEventContext<PlayerManager> utx, GetPlayerInfoEventUser event, Promise<Object> promise) {
        GetPlayerByIdMsgResponse response = new GetPlayerByIdMsgResponse();
        Player player = utx.getDataManager().getPlayer();
        response.getBodyObj().setPlayerId(player.getPlayerId());
        response.getBodyObj().setNickName(player.getNickName());
        /*Map<String, String> heros = new HashMap<>();
        player.getCharacters().forEach((k, v) -> {// 复制处理一下，防止对象安全溢出。
            heros.put(k, v);
        });*/
        // response.getBodyObj().setHeros(this.player.getHeros());不要使用这种方式，它会把这个map传递到其它线程
        /*response.getBodyObj().setHeros(heros);*/
        promise.setSuccess(response);
    }

    @UserEvent(GetStaminaEventUser.class)
    public void getStaminaEvent(UserEventContext<PlayerManager> utx, GetStaminaEventUser event, Promise<Object> promise) {
        GetStaminaMsgResponse response = new GetStaminaMsgResponse();
        utx.getDataManager().getStaminaManager().checkStamina();
        Stamina stamina = utx.getDataManager().getStaminaManager().getStamina();
        BeanUtils.copyProperties(stamina, response.getBodyObj());
        promise.setSuccess(response);
    }

    @UserEvent(GetInventoryEventUser.class)
    public void getInventoryEvent(UserEventContext<PlayerManager> utx, GetInventoryEventUser event, Promise<Object> promise) {
        GetInventoryMsgResponse response = new GetInventoryMsgResponse();
        InventoryManager inventoryManager = utx.getDataManager().getInventoryManager();
        Inventory inventory = inventoryManager.getInventory().clone();
        BeanUtils.copyProperties(inventory, response.getBodyObj());
        promise.setSuccess(response);
    }

    @UserEvent(GetMailBoxEventUser.class)
    public void getMailBoxEvent(UserEventContext<PlayerManager> utx, GetMailBoxEventUser event, Promise<Object> promise) {
        GetMailBoxMsgResponse response = new GetMailBoxMsgResponse();
        PlayerManager playerManager = utx.getDataManager();
        long playerId = playerManager.getPlayer().getPlayerId();
        SortParam sortParam = new SortParam();
        sortParam.setSortDirection(Sort.Direction.DESC);
        PageResult<MailVo> result = mailBoxService.findByPage(playerId, -1, 1, 10, sortParam);
        BeanUtils.copyProperties(result, response.getBodyObj());
        promise.setSuccess(response);
    }

    @UserEvent(GetPlayerCharacterListEventUser.class)
    public void getPlayerCharacterListEventUser(UserEventContext<PlayerManager> utx, GetPlayerCharacterListEventUser event, Promise<Object> promise) {
        GetPlayerCharacterListMsgResponse response = new GetPlayerCharacterListMsgResponse();
        PlayerManager playerManager = utx.getDataManager();
        long playerId = playerManager.getPlayer().getPlayerId();
        Map<String, Character> characterMap = playerManager.getCharacterManager().getCharacterMap();
        Map<String, CharacterVo> map = response.getBodyObj().getCharacterMap();
        characterMap.forEach((id,chara)->{
            CharacterVo characterVo = new CharacterVo();
            BeanUtils.copyProperties(chara,characterVo);
            map.put(id,characterVo);
        });
        promise.setSuccess(response);
    }


    @SneakyThrows
    @UserEvent(DoCreateBattleEventUser.class)
    public void createBattle(UserEventContext<PlayerManager> utx, DoCreateBattleEventUser event, Promise<Object> promise) {
        EventExecutor executor = new DefaultEventExecutor();
        PlayerManager playerManager = utx.getDataManager();
        Player player = playerManager.getPlayer();
        long playerId = event.getPlayerId();
        DoCreateBattleMsgResponse response = new DoCreateBattleMsgResponse();
        RaidBattleInstance raidBattle = raidBattleService.findRaidBattleDb((DoCreateBattleMsgRequest)event.request);
        //不存在的关卡
        if (raidBattle == null) {
            promise.setFailure(GameErrorException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.StageDbNotFound).build());
            return;
        }
        String stageId = raidBattle.getStageId();
        if (stageId == null) {
            logger.error("未设定StageId,请必须设定 {}", raidBattle);
            promise.setFailure(GameErrorException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.StageDbNotFound).build());
            return;
        }

        //同一个副本同一时间只能有一个
        String stageIdPlayerIdKey = EnumRedisKey.RAIDBATTLE_STAGEID_PLAYERID_TO_RAIDID.getKey(stageId, String.valueOf(playerId));
        String hasBeenCreatedKey = redisTemplate.opsForValue().get(stageIdPlayerIdKey);
        if (StringUtils.isNotEmpty(hasBeenCreatedKey)) {
            String raidKey = EnumRedisKey.RAIDBATTLE_RAIDID_DETAILS.getKey(hasBeenCreatedKey);
            String battleJson = redisTemplate.opsForValue().get(raidKey);
            try {
                if(StringUtils.isNotEmpty(battleJson)){
                    RaidBattleInstance createdBattle = JacksonUtils.parseObjectV2(battleJson, RaidBattleInstance.class);
                    if (createdBattle != null) {
                        BeanUtils.copyProperties(createdBattle, response.getBodyObj());
                        promise.setSuccess(response);
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                promise.setFailure(null);
            }
        }
        //未激活/已关闭的活动关卡
        if (!raidBattle.getActive()) {
            promise.setFailure(GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.StageDbClosed).build());
            return;
        }
        //其他特殊条件========= 如不能使用某个角色,必须多少级,必须完成过某个关卡等等
        if (false) {
            promise.setFailure(null);
            return;
        }



        long limitCounter = raidBattle.getLimitCounter();
        boolean flagLimitCounter = false;
        String raidBattleLimitCounterKey = EnumRedisKey.RAIDBATTLE_LIMIT_COUNTER.getKey(String.valueOf(playerId), stageId);
        if (limitCounter > 0) {
            flagLimitCounter = true;
            String timesStr = redisTemplate.opsForValue().get(raidBattleLimitCounterKey);
            if (!StringUtils.isEmpty(timesStr)) {
                if (Long.valueOf(timesStr) >= limitCounter) {
                    promise.setFailure(GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.StageReachLimit).build());
                    return;
                }
            }
        }
        //消耗道具
        Map<String, Integer> costItemMap = raidBattle.getEntryCostItemMap();
        boolean flagCostItem = false;
        if (costItemMap.size() > 0) {
            flagCostItem = playerManager.getInventoryManager().checkItemEnough(costItemMap);
//            try {
//                flagCostItem = playerManager.getInventoryManager().checkItemEnough(costItemMap);
//            } catch (RuntimeException e) {
//                promise.setFailure(e);
//                return;
//            }
        }
        //疲劳
        int costStaminaPoint = raidBattle.getEntryCostStaminaPoint();
        if (costStaminaPoint > 0) {
            if (costStaminaPoint > player.getStamina().getValue()) {
                promise.setFailure(GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.StaminaNotEnough).build());
                return;
            }
        }

        //检查队伍
        Map<String, Character> characters = player.getCharacters();
        if(characters == null || characters.size() == 0){
            promise.setFailure(GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.RaidBattleJoinWithEmptyParty).build());
            return;
        }

        //同时战斗不允许超过N个
        //非多人战 1
        long now = System.currentTimeMillis();
        String sameTimeSingleKey = EnumRedisKey.RAIDBATTLE_SAMETIME_SINGLE_LIMIT.getKey(String.valueOf(playerId));
        String sameTimeMultiKey = EnumRedisKey.RAIDBATTLE_SAMETIME_MULTI_LIMIT_SET.getKey(String.valueOf(playerId));
        if (!raidBattle.getMultiRaid()) {
            String singleRaidBattleRaidId = redisTemplate.opsForValue().get(sameTimeSingleKey);
            if (StringUtils.isNotEmpty(singleRaidBattleRaidId)) {
                String singleRaidBattleRaidKey = EnumRedisKey.RAIDBATTLE_RAIDID_DETAILS.getKey(singleRaidBattleRaidId);
                String singleRaidBattleJson = redisTemplate.opsForValue().get(singleRaidBattleRaidKey);
                if (StringUtils.isNotEmpty(singleRaidBattleJson)) {
                    //RaidBattle tempRbd = JSON.parseObject(singleRaidBattleJson, RaidBattle.class);
                    RaidBattleInstance tempRbd = JacksonUtils.parseObjectV2(singleRaidBattleJson, RaidBattleInstance.class);
                    if (tempRbd != null && (!tempRbd.getFailed() || tempRbd.getExpireTimestamp() > now) && !tempRbd.getMultiRaid()) {
                        promise.setFailure(GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.SingleRaidBattleSameTimeOnlyOne).build());
                        return;
                    } else {
                        redisTemplate.opsForValue().getOperations().delete(sameTimeSingleKey);
                    }
                } else {
                    redisTemplate.opsForValue().getOperations().delete(sameTimeSingleKey);
                }
            }
        } else {
            //多人战 5
            Set<String> sameTimeRaids = redisTemplate.opsForSet().members(sameTimeMultiKey);
            if (sameTimeRaids != null && sameTimeRaids.size() > 0) {
                List<String> removeList = new ArrayList<>();
                sameTimeRaids.forEach(eachRaidId -> {
                    String tempKey = EnumRedisKey.RAIDBATTLE_RAIDID_DETAILS.getKey(eachRaidId);
                    String tempBattleDetails = redisTemplate.opsForValue().get(tempKey);
                    if (StringUtils.isEmpty(tempBattleDetails)) {
                        removeList.add(eachRaidId);
                    }
                });
                //顺便清理已不存在的战斗
                if (removeList.size() > 0) {
                    redisTemplate.opsForSet().remove(sameTimeMultiKey, removeList.toArray());
                }
                sameTimeRaids = redisTemplate.opsForSet().members(sameTimeMultiKey);
                if (sameTimeRaids != null && sameTimeRaids.size() >= 5) {
                    promise.setFailure(GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.MultiRaidBattleSameTimeReachLimit).build());
                    return;
                }
            }
        }


        //检查怪物配置 - 如果没有任何怪物，提示关卡不存在
        String enemiesRedisKey = EnumRedisKey.ENEMY_DB.getKey();
//        List<String> enemyIds = new ArrayList<>();
//        raidBattle.getEnemies().forEach(each->{
//            enemyIds.add(each.getMonsterId());
//        });
//        enemyIds.forEach(enemyId->{
//            Object enemyDetails = redisTemplate.opsForHash().get(enemiesRedisKey, enemyId);
//            if(enemyDetails != null){
//                RaidBattleEnemy enemy = objectMapper.readValue((String) enemyDetails, RaidBattleEnemy.class);
//                raidBattle.getEnemies().add(enemy);
//            }
//        });
        List<RaidBattleEnemy> enemies = raidBattle.getEnemies();
        if (enemies.size() == 0) {
            promise.setFailure(GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.StageDbClosed).build());
            return;
        }

        AtomicInteger gid = new AtomicInteger();
        //取得相关时间
        //生成RaidId
        String raidId = DigestUtils.md5Hex(playerId + stageId + UUID.randomUUID().toString());
        //设定过期时间
        JoinRaidBattleMsgRequest joinRaidBattleMsgRequest = new JoinRaidBattleMsgRequest();
        int serviceId = joinRaidBattleMsgRequest.getHeader().getServiceId();
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> raidBattleServerInstance.selectRaidBattleServerId(raidId, serviceId), executor).whenComplete((r, e) -> {
            if (e != null) {
                e.printStackTrace();
                logger.error("取得负载ID失败", e);
            }
        });
        raidBattle.setOwnerPlayerId(playerId);
        raidBattle.setRaidId(raidId);
        raidBattle.setFailed(false);
        raidBattle.setFinish(false);
        //初始化创建者自身信息
        RaidBattlePlayer addSelfPlayer = new RaidBattlePlayer();
        BeanUtils.copyProperties(player, addSelfPlayer);
        //随机选N个角色加入到战斗队伍
        Map<String, RaidBattleCharacter> party = addSelfPlayer.getParty();
        List<Character> characterList = new ArrayList<>(characters.values());
        Collections.shuffle(characterList);
        int getNumber = Math.min(4,characterList.size());
        //初始化Player角色数据
        for(int i = 0;i < getNumber;i++){
            Character chara = characterList.get(i);
            RaidBattleCharacter character = raidBattleService.CalcRaidBattleInitCharacterStatus(chara);
            party.put(chara.getCharacterId(),character);
        }

        //加入到战斗中
        raidBattle.getPlayers().putIfAbsent(addSelfPlayer.getPlayerId(),addSelfPlayer);

        //初始化Enemies数据
        enemies.forEach(each->{
            EnemyDB entity = enemyDBDao.findEnemy(each.getCharacterId());
            BeanUtils.copyProperties(entity,each);

            each.setHp(entity.getBaseHp());
            each.setMaxHp(each.getHp());

            each.setAtk(entity.getBaseAtk());

            each.setDef(entity.getBaseDef());

            each.setGid(gid.getAndIncrement());

        });

        //扣除消耗的道具
        if (flagCostItem) {
            playerManager.getInventoryManager().consumeItem(costItemMap);
        }
        //扣除消耗的体力
        StaminaSubPointEvent staminaSubPointEvent = new StaminaSubPointEvent(this, playerManager, costStaminaPoint);
        context.publishEvent(staminaSubPointEvent);
        //扣除次数
        if (flagLimitCounter) {
            long coolDownTimestamp = CalcCoolDownUtils.calcCoolDownTimestamp(raidBattle.getLimitCounterRefreshType());
            redisTemplate.opsForValue().setIfAbsent(raidBattleLimitCounterKey, "0", coolDownTimestamp, TimeUnit.MILLISECONDS);
            redisTemplate.opsForValue().increment(raidBattleLimitCounterKey);
        }
        //redis缓存相关
        //String battleDetailsJson = JSON.toJSONString(raidBattle);
        Duration raidBattleDuration = Duration.ofMillis(raidBattle.getLimitTime());
        String battleDetailsJson = JacksonUtils.toJSONStringV2(raidBattle);
        String raidIdKey = EnumRedisKey.RAIDBATTLE_RAIDID_DETAILS.getKey(raidId);
        //可通过 raidId查找 战斗详情
        redisTemplate.opsForValue().setIfAbsent(raidIdKey, battleDetailsJson, raidBattleDuration);
        //映射 stageId playerId - raidId方便查找
        redisTemplate.opsForValue().set(stageIdPlayerIdKey, raidId, raidBattleDuration);
        //加入到个人拥有的raid数组当中
        if (raidBattle.getMultiRaid()) {
            redisTemplate.opsForSet().add(sameTimeMultiKey, raidId);
            redisTemplate.expire(sameTimeMultiKey, EnumRedisKey.RAIDBATTLE_SAMETIME_MULTI_LIMIT_SET.getTimeout());
        } else {
            redisTemplate.opsForValue().set(sameTimeSingleKey, raidId);
            redisTemplate.expire(sameTimeSingleKey, raidBattleDuration);
        }


        Integer serverId = future.get();
//        DefaultPromise<Integer> serverIdPromise = new DefaultPromise<>(executor);
//        Promise<Integer> await = raidBattleServerInstance.selectRaidBattleServerId(raidId, serverConfig.getServiceId(), serverIdPromise).sync();
//        Integer serverId = await.get();
        if (raidBattle.getMultiRaid()) {
//            CopyOnWriteArraySet<String> set = new CopyOnWriteArraySet<>();
//            RaidBattleService.ALL_RAIDBATTLE_MAP.putIfAbsent(stageId,set);
//            RaidBattleService.ALL_RAIDBATTLE_MAP.get(stageId).add(raidId);
            //TODO:应该由玩家在游戏中允许战斗发布救援时才加入
            // 当前暂时全部加入
            String allKey = EnumRedisKey.RAIDBATTLE_RESCUE_ALL.getKey();
            redisTemplate.opsForList().leftPush(allKey, raidId);
            redisTemplate.opsForList().trim(allKey, 0, 10000);
            redisTemplate.expire(allKey, EnumRedisKey.RAIDBATTLE_RESCUE_ALL.getTimeout());
            //根据STAGE_ID加入,方便筛选
            String stageidKey = EnumRedisKey.RAIDBATTLE_RESCUE_STAGEID.getKey(stageId);
            redisTemplate.opsForList().leftPush(stageidKey, raidId);
            redisTemplate.opsForList().trim(stageidKey, 0, 10000);
            redisTemplate.expire(stageidKey, EnumRedisKey.RAIDBATTLE_RESCUE_STAGEID.getTimeout());
        }
        BeanUtils.copyProperties(raidBattle, response.getBodyObj());
        promise.setSuccess(response);
    }

    @UserEvent(GetArenaPlayerEventUser.class)
    public void getArenaPlayer(UserEventContext<PlayerManager> utx, GetArenaPlayerEventUser event, Promise<Object> promise) {
        GetArenaPlayerListMsgResponse.ArenaPlayer arenaPlayer = new GetArenaPlayerListMsgResponse.ArenaPlayer();
        Player player = utx.getDataManager().getPlayer();
        arenaPlayer.setPlayerId(player.getPlayerId());
        arenaPlayer.setNickName(player.getNickName());
        /*Map<String, String> heros = new HashMap<>();
        player.getCharacters().forEach((k, v) -> {// 复制处理一下，防止对象安全溢出。
            heros.put(k, v);
        });
        arenaPlayer.setHeros(heros);*/
        promise.setSuccess(arenaPlayer);
    }

    @UserEvent(GetRaidBattleListEventUser.class)
    public void getRaidBattleListEventUser(UserEventContext<PlayerManager> utx, GetRaidBattleListEventUser event, Promise<Object> promise) {
        long start = System.currentTimeMillis();
        GetRaidBattleListMsgResponse response = new GetRaidBattleListMsgResponse();
        PlayerManager playerManager = utx.getDataManager();
        long playerId = playerManager.getPlayer().getPlayerId();
        SortParam sortParam = new SortParam();
        sortParam.setSortDirection(Sort.Direction.DESC);
        PageResult<RaidBattleVo> result;
        //如果是查找历史的
        if (event.isFinish()) {
            result = raidBattleService.findRaidBattleHistoryByPage(playerId, event.getPage(), event.getLimit());
            BeanUtils.copyProperties(result, response.getBodyObj());
        } else {
            //如果是查找当前的
            // 当前的 = 自己正在进行中的 + 一定间隔随机的
            String sameTimeMultiKey = EnumRedisKey.RAIDBATTLE_SAMETIME_MULTI_LIMIT_SET.getKey(String.valueOf(playerId));
            Set<String> sameTimeRaids = redisTemplate.opsForSet().members(sameTimeMultiKey);
            if (sameTimeRaids != null && sameTimeRaids.size() > 0) {
                List<String> removeList = new ArrayList<>();
                sameTimeRaids.forEach(eachRaidId -> {
                    String tempKey = EnumRedisKey.RAIDBATTLE_RAIDID_DETAILS.getKey(eachRaidId);
                    String tempBattleDetails = redisTemplate.opsForValue().get(tempKey);
                    if (StringUtils.isEmpty(tempBattleDetails)) {
                        removeList.add(eachRaidId);
                    }
                });
                if (removeList.size() > 0) {
                    redisTemplate.opsForSet().remove(sameTimeMultiKey, removeList.toArray());
                }
                sameTimeRaids = redisTemplate.opsForSet().members(sameTimeMultiKey);
            }
            String randomSetKey = EnumRedisKey.RAIDBATTLE_PLAYER_RANDOM_SET.getKey(String.valueOf(playerId));
            Set<String> members = redisTemplate.opsForSet().members(randomSetKey);
            if (members == null) {
                members = new HashSet<>();
            }
            if (members.size() > 0) {
                Set<String> collect = members.stream().filter(raidId -> {
                    String key = EnumRedisKey.RAIDBATTLE_RAIDID_DETAILS.getKey(raidId);
                    return redisTemplate.hasKey(key);
                }).collect(Collectors.toSet());
                members.removeAll(collect);
                if (members.size() > 0) {
                    redisTemplate.opsForSet().remove(randomSetKey, members.toArray());
                    members.clear();
                }
                members.addAll(collect);
                members.removeAll(Collections.singletonList(null));
            }
            if (members.size() == 0) {
                String key = EnumRedisKey.RAIDBATTLE_RESCUE_ALL.getKey();
                Long size = redisTemplate.opsForList().size(key);
                if (size != null && size > 0) {
                    int takeSize = Math.min((int) Math.max(100d, size * 0.1d), size.intValue());
                    List<String> takeList = redisTemplate.opsForList().range(key, 0, takeSize);
                    if (takeList != null) {
                        Collections.shuffle(takeList);
                        Set<String> collect = takeList.stream().limit(6).collect(Collectors.toSet());
                        members.addAll(collect);
                    }
                }
                members.forEach(each -> redisTemplate.opsForSet().add(randomSetKey, each));
                redisTemplate.opsForSet().getOperations().expire(randomSetKey, EnumRedisKey.RAIDBATTLE_PLAYER_RANDOM_SET.getTimeout());
            }
            //合并
            Set<String> resultSet = new HashSet<>();
            if (sameTimeRaids == null) {
                sameTimeRaids = new HashSet<>();
            }
            resultSet.addAll(sameTimeRaids);
            resultSet.addAll(members);
            result = new PageResult<>();
            List<String> raidBattleKeyList = resultSet.stream().map(EnumRedisKey.RAIDBATTLE_RAIDID_DETAILS::getKey).collect(Collectors.toList());
            List<String> raidBattleJsonList = redisTemplate.opsForValue().multiGet(raidBattleKeyList);
            if (raidBattleJsonList == null) {
                raidBattleJsonList = new ArrayList<>();
            }
            raidBattleJsonList.removeAll(Collections.singleton(null));
            result.setPageNum(1);
            result.setPageSize(raidBattleJsonList.size());
            result.setPages(1);
            result.setTotal((long) raidBattleJsonList.size());
            raidBattleJsonList.forEach(each -> {
                //RaidBattleDTO raidBattleDTO = JSON.parseObject(each, RaidBattleDTO.class);
                RaidBattleVo raidBattleDTO = JacksonUtils.parseObjectV2(each, RaidBattleVo.class);
                result.getList().add(raidBattleDTO);
            });
            BeanUtils.copyProperties(result, response.getBodyObj());
        }
        response.getBodyObj().setFinish(event.isFinish());
        promise.setSuccess(response);
        logger.info("取得playerId {} 的战斗列表消耗 {}ms", playerId, (System.currentTimeMillis() - start));
    }


    @UserEvent(GetRaidBattleRewardListEventUser.class)
    public void getRaidBattleRewardList(UserEventContext<PlayerManager> utx, GetRaidBattleRewardListEventUser event, Promise<Object> promise) {
        GetRaidBattleRewardListMsgResponse response = new GetRaidBattleRewardListMsgResponse();
        PlayerManager playerManager = utx.getDataManager();
        long playerId = playerManager.getPlayer().getPlayerId();
        SortParam sortParam = new SortParam();
        sortParam.setSortDirection(Sort.Direction.DESC);
        PageResult<RaidBattleRewardVo> result;
        int claimed = event.getClaimed();
        if (claimed == 0) {
            result = raidBattleService.findUnclaimedRewardByPage(playerId, event.getPage(), event.getLimit());
        } else {
            result = raidBattleService.findClaimedRewardByPage(playerId, event.getPage(), event.getLimit());
        }
        BeanUtils.copyProperties(result, response.getBodyObj());
        promise.setSuccess(response);
    }

    @SuppressWarnings("unchecked")
    @UserEvent(DoReceiveMailEventUser.class)
    public void receiveMailEventUser(UserEventContext<PlayerManager> utx, DoReceiveMailEventUser event, Promise<Object> promise) {
        PlayerManager dataManager = utx.getDataManager();
        long playerId = dataManager.getPlayer().getPlayerId();
        DoReceiveMailMsgResponse response = new DoReceiveMailMsgResponse();
        String mailId = event.getRequest().getMailId();
        int page = event.getRequest().getPage();
        int type = event.getRequest().getType();
        String lastId = event.getRequest().getLastId();
        boolean allPages = event.getRequest().isAllPages();
        List<MailVo> list = new ArrayList<>();
        List<MailBox> mailBoxes;
        if (StringUtils.isNotEmpty(mailId)) {
            //单个
            mailBoxes = mailBoxService.findMailById(dataManager, mailId);
        } else if (allPages) {
            //全部
            mailBoxes = mailBoxService.findMailAllPages(dataManager);
        } else {
            //某一页
            mailBoxes = mailBoxService.findMailByPage(dataManager, page, type,lastId);
        }
        mailBoxes.forEach(mailBox -> {
            List<ItemDTO> failedList = new ArrayList<>();
            List<ItemDTO> receivedList = mailBox.getGifts().stream().filter(gift -> {
                Item overFlowItem = dataManager.getInventoryManager().produceItem(gift.getItemId(), gift.getAmount());
                if (overFlowItem != null) {
                    failedList.add(gift);
                    logger.info("PlayerId:{} 溢出了道具{} 数量:{}",playerId,gift.getItemId(),gift.getAmount());
                    return false;
                }
                logger.debug("PlayerId:{} 领取道具{} 数量:{}",playerId,gift.getItemId(),gift.getAmount());
                return true;
            }).collect(Collectors.toList());
            if(receivedList.size() > 0){
                MailBox returnMail = mailBoxService.receiveMailBox(mailBox);
                if(returnMail != null && returnMail.getReceived() == 1){
                    MailVo mailDTO = new MailVo();
                    BeanUtils.copyProperties(returnMail,mailDTO);
                    mailDTO.setGifts(receivedList);
                    list.add(mailDTO);
                }
                if(failedList.size() > 0){
                    TriggerSystemSendMailEvent triggerEvent = new TriggerSystemSendMailEvent(this, dataManager, failedList);
                    context.publishEvent(triggerEvent);
                }
            }
        });
        response.getBodyObj().setList(list);
        promise.setSuccess(response);
    }

    @UserEvent(DoClaimRaidBattleRewardEventUser.class)
    public void claimRaidBattleRewardEventUser(UserEventContext<PlayerManager> utx, DoClaimRaidBattleRewardEventUser event, Promise<Object> promise){
        PlayerManager dataManager = utx.getDataManager();
        long playerId = dataManager.getPlayer().getPlayerId();
        String raidId = event.getRaidId();
        DoClaimRaidBattleRewardMsgResponse response = new DoClaimRaidBattleRewardMsgResponse();

        RaidBattleReward reward = raidBattleService.findUnclaimedRewardByRaidId(playerId, raidId);
        if(reward != null){
            List<ItemDTO> failedList = new ArrayList<>();
            List<ItemDTO> receivedList = reward.getItems().stream().filter(itemDTO -> {
                Item overFlowItem = dataManager.getInventoryManager().produceItem(itemDTO.getItemId(), itemDTO.getAmount());
                if (overFlowItem != null) {
                    failedList.add(itemDTO);
                    logger.info("PlayerId:{} 溢出了道具{} 数量:{}",playerId,itemDTO.getItemId(),itemDTO.getAmount());
                    return false;
                }
                logger.debug("PlayerId:{} 领取道具{} 数量:{}",playerId,itemDTO.getItemId(),itemDTO.getAmount());
                return true;
            }).collect(Collectors.toList());
            if(receivedList.size() > 0){
                RaidBattleReward returnResult = raidBattleService.claimedRewardByRaidId(playerId, raidId);
                if(returnResult != null && returnResult.getClaimed() == RaidBattleService.Constants.Claimed.getType()){
                    returnResult.setItems(receivedList);
                    BeanUtils.copyProperties(returnResult,response.getBodyObj());
                }
                if(failedList.size() > 0){
                    TriggerSystemSendMailEvent triggerEvent = new TriggerSystemSendMailEvent(this, dataManager, failedList);
                    context.publishEvent(triggerEvent);
                }
            }
        }
        promise.setSuccess(response);
    }

    @UserEvent(DoDiamondGachaEventUser.class)
    public void diamondGachaEventUser(UserEventContext<PlayerManager> utx, DoDiamondGachaEventUser event, Promise<Object> promise){
        long startGachaTime = System.currentTimeMillis();
        PlayerManager playerManager = event.getPlayerManager();
        String gachaPoolsId = event.getGachaPoolsId();
        int gachaType = event.getGachaType();
        long playerId = playerManager.getPlayer().getPlayerId();
        GameChannel gameChannel = playerManager.getGameChannel();
        EventExecutor executor = gameChannel.executor();

        CharacterManager characterManager = playerManager.getCharacterManager();
        DiamondManager diamondManager = playerManager.getDiamondManager();
        InventoryManager inventoryManager = playerManager.getInventoryManager();


        if(StringUtils.isEmpty(gachaPoolsId)){
            throw GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.GachaPoolsNotExist).build();
        }
        GachaPoolDB gachaPoolsDB = gachaPoolsDbDao.findGachaPoolsDB(gachaPoolsId);
        if(gachaPoolsDB == null){
            throw GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.GachaPoolsNotExist).build();
        }
        long starTime = gachaPoolsDB.getStarTime();
        long endTime = gachaPoolsDB.getEndTime();
        boolean active = GameTimeUtils.checkTimeIsBetween(starTime, endTime);
        if(!active){
            throw GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.GachaPoolsNotActive).build();
        }
        List<GachaPoolDB.GachaPoolCharacter> characters = gachaPoolsDB.getCharacters();
        int times = gachaType == 1 ? 1 : 10;

        int costDiamond = gachaPoolsDB.getCostDiamond() * times;
        if(costDiamond == 0){
            throw GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.GachaPoolsLogicError).build();
        }
        if(!diamondManager.checkDiamondEnough(costDiamond)){
            throw GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.GachaPoolsDiamondNotEnough).build();
        }
        if(times == 10){
            times++;
        }
        List<GachaPoolDB.GachaPoolCharacter> list = new ArrayList<>();
        for (int i = 0;i < times;i++){
            GachaPoolDB.GachaPoolCharacter drawResult = DrawUtils.draw(characters);
            list.add(drawResult);
        }

        //生成要写入的角色
        List<Character> characterList = list.stream().map(each -> {
            String charaId = each.getCharacterId();
            CharacterDB db = charactersDbDao.findChara(charaId);
            Character character = new Character();
            BeanUtils.copyProperties(db, character);
            return character;
        }).collect(Collectors.toList());

        //生成展示抽奖结果
        List<CharacterVo> result = list.stream().map(each -> {
            CharacterVo characterDTO = new CharacterVo();
            characterDTO.setCharacterId(each.getCharacterId());
            characterDTO.setIsNew(!characterManager.checkCharaExist(each.getCharacterId()));
            return characterDTO;
        }).collect(Collectors.toList());

        //扣除钻石
        diamondManager.subDiamond(costDiamond);

        //写入角色
        characterList.forEach(each->{
            String charaId = each.getCharacterId();
            if(characterManager.checkCharaExist(charaId)){
                inventoryManager.produceItemWithOverFlowProcess("6",10);
            }else{
                characterManager.addChara(each);
            }
        });
        long endGachaTime = System.currentTimeMillis();
        logger.info("Player:{} 抽卡耗时:{}",playerId,(endGachaTime-startGachaTime));
        DefaultPromise<Object> promise1 = new DefaultPromise<>(executor);
        GetPlayerCharacterListEventUser event1 = new GetPlayerCharacterListEventUser();
        gameChannel.getEventDispatchService().fireUserEventWithFuture(playerId,event1,promise1).addListener(future1 -> {
            if (future1.isSuccess()) {
                GetPlayerCharacterListMsgResponse response = (GetPlayerCharacterListMsgResponse) future1.get();
                response.getHeader().setClientSendTime(System.currentTimeMillis());
                utx.getCtx().writeAndFlush(response);
            }
            //再发送抽取结果
            DoDiamondGachaMsgResponse response2 = new DoDiamondGachaMsgResponse();
            response2.getBodyObj().setResult(result);
            promise.setSuccess(response2);
        });
    }
}
