package com.example.ecommerce.vo;

import com.example.ecommerce.entity.Favorite;
import com.example.ecommerce.entity.Product;

public class FavoriteVO {

    private Favorite favorite;

    private Product product;

    public FavoriteVO(Favorite favorite, Product product) {
        this.favorite = favorite;
        this.product = product;
    }

    public Favorite getFavorite() {
        return favorite;
    }

    public Product getProduct() {
        return product;
    }
}
