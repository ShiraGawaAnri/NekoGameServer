package com.nekonade.common.basePojo;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName: BaseRaidBattle
 * @Author: Lily
 * @Description: 副本基础类
 * @Date: 2021/6/28
 * @Version: 1.0
 */
@Getter
@Setter
public abstract class BaseRaidBattle {

    protected long ownerPlayerId;//创建者/拥有者的PlayerId

    protected String stageId;//通过某种方式生成的stageId

    protected Boolean multiRaid;//是否为多人战

    protected Integer area;

    protected Integer episode;

    protected Integer chapter;

    protected Integer stage;

    protected Integer difficulty;

    protected Integer EntryCostStaminaPoint;//入场前消耗的Stamina

    protected Map<String, Integer> EntryCostItemMap = new HashMap<>();//入场前消耗的道具

    protected Integer maxPlayers = 30;//最大玩家数

    protected Long limitCounter = 0L;//不限制次数

    protected Integer limitCounterRefreshType;//如果限制次数，那么选择哪种刷新方式

    protected Boolean active = true;//是否激活该关卡,即允许查阅,建立战斗

    protected Boolean finish;//战斗是否结束了

    protected Boolean failed;//战斗是否失败了

    protected Long limitTime = 5400 * 1000L;//一场战斗的限制时间

    protected Long expireTimestamp = -1L;//战斗过期时间戳 毫秒，影响在Redis中的存在时间,也返回给客户端

    protected Long remainTime = 5400 * 1000L;//战斗剩余时间 毫秒
}
