package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.dto.CategorySortDTO;
import com.example.ecommerce.entity.Banner;
import com.example.ecommerce.mapper.BannerMapper;
import com.example.ecommerce.service.BannerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {
    public Banner createBanner(Banner banner) {
        banner.setDeleted(0);
        if (banner.getStatus() == null) banner.setStatus(1);
        if (banner.getSort() == null) banner.setSort(0);
        save(banner);
        return banner;
    }
    public Banner updateBanner(Long id, Banner banner) {
        banner.setId(id);
        updateById(banner);
        return getById(id);
    }
    public void deleteBanner(Long id) { removeById(id); }
    public List<Banner> listBanners(Integer status) {
        return list(new LambdaQueryWrapper<Banner>()
                .eq(status != null, Banner::getStatus, status)
                .eq(Banner::getDeleted, 0)
                .orderByAsc(Banner::getSort)
                .orderByDesc(Banner::getCreateTime));
    }
    public void updateSort(List<CategorySortDTO> sortList) {
        for (CategorySortDTO sortDTO : sortList) {
            Banner banner = new Banner();
            banner.setId(sortDTO.getId());
            banner.setSort(sortDTO.getSort());
            updateById(banner);
        }
    }
}
