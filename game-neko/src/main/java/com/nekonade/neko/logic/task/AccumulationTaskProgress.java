package com.nekonade.neko.logic.task;

import com.nekonade.dao.db.entity.Task;
import com.nekonade.dao.db.entity.data.task.BasicTask;

//数值累计型进度值管理
public class AccumulationTaskProgress implements ITaskProgress {

    @Override
    public boolean updateProgress(Task task, Object data) {
        int value = task.getValue() == null ? 0 : (int)task.getValue();
        task.setValue(value+(int) (data));
        return true;
    }

    @Override
    public boolean isFinish(Task task) {
        BasicTask taskEntity = task.getTaskEntity();
        //如果重写了,则用重写的方法判断
        if(taskEntity.rewriteCheckFinish()){
            return taskEntity.finishCheck();
        }
        int value = task.getValue() == null ? 0 : (int)task.getValue();
        Object taskQuota = taskEntity.taskQuota();
        return value >= (int)taskQuota;
    }

    @Override
    public boolean checkCondition(Task task) {
        BasicTask taskEntity = task.getTaskEntity();

        return false;
    }

    @Override
    public Object getProgressValue(Task task) {
        BasicTask taskEntity = task.getTaskEntity();
        return taskEntity.taskQuota();
    }
}
