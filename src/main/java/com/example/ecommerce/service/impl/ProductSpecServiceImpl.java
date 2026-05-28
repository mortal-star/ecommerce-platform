package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.entity.ProductSpec;
import com.example.ecommerce.mapper.ProductSpecMapper;
import com.example.ecommerce.service.ProductSpecService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductSpecServiceImpl extends ServiceImpl<ProductSpecMapper, ProductSpec> implements ProductSpecService {

    @Override
    public ProductSpec createSpec(ProductSpec spec) {
        spec.setDeleted(0);
        if (spec.getStatus() == null) {
            spec.setStatus(1);
        }
        save(spec);
        return spec;
    }

    @Override
    public ProductSpec updateSpec(Long id, ProductSpec spec) {
        ProductSpec dbSpec = getById(id);
        if (dbSpec == null || !Integer.valueOf(0).equals(dbSpec.getDeleted())) {
            throw new IllegalArgumentException("商品规格不存在");
        }
        spec.setId(id);
        updateById(spec);
        return getById(id);
    }

    @Override
    public void deleteSpec(Long id) {
        removeById(id);
    }

    @Override
    public List<ProductSpec> listByProductId(Long productId) {
        return list(new LambdaQueryWrapper<ProductSpec>()
                .eq(ProductSpec::getProductId, productId)
                .eq(ProductSpec::getDeleted, 0)
                .orderByAsc(ProductSpec::getId));
    }
}
