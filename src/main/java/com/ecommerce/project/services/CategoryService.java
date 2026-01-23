package com.ecommerce.project.services;

import com.ecommerce.project.models.Category;
import com.ecommerce.project.payloads.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse getAllCategories();
    void createCategory(Category category);

    String deleteCategory(Long categoryId);

    Category updateCategory(Category category, Long categoryId);
}
