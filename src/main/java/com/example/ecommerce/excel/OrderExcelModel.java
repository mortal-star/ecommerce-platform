package com.example.ecommerce.excel;

import com.alibaba.excel.annotation.ExcelProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderExcelModel {
    @ExcelProperty("订单号")
    private String orderNo;
    @ExcelProperty("用户ID")
    private Long userId;
    @ExcelProperty("订单状态")
    private Integer orderStatus;
    @ExcelProperty("实付金额")
    private BigDecimal payAmount;
    @ExcelProperty("收货人")
    private String receiverName;
    @ExcelProperty("收货电话")
    private String receiverPhone;
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Integer orderStatus) { this.orderStatus = orderStatus; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
