package com.example.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ecommerce.entity.Favorite;
import com.example.ecommerce.vo.FavoriteVO;
import com.example.ecommerce.vo.PageVO;

public interface FavoriteService extends IService<Favorite> {

    void addFavorite(Long userId, Long productId);

    void cancelFavorite(Long userId, Long productId);

    boolean isFavorite(Long userId, Long productId);

    PageVO<FavoriteVO> listFavorites(Long userId, Long pageNum, Long pageSize);
}
