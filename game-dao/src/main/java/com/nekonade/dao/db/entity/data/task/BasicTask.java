package com.nekonade.dao.db.entity.data.task;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;




@Getter
@Setter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "_classType"
        )
@JsonSubTypes({
        @JsonSubTypes.Type(value = DayFirstLoginTask.class, name = "1"),
        @JsonSubTypes.Type(value = ConsumeGoldTask.class, name = "2"),
        @JsonSubTypes.Type(value = SpecificStagePassTimesTask.class, name = "5"),
})
public abstract class BasicTask {

    public abstract boolean finishCheck();

    public abstract boolean rewriteCheckFinish();

    public abstract boolean checkParam();

    public abstract Object taskQuota();


}
