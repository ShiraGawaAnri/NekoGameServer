package com.nekonade.common.redis;

import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;

public enum EnumRedisKey {
    SEQUENCE(null),//默认共用自增key
    SERVICE_INSTANCE(Duration.ofDays(7)),
    USER_ID_INCR(null), // UserId 自增key
    PLAYER_ITEM_ID_INCR(null),//玩家道具用的INCR
    SESSION_ID_INCR(null),
    USER_ACCOUNT(Duration.ofDays(7)), // 用户信息
    USER_NAME_REGISTER(Duration.ofDays(7)),//已被注册的用户名
    PLAYER_ID_INCR(null),// PlayerId 自增Key
    IM_ID_INCR(Duration.ofHours(1)),//聊天自增ID
    MAIL_ID_INCR(null),//邮件自增ID
    PLAYER_NICKNAME(Duration.ofSeconds(30)),
    PLAYERID_TO_NICKNAME(Duration.ofDays(7)),
    PLAYER_INFO(Duration.ofDays(7)),
    CONFIG_GLOBAL(null),
    ARENA(Duration.ofDays(7)),
    ITEM_DB(null),
    RAIDBATTLE_DB(null),
    RAIDBATTLE_EFFECT_DB(null),
    RAIDBATTLE_EFFECT_GROUP_DB(null),
    ENEMY_DB(null),
    REWARD_DB(null),
    CHARACTER_DB(null),
    CARD_DB(null),
    CARDSKILL_DB(null),
    GACHAPOOL_DB(null),
    //RAIDBATTLE_LIMIT_STAGEIDS(null),
    RAIDBATTLE_RESCUE_ALL(Duration.ofHours(3)),
    RAIDBATTLE_RESCUE_STAGEID(Duration.ofHours(3)),
    RAIDBATTLE_NOT_FOUND(Duration.ofSeconds(5)),//如果RaidBattle在数据库里也找不到，那设置5秒防止穿透缓存
    RAIDBATTLE_LIMIT_COUNTER(null),//特定RaidBattle的每个玩家的每日上限
    RAIDBATTLE_STAGEID_PLAYERID_TO_RAIDID(Duration.ofDays(1)),//RaidBattle的StageId_PlayerId - RaidId映射，同一副本一个玩家只能有一个

    RAIDBATTLE_RAIDID_DETAILS(Duration.ofMinutes(90)),//RaidBattle主体
    RAIDBATTLE_RAIDID_TO_SERVERID(Duration.ofMinutes(90)),//管理RaidBattle主要负责的服务器
    RAIDBATTLE_RAIDID_TO_SERVERID_BACKUP(Duration.ofMinutes(90)),//管理RaidBattle备份负责的服务器
    RAIDBATTLE_SAMETIME_SINGLE_LIMIT(Duration.ofDays(1)),//单人的
    RAIDBATTLE_SAMETIME_MULTI_LIMIT_SET(Duration.ofDays(1)),//管理每个玩家同时可拥有N个副本
    RAIDBATTLE_REWARD(Duration.ofDays(30)),//RaidBattle的报酬
    RAIDBATTLE_PLAYER_RANDOM_SET(Duration.ofMinutes(30)),//个人随机多人战临时列表
//    RAIDBATTLE_REWARD_SET(null)//每位玩家RaidBattle的报酬的列表
    COOLDOWN_DO_RECEIVE_MAILBOX_REWARD(Duration.ofSeconds(60)),
    COOLDOWN_DO_CLAIM_RAIDBATTLE_REWARD(Duration.ofSeconds(10)),
    TASK_DB(null),
    ;
    private final Duration timeout;// 此key的value的expire时间,如果为null，表示value永远不过期

    EnumRedisKey(Duration timeout) {
        this.timeout = timeout;
    }

    public String getKey(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("参数不能为空");
        }
        return this.name() + "_" + id;
    }

    public String getKey(String... ids) {
        if (ids.length == 0) {
            throw new IllegalArgumentException("参数不能为空");
        }
        return this.name() + "_" + String.join("_", Arrays.asList(ids));
    }

    public Duration getTimeout() {
        return timeout;
    }

    public String getKey() {
        return this.name();
    }

}
