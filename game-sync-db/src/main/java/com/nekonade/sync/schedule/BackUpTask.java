package com.nekonade.sync.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.common.utils.JacksonUtils;
import com.nekonade.dao.daos.PlayerDao;
import com.nekonade.dao.db.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BackUpTask {

    //冷数据同步也有间隔
    private static final Map<String, Long> logMap = new ConcurrentHashMap<>();

    @Autowired
    private StringRedisTemplate redisTemplate;


//    @Override
//    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//        taskRegistrar.addTriggerTask(
//                //1.添加任务内容(Runnable)
//                () -> {
//                    try {
//                        long start = System.currentTimeMillis();
//                        String patternKey = EnumRedisKey.PLAYER_INFO.getKey()+":*";
//                        ScanOptions options = ScanOptions.scanOptions()
//                                //这里指定每次扫描key的数量(很多博客瞎说要指定Integer.MAX_VALUE，这样的话跟        keys有什么区别？)
//                                .count(10000)
//                                .match(patternKey).build();
//                        RedisSerializer<String> redisSerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
//                        Cursor cursor = (Cursor) redisTemplate.executeWithStickyConnection(redisConnection -> new ConvertingCursor<>(redisConnection.scan(options), redisSerializer::deserialize));
//                        List<String> result = new ArrayList<>();
//                        while(cursor.hasNext()){
//                            result.add(cursor.next().toString());
//                        }
//                        cursor.close();
//                        logger.info("scan扫描共耗时：{} ms key数量：{}",System.currentTimeMillis()-start,result.size());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                },
//                //2.设置执行周期(Trigger)
//                triggerContext -> {
//                    //2.3 返回执行周期(Date)
//                    return new CronTrigger("* * * * * ? *").nextExecutionTime(triggerContext);
//                }
//        );
//    }
    @Autowired
    private PlayerDao playerDao;

    @Scheduled(cron = "* 0 * * * ?")
    public void task() {
        try {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            long now = System.currentTimeMillis();
            String patternKey = EnumRedisKey.PLAYER_INFO.getKey() + "_" + "*";
//            ScanOptions options = ScanOptions.scanOptions()
//                    .count(10000)
//                    .match(patternKey).build();
//            RedisSerializer<String> redisSerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
//            Cursor cursor = (Cursor) redisTemplate.executeWithStickyConnection(redisConnection -> new ConvertingCursor<>(redisConnection.scan(options), redisSerializer::deserialize));
//            List<String> result = new ArrayList<>();
//            while(cursor.hasNext()){
//                result.add(cursor.next().toString());
//            }
//            cursor.close();
//            logger.info("scan扫描共耗时：{} ms key数量：{}",System.currentTimeMillis()-start,result.size());
            Set<String> scan = scan(patternKey);
            if (scan != null) {
//                System.out.println(Arrays.toString(scan.toArray()));
                Long syncLogTime = System.currentTimeMillis() / 1000;
                for (String key : scan) {
                    Long expire = redisTemplate.getExpire(key);
                    //10分钟
                    if (expire != null && expire != -1 && Duration.ofDays(7).toSeconds() - expire >= 600) {
                        Long syncTimestamp = logMap.get(key);
                        if (syncTimestamp != null) {
                            if (syncLogTime - syncTimestamp <= 3600) {
                                continue;
                            }
                        }
                        String value = redisTemplate.opsForValue().get(key);
                        try {
                            //Player player = JSON.parseObject(value, Player.class);
                            Player player = JacksonUtils.parseObjectV2(value,Player.class);
                            expire = redisTemplate.getExpire(key);
                            if (player != null && expire != null && expire != -1 && Duration.ofDays(7).toMillis() - expire >= 600) {
                                logger.info("同步 player {} 至 db", key);
                                logMap.put(key, syncLogTime);
                                playerDao.saveOrUpdateToDB(player);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public Set<String> scan(String matchKey) {
        try {
            return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
                Set<String> keysTmp = new HashSet<>();
                Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match(matchKey).count(100).build());
                while (cursor.hasNext()) {
                    keysTmp.add(new String(cursor.next()));
                }
                if (!cursor.isClosed()) {
                    try {
                        cursor.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return keysTmp;
            });
        } catch (Exception e) {
            return null;
        }
    }
}
