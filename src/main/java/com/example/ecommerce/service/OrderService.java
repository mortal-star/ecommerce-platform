package com.example.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ecommerce.dto.CreateOrderDTO;
import com.example.ecommerce.dto.OrderQueryDTO;
import com.example.ecommerce.dto.PayOrderDTO;
import com.example.ecommerce.dto.RefundProcessDTO;
import com.example.ecommerce.dto.ShipOrderDTO;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.vo.OrderDetailVO;
import com.example.ecommerce.vo.OrderExportVO;
import com.example.ecommerce.vo.PageVO;

import java.util.List;

public interface OrderService extends IService<Order> {
    OrderDetailVO createOrder(Long userId, CreateOrderDTO request);
    OrderDetailVO payOrder(Long userId, Long orderId, PayOrderDTO request);
    PageVO<Order> listUserOrders(Long userId, OrderQueryDTO query);
    OrderDetailVO getOrderDetail(Long userId, Long orderId, boolean admin);
    void cancelOrder(Long userId, Long orderId);
    void confirmReceive(Long userId, Long orderId);
    void applyRefund(Long userId, Long orderId);
    void shipOrder(Long adminId, Long orderId, ShipOrderDTO request);
    void processRefund(Long adminId, Long orderId, RefundProcessDTO request);
    PageVO<Order> listAdminOrders(OrderQueryDTO query);
    void adminCancelOrder(Long orderId);
    List<OrderExportVO> exportOrders(Integer status);
}
