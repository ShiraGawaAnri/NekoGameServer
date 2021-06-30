package com.nekonade.common.basePojo;

import com.nekonade.common.constcollections.EnumCollections;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: BaseSkillStatus
 * @Author: Lily
 * @Description: 技能基础增益(Buff)/减益(Debuff)类
 * @Date: 2021/6/27 15:21
 * @Version: 1.0
 */

@Getter
@Setter
public abstract class BaseSkillStatus {

    @Field("_ssid")
    private String id;

    @Field("_skillStatusId")
    private String skillStatusId;

    private EnumCollections.DataBaseMapper.SkillStatusProp prop;

    private EnumCollections.DataBaseMapper.SkillStatusType type;


    @Getter
    @Setter
    public static class DamageFlags implements Serializable {

        private boolean noDamage; //无伤害

        private boolean splash; //范围技

        private boolean splashSplit; //范围分摊技

        private boolean critical; //必定暴击

        private boolean ignoreDefense; //无视防御

        private boolean ignoreElement; //无视属性

        private boolean ignoreFlee; //无视回避

        private boolean ignoreAtk; //无视攻击Buff加成

    }

    @Getter
    @Setter
    public static class Flags implements Serializable{

        private boolean targetTrap;//对陷阱有效

        private boolean targetHidden;//对隐身/隐匿怪物有效

        private boolean ignoreHovering;//无视浮空 - 对空中有效

        private boolean ignoreUnderground;//无视地下 - 对地下有效

        private boolean noTargetSelf;//不能够选取自己

        private boolean targetSelf;//强制选择自己,以自己为目标发动

        private boolean ignoreStasis;//无视停滞

        private boolean trap;//此技能是个陷阱类型的技能

        private boolean quest;//是任务技能

        private boolean npc;//是npc/monster的专属技能

        //团队系列
        private boolean guildOnly;

        private boolean partyOnly;

        //演奏系列
        private boolean ensemble;

        private boolean song;

        private boolean allowWhenPerforming;

        public static Flags build() {
            return new Flags();
        }
    }

    @Getter
    @Setter
    public static class Range implements Serializable{

        private int level = 1;

        private int size = 0;

        public static Range build() {
            return new Range();
        }
    }

    @Getter
    @Setter
    public static class HitCount implements Serializable{

        private int level = 1;

        private int count = 0;

        public static HitCount build() {
            return new HitCount();
        }
    }

    @Getter
    @Setter
    public static class Element implements Serializable{

        private int level = 1;

        private Object Element;

        public static Element build() {
            return new Element();
        }

    }

    @Getter
    @Setter
    public static class SplashArea implements Serializable{

        private int level = 1;

        private int area = 0;

        public static SplashArea build() {
            return new SplashArea();
        }

    }

    @Getter
    @Setter
    public static class ActiveInstance implements Serializable{

        private int level = 1;

        private int max = 0;

        public static ActiveInstance build() {
            return new ActiveInstance();
        }

    }

    @Getter
    @Setter
    public static class KnockBack implements Serializable{

        private int level = 1;

        private int amount = 0;

        public static KnockBack build() {
            return new KnockBack();
        }

    }

    @Getter
    @Setter
    public static class Requires implements Serializable{

        private List<Requires> requires = new ArrayList<>();

        public static Requires build() {
            return new Requires();
        }
    }

    @Getter
    @Setter
    public static class Status implements Serializable{


        public static Status build() {
            return new Status();
        }
    }
}
