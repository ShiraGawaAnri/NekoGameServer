package com.nekonade.neko.service;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.spel.SpelExecutor;
import com.nekonade.common.utils.CalcCoolDownUtils;
import com.nekonade.common.utils.GameBeanUtils;
import com.nekonade.dao.daos.db.TaskDBDao;
import com.nekonade.dao.db.entity.Player;
import com.nekonade.dao.db.entity.Task;
import com.nekonade.dao.db.entity.data.task.BasicTask;
import com.nekonade.dao.db.entity.data.task.SpecificStagePassTimesTask;
import com.nekonade.dao.db.entity.data.task.TaskDB;
import com.nekonade.neko.logic.task.StagePassTimesProgressEntity;
import com.nekonade.neko.logic.task.TaskEnumCollections;
import com.nekonade.network.message.event.function.ConsumeGoldEvent;
import com.nekonade.network.message.event.function.EnterGameEvent;
import com.nekonade.network.message.event.function.StagePassEvent;
import com.nekonade.network.message.manager.PlayerManager;
import com.nekonade.network.message.manager.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CacheConfig(cacheManager = "caffeineCacheManager")
@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskDBDao tasksDbDao;

    @Autowired
    private SpelExecutor spelExecutor;


    @Cacheable(cacheNames = "findTaskIdsByType",key = "#taskType",sync = true)
    public List<String> findTaskIdsByType(TaskEnumCollections.EnumTaskType taskType){
        Map<String, TaskDB> tasksDBMap = tasksDbDao.findAllInMap();
        return tasksDBMap.entrySet().stream().filter(it->{
            TaskDB tasksDB = it.getValue();
            return tasksDB.getTaskType().equals(taskType.getType());
        }).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "findTaskIdsByStageId",key = "#stageId",sync = true)
    public List<String> findTaskIdsByStageId(String stageId){
        Map<String, TaskDB> tasksDBMap = tasksDbDao.findAllInMap();
        return tasksDBMap.entrySet().stream().filter(it->{
            TaskDB tasksDB = it.getValue();
            BasicTask taskEntity = tasksDB.getTaskEntity();
            if(taskEntity instanceof SpecificStagePassTimesTask){
                return ((SpecificStagePassTimesTask) taskEntity).getStageId().equals(stageId);
            }
            return false;
        }).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private List<Task> getTaskList(TaskManager taskManager, TaskEnumCollections.EnumTaskType targetTaskType){
        Map<String, Task> tasks = taskManager.getTasks();
        List<String> taskIds= findTaskIdsByType(targetTaskType);
        return taskIds.stream().filter(id->{
            Task task = tasks.get(id);
            return task != null;
        }).map(tasks::get).collect(Collectors.toList());
    }

    private List<Task> getNotClearTaskList(TaskManager taskManager, TaskEnumCollections.EnumTaskType targetTaskType){
        Map<String, Task> tasks = taskManager.getTasks();
        List<String> taskIds= findTaskIdsByType(targetTaskType);
        return taskIds.stream().filter(id->{
            Task task = tasks.get(id);
            return task != null && !task.isClear();
        }).map(tasks::get).collect(Collectors.toList());
    }

    private Map<String, Object> createDataMap(PlayerManager playerManager){
        LocalDateTime localDateTime = LocalDateTime.now();

        Map<String, Object> dataMap = new HashMap<>();
        Player player = GameBeanUtils.deepCopyByJsonV2(playerManager.getPlayer(), Player.class);
        dataMap.put("player",player);
        dataMap.put("now",System.currentTimeMillis());
        dataMap.put("level",player.getLevel());
        dataMap.put("characters",player.getCharacters());
        dataMap.put("inventory",player.getInventory());
        dataMap.put("hour",localDateTime.getHour());
        dataMap.put("minute",localDateTime.getMinute());
        dataMap.put("second",localDateTime.getSecond());
        return dataMap;
    }

    private boolean checkCondition(Task task,Map<String,Object> dataMap){
        String taskId = task.getTaskId();
        TaskDB tasksDb = tasksDbDao.findTasksDb(taskId);
        String condition = tasksDb.getCondition();
        boolean conditionFlag = false;
        if(condition != null){
            try{
                conditionFlag = spelExecutor.doneInSpringContext(dataMap,condition);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            conditionFlag = true;
        }
        return conditionFlag;
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    public void EnterGameInitAllTask(EnterGameEvent event) {
        // 进入游戏的时候，判断一下任务有没有实始化，没有初始化的，自动接收第一个任务
        long now = System.currentTimeMillis();
        TaskManager taskManager = event.getPlayerManager().getTaskManager();
        Map<String, Task> tasks = taskManager.getTasks();
        Map<String, TaskDB> tasksDBMap = tasksDbDao.findAllInMap();

        //移除不在任务DB中的任务 - 注意这tasks至少必须是ConcurrentHashmap
        for(String taskId:tasks.keySet()){
            if(!tasksDBMap.containsKey(taskId)){
                taskManager.removeTask(taskId);
            }
        }
        //添加新增的部分
        tasksDBMap.forEach((taskId, entity) -> {
            if(!tasks.containsKey(taskId)){
                Task task = new Task();
                BeanUtils.copyProperties(entity,task);
                taskManager.addTask(task);
            }
        });
        //初始化所有task的刷新时间
        tasks.forEach((taskId,task)->{
            TaskDB tasksDB = tasksDBMap.get(taskId);
            Integer refreshType = tasksDB.getRefreshType();
            Long refreshTime = task.getRefreshTime();
            if(refreshType != EnumCollections.DataBaseMapper.EnumNumber.No_Refresh.getValue()){
                if(refreshTime == null || now >= refreshTime){
                    long time = CalcCoolDownUtils.calcCoolDownTimestamp(refreshType);
                    task.setRefreshTime(now + time);
                    task.setClear(false);
                    //初始化值
                    task.setValue(null);
                }
            }else{
                task.setRefreshTime(null);
            }
            Object value = task.getValue();
            if(value == null){
                Integer taskType = task.getTaskType();
                try {
                    Class<?> initEntityClazz = getEnumTaskType(taskType).getInitEntityClazz();
                    if(Integer.class.isAssignableFrom(initEntityClazz)){
                        task.setValue(0);
                    }else if(Long.class.isAssignableFrom(initEntityClazz)){
                        task.setValue(0L);
                    }else if(String.class.isAssignableFrom(initEntityClazz)){
                        task.setValue("");
                    }else{
                        task.setValue(initEntityClazz.getDeclaredConstructor().newInstance());
                    }

                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    @EventListener
    public void LoginTask(EnterGameEvent event){
        PlayerManager playerManager = event.getPlayerManager();
        TaskManager taskManager = playerManager.getTaskManager();
        TaskEnumCollections.EnumTaskType targetTaskType = TaskEnumCollections.EnumTaskType.DayFirstLogin;
        List<Task> checkTaskList = getNotClearTaskList(taskManager, targetTaskType);
        Map<String, Object> dataMap = createDataMap(playerManager);
        checkTaskList.forEach(task->{
            boolean updateEffective = updateTaskProgress(targetTaskType, task, 1);
            if(updateEffective){
                boolean conditionFlag = checkCondition(task,dataMap);
                boolean finishFlag = checkTaskIsFinish(targetTaskType, task);
                if(conditionFlag && finishFlag){
                    task.setClear(true);
                }
            }
        });
    }


    @EventListener
    public void consumeGold(ConsumeGoldEvent event) {
        // 接收金币消耗事件
        PlayerManager playerManager = event.getPlayerManager();
        TaskManager taskManager = playerManager.getTaskManager();
        TaskEnumCollections.EnumTaskType targetTaskType = TaskEnumCollections.EnumTaskType.ConsumeGold;
        List<Task> checkTaskList = getNotClearTaskList(taskManager, targetTaskType);
        Map<String, Object> dataMap = createDataMap(playerManager);
        checkTaskList.forEach(task->{
            boolean updateEffective = updateTaskProgress(targetTaskType, task, event.getGold());
            if(updateEffective){
                boolean conditionFlag = checkCondition(task,dataMap);
                boolean finishFlag = checkTaskIsFinish(targetTaskType, task);
                if(conditionFlag && finishFlag){
                    task.setClear(true);
                }
            }
        });
    }

    @EventListener
    public void stagePass(StagePassEvent event){
        PlayerManager playerManager = event.getPlayerManager();
        TaskManager taskManager = playerManager.getTaskManager();
        TaskEnumCollections.EnumTaskType targetTaskType = TaskEnumCollections.EnumTaskType.StagePassTimes;
        List<Task> checkTaskList = getNotClearTaskList(taskManager, targetTaskType);
        StagePassTimesProgressEntity data = new StagePassTimesProgressEntity();
        data.setStageId(event.getStageId());
        data.setTime(event.getTime());
        Map<String, Object> dataMap = createDataMap(playerManager);
        checkTaskList.forEach(task->{
            boolean updateEffective = updateTaskProgress(targetTaskType, task, data);
            if(updateEffective){
                boolean conditionFlag = checkCondition(task,dataMap);
                boolean finishFlag = checkTaskIsFinish(targetTaskType, task);
                if(conditionFlag && finishFlag){
                    task.setClear(true);
                }
            }
        });
    }

    /*@EventListener
    public void passBlockPoint(PassBlockPointEvent event) {
        //通关事件影响多个任务类型的进度
        this.updateTaskProgress(event.getPlayerManager().getTaskManager(), TaskEnumCollections.EnumTaskType.PassBlockPoint, event.getPointId());
        this.updateTaskProgress(event.getPlayerManager().getTaskManager(), TaskEnumCollections.EnumTaskType.PassBlockPointTimes, event.getPointId());
    }*/

    private boolean updateTaskProgress(TaskEnumCollections.EnumTaskType taskType,Task task, Object value) {
        if (task.getTaskType() == taskType.getType()) {//如果事件更新的任务类型，与当前接受的任务类型一致，更新任务进度
            return taskType.getTaskProgress().updateProgress(task, value);
        }
        return false;
    }

    private boolean checkTaskIsFinish(TaskEnumCollections.EnumTaskType taskType,Task task) {
        if (task.getTaskType() == taskType.getType()) {//如果事件更新的任务类型，与当前接受的任务类型一致，更新任务进度
           return taskType.getTaskProgress().isFinish(task);
        }
        return false;
    }



    /*
    public GlobalConfig.TaskConfig getTaskDataConfig(String taskId) {
        // 根据taskId获取这个taskId对应的配置数据，这里直模拟返回一个
        return new GlobalConfig.TaskConfig();
    }*/

    public TaskEnumCollections.EnumTaskType getEnumTaskType(int taskType) {
        for (TaskEnumCollections.EnumTaskType enumTaskType : TaskEnumCollections.EnumTaskType.values()) {
            if (enumTaskType.getType() == taskType) {
                return enumTaskType;
            }
        }
        throw new IllegalArgumentException("任务类型不存在：" + taskType);
    }
}
