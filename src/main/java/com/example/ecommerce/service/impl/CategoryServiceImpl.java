package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.dto.CategorySortDTO;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.mapper.CategoryMapper;
import com.example.ecommerce.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public Category createCategory(Category category) {
        category.setDeleted(0);
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getLevel() == null) {
            category.setLevel(category.getParentId() == 0 ? 1 : 2);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        save(category);
        return category;
    }

    @Override
    public Category updateCategory(Long id, Category category) {
        Category dbCategory = getById(id);
        if (dbCategory == null || !Integer.valueOf(0).equals(dbCategory.getDeleted())) {
            throw new IllegalArgumentException("商品分类不存在");
        }
        category.setId(id);
        updateById(category);
        return getById(id);
    }

    @Override
    public void deleteCategory(Long id) {
        long children = count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id)
                .eq(Category::getDeleted, 0));
        if (children > 0) {
            throw new IllegalArgumentException("请先删除子分类");
        }
        removeById(id);
    }

    @Override
    public List<Category> listCategories(Long parentId, Integer status) {
        return list(new LambdaQueryWrapper<Category>()
                .eq(parentId != null, Category::getParentId, parentId)
                .eq(status != null, Category::getStatus, status)
                .eq(Category::getDeleted, 0)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSort(List<CategorySortDTO> sortList) {
        for (CategorySortDTO sortDTO : sortList) {
            Category category = new Category();
            category.setId(sortDTO.getId());
            category.setSort(sortDTO.getSort());
            updateById(category);
        }
    }
}
