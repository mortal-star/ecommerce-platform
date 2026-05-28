package com.example.ecommerce.enums;

public enum OrderStatus {

    PENDING_PAYMENT(0, "待支付"),
    PENDING_DELIVERY(1, "待发货"),
    PENDING_RECEIVE(2, "待收货"),
    COMPLETED(3, "已完成"),
    CLOSED(4, "已关闭"),
    CANCELED(5, "已取消"),
    REFUNDING(6, "退款中"),
    REFUNDED(7, "已退款");

    private final Integer code;

    private final String description;

    OrderStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() { return code; }

    public String getDescription() { return description; }
}
