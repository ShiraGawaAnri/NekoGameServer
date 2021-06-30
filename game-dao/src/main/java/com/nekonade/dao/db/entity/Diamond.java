package com.nekonade.dao.db.entity;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Diamond {

    private int amount;

    public void addAmount(int amount){
        this.amount += amount;
    }

    public void subAmount(int amount){
        this.amount -= amount;
    }
}
