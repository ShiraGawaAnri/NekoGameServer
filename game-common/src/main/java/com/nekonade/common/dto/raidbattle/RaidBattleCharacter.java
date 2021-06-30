package com.nekonade.common.dto.raidbattle;

import com.nekonade.common.basePojo.BaseRaidBattleCharacter;
import com.nekonade.common.constcollections.EnumCollections;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName: RaidBattleCharacter
 * @Author: Lily
 * @Description: 战斗时的角色类, 含有大量临时变量, 回传客户端时应创建对应的VO类
 * @Date: 2021/6/28
 * @Version: 1.0
 */
@Getter
@Setter
public class RaidBattleCharacter extends BaseRaidBattleCharacter implements Cloneable {
    public RaidBattleCharacter() {
        this.prop = EnumCollections.DataBaseMapper.CharacterProp.Player;
    }
}
