package com.nekonade.common.basePojo;

import com.nekonade.common.constcollections.EnumCollections;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

/**
 * @ClassName: BaseCharacter
 * @Author: Lily
 * @Description: 基础角色类(我方, 敌人)
 * @Date: 2021/6/27
 * @Version: 1.0
 */


@Getter
@Setter
@ToString
public abstract class BaseCharacter {

    @Field("_cid")
    protected String id;

    @Field("_characterId")
    protected String characterId;

    //一般在客户端国际化实现,这里只是方便后台查阅
    protected String name;

    protected Integer level;

    protected Long baseHp;

    protected Integer baseAtk;

    protected Integer baseDef;

    protected EnumCollections.DataBaseMapper.CharacterType type;

    protected Map<String,? extends BaseSkill> skills;

}
