package com.example.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ecommerce.entity.ProductSpec;

import java.util.List;

public interface ProductSpecService extends IService<ProductSpec> {

    ProductSpec createSpec(ProductSpec spec);

    ProductSpec updateSpec(Long id, ProductSpec spec);

    void deleteSpec(Long id);

    List<ProductSpec> listByProductId(Long productId);
}
