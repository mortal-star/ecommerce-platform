package com.example.ecommerce.dto;

public class OrderQueryDTO {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private Integer status;
    private String orderNo;
    private String userKeyword;

    public Long getPageNum() { return pageNum; }
    public void setPageNum(Long pageNum) { this.pageNum = pageNum; }
    public Long getPageSize() { return pageSize; }
    public void setPageSize(Long pageSize) { this.pageSize = pageSize; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getUserKeyword() { return userKeyword; }
    public void setUserKeyword(String userKeyword) { this.userKeyword = userKeyword; }
}
