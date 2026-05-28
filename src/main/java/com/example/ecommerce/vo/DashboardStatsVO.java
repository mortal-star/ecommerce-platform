package com.example.ecommerce.vo;

import java.math.BigDecimal;

public class DashboardStatsVO {
    private Long totalUsers;
    private Long totalOrders;
    private BigDecimal totalSalesAmount;
    private Long todayUsers;
    private Long todayOrders;
    private BigDecimal todaySalesAmount;

    public DashboardStatsVO(Long totalUsers, Long totalOrders, BigDecimal totalSalesAmount, Long todayUsers, Long todayOrders, BigDecimal todaySalesAmount) {
        this.totalUsers = totalUsers;
        this.totalOrders = totalOrders;
        this.totalSalesAmount = totalSalesAmount;
        this.todayUsers = todayUsers;
        this.todayOrders = todayOrders;
        this.todaySalesAmount = todaySalesAmount;
    }

    public Long getTotalUsers() { return totalUsers; }
    public Long getTotalOrders() { return totalOrders; }
    public BigDecimal getTotalSalesAmount() { return totalSalesAmount; }
    public Long getTodayUsers() { return todayUsers; }
    public Long getTodayOrders() { return todayOrders; }
    public BigDecimal getTodaySalesAmount() { return todaySalesAmount; }
}
