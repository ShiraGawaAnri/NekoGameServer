package com.nekonade.dao.db.entity.config;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@Document(collection = "GlobalConfig")
public class GlobalConfig implements Serializable {

    @Id
    private long id = 1;

    private long version = 1001;

    private StaminaConfig staminaConfig = new StaminaConfig();

    private LevelConfig levelConfig = new LevelConfig();

    private DiamondConfig diamondConfig = new DiamondConfig();

    private CharacterConfig characterConfig = new CharacterConfig();

    @Getter
    @Setter
    public static class StaminaConfig implements Serializable{

        private int defaultStarterValue = 20;

        private int maxValue = 9999;

        private double staminaFactor = 0.3;

        private int eachLevelAddPoint = 25;

        private long recoverTime = 5 * 60 * 1000L;

        private int recoverValue = 1;

        public int CalcStaminaMaxValue(int playerLevel){
            return this.defaultStarterValue + this.eachLevelAddPoint * (playerLevel - 1);
        }
    }

    @Getter
    @Setter
    public static class LevelConfig implements Serializable{

        private int defaultStarterValue = 1;

        private int maxValue = 150;

        private int expRatio = 30;

        public long getNextLevelUpPoint(int level) {
            double v = expRatio * (Math.pow(level, 3.0) + 5 * level + 1) - 80;
            return (long) v;
        }

        private long getNextLevelUpNeedPoint(int level, Long exp) {
            return getNextLevelUpPoint(level) - exp;
        }
    }

    @Getter
    @Setter
    public static class DiamondConfig implements Serializable{

        private long maxValue = 9999999999L;
    }

    @Getter
    @Setter
    public static class CharacterConfig implements Serializable{

        private Map<String,StatusDataBase> statusDataBase = new HashMap<>();//素质方面设定


        @Getter
        @Setter
        public static class StatusDataBase {

            private String charaId;

            private double hpFactor = 1.0;

            private double hpMultiplicator = 1.0;

            private double atkFactor = 1.0;

            private double defFactor = 1.0;

            private double speedFactor = 1.0;
        }
    }

    public static class TaskConfig {
        public String taskId;//任务id
        public String preTaskId;//上一个任务的id
        public String nextTaskId;//下一个任务的id
        public int taskType; //任务类型
        public String param; //任务参数
        public String rewardId; //任务奖励id
    }
}
