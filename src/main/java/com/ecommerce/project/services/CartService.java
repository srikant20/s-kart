package com.ecommerce.project.services;

import com.ecommerce.project.dto.CartDTO;

import java.util.List;

public interface CartService {
    List<CartDTO> getAllCarts();

    CartDTO addProductToCart(Long productId, Integer quantity);

    CartDTO getCart(String emailId, Long cartId);
}
