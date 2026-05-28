package com.example.ecommerce.vo;

import java.util.List;

public class PageVO<T> {

    private List<T> records;

    private Long total;

    private Long pageNum;

    private Long pageSize;

    private Long pages;

    public PageVO(List<T> records, Long total, Long pageNum, Long pageSize, Long pages) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = pages;
    }

    public List<T> getRecords() {
        return records;
    }

    public Long getTotal() {
        return total;
    }

    public Long getPageNum() {
        return pageNum;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public Long getPages() {
        return pages;
    }
}
