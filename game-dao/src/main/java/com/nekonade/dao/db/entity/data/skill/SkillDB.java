package com.nekonade.dao.db.entity.data.skill;

import com.nekonade.common.basePojo.BaseSkill;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @ClassName: SkillsDb
 * @Author: Lily
 * @Description: DB雷,技能属性模板
 * @Date: 2021/6/27
 * @Version: 1.0
 */

@Getter
@Setter
@Document("SkillDB")
public class SkillDB extends BaseSkill implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true,sparse = true)
    private String skillId;
}
