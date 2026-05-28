package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.dto.ProductReviewDTO;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.ProductReview;
import com.example.ecommerce.mapper.ProductReviewMapper;
import com.example.ecommerce.service.ProductReviewService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.vo.PageVO;
import org.springframework.stereotype.Service;

@Service
public class ProductReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview> implements ProductReviewService {

    private final ProductService productService;

    public ProductReviewServiceImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public ProductReview submitReview(Long userId, ProductReviewDTO request) {
        Product product = productService.getById(request.getProductId());
        if (product == null || !Integer.valueOf(0).equals(product.getDeleted())) {
            throw new IllegalArgumentException("商品不存在");
        }
        ProductReview review = new ProductReview();
        review.setUserId(userId);
        review.setProductId(request.getProductId());
        review.setOrderItemId(request.getOrderItemId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setImages(request.getImages());
        review.setStatus(1);
        review.setDeleted(0);
        save(review);
        return review;
    }

    @Override
    public PageVO<ProductReview> listReviews(Long productId, Long pageNum, Long pageSize) {
        Long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        Long size = pageSize == null || pageSize < 1 ? 10L : Math.min(pageSize, 100L);
        Page<ProductReview> page = page(new Page<>(current, size), new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getProductId, productId)
                .eq(ProductReview::getStatus, 1)
                .eq(ProductReview::getDeleted, 0)
                .orderByDesc(ProductReview::getCreateTime));
        return new PageVO<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public void deleteReview(Long userId, Long reviewId, boolean admin) {
        ProductReview review = getById(reviewId);
        if (review == null || !Integer.valueOf(0).equals(review.getDeleted())) {
            throw new IllegalArgumentException("评价不存在");
        }
        if (!admin && !review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只能删除自己的评价");
        }
        removeById(reviewId);
    }
}
