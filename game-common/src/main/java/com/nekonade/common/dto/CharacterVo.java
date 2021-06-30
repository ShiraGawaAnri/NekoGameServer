package com.nekonade.common.dto;

import com.nekonade.common.basePojo.BaseCharacter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;


/**
 * @ClassName: CharacterDTO
 * @Author: Lily
 * @Description: 角色展示类，用于非战斗下的查看，编辑操作
 * @Date: 2021/6/28
 * @Version: 1.0
 */
@Getter
@Setter
public class CharacterVo extends BaseCharacter implements Cloneable {


    private Boolean isNew = false;

    @Override
    public String toString() {
        return "CharacterVo{" +
                "isNew=" + isNew +
                ", characterId='" + characterId + '\'' +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", baseHp=" + baseHp +
                ", baseAtk=" + baseAtk +
                ", baseDef=" + baseDef +
                ", type=" + type +
                ", skills=" + skills +
                '}';
    }
}
