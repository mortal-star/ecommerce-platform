package com.example.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ecommerce.dto.AddCartDTO;
import com.example.ecommerce.dto.CartSelectDTO;
import com.example.ecommerce.dto.UpdateCartQuantityDTO;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.vo.CartItemVO;

import java.util.List;

public interface CartService extends IService<Cart> {
    void addCart(Long userId, AddCartDTO request);
    void updateQuantity(Long userId, Long cartId, UpdateCartQuantityDTO request);
    void deleteCart(Long userId, Long cartId);
    List<CartItemVO> listCart(Long userId);
    void updateSelected(Long userId, CartSelectDTO request);
    List<Cart> listSelectedItems(Long userId);
}
