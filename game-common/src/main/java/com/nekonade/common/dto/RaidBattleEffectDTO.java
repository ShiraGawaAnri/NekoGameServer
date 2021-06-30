package com.nekonade.common.dto;

import lombok.Getter;
import lombok.Setter;



@Deprecated //需要重构
@Getter
@Setter
public class RaidBattleEffectDTO {

    private String effectId;

    private int effectProp;//0 : buff 1: debuff 2:field(All)

    private int effectType;//0:undefined 1.dot 2:support 3:heal 4:revive 5:special

    private RaidBattleEffectGroupDTO effectGroup;//表示同一组buff/debuff时,方便设计累计上限 0:common

    private int effectStack = 0;

    private int effectMaxStack = 0;//限制此effectId的最大叠层数 假设数值为5,则最大为1001_5 即第1+5层

    private int effectiveSecond = -1;//该buff/debuff有效时间 -1则代表使用其他判定

    private int effectiveTurn = -1;//该buff/debuff有效回合 可分别独立计算 -1则代表使用其他判定 两者皆为-1时表示永续

    private int dispelByOther = 1;//该buff/debuff允许被普通驱散技能驱散

    private double value1;//用于公式计算

    private double value2;

    private double value3;

    private double value4;

    private String description;
}
