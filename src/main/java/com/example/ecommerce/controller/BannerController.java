package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.CategorySortDTO;
import com.example.ecommerce.entity.Banner;
import com.example.ecommerce.service.BannerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/banners")
public class BannerController {
    private final BannerService bannerService;
    public BannerController(BannerService bannerService) { this.bannerService = bannerService; }

    @GetMapping
    public Result<List<Banner>> list(@RequestParam(required = false) Integer status) { return Result.success(bannerService.listBanners(status)); }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Banner> create(@RequestBody Banner banner) { return Result.success("轮播图新增成功", bannerService.createBanner(banner)); }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Banner> update(@PathVariable Long id, @RequestBody Banner banner) { return Result.success("轮播图修改成功", bannerService.updateBanner(id, banner)); }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) { bannerService.deleteBanner(id); return Result.success("轮播图删除成功", null); }
    @PutMapping("/sort")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> sort(@RequestBody List<CategorySortDTO> sortList) { bannerService.updateSort(sortList); return Result.success("轮播图排序成功", null); }
}
