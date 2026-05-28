package com.example.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ecommerce.dto.CategorySortDTO;
import com.example.ecommerce.entity.Banner;

import java.util.List;

public interface BannerService extends IService<Banner> {
    Banner createBanner(Banner banner);
    Banner updateBanner(Long id, Banner banner);
    void deleteBanner(Long id);
    List<Banner> listBanners(Integer status);
    void updateSort(List<CategorySortDTO> sortList);
}
