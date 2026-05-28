package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.service.DashboardService;
import com.example.ecommerce.service.OrderItemService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.UserService;
import com.example.ecommerce.vo.DashboardStatsVO;
import com.example.ecommerce.vo.OrderStatusStatVO;
import com.example.ecommerce.vo.SalesTrendVO;
import com.example.ecommerce.vo.TopProductVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final UserService userService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");

    public DashboardServiceImpl(UserService userService, OrderService orderService,
                                OrderItemService orderItemService, ProductService productService) {
        this.userService = userService;
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.productService = productService;
    }

    @Override
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

    @Override
    public SalesTrendVO getSalesTrend(Integer days) {
        int n = days == null || days < 1 ? 7 : Math.min(days, 90);
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(n - 1L);
        LocalDateTime startTime = start.atStartOfDay();

        List<Order> orders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getDeleted, 0)
                .in(Order::getOrderStatus,
                        OrderStatus.PENDING_DELIVERY.getCode(),
                        OrderStatus.PENDING_RECEIVE.getCode(),
                        OrderStatus.COMPLETED.getCode())
                .ge(Order::getPayTime, startTime));

        Map<LocalDate, BigDecimal> amountByDay = new HashMap<>();
        Map<LocalDate, Long> countByDay = new HashMap<>();
        for (Order o : orders) {
            if (o.getPayTime() == null) continue;
            LocalDate d = o.getPayTime().toLocalDate();
            amountByDay.merge(d, o.getPayAmount() == null ? BigDecimal.ZERO : o.getPayAmount(), BigDecimal::add);
            countByDay.merge(d, 1L, Long::sum);
        }

        List<String> dates = new ArrayList<>(n);
        List<BigDecimal> amounts = new ArrayList<>(n);
        List<Long> counts = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            LocalDate d = start.plusDays(i);
            dates.add(d.format(DATE_FMT));
            amounts.add(amountByDay.getOrDefault(d, BigDecimal.ZERO));
            counts.add(countByDay.getOrDefault(d, 0L));
        }
        return new SalesTrendVO(dates, amounts, counts);
    }

    @Override
    public List<OrderStatusStatVO> getOrderStatusStats() {
        List<Order> orders = orderService.list(new LambdaQueryWrapper<Order>().eq(Order::getDeleted, 0));
        Map<Integer, Long> grouped = orders.stream()
                .filter(o -> o.getOrderStatus() != null)
                .collect(Collectors.groupingBy(Order::getOrderStatus, Collectors.counting()));

        List<OrderStatusStatVO> result = new ArrayList<>();
        for (OrderStatus s : OrderStatus.values()) {
            Long count = grouped.getOrDefault(s.getCode(), 0L);
            result.add(new OrderStatusStatVO(s.getCode(), s.getDescription(), count));
        }
        return result;
    }

    @Override
    public List<TopProductVO> getTopProducts(Integer limit) {
        int n = limit == null || limit < 1 ? 10 : Math.min(limit, 50);

        List<Order> paidOrders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getDeleted, 0)
                .in(Order::getOrderStatus,
                        OrderStatus.PENDING_DELIVERY.getCode(),
                        OrderStatus.PENDING_RECEIVE.getCode(),
                        OrderStatus.COMPLETED.getCode()));
        if (paidOrders.isEmpty()) return Collections.emptyList();

        List<Long> orderIds = paidOrders.stream().map(Order::getId).toList();
        List<OrderItem> items = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getDeleted, 0)
                .in(OrderItem::getOrderId, orderIds));

        Map<Long, long[]> stats = new HashMap<>();
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        Map<Long, String> nameMap = new HashMap<>();
        Map<Long, String> imageMap = new HashMap<>();
        for (OrderItem item : items) {
            Long pid = item.getProductId();
            if (pid == null) continue;
            stats.computeIfAbsent(pid, k -> new long[1])[0] += item.getQuantity() == null ? 0 : item.getQuantity();
            amountMap.merge(pid, item.getTotalAmount() == null ? BigDecimal.ZERO : item.getTotalAmount(), BigDecimal::add);
            nameMap.putIfAbsent(pid, item.getProductName());
            imageMap.putIfAbsent(pid, item.getProductImage());
        }

        List<TopProductVO> result = stats.entrySet().stream()
                .map(e -> new TopProductVO(
                        e.getKey(),
                        nameMap.get(e.getKey()),
                        imageMap.get(e.getKey()),
                        e.getValue()[0],
                        amountMap.getOrDefault(e.getKey(), BigDecimal.ZERO)
                ))
                .sorted(Comparator.comparingLong(TopProductVO::getSalesCount).reversed())
                .limit(n)
                .collect(Collectors.toList());

        // Try fill missing image from product table
        List<Long> needImage = result.stream()
                .filter(p -> p.getMainImage() == null || p.getMainImage().isEmpty())
                .map(TopProductVO::getProductId)
                .toList();
        if (!needImage.isEmpty()) {
            List<Product> products = productService.listByIds(needImage);
            Map<Long, Product> pmap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
            result = result.stream().map(t -> {
                if (t.getMainImage() == null || t.getMainImage().isEmpty()) {
                    Product p = pmap.get(t.getProductId());
                    if (p != null) {
                        return new TopProductVO(t.getProductId(), t.getProductName(), p.getMainImage(), t.getSalesCount(), t.getSalesAmount());
                    }
                }
                return t;
            }).collect(Collectors.toList());
        }
        return result;
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
