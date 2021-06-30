package com.nekonade.dao.db.entity.data.rbeffect;

import com.nekonade.common.constcollections.EnumCollections;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Getter
@Setter
@Document("RaidBattleEffectGroupDB")
public class RaidBattleEffectGroupDB implements Serializable {

    @Id
    private String effectGroupId;

    private int groupOverlapping = EnumCollections.DataBaseMapper.EnumNumber.RaidBattle_EffectGroups_Overlapping.getValue();//是否允许叠加 0:取最大值 1:允许

    private double groupMaxStackValue = 50.0D;//同一组buff/debuff时,最大上限

//    @DBRef
//    private RaidBattleEffectGroupType groupType;

    private String description;
}
