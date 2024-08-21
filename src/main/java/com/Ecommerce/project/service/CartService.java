package com.Ecommerce.project.service;

import com.Ecommerce.project.payload.CartDTO;
import com.Ecommerce.project.payload.CartResponse;
import jakarta.transaction.Transactional;

public interface CartService {
    CartDTO addProductToCart(Long productId, int quantity);

    CartResponse getAllCarts(Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    CartDTO getCart();

    @Transactional
    CartDTO updateProductQuantityInCart(Long productId, String operation, Integer quantity);

    String deleteProductFromCart(Long cartId, Long productId);

    void updateProductInCarts(Long cartId, Long productId);
}
