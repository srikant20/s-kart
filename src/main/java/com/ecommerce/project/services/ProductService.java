package com.ecommerce.project.services;

import com.ecommerce.project.dto.ProductDTO;
import com.ecommerce.project.models.Product;
import com.ecommerce.project.payloads.response.ProductResponse;

public interface ProductService {
    ProductDTO addProduct(Product product, Long categoryId);

    ProductResponse getAllProducts();

    ProductResponse searchByCategory(Long categoryId);
}
