package com.nekonade.neko.service;


import com.nekonade.common.dto.RaidBattleRewardVo;
import com.nekonade.common.dto.raidbattle.RaidBattleCharacter;
import com.nekonade.common.dto.raidbattle.vo.RaidBattleVo;
import com.nekonade.common.model.PageResult;
import com.nekonade.common.utils.FunctionMapper;
import com.nekonade.dao.daos.RaidBattleRewardDao;
import com.nekonade.dao.daos.db.CharacterDBDao;
import com.nekonade.dao.daos.db.GlobalConfigDao;
import com.nekonade.dao.daos.db.RaidBattleDbDao;
import com.nekonade.dao.db.entity.Character;
import com.nekonade.dao.db.entity.RaidBattleInstance;
import com.nekonade.dao.db.entity.RaidBattleReward;
import com.nekonade.dao.db.entity.config.GlobalConfig;
import com.nekonade.dao.db.entity.data.CharacterDB;
import com.nekonade.dao.db.repository.RaidBattleDbRepository;
import com.nekonade.dao.helper.MongoPageHelper;
import com.nekonade.dao.helper.SortParam;
import com.nekonade.network.message.event.function.StageFailedEvent;
import com.nekonade.network.message.event.function.StagePassEvent;
import com.nekonade.network.message.event.function.StageRetreatedEvent;
import com.nekonade.network.param.game.message.neko.DoCreateBattleMsgRequest;
import lombok.Getter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
public class RaidBattleService {

//    public static final ConcurrentHashMap<String, CopyOnWriteArraySet<String>> ALL_RAIDBATTLE_MAP = new ConcurrentHashMap<>();

    @Autowired
    private DataConfigService dataConfigService;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private GlobalConfigDao globalConfigDao;
    @Autowired
    private RaidBattleDbDao raidBattleDbDao;
    @Autowired
    private RaidBattleDbRepository raidBattleDbRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MongoPageHelper mongoPageHelper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RaidBattleRewardDao raidBattleRewardDao;
    @Autowired
    private CharacterDBDao charactersDbDao;

    public enum Constants {
        Unclaimed(0),
        Claimed(1)
        ;

        @Getter
        private final int type;

        Constants(int type) {
            this.type = type;
        }
    }

    @EventListener
    public void stageClear(StagePassEvent event){
        
    }

    @EventListener
    public void stageFailed(StageFailedEvent event){

    }

    @EventListener
    public void stageRetreatedEvent(StageRetreatedEvent event){

    }

    public RaidBattleInstance findRaidBattleDb(DoCreateBattleMsgRequest request) {
        DoCreateBattleMsgRequest.RequestBody bodyObj = request.getBodyObj();
        int area = bodyObj.getArea();
        int episode = bodyObj.getEpisode();
        int chapter = bodyObj.getChapter();
        int stage = bodyObj.getStage();
        int difficulty = bodyObj.getDifficulty();
        if (area == 0 || episode == 0 || chapter == 0 || stage == 0 || difficulty == 0) {
            return null;
        }
        return raidBattleDbDao.getRaidBattle(area, episode, chapter, stage, difficulty);
    }

    /*public PageResult<RaidBattleRewardDTO> findByPage(long playerId, Integer page, Integer limit) {
        *//*final Query query = new Query(Criteria.where("receiverId").is(playerId));
        Function<MailBox, MailDTO> mapper = FunctionMapper.Mapper(MailBox.class, MailDTO.class);
        return mongoPageHelper.pageQuery(query, MailBox.class, limit, page, sortParam, mapper);*//*
        String playerRewardSetKey = EnumRedisKey.RAIDBATTLE_REWARD_SET.getKey(String.valueOf(playerId));
        Set<String> members = redisTemplate.opsForSet().members(playerRewardSetKey);
        PageResult<RaidBattleRewardDTO> pageResult = new PageResult<>();
        if(members  == null || members.size() == 0){
            return pageResult;
        }
        List<String> unclaimedList = members.stream().filter(raidId -> {
            String playerRewardKey = EnumRedisKey.RAIDBATTLE_REWARD.getKey(raidId, String.valueOf(playerId));
            String rewardJson = redisTemplate.opsForValue().get(playerRewardKey);
            if(rewardJson == null){
                redisTemplate.opsForSet().remove(playerRewardSetKey,raidId);
                return false;
            }
            return true;
        }).collect(Collectors.toList());
        int start = page * limit;
        int end = start + limit;
        if(start > unclaimedList.size()){
            return pageResult;
        }
        end = Math.min(end,unclaimedList.size());
        List<String> ids = new ArrayList<>();
        for(int i =0;i < end;i++){
            String raidId = unclaimedList.get(i);
            String playerRewardKey = EnumRedisKey.RAIDBATTLE_REWARD.getKey(raidId, String.valueOf(playerId));
            ids.add(playerRewardKey);
        }
        List<String> resultJsonList = redisTemplate.opsForValue().multiGet(ids);
        List<RaidBattleRewardDTO> rewardDTOList = new ArrayList<>();
        if(resultJsonList == null){
            return pageResult;
        }
        resultJsonList.forEach(entityJson->{
            rewardDTOList.add(JSON.parseObject(entityJson,RaidBattleRewardDTO.class));
        });
        rewardDTOList.sort(Comparator.comparingLong(RaidBattleRewardDTO::getTimestamp));
        //写入报酬
        redisTemplate.opsForValue().setIfAbsent(playerRewardKey,rewardJson,EnumRedisKey.RAIDBATTLE_REWARD.getTimeout());
        //写入到该玩家的报酬Set，方便查询未领取的报酬
        redisTemplate.opsForSet().add(playerRewardSetKey,raidId);
    }*/

    public PageResult<RaidBattleVo> findRaidBattleHistoryByPage(long playerId, Integer page, Integer limit) {
        SortParam sortParam = new SortParam();
        sortParam.setSortDirection(Sort.Direction.DESC);
        /*RaidBattle example = new RaidBattle();
        RaidBattle.Player player = new RaidBattle.Player();
        player.setPlayerId(playerId);
        example.setFinish(true);
        example.getPlayers().add(player);
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnorePaths("items");
        Example<RaidBattle> queryEntity = Example.of(example, matcher);
        Criteria criteria = Criteria.byExample(queryEntity);
        final Query query = new Query().addCriteria(criteria);*/

        Criteria criteria = Criteria.where("finish").is(true).and("players" + "." + playerId).exists(true);
        final Query query = new Query(criteria);
        Function<RaidBattleInstance, RaidBattleVo> mapper = FunctionMapper.Mapper(RaidBattleInstance.class, RaidBattleVo.class);
        return mongoPageHelper.pageQuery(query, RaidBattleInstance.class, limit, page, sortParam,mapper);
    }

    public RaidBattleReward findUnclaimedRewardByRaidId(long playerId, String raidId) {
        Constants type = Constants.Unclaimed;
        Optional<RaidBattleReward> op = raidBattleRewardDao.findUnclaimedRewardByRaidId(playerId, raidId,type.getType());
        return op.orElse(null);
    }

    public RaidBattleReward claimedRewardByRaidId(long playerId, String raidId) {
        final Query query = new Query(Criteria.where("raidId").is(raidId).and("playerId").is(playerId));
        Update update = new Update();
        update.set("claimed", Constants.Claimed.getType());
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(false);
        options.returnNew(true);
        RaidBattleReward result = mongoTemplate.findAndModify(query,update,options, RaidBattleReward.class);
        return result;
    }

    public PageResult<RaidBattleRewardVo> findUnclaimedRewardByPage(long playerId, Integer page, Integer limit) {
        SortParam sortParam = new SortParam();
        Constants type = Constants.Unclaimed;
        return findRewardByPage(playerId,type.getType(),page,limit,sortParam);
    }
    public PageResult<RaidBattleRewardVo> findClaimedRewardByPage(long playerId, Integer page, Integer limit) {
        SortParam sortParam = new SortParam();
        sortParam.setSortDirection(Sort.Direction.DESC);
        Constants type = Constants.Claimed;
        return findRewardByPage(playerId,type.getType(),page,limit,sortParam);
    }

    private PageResult<RaidBattleRewardVo> findRewardByPage(long playerId, int type, Integer page, Integer limit, SortParam sortParam) {
        RaidBattleReward example = new RaidBattleReward();
        example.setPlayerId(playerId);
        example.setClaimed(type);
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnorePaths("items").withIgnorePaths("timestamp");
        Example<RaidBattleReward> queryEntity = Example.of(example, matcher);
        Criteria criteria = Criteria.byExample(queryEntity);
        final Query query = new Query(criteria);
        /*final Query query = new Query(Criteria.where("playerId").is(playerId).and("claimed").is(type));*/
        Function<RaidBattleReward, RaidBattleRewardVo> mapper = FunctionMapper.Mapper(RaidBattleReward.class, RaidBattleRewardVo.class);
        return mongoPageHelper.pageQuery(query, RaidBattleReward.class, limit, page, sortParam,mapper);
    }

    public RaidBattleCharacter CalcRaidBattleInitCharacterStatus(Character source){
        RaidBattleCharacter character = new RaidBattleCharacter();
        CalcRaidBattleInitCharacterStatus(source,character);
        return character;
    }

    /**
     * 应该和RaidBattle模块中的计算统一,或者分两步去做
     * @param source
     * @param target
     */
    public void CalcRaidBattleInitCharacterStatus(Character source, RaidBattleCharacter target){
        String charaId = source.getCharacterId();
        CharacterDB db = charactersDbDao.findChara(charaId);
        Map<String, GlobalConfig.CharacterConfig.StatusDataBase> statusDataBase = globalConfigDao.getGlobalConfig().getCharacterConfig().getStatusDataBase();
        GlobalConfig.CharacterConfig.StatusDataBase dataBase = statusDataBase.get(db.getCharacterId());
        BeanUtils.copyProperties(source,target);

        //计算Hp
        //HP = Floor ( 100 + HP_JOB_A * BaseLv + HP_JOB_B * ( 1 + 2 + 3 ... + BaseLv ) ) * ( 1 + VIT / 100 )
        if(dataBase == null){
            dataBase = new GlobalConfig.CharacterConfig.StatusDataBase();
        }
        //计算Hp
        //HP = Floor ( 100 + HP_JOB_A * BaseLv + HP_JOB_B * ( 1 + 2 + 3 ... + BaseLv ) ) * ( 1 + VIT / 100 )
        int level = source.getLevel();
        double hpFactor = dataBase.getHpFactor();
        int hp0 = 0;
        for(int i = 0; i < level;i++){
            hp0 += level;
        }
        //随便算算
        //后期可以缓存计算结果
        long hp = (long)((100d + level * hpFactor + Math.pow(hpFactor,2) * hp0) * (1 + level / 100d));
        target.setMaxHp(hp);
        target.setHp(target.getMaxHp());

        double atkFactor = dataBase.getAtkFactor();
        int atk = (int)(db.getBaseAtk() * (1 + Math.pow( atkFactor,2) + level / 100d));
        target.setAtk(atk);

        double defFactor = dataBase.getDefFactor();
        int def = (int)(db.getBaseDef() * (1 + Math.pow(defFactor,1.5) + level / 100d));
        target.setDef(def);
    }

}
