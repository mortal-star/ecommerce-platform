package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CartSelectDTO {
    private List<Long> cartIds;
    @NotNull(message = "勾选状态不能为空")
    private Boolean selected;

    public List<Long> getCartIds() { return cartIds; }
    public void setCartIds(List<Long> cartIds) { this.cartIds = cartIds; }
    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }
}
