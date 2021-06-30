package com.nekonade.dao.db.entity;


import com.nekonade.common.dto.ItemDTO;
import com.nekonade.dao.seq.AutoIncKey;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document("MailBox")
public class MailBox {

    @Id
    @AutoIncKey
    private long id;

    @Indexed
    private Long receiverId;

    private Long senderId;

    private String senderName;

    private String title;

    private String content;

    private List<ItemDTO> gifts = new ArrayList<>();

    private Long timestamp = System.currentTimeMillis();

    private Long expired;

    private Integer received = 0;

    private Integer type = 0;
}
