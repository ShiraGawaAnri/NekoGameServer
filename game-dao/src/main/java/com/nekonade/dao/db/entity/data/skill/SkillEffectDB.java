package com.nekonade.dao.db.entity.data.skill;

import com.nekonade.common.basePojo.BaseSkillEffect;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @ClassName: SkillEffectsDb
 * @Author: Lily
 * @Description: DB类, 技能效果属性模板
 * @Date: 2021/6/27
 * @Version: 1.0
 */
@Getter
@Setter
@Document("SkillEffectDB")
public class SkillEffectDB extends BaseSkillEffect {

    @Id
    private String id;

    @Indexed(unique = true,sparse = true)
    private String skillEffectId;

}
