package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.dto.ProductSearchDTO;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.service.FavoriteService;
import com.example.ecommerce.service.ProductImageService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.ProductSpecService;
import com.example.ecommerce.vo.PageVO;
import com.example.ecommerce.vo.ProductDetailVO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("price", "sales", "createTime", "sort", "id");

    private final ProductImageService productImageService;

    private final ProductSpecService productSpecService;

    private final FavoriteService favoriteService;

    public ProductServiceImpl(ProductImageService productImageService, ProductSpecService productSpecService, @Lazy FavoriteService favoriteService) {
        this.productImageService = productImageService;
        this.productSpecService = productSpecService;
        this.favoriteService = favoriteService;
    }

    @Override
    public Product createProduct(Product product) {
        product.setDeleted(0);
        if (product.getStatus() == null) {
            product.setStatus(0);
        }
        if (product.getSort() == null) {
            product.setSort(0);
        }
        if (product.getSales() == null) {
            product.setSales(0);
        }
        save(product);
        return product;
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        Product dbProduct = getById(id);
        if (dbProduct == null || !Integer.valueOf(0).equals(dbProduct.getDeleted())) {
            throw new IllegalArgumentException("商品不存在");
        }
        product.setId(id);
        updateById(product);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        removeById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = getById(id);
        if (product == null || !Integer.valueOf(0).equals(product.getDeleted())) {
            throw new IllegalArgumentException("商品不存在");
        }
        product.setStatus(status);
        updateById(product);
    }

    @Override
    public PageVO<Product> searchProducts(ProductSearchDTO query) {
        Long pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1L : query.getPageNum();
        Long pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10L : Math.min(query.getPageSize(), 100L);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, 0)
                .eq(query.getCategoryId() != null, Product::getCategoryId, query.getCategoryId())
                .eq(query.getStatus() != null, Product::getStatus, query.getStatus())
                .ge(query.getMinPrice() != null, Product::getPrice, query.getMinPrice())
                .le(query.getMaxPrice() != null, Product::getPrice, query.getMaxPrice())
                .and(StringUtils.hasText(query.getKeyword()), w -> w.like(Product::getName, query.getKeyword()).or().like(Product::getSubtitle, query.getKeyword()));
        applySort(wrapper, query);
        Page<Product> page = page(new Page<>(pageNum, pageSize), wrapper);
        return new PageVO<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public ProductDetailVO getProductDetail(Long id, Long userId) {
        Product product = getById(id);
        if (product == null || !Integer.valueOf(0).equals(product.getDeleted())) {
            throw new IllegalArgumentException("商品不存在");
        }
        return new ProductDetailVO(
                product,
                productImageService.listByProductId(id),
                productSpecService.listByProductId(id),
                favoriteService.isFavorite(userId, id)
        );
    }

    private void applySort(LambdaQueryWrapper<Product> wrapper, ProductSearchDTO query) {
        String sortBy = query.getSortBy();
        boolean asc = !"desc".equalsIgnoreCase(query.getSortOrder());
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "sort";
        }
        if ("price".equals(sortBy)) {
            wrapper.orderBy(true, asc, Product::getPrice);
        } else if ("sales".equals(sortBy)) {
            wrapper.orderBy(true, asc, Product::getSales);
        } else if ("createTime".equals(sortBy)) {
            wrapper.orderBy(true, asc, Product::getCreateTime);
        } else if ("id".equals(sortBy)) {
            wrapper.orderBy(true, asc, Product::getId);
        } else {
            wrapper.orderBy(true, asc, Product::getSort).orderByDesc(Product::getId);
        }
    }
}
