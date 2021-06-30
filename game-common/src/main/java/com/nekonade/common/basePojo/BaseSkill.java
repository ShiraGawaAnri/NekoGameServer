package com.nekonade.common.basePojo;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nekonade.common.constcollections.EnumCollections;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * @ClassName: BaseSkill
 * @Author: Lily
 * @Description: 技能基础类
 * @Date: 2021/6/27
 * @Version: 1.0
 */

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "_classType")
public abstract class BaseSkill {

    @Field("_sid")
    private String id;

    @Field("_skillId")
    private String skillId;

    private String name;

    //冷却回合
    private Integer defaultRecast;

    //技能倍率
    private Float damageRatio;

    //范围
    private Integer range;

    //不定参数
    private Float value1;

    private Float value2;

    private Float value3;

    private Float value4;

    private List<? extends BaseSkillEffect> effects;

    //private List<BaseSkillStatus> statuses;

    private EnumCollections.DataBaseMapper.SkillProp prop;

    private EnumCollections.DataBaseMapper.SkillType type;
}
