package com.nekonade.common.basePojo;

import com.nekonade.common.constcollections.EnumCollections;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: BaseRaidBattleCharacter
 * @Author: Lily
 * @Description: 战斗中角色的基础类
 * @Date: 2021/6/27
 * @Version: 1.0
 */
public abstract class BaseRaidBattleCharacter extends BaseCharacter{

    @Getter
    @Setter
    protected Integer gid;

    @Getter
    @Setter
    protected EnumCollections.DataBaseMapper.CharacterProp prop;

    @Getter
    @Setter
    protected Integer pos;

    @Getter
    @Setter
    protected volatile Long hp;

    @Getter
    @Setter
    protected volatile Long maxHp;

    @Getter
    @Setter
    protected volatile Integer atk;

    @Getter
    @Setter
    protected volatile Integer def;

    protected boolean alive;
    public boolean isAlive() {
        return hp > 0;
    }

    @Getter
    @Setter
    private Map<String,? extends BaseRaidBattleSkillStatus> buffs = new ConcurrentHashMap<>();

    /**
     * 接收伤害，如果伤害溢出,则返回溢出的值
     * @param damage 所受到的伤害值
     * @return 溢出的伤害值
     */
    public long takeDamage(long damage){
        long overflow = 0;
        long forecastHp = hp - damage;
        if(forecastHp < 0){
            overflow = Math.abs(forecastHp);
            forecastHp = 0;
        }
        hp = forecastHp;
        return overflow;
    }
}
