package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.request.CartAddRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.CartItemDTO;

import java.util.List;

public interface CartService {

    /**
     * Add a gear item to customer's cart; merges quantity if same gear and hub already exist.
     */
    CartItemDTO addToCart(Long customerId, CartAddRequestDTO request);

    /**
     * Get all cart items for a customer.
     */
    List<CartItemDTO> getCartItems(Long customerId);

    /**
     * Remove a specific cart item belonging to the customer.
     */
    void removeCartItem(Long customerId, Long cartItemId);
}
