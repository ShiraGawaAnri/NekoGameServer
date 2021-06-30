package com.nekonade.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页结果.
 *
 * @author Ryan
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PageResult<T> {

    /**
     * 页码，从1开始
     */
    private Integer pageNum = 1;

    /**
     * 页面大小
     */
    private Integer pageSize = 10;


    /**
     * 总数
     */
    private Long total = 0L;

    /**
     * 总页数
     */
    private Integer pages = 0;

    /**
     * 数据
     */
    private List<T> list = new ArrayList<>();

}