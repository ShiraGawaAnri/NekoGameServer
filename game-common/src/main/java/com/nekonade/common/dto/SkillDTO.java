package com.nekonade.common.dto;


@Deprecated //需要重构
public class SkillDTO implements Cloneable{

    private String skillId;

    private String name;

    private String description;

    private int cost = 1;

    private int load = 100;

    private int coolDownTurn = 0;

    private int coolDownTime = 0;

    private int maxLevel = 0;

    //private ActiveSkillsDB activeSkillsDB;//只拥有1个Skill

    //用于动态修改Skill的参数
    private int value1;

    private int value2;

    private int value3;

    private int value4;

    //private List<RaidBattleEffectDTO> effects;
}
