package com.nekonade.dao.db.entity.data.rbeffect;


import com.nekonade.common.constcollections.EnumCollections;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 * 描述由Card引起的Buff和Debuff
 *
 */
@Getter
@Setter
public class RaidBattleDirectiveEffect implements Serializable {

    private String targetTo = EnumCollections.DataBaseMapper.EnumString.RaidBattle_Effect_TargetTo_Character.getValue();//player or enemy

    private int posType;//适用的位置类型 0:自身 1:指定对象 2:指定范围 3:指定对象及其范围

    private int pos;//位置下标

    private int range;//当posType = 2,3时有效

    private double rate = 100.0d;//命中/触发率

    private boolean dependSkillHit = true;//是否依赖技能命中才能给予

    private RaidBattleEffectDB effect;//效果
}
