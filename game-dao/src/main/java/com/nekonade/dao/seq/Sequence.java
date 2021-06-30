package com.nekonade.dao.seq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "Sequence")
public class Sequence {

    @Id
    private String id;// 主键

    private String collName;// 集合名称

    private Long seqId;// 序列值

    // 省略getter、setter
}
