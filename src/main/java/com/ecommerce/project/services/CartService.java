package com.ecommerce.project.services;

import com.ecommerce.project.dto.CartDTO;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);
}
