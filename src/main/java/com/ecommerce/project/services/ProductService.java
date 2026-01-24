package com.ecommerce.project.services;

import com.ecommerce.project.dto.ProductDTO;
import com.ecommerce.project.models.Product;

public interface ProductService {
    ProductDTO addProduct(Product product, Long categoryId);
}
