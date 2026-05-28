package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotNull;

public class PayOrderDTO {
    @NotNull(message = "支付方式不能为空")
    private Integer paymentType;

    public Integer getPaymentType() { return paymentType; }
    public void setPaymentType(Integer paymentType) { this.paymentType = paymentType; }
}
