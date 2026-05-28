package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotNull;

public class CategorySortDTO {

    @NotNull(message = "分类ID不能为空")
    private Long id;

    @NotNull(message = "排序值不能为空")
    private Integer sort;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
