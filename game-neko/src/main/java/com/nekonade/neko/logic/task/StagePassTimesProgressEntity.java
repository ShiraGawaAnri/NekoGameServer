package com.nekonade.neko.logic.task;

import lombok.Data;

@Data
public class StagePassTimesProgressEntity {

    private String stageId;

    private int time;

    private Long lastTimestamp;

    public void addTime(int time){
        this.time += time;
        this.lastTimestamp = System.currentTimeMillis();
    }
}
