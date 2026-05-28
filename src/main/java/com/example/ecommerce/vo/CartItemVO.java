package com.example.ecommerce.vo;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.ProductSpec;

import java.math.BigDecimal;

public class CartItemVO {
    private Cart cart;
    private Product product;
    private ProductSpec spec;
    private BigDecimal itemAmount;

    public CartItemVO(Cart cart, Product product, ProductSpec spec, BigDecimal itemAmount) {
        this.cart = cart;
        this.product = product;
        this.spec = spec;
        this.itemAmount = itemAmount;
    }

    public Cart getCart() { return cart; }
    public Product getProduct() { return product; }
    public ProductSpec getSpec() { return spec; }
    public BigDecimal getItemAmount() { return itemAmount; }
}
