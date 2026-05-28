package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.ProductSearchDTO;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.security.SecurityUserDetails;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.vo.PageVO;
import com.example.ecommerce.vo.ProductDetailVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Result<PageVO<Product>> search(@ModelAttribute ProductSearchDTO query) {
        if (query.getStatus() == null) {
            query.setStatus(1);
        }
        return Result.success(productService.searchProducts(query));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageVO<Product>> adminSearch(@ModelAttribute ProductSearchDTO query) {
        return Result.success(productService.searchProducts(query));
    }

    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id, @AuthenticationPrincipal SecurityUserDetails principal) {
        Long userId = principal == null ? null : principal.getUserId();
        return Result.success(productService.getProductDetail(id, userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Product> create(@RequestBody Product product) {
        return Result.success("商品新增成功", productService.createProduct(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        return Result.success("商品修改成功", productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("商品删除成功", null);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.success("商品状态更新成功", null);
    }
}
