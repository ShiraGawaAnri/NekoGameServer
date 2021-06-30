package com.nekonade.common.dto.raidbattle;

import com.nekonade.common.basePojo.BaseRaidBattleCharacter;
import com.nekonade.common.constcollections.EnumCollections;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName: RaidBattleEnemy
 * @Author: Lily
 * @Description: 战斗时敌人类
 * @Date: 2021/6/28
 * @Version: 1.0
 */

@Getter
@Setter
public class RaidBattleEnemy extends BaseRaidBattleCharacter implements Cloneable{

    public RaidBattleEnemy() {
        this.prop = EnumCollections.DataBaseMapper.CharacterProp.Enemy;
    }

    private Integer key = 0;

}
