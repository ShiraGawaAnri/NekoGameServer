package com.nekonade.common.basePojo;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @ClassName: BaseSkillEffect
 * @Author: Lily
 * @Description: 技能效果基础类,如造成回合推迟,退场,强制换人,死亡等立即效果
 * @Date: 2021/6/27
 * @Version: 1.0
 */
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "_classType")
public abstract class BaseSkillEffect {

    @Field("_seid")
    private String id;

    @Field("_skillEffectId")
    private String skillEffectId;
}
