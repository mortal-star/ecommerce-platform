package com.example.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ecommerce.dto.ProductSearchDTO;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.vo.PageVO;
import com.example.ecommerce.vo.ProductDetailVO;

public interface ProductService extends IService<Product> {

    Product createProduct(Product product);

    Product updateProduct(Long id, Product product);

    void deleteProduct(Long id);

    void updateStatus(Long id, Integer status);

    PageVO<Product> searchProducts(ProductSearchDTO query);

    ProductDetailVO getProductDetail(Long id, Long userId);
}
