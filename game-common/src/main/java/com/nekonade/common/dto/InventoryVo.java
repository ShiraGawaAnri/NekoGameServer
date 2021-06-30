package com.nekonade.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class InventoryVo {
    //武器包
    protected ConcurrentHashMap<String, WeaponDTO> weaponMap = new ConcurrentHashMap<>();
    //道具包
    protected ConcurrentHashMap<String, ItemDTO> itemMap = new ConcurrentHashMap<>();
}
