package com.nekonade.dao.db.entity;

import com.nekonade.dao.db.entity.data.task.BasicTask;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Task {

    private String taskId;//当前接受的任务id

    private Object value;//当前的任务进度值

    private boolean clear;

    private Integer taskType;

    private BasicTask taskEntity;

    private Long refreshTime;

}
