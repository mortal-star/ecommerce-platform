package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.service.DashboardService;
import com.example.ecommerce.vo.DashboardStatsVO;
import com.example.ecommerce.vo.OrderStatusStatVO;
import com.example.ecommerce.vo.SalesTrendVO;
import com.example.ecommerce.vo.TopProductVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        return Result.success(dashboardService.getStats());
    }

    @GetMapping("/sales-trend")
    public Result<SalesTrendVO> salesTrend(@RequestParam(required = false, defaultValue = "7") Integer days) {
        return Result.success(dashboardService.getSalesTrend(days));
    }

    @GetMapping("/order-status")
    public Result<List<OrderStatusStatVO>> orderStatusStats() {
        return Result.success(dashboardService.getOrderStatusStats());
    }

    @GetMapping("/top-products")
    public Result<List<TopProductVO>> topProducts(@RequestParam(required = false, defaultValue = "10") Integer limit) {
        return Result.success(dashboardService.getTopProducts(limit));
    }
}
