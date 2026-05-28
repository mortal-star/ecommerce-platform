package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotNull;

public class CreateOrderDTO {
    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;
    private String remark;

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
