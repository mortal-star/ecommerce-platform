package com.example.ecommerce.service;

import com.example.ecommerce.vo.DashboardStatsVO;
import com.example.ecommerce.vo.OrderStatusStatVO;
import com.example.ecommerce.vo.SalesTrendVO;
import com.example.ecommerce.vo.TopProductVO;

import java.util.List;

public interface DashboardService {
    DashboardStatsVO getStats();

    SalesTrendVO getSalesTrend(Integer days);

    List<OrderStatusStatVO> getOrderStatusStats();

    List<TopProductVO> getTopProducts(Integer limit);
}
