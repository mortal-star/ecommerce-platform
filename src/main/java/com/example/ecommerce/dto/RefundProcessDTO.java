package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotNull;

public class RefundProcessDTO {
    @NotNull(message = "处理结果不能为空")
    private Boolean approved;
    private String remark;

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
