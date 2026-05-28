package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.security.SecurityUserDetails;
import com.example.ecommerce.service.FavoriteService;
import com.example.ecommerce.vo.FavoriteVO;
import com.example.ecommerce.vo.PageVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorites")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{productId}")
    public Result<Void> addFavorite(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long productId) {
        favoriteService.addFavorite(principal.getUserId(), productId);
        return Result.success("收藏成功", null);
    }

    @DeleteMapping("/{productId}")
    public Result<Void> cancelFavorite(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long productId) {
        favoriteService.cancelFavorite(principal.getUserId(), productId);
        return Result.success("取消收藏成功", null);
    }

    @GetMapping
    public Result<PageVO<FavoriteVO>> listFavorites(@AuthenticationPrincipal SecurityUserDetails principal,
                                                    @RequestParam(defaultValue = "1") Long pageNum,
                                                    @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.success(favoriteService.listFavorites(principal.getUserId(), pageNum, pageSize));
    }
}
