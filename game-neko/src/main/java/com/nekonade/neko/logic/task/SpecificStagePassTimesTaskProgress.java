package com.nekonade.neko.logic.task;

import com.mongodb.lang.NonNull;
import com.nekonade.dao.db.entity.Task;
import com.nekonade.dao.db.entity.data.task.SpecificStagePassTimesTask;


//通关某个关卡进度值管理
public class SpecificStagePassTimesTaskProgress implements ITaskProgress{

    @Override
    public boolean updateProgress(@NonNull Task task, Object data) {
        SpecificStagePassTimesTask taskEntity = (SpecificStagePassTimesTask) task.getTaskEntity();
        StagePassTimesProgressEntity entity = (StagePassTimesProgressEntity) data;
        StagePassTimesProgressEntity value = (StagePassTimesProgressEntity) task.getValue();
        String stageId = entity.getStageId();
        if(!taskEntity.getStageId().equals(stageId)){
            return false;
        }
        if(value == null){
            value = new StagePassTimesProgressEntity();
            value.setStageId(stageId);
        }
        value.addTime(entity.getTime());
        task.setValue(value);
        return true;
    }

    @Override
    public boolean isFinish(Task task) {
        SpecificStagePassTimesTask taskEntity = (SpecificStagePassTimesTask) task.getTaskEntity();
        StagePassTimesProgressEntity value = (StagePassTimesProgressEntity) task.getValue();
        return value.getTime() >= taskEntity.getQuota();
    }

    @Override
    public boolean checkCondition(Task task) {
        return false;
    }

    @Override
    public Object getProgressValue(Task task) {
        StagePassTimesProgressEntity value = (StagePassTimesProgressEntity) task.getValue();
        return value.getTime();
    }
}
