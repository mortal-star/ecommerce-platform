package com.example.ecommerce.excel;

import com.alibaba.excel.annotation.ExcelProperty;

import java.math.BigDecimal;

public class ProductExcelModel {
    @ExcelProperty("分类ID")
    private Long categoryId;
    @ExcelProperty("商品名称")
    private String name;
    @ExcelProperty("副标题")
    private String subtitle;
    @ExcelProperty("主图")
    private String mainImage;
    @ExcelProperty("详情")
    private String detail;
    @ExcelProperty("价格")
    private BigDecimal price;
    @ExcelProperty("原价")
    private BigDecimal originalPrice;
    @ExcelProperty("库存")
    private Integer stock;
    @ExcelProperty("单位")
    private String unit;
    @ExcelProperty("状态")
    private Integer status;
    @ExcelProperty("排序")
    private Integer sort;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
