package com.nekonade.common.basePojo;


import com.nekonade.common.dto.RaidBattleEffectDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;


@Deprecated //旧版,参考用
@Getter
@Setter
public abstract class RaidBattleCharacterBackup {

    private String gid;

    private Integer level = 1;

    private Integer pos;

    private volatile Long hp = 1L;

    private volatile Long maxHp = 1L;

    private volatile Double speed = 1d;

    private volatile Integer guard = 1;

    private volatile Integer atk = 1;

    private volatile Integer def = 1;

    private volatile Integer alive = 1;

    private volatile Double maxSpeed = 1d;

    private volatile Integer maxGuard = 1;

    private volatile Integer maxAtk = 1;

    private volatile Integer maxDef = 1;

    private ConcurrentHashMap<String, RaidBattleEffectDTO> buffs = new ConcurrentHashMap<>();

    public long takeDamage(long damage){
        final long value = this.hp - damage;
        this.hp = Math.max(0, value);
        return damage - (this.hp - value);
    }

    public abstract int sourceType();
}
