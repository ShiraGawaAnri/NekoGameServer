package com.nekonade.dao.db.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;

//背包，这里用于模拟代码，具体实际应用根据自己的需求完善。


@Getter
@Setter
public class Inventory implements Cloneable {

    //道具包
    private ConcurrentHashMap<String, Item> itemMap = new ConcurrentHashMap<>();

    @Override
    public Inventory clone() {
        Inventory obj = null;
        try {
            obj = (Inventory) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
