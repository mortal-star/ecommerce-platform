package com.example.ecommerce.dto;

public class UserQueryDTO {
    private String keyword;
    private Integer status;
    private Integer userType;
    private Long pageNum;
    private Long pageSize;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getUserType() { return userType; }
    public void setUserType(Integer userType) { this.userType = userType; }
    public Long getPageNum() { return pageNum; }
    public void setPageNum(Long pageNum) { this.pageNum = pageNum; }
    public Long getPageSize() { return pageSize; }
    public void setPageSize(Long pageSize) { this.pageSize = pageSize; }
}
