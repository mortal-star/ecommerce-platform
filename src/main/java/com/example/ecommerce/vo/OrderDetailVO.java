package com.example.ecommerce.vo;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;

import java.util.List;

public class OrderDetailVO {
    private Order order;
    private List<OrderItem> items;

    public OrderDetailVO(Order order, List<OrderItem> items) {
        this.order = order;
        this.items = items;
    }

    public Order getOrder() { return order; }
    public List<OrderItem> getItems() { return items; }
}
