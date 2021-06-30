package com.nekonade.network.message.manager;


import com.nekonade.dao.db.entity.Task;
import lombok.Getter;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ConcurrentHashMap;

public class TaskManager {

    private final PlayerManager playerManager;

    private final ApplicationContext context;

    @Getter
    private final ConcurrentHashMap<String,Task> tasks;

    public TaskManager(PlayerManager playerManager) {
        this.context = playerManager.getContext();
        this.playerManager = playerManager;
        this.tasks = playerManager.getPlayer().getTasks();
    }

    public Task removeTask(String taskId){
        return tasks.remove(taskId);
    }

    public Task addTask(Task task){
        return tasks.putIfAbsent(task.getTaskId(),task);
    }

    public boolean isTaskInited(String taskId){
        return tasks.get(taskId) != null;
    }

    /*public boolean isInitTask() {
        return task.getTaskId() != null;
    }

    public void receiveTask(String taskId) {
        task.setTaskId(taskId);
    }

    public void addValue(int value) {

        int newValue = (task.getValue() == null ? 0 : (int) task.getValue()) + value;
        task.setValue(newValue);
    }

    public void setValue(String value) {

        task.setValue(value);
    }

    public int getTaskIntValue() {
        return task.getValue() == null ? 0 : (int) task.getValue();
    }

    //获取String类型的进度值
    public String getTaskStringValue() {
        return task.getValue() == null ? null : (String) task.getValue();
    }

    public void addManyIntValue(String key, int value) {
        Object oldValue = task.getManyValue().get(key);
        int newValue = value;
        if (oldValue != null) {
            newValue += (int) oldValue;
        }
        task.getManyValue().put(key, newValue);
    }

    public int getManayIntValue(String key) {
        Object objValue = task.getManyValue().get(key);
        int value = objValue == null ? 0 : (int) objValue;
        return value;
    }

    public String getNowReceiveTaskId() {
        return task.getTaskId();
    }
*/
}
