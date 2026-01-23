package com.ecommerce.project.services;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.models.Category;
import com.ecommerce.project.payloads.CategoryDTO;
import com.ecommerce.project.payloads.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private List<Category> categories = new ArrayList<>();

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories() {

        List<Category> categories=  categoryRepository.findAll();
        if(categories.isEmpty())
            throw new APIException("Currently, no any category created.");

        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        return categoryResponse;
    }

    @Override
    public void createCategory(Category category) {
        Category savedCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if(savedCategory != null)
            throw new APIException("Category "+category.getCategoryName()+" already exists.!!");
        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) { //we could use deleteById but in that case we will not able to throw exception
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","CategoryId", categoryId));
        categoryRepository.delete(category);
        return "Category with CategoryId: "+ categoryId +" deleted successfully!";
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {
        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","CategoryId", categoryId));
        savedCategory = categoryRepository.save(category);
        return savedCategory;
    }
}
