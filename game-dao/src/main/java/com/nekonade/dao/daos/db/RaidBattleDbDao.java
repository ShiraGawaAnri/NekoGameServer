package com.nekonade.dao.daos.db;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.dto.raidbattle.RaidBattleEnemy;
import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.dao.daos.AbstractDao;
import com.nekonade.dao.db.entity.RaidBattleInstance;
import com.nekonade.dao.db.entity.data.RaidBattleDB;
import com.nekonade.dao.db.repository.RaidBattleDbRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Arrays;

@Service
public class RaidBattleDbDao extends AbstractDao<RaidBattleDB, String> {

    @Autowired
    private RaidBattleDbRepository repository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public static String createStageRedisKey(String[] list) {
        return "STAGE_" + String.join("_", Arrays.asList(list));
    }

    public static String createStageRedisKey(RaidBattleDB raidBattleDB){
        return MessageFormat.format("STAGE_{0}_{1}_{2}_{3}_{4}",raidBattleDB.getArea(),raidBattleDB.getEpisode(),raidBattleDB.getChapter(),raidBattleDB.getStage(),raidBattleDB.getDifficulty());
    }

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.RAIDBATTLE_DB;
    }

    @Override
    protected MongoRepository<RaidBattleDB, String> getMongoRepository() {
        return repository;
    }

    @Override
    protected Class<RaidBattleDB> getEntityClass() {
        return RaidBattleDB.class;
    }

    /*public RaidBattleDB findRaidBattleDb(int area,int episode,int chapter,int stage,int difficulty){
        String[] list = new String[]{String.valueOf(area),String.valueOf(episode),String.valueOf(chapter),String.valueOf(stage),String.valueOf(difficulty)};
        String key = EnumRedisKey.RAIDBATTLE_DB.getKey();
        String stageKey = createStageRedisKey(list);
        Object value = stringRedisTemplate.opsForHash().get(key, stageKey);
        if(value != null){
            return JSON.parseObject((String)value,RaidBattleDB.class);
        }
        RaidBattleDB raidBattleDB = new RaidBattleDB();
        raidBattleDB.setArea(area);
        raidBattleDB.setEpisode(episode);
        raidBattleDB.setChapter(chapter);
        raidBattleDB.setStage(stage);
        ExampleMatcher matcher = ExampleMatcher.matching().withIncludeNullValues();
        Example<RaidBattleDB> queryEntity = Example.of(raidBattleDB, matcher);
        Optional<RaidBattleDB> op = repository.findOne(queryEntity);
        return op.orElse(null);
    }*/

    public RaidBattleDB findRaidBattleDb(int area, int episode, int chapter, int stage, int difficulty) {
        String[] list = new String[]{String.valueOf(area), String.valueOf(episode), String.valueOf(chapter), String.valueOf(stage), String.valueOf(difficulty)};
        String stageKey = createStageRedisKey(list);
       /* RaidBattleDB raidBattleDB = new RaidBattleDB();
        raidBattleDB.setArea(area);
        raidBattleDB.setEpisode(episode);
        raidBattleDB.setChapter(chapter);
        raidBattleDB.setStage(stage);
        Optional<RaidBattleDB> op = findByIdInMap(raidBattleDB, stageKey);
        return op.orElse(null);*/
        Query query = new Query(Criteria.where("area").is(area).and("episode").is(episode).and("chapter").is(chapter).and("stage").is(stage));
        RaidBattleDB result = this.findByIdInMap(query, stageKey, RaidBattleDB.class);
        return result;
    }

    public RaidBattleDB findRaidBattleDb(String stageId) {
        /*RaidBattleDB raidBattleDB = new RaidBattleDB();
        raidBattleDB.setStageId(stageId);
        Optional<RaidBattleDB> op = findByIdInMap(raidBattleDB, stageId);
        return op.orElse(null);*/
        Query query = new Query(Criteria.where("stageId").is(stageId));
        RaidBattleDB result = this.findByIdInMap(query, stageId, RaidBattleDB.class);
        return result;
    }

    public RaidBattleInstance getRaidBattle(int area, int episode, int chapter, int stage, int difficulty) {
        String[] list = new String[]{String.valueOf(area), String.valueOf(episode), String.valueOf(chapter), String.valueOf(stage), String.valueOf(difficulty)};
        String stageKey = createStageRedisKey(list);
        RaidBattleDB result = findRaidBattleDb(stageKey);
        if (result == null) {
            return null;
        }
        RaidBattleInstance raidBattle = new RaidBattleInstance();
        BeanUtils.copyProperties(result, raidBattle);
        result.getEnemyList().forEach(enemiesDB -> {
            RaidBattleEnemy enemy = new RaidBattleEnemy();
            BeanUtils.copyProperties(enemiesDB, enemy);
            raidBattle.getEnemies().add(enemy);
        });
        long now = System.currentTimeMillis();
        raidBattle.setExpireTimestamp(now + result.getLimitTime());
        raidBattle.setRemainTime(result.getRemainTime());
        return raidBattle;
    }
}
