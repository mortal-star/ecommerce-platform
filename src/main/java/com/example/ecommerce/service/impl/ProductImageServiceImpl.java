package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.entity.ProductImage;
import com.example.ecommerce.mapper.ProductImageMapper;
import com.example.ecommerce.service.ProductImageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductImageServiceImpl extends ServiceImpl<ProductImageMapper, ProductImage> implements ProductImageService {

    @Override
    public List<ProductImage> listByProductId(Long productId) {
        return list(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, productId)
                .eq(ProductImage::getDeleted, 0)
                .orderByAsc(ProductImage::getSort)
                .orderByAsc(ProductImage::getId));
    }
}
