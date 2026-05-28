package com.example.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ecommerce.dto.CategorySortDTO;
import com.example.ecommerce.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    Category createCategory(Category category);

    Category updateCategory(Long id, Category category);

    void deleteCategory(Long id);

    List<Category> listCategories(Long parentId, Integer status);

    void updateSort(List<CategorySortDTO> sortList);
}
