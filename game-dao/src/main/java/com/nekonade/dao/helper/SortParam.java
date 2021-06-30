package com.nekonade.dao.helper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class SortParam {

    private Sort.Direction sortDirection = Sort.Direction.ASC;

    private String sortField = "_id";
}
