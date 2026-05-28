package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.CategorySortDTO;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<Category>> list(@RequestParam(required = false) Long parentId, @RequestParam(required = false) Integer status) {
        return Result.success(categoryService.listCategories(parentId, status));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Category> create(@RequestBody Category category) {
        return Result.success("分类新增成功", categoryService.createCategory(category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Category> update(@PathVariable Long id, @RequestBody Category category) {
        return Result.success("分类修改成功", categoryService.updateCategory(id, category));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("分类删除成功", null);
    }

    @PutMapping("/sort")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateSort(@Valid @RequestBody List<CategorySortDTO> sortList) {
        categoryService.updateSort(sortList);
        return Result.success("分类排序更新成功", null);
    }
}
