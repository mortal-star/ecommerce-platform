package com.example.ecommerce.vo;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.ProductImage;
import com.example.ecommerce.entity.ProductSpec;

import java.util.List;

public class ProductDetailVO {

    private Product product;

    private List<ProductImage> images;

    private List<ProductSpec> specs;

    private Boolean favorite;

    public ProductDetailVO(Product product, List<ProductImage> images, List<ProductSpec> specs, Boolean favorite) {
        this.product = product;
        this.images = images;
        this.specs = specs;
        this.favorite = favorite;
    }

    public Product getProduct() {
        return product;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public List<ProductSpec> getSpecs() {
        return specs;
    }

    public Boolean getFavorite() {
        return favorite;
    }
}
