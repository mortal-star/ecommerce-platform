package com.example.ecommerce.vo;

public class OrderStatusStatVO {
    private Integer status;
    private String label;
    private Long count;

    public OrderStatusStatVO(Integer status, String label, Long count) {
        this.status = status;
        this.label = label;
        this.count = count;
    }

    public Integer getStatus() { return status; }
    public String getLabel() { return label; }
    public Long getCount() { return count; }
}
