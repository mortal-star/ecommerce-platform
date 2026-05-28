package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.entity.Favorite;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.mapper.FavoriteMapper;
import com.example.ecommerce.service.FavoriteService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.vo.FavoriteVO;
import com.example.ecommerce.vo.PageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private final ProductService productService;

    public FavoriteServiceImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long userId, Long productId) {
        Product product = productService.getById(productId);
        if (product == null || !Integer.valueOf(0).equals(product.getDeleted())) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (isFavorite(userId, productId)) {
            return;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setDeleted(0);
        save(favorite);
    }

    @Override
    public void cancelFavorite(Long userId, Long productId) {
        remove(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        return count(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId)
                .eq(Favorite::getDeleted, 0)) > 0;
    }

    @Override
    public PageVO<FavoriteVO> listFavorites(Long userId, Long pageNum, Long pageSize) {
        Page<Favorite> page = page(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getDeleted, 0)
                .orderByDesc(Favorite::getCreateTime));
        List<FavoriteVO> records = page.getRecords().stream()
                .map(favorite -> new FavoriteVO(favorite, productService.getById(favorite.getProductId())))
                .toList();
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }
}
