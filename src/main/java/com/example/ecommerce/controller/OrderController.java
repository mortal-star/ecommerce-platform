package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.CreateOrderDTO;
import com.example.ecommerce.dto.OrderQueryDTO;
import com.example.ecommerce.dto.PayOrderDTO;
import com.example.ecommerce.dto.RefundProcessDTO;
import com.example.ecommerce.dto.ShipOrderDTO;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.security.SecurityUserDetails;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.vo.OrderDetailVO;
import com.example.ecommerce.vo.OrderExportVO;
import com.example.ecommerce.vo.PageVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<OrderDetailVO> createOrder(@AuthenticationPrincipal SecurityUserDetails principal, @Valid @RequestBody CreateOrderDTO request) {
        return Result.success("订单创建成功", orderService.createOrder(principal.getUserId(), request));
    }

    @PostMapping("/{orderId}/pay")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<OrderDetailVO> payOrder(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long orderId, @Valid @RequestBody PayOrderDTO request) {
        return Result.success("支付成功", orderService.payOrder(principal.getUserId(), orderId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<PageVO<Order>> listUserOrders(@AuthenticationPrincipal SecurityUserDetails principal, @ModelAttribute OrderQueryDTO query) {
        return Result.success(orderService.listUserOrders(principal.getUserId(), query));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<OrderDetailVO> detail(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long orderId) {
        boolean admin = principal.getRoles().contains("ADMIN") || principal.getRoles().contains("ROLE_ADMIN");
        return Result.success(orderService.getOrderDetail(principal.getUserId(), orderId, admin));
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<Void> cancelOrder(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long orderId) {
        orderService.cancelOrder(principal.getUserId(), orderId);
        return Result.success("订单取消成功", null);
    }

    @PatchMapping("/{orderId}/receive")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<Void> confirmReceive(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long orderId) {
        orderService.confirmReceive(principal.getUserId(), orderId);
        return Result.success("确认收货成功", null);
    }

    @PatchMapping("/{orderId}/refund")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<Void> applyRefund(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long orderId) {
        orderService.applyRefund(principal.getUserId(), orderId);
        return Result.success("退款申请提交成功", null);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageVO<Order>> listAdminOrders(@ModelAttribute OrderQueryDTO query) {
        return Result.success(orderService.listAdminOrders(query));
    }

    @PatchMapping("/admin/{orderId}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> shipOrder(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long orderId, @RequestBody ShipOrderDTO request) {
        orderService.shipOrder(principal.getUserId(), orderId, request);
        return Result.success("订单发货成功", null);
    }

    @PatchMapping("/admin/{orderId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> processRefund(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long orderId, @Valid @RequestBody RefundProcessDTO request) {
        orderService.processRefund(principal.getUserId(), orderId, request);
        return Result.success("退款处理完成", null);
    }

    @GetMapping("/admin/export")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<OrderExportVO>> exportOrders(@RequestParam(required = false) Integer status) {
        return Result.success(orderService.exportOrders(status));
    }
}
