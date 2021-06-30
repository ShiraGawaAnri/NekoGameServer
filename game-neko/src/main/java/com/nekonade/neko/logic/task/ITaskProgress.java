package com.nekonade.neko.logic.task;

import com.nekonade.dao.db.entity.Task;

public abstract interface ITaskProgress {
    //更新任务进度的接口,taskDataConfig是任务的配置数据，data是任务进度变化的进度，因为这个值的类型是多个的，有的是int
    //有的是String，有的是list等，所以使用Object类

    boolean updateProgress(Task task, Object data);

    boolean isFinish(Task task);//判断任务的进度是否已完成，表示可以领取任务奖励

    boolean checkCondition(Task task);

    Object getProgressValue(Task task);//获取任务进行的进度值

}
