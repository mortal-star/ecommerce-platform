package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.entity.ProductSpec;
import com.example.ecommerce.service.ProductSpecService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product-specs")
public class ProductSpecController {

    private final ProductSpecService productSpecService;

    public ProductSpecController(ProductSpecService productSpecService) {
        this.productSpecService = productSpecService;
    }

    @GetMapping("/product/{productId}")
    public Result<List<ProductSpec>> listByProduct(@PathVariable Long productId) {
        return Result.success(productSpecService.listByProductId(productId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ProductSpec> create(@RequestBody ProductSpec spec) {
        return Result.success("规格新增成功", productSpecService.createSpec(spec));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ProductSpec> update(@PathVariable Long id, @RequestBody ProductSpec spec) {
        return Result.success("规格修改成功", productSpecService.updateSpec(id, spec));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        productSpecService.deleteSpec(id);
        return Result.success("规格删除成功", null);
    }
}
