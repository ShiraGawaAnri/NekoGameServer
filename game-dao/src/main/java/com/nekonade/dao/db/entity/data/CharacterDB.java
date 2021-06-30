package com.nekonade.dao.db.entity.data;

import com.nekonade.common.basePojo.BaseCharacter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;


/**
 * @ClassName: CharactersDB
 * @Author: Lily
 * @Description: DB类，描述角色属性的模板
 * @Date: 2021/6/27
 * @Version: 1.0
 */

@Getter
@Setter
@Document("CharacterDB")
public class CharacterDB extends BaseCharacter implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true,sparse = true)
    private String characterId;
}
