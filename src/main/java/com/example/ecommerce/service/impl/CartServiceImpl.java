package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.dto.AddCartDTO;
import com.example.ecommerce.dto.CartSelectDTO;
import com.example.ecommerce.dto.UpdateCartQuantityDTO;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.ProductSpec;
import com.example.ecommerce.mapper.CartMapper;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.ProductSpecService;
import com.example.ecommerce.vo.CartItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final ProductService productService;
    private final ProductSpecService productSpecService;

    public CartServiceImpl(ProductService productService, ProductSpecService productSpecService) {
        this.productService = productService;
        this.productSpecService = productSpecService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCart(Long userId, AddCartDTO request) {
        Product product = productService.getById(request.getProductId());
        ProductSpec spec = productSpecService.getById(request.getSkuId());
        if (product == null || spec == null || !Integer.valueOf(1).equals(product.getStatus()) || !Integer.valueOf(1).equals(spec.getStatus())) {
            throw new IllegalArgumentException("商品不存在或已下架");
        }
        Cart cart = getOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, request.getProductId())
                .eq(Cart::getSkuId, request.getSkuId())
                .eq(Cart::getDeleted, 0), false);
        int quantity = request.getQuantity();
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(request.getProductId());
            cart.setSkuId(request.getSkuId());
            cart.setQuantity(quantity);
            cart.setSelected(1);
            cart.setDeleted(0);
            save(cart);
        } else {
            cart.setQuantity(cart.getQuantity() + quantity);
            updateById(cart);
        }
    }

    @Override
    public void updateQuantity(Long userId, Long cartId, UpdateCartQuantityDTO request) {
        Cart cart = getUserCart(userId, cartId);
        cart.setQuantity(request.getQuantity());
        updateById(cart);
    }

    @Override
    public void deleteCart(Long userId, Long cartId) {
        getUserCart(userId, cartId);
        removeById(cartId);
    }

    @Override
    public List<CartItemVO> listCart(Long userId) {
        return list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getDeleted, 0)
                .orderByDesc(Cart::getCreateTime))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public void updateSelected(Long userId, CartSelectDTO request) {
        List<Cart> carts = list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getDeleted, 0)
                .in(request.getCartIds() != null && !request.getCartIds().isEmpty(), Cart::getId, request.getCartIds()));
        for (Cart cart : carts) {
            cart.setSelected(Boolean.TRUE.equals(request.getSelected()) ? 1 : 0);
            updateById(cart);
        }
    }

    @Override
    public List<Cart> listSelectedItems(Long userId) {
        return list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, 1)
                .eq(Cart::getDeleted, 0));
    }

    private Cart getUserCart(Long userId, Long cartId) {
        Cart cart = getById(cartId);
        if (cart == null || !Integer.valueOf(0).equals(cart.getDeleted()) || !cart.getUserId().equals(userId)) {
            throw new IllegalArgumentException("购物车商品不存在");
        }
        return cart;
    }

    private CartItemVO toVO(Cart cart) {
        Product product = productService.getById(cart.getProductId());
        ProductSpec spec = productSpecService.getById(cart.getSkuId());
        BigDecimal price = spec == null || spec.getPrice() == null ? BigDecimal.ZERO : spec.getPrice();
        BigDecimal itemAmount = price.multiply(BigDecimal.valueOf(cart.getQuantity()));
        return new CartItemVO(cart, product, spec, itemAmount);
    }
}
