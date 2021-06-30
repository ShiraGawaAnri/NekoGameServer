package com.nekonade.dao.db.entity.data;

import com.mongodb.lang.NonNull;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.dao.db.entity.Item;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document("ItemDB")
public class ItemDB implements Serializable {

    @Id
    private String itemId;

    @NonNull
    private String name;

    private Integer type = EnumCollections.DataBaseMapper.EnumNumber.Item_Type_Etc.getValue();//功能性区分 0 - 回复道具,  1 - 可使用道具 , 2 - 其他,10 - 回复道具但需要经过询问,11 - 可使用道具但需要经过询问

    private Integer category = EnumCollections.DataBaseMapper.EnumNumber.Item_Category_Common.getValue();//普通道具，活动道具等某种共通属性区分

    private Stack stack = new Stack();

    private Sell sell = new Sell();

    private Flags flags = new Flags();

    private Delay delay = new Delay();

    private List<Item> containerContent = new ArrayList<>();

    /**
     * 特殊道具拥有uniqueId,此时Stack::getViewAsStack必定为true判定
     */
    /*@AutoIncKey(use = "redis",id = "PLAYER_ITEM_ID_INCR")
    @Indexed(unique = true,sparse = true)
    private Long uniqueId;*/

    @Getter
    @Setter
    public static class Stack implements Serializable{

        private boolean viewAsStack = true;//是否允许堆叠显示

        //道具必然允许堆叠,这里只设定最大允许堆叠数
        private Long maxAmount = 999999L;//

    }

    @Getter
    @Setter
    public static class Sell implements Serializable{

        private boolean sellable = false;

        private long prize;
    }

    @Getter
    @Setter
    public static class Flags implements Serializable{

        private boolean autoUse;//获取时立刻使用，多数用于从邮件或奖励

        private boolean container;

        private boolean uniqueId;

        private boolean noConsume;
    }

    @Getter
    @Setter
    public static class Delay implements Serializable{

        private int type = EnumCollections.DataBaseMapper.EnumNumber.Item_Delay_Type_Unable.getValue();// -1 没有使用间隔,0 指定下列时间,1-7 每周x刷新

        private long duration;

    }
}
