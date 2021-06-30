package com.nekonade.dao.db.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Stamina implements Cloneable {

    private Integer value = 20;

    private Integer maxValue = 1000;

    private Long preQueryTime = System.currentTimeMillis();

    private Long nextRecoverTime = 0L;

    private Long nextRecoverTimestamp = 0L;

    private Integer cutTime = 0;

    private double cutPercent = 0;

    @Override
    public Stamina clone() {
        Stamina obj = null;
        try {
            obj = (Stamina) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
