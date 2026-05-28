package com.example.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ecommerce.dto.ProductReviewDTO;
import com.example.ecommerce.dto.ReviewQueryDTO;
import com.example.ecommerce.entity.ProductReview;
import com.example.ecommerce.vo.PageVO;

public interface ProductReviewService extends IService<ProductReview> {

    ProductReview submitReview(Long userId, ProductReviewDTO request);

    PageVO<ProductReview> listReviews(Long productId, Long pageNum, Long pageSize);

    PageVO<ProductReview> listAdminReviews(ReviewQueryDTO query);

    void deleteReview(Long userId, Long reviewId, boolean admin);
}
