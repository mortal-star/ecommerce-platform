package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.service.DashboardService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.UserService;
import com.example.ecommerce.vo.DashboardStatsVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final UserService userService;
    private final OrderService orderService;

    public DashboardServiceImpl(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    public DashboardStatsVO getStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long totalUsers = userService.count(new LambdaQueryWrapper<User>().eq(User::getDeleted, 0));
        Long todayUsers = userService.count(new LambdaQueryWrapper<User>().eq(User::getDeleted, 0).ge(User::getCreateTime, todayStart));
        Long totalOrders = orderService.count(new LambdaQueryWrapper<Order>().eq(Order::getDeleted, 0));
        Long todayOrders = orderService.count(new LambdaQueryWrapper<Order>().eq(Order::getDeleted, 0).ge(Order::getCreateTime, todayStart));
        BigDecimal totalSales = sumPaidAmount(null);
        BigDecimal todaySales = sumPaidAmount(todayStart);
        return new DashboardStatsVO(totalUsers, totalOrders, totalSales, todayUsers, todayOrders, todaySales);
    }

    private BigDecimal sumPaidAmount(LocalDateTime startTime) {
        return orderService.list(new LambdaQueryWrapper<Order>()
                        .eq(Order::getDeleted, 0)
                        .in(Order::getOrderStatus, OrderStatus.PENDING_DELIVERY.getCode(), OrderStatus.PENDING_RECEIVE.getCode(), OrderStatus.COMPLETED.getCode())
                        .ge(startTime != null, Order::getPayTime, startTime))
                .stream()
                .map(Order::getPayAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
