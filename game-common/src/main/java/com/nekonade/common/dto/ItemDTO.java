package com.nekonade.common.dto;

import lombok.Getter;
import lombok.Setter;

@Deprecated //需要重构
@Getter
@Setter
public class ItemDTO {

    private String itemId;

    private Integer amount;

    private Integer type;

    private Integer category;

    private Long expired;

}
