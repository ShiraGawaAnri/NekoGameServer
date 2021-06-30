package com.nekonade.neko.logic.task;

import lombok.Getter;

public class TaskEnumCollections {

    @Getter
    public enum EnumTaskType {
        DayFirstLogin(1, new AccumulationTaskProgress(), "每日首次登录", Integer.class),
        ConsumeGold(2, new AccumulationTaskProgress(), "消耗x金币", Integer.class),
        ConsumeDiamond(3, new AccumulationTaskProgress(), "消耗x钻石", Integer.class),
        StagePassTimes(5, new SpecificStagePassTimesTaskProgress(), "通关某个关卡多少次", StagePassTimesProgressEntity.class),
        ;
        private final int type;
        private final ITaskProgress taskProgress;
        private final String desc;
        private final Class<?> initEntityClazz;

        EnumTaskType(int type, ITaskProgress taskProgress, String desc, Class<?> initEntityClazz) {
            this.type = type;
            this.taskProgress = taskProgress;
            this.desc = desc;
            this.initEntityClazz = initEntityClazz;
        }


    }
}
