package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.dto.CreateOrderDTO;
import com.example.ecommerce.dto.OrderQueryDTO;
import com.example.ecommerce.dto.PayOrderDTO;
import com.example.ecommerce.dto.RefundProcessDTO;
import com.example.ecommerce.dto.ShipOrderDTO;
import com.example.ecommerce.entity.Address;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.ProductSpec;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.mapper.OrderMapper;
import com.example.ecommerce.service.AddressService;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.OrderItemService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.ProductSpecService;
import com.example.ecommerce.util.OrderNoUtil;
import com.example.ecommerce.vo.OrderDetailVO;
import com.example.ecommerce.vo.OrderExportVO;
import com.example.ecommerce.vo.PageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final CartService cartService;
    private final AddressService addressService;
    private final ProductService productService;
    private final ProductSpecService productSpecService;
    private final OrderItemService orderItemService;

    public OrderServiceImpl(CartService cartService, AddressService addressService, ProductService productService, ProductSpecService productSpecService, OrderItemService orderItemService) {
        this.cartService = cartService;
        this.addressService = addressService;
        this.productService = productService;
        this.productSpecService = productSpecService;
        this.orderItemService = orderItemService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO createOrder(Long userId, CreateOrderDTO request) {
        List<Cart> carts = cartService.listSelectedItems(userId);
        if (carts.isEmpty()) {
            throw new IllegalArgumentException("请选择要结算的商品");
        }
        Address address = addressService.getUserAddress(userId, request.getAddressId());
        String orderNo = OrderNoUtil.generateOrderNo();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (Cart cart : carts) {
            Product product = productService.getById(cart.getProductId());
            ProductSpec spec = productSpecService.getById(cart.getSkuId());
            if (product == null || spec == null || !Integer.valueOf(1).equals(product.getStatus()) || !Integer.valueOf(1).equals(spec.getStatus())) {
                throw new IllegalArgumentException("购物车中存在无效商品");
            }
            if (spec.getStock() == null || spec.getStock() < cart.getQuantity()) {
                throw new IllegalArgumentException("商品库存不足：" + product.getName());
            }
            BigDecimal itemAmount = spec.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalAmount = totalAmount.add(itemAmount);
            OrderItem item = new OrderItem();
            item.setOrderNo(orderNo);
            item.setProductId(product.getId());
            item.setSkuId(spec.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getMainImage());
            item.setSkuName(spec.getSkuName());
            item.setProductPrice(spec.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setTotalAmount(itemAmount);
            item.setDeleted(0);
            items.add(item);
            spec.setStock(spec.getStock() - cart.getQuantity());
            productSpecService.updateById(spec);
        }
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(totalAmount);
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverAddress(address.getDetailAddress());
        order.setRemark(request.getRemark());
        order.setDeleted(0);
        save(order);
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
        }
        orderItemService.saveBatch(items);
        for (Cart cart : carts) {
            cartService.removeById(cart.getId());
        }
        return new OrderDetailVO(order, items);
    }

    @Override
    public OrderDetailVO payOrder(Long userId, Long orderId, PayOrderDTO request) {
        Order order = getUserOrder(userId, orderId);
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("当前订单状态不允许支付");
        }
        order.setPaymentType(request.getPaymentType());
        order.setOrderStatus(OrderStatus.PENDING_DELIVERY.getCode());
        order.setPayTime(LocalDateTime.now());
        updateById(order);
        return new OrderDetailVO(order, orderItemService.listByOrderId(orderId));
    }

    @Override
    public PageVO<Order> listUserOrders(Long userId, OrderQueryDTO query) {
        Page<Order> page = page(new Page<>(safePageNum(query), safePageSize(query)), new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(query.getStatus() != null, Order::getOrderStatus, query.getStatus())
                .eq(Order::getDeleted, 0)
                .orderByDesc(Order::getCreateTime));
        return new PageVO<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long userId, Long orderId, boolean admin) {
        Order order = admin ? getById(orderId) : getUserOrder(userId, orderId);
        if (order == null || !Integer.valueOf(0).equals(order.getDeleted())) {
            throw new IllegalArgumentException("订单不存在");
        }
        return new OrderDetailVO(order, orderItemService.listByOrderId(orderId));
    }

    @Override
    public void cancelOrder(Long userId, Long orderId) {
        Order order = getUserOrder(userId, orderId);
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("仅待支付订单可取消");
        }
        order.setOrderStatus(OrderStatus.CANCELED.getCode());
        order.setCloseTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void confirmReceive(Long userId, Long orderId) {
        Order order = getUserOrder(userId, orderId);
        if (!OrderStatus.PENDING_RECEIVE.getCode().equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("当前订单状态不允许确认收货");
        }
        order.setOrderStatus(OrderStatus.COMPLETED.getCode());
        order.setReceiveTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void applyRefund(Long userId, Long orderId) {
        Order order = getUserOrder(userId, orderId);
        if (!OrderStatus.PENDING_DELIVERY.getCode().equals(order.getOrderStatus()) && !OrderStatus.PENDING_RECEIVE.getCode().equals(order.getOrderStatus()) && !OrderStatus.COMPLETED.getCode().equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("当前订单状态不允许申请退款");
        }
        order.setOrderStatus(OrderStatus.REFUNDING.getCode());
        updateById(order);
    }

    @Override
    public void shipOrder(Long adminId, Long orderId, ShipOrderDTO request) {
        Order order = getValidOrder(orderId);
        if (!OrderStatus.PENDING_DELIVERY.getCode().equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("仅待发货订单可发货");
        }
        order.setOrderStatus(OrderStatus.PENDING_RECEIVE.getCode());
        order.setDeliveryTime(LocalDateTime.now());
        order.setRemark(request.getRemark());
        updateById(order);
    }

    @Override
    public void processRefund(Long adminId, Long orderId, RefundProcessDTO request) {
        Order order = getValidOrder(orderId);
        if (!OrderStatus.REFUNDING.getCode().equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("订单不处于退款中状态");
        }
        order.setOrderStatus(Boolean.TRUE.equals(request.getApproved()) ? OrderStatus.REFUNDED.getCode() : OrderStatus.COMPLETED.getCode());
        order.setRemark(request.getRemark());
        updateById(order);
    }

    @Override
    public PageVO<Order> listAdminOrders(OrderQueryDTO query) {
        Page<Order> page = page(new Page<>(safePageNum(query), safePageSize(query)), new LambdaQueryWrapper<Order>()
                .eq(query.getStatus() != null, Order::getOrderStatus, query.getStatus())
                .eq(Order::getDeleted, 0)
                .orderByDesc(Order::getCreateTime));
        return new PageVO<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public List<OrderExportVO> exportOrders(Integer status) {
        return list(new LambdaQueryWrapper<Order>()
                .eq(status != null, Order::getOrderStatus, status)
                .eq(Order::getDeleted, 0)
                .orderByDesc(Order::getCreateTime))
                .stream()
                .map(order -> new OrderExportVO(order.getOrderNo(), order.getUserId(), order.getOrderStatus(), order.getPayAmount(), order.getReceiverName(), order.getReceiverPhone(), order.getCreateTime()))
                .toList();
    }

    private Order getUserOrder(Long userId, Long orderId) {
        Order order = getById(orderId);
        if (order == null || !Integer.valueOf(0).equals(order.getDeleted()) || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        return order;
    }

    private Order getValidOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null || !Integer.valueOf(0).equals(order.getDeleted())) {
            throw new IllegalArgumentException("订单不存在");
        }
        return order;
    }

    private long safePageNum(OrderQueryDTO query) {
        return query.getPageNum() == null || query.getPageNum() < 1 ? 1L : query.getPageNum();
    }

    private long safePageSize(OrderQueryDTO query) {
        return query.getPageSize() == null || query.getPageSize() < 1 ? 10L : Math.min(query.getPageSize(), 100L);
    }
}
