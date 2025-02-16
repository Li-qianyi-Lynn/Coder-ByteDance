package com.coder.mall.order.model.dto;

import java.util.List;

import lombok.Data;

@Data
public class PageResult<T> {
    private List<T> content;      // 当前页数据
    private int pageNum;          // 当前页码
    private int pageSize;         // 每页大小
    private long total;           // 总记录数
    private int totalPages;       // 总页数
    
    public PageResult(List<T> content, int pageNum, int pageSize, long total) {
        this.content = content;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }
} 