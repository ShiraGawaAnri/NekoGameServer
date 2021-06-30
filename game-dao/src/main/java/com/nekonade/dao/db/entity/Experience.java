package com.nekonade.dao.db.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Experience {

    Long exp = 0L;

    Long nextLevelExp = 100L;

    public void addExp(long exp){
        this.exp += exp;
    }
}
