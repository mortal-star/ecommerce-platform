package com.example.ecommerce.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderExportVO {
    private String orderNo;
    private Long userId;
    private Integer orderStatus;
    private BigDecimal payAmount;
    private String receiverName;
    private String receiverPhone;
    private LocalDateTime createTime;

    public OrderExportVO(String orderNo, Long userId, Integer orderStatus, BigDecimal payAmount, String receiverName, String receiverPhone, LocalDateTime createTime) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.payAmount = payAmount;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.createTime = createTime;
    }

    public String getOrderNo() { return orderNo; }
    public Long getUserId() { return userId; }
    public Integer getOrderStatus() { return orderStatus; }
    public BigDecimal getPayAmount() { return payAmount; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public LocalDateTime getCreateTime() { return createTime; }
}
