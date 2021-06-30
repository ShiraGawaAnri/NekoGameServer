package com.nekonade.common.basePojo;

import com.nekonade.common.constcollections.EnumCollections;

/**
 * @ClassName: BaseRaidBattleStatus
 * @Author: Lily
 * @Description: 战斗中SkillStatus的基础类
 * @Date: 2021/6/27
 * @Version: 1.0
 */
public abstract class BaseRaidBattleSkillStatus {

    private String skillStatusId;

    private EnumCollections.DataBaseMapper.SkillStatusProp prop;

    private EnumCollections.DataBaseMapper.SkillStatusType type;
}
