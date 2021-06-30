package com.nekonade.dao.db.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Item implements Serializable {

    private String itemId;

    private Integer amount;

    private Long expired;

    private String uniqueId;

    private Long delay;
}
