package com.example.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("pms_product_image")
public class ProductImage extends BaseEntity {

    private Long productId;

    private String imageUrl;

    private Integer imageType;

    private Integer sort;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getImageType() {
        return imageType;
    }

    public void setImageType(Integer imageType) {
        this.imageType = imageType;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
