package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.AddCartDTO;
import com.example.ecommerce.dto.CartSelectDTO;
import com.example.ecommerce.dto.UpdateCartQuantityDTO;
import com.example.ecommerce.security.SecurityUserDetails;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.vo.CartItemVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cart")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public Result<Void> addCart(@AuthenticationPrincipal SecurityUserDetails principal, @Valid @RequestBody AddCartDTO request) {
        cartService.addCart(principal.getUserId(), request);
        return Result.success("添加购物车成功", null);
    }

    @PutMapping("/{cartId}/quantity")
    public Result<Void> updateQuantity(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long cartId, @Valid @RequestBody UpdateCartQuantityDTO request) {
        cartService.updateQuantity(principal.getUserId(), cartId, request);
        return Result.success("购物车数量修改成功", null);
    }

    @DeleteMapping("/{cartId}")
    public Result<Void> deleteCart(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long cartId) {
        cartService.deleteCart(principal.getUserId(), cartId);
        return Result.success("购物车商品删除成功", null);
    }

    @GetMapping
    public Result<List<CartItemVO>> listCart(@AuthenticationPrincipal SecurityUserDetails principal) {
        return Result.success(cartService.listCart(principal.getUserId()));
    }

    @PatchMapping("/selected")
    public Result<Void> updateSelected(@AuthenticationPrincipal SecurityUserDetails principal, @Valid @RequestBody CartSelectDTO request) {
        cartService.updateSelected(principal.getUserId(), request);
        return Result.success("购物车勾选状态更新成功", null);
    }
}
