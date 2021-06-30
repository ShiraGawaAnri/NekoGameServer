package com.nekonade.common.dto;

import lombok.Getter;
import lombok.Setter;

@Deprecated //需要重构
@Getter
@Setter
public class WeaponDTO {

    protected String id;

    protected Boolean enable = true;

    protected Integer type;

    protected Integer category;

    protected String equippedByHero;

    protected Boolean locked = false;

}
