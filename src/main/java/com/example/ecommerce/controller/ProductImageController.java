package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.entity.ProductImage;
import com.example.ecommerce.service.ProductImageService;
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
@RequestMapping("/product-images")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @GetMapping("/product/{productId}")
    public Result<List<ProductImage>> listByProduct(@PathVariable Long productId) {
        return Result.success(productImageService.listByProductId(productId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ProductImage> create(@RequestBody ProductImage image) {
        image.setDeleted(0);
        productImageService.save(image);
        return Result.success("商品图片新增成功", image);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ProductImage> update(@PathVariable Long id, @RequestBody ProductImage image) {
        image.setId(id);
        productImageService.updateById(image);
        return Result.success("商品图片修改成功", productImageService.getById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        productImageService.removeById(id);
        return Result.success("商品图片删除成功", null);
    }
}
