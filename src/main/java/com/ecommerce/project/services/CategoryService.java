package com.ecommerce.project.services;

import com.ecommerce.project.models.Category;
import com.ecommerce.project.payloads.CategoryDTO;
import com.ecommerce.project.payloads.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize);
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    CategoryDTO deleteCategory(Long categoryId);

    CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId);
}
