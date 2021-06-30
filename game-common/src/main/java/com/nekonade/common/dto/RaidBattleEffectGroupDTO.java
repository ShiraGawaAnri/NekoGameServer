package com.nekonade.common.dto;


import lombok.Getter;
import lombok.Setter;

@Deprecated //需要重构
@Getter
@Setter
public class RaidBattleEffectGroupDTO {

    private String effectGroupId;

    private int groupOverlapping = 1;//是否允许叠加 0:取最大值 1:允许

    private double groupMaxStackValue = 50.0D;//同一组buff/debuff时,最大上限

    private String description;
}
