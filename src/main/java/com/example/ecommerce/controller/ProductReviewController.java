package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.ProductReviewDTO;
import com.example.ecommerce.dto.ReviewQueryDTO;
import com.example.ecommerce.entity.ProductReview;
import com.example.ecommerce.security.SecurityUserDetails;
import com.example.ecommerce.service.ProductReviewService;
import com.example.ecommerce.vo.PageVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product-reviews")
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    public ProductReviewController(ProductReviewService productReviewService) {
        this.productReviewService = productReviewService;
    }

    @GetMapping("/product/{productId}")
    public Result<PageVO<ProductReview>> listByProduct(@PathVariable Long productId,
                                                       @RequestParam(defaultValue = "1") Long pageNum,
                                                       @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.success(productReviewService.listReviews(productId, pageNum, pageSize));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageVO<ProductReview>> adminList(@ModelAttribute ReviewQueryDTO query) {
        return Result.success(productReviewService.listAdminReviews(query));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<ProductReview> submit(@AuthenticationPrincipal SecurityUserDetails principal, @Valid @RequestBody ProductReviewDTO request) {
        return Result.success("评价提交成功", productReviewService.submitReview(principal.getUserId(), request));
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<Void> delete(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long reviewId) {
        boolean admin = principal.getRoles().contains("ADMIN") || principal.getRoles().contains("ROLE_ADMIN");
        productReviewService.deleteReview(principal.getUserId(), reviewId, admin);
        return Result.success("评价删除成功", null);
    }
}
