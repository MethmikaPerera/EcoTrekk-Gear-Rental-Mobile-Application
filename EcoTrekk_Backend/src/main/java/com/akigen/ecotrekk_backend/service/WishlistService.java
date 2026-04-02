package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.response.WishlistItemDTO;

import java.util.List;

public interface WishlistService {
    /**
     * Get all wishlist items for a customer
     */
    List<WishlistItemDTO> getCustomerWishlist(Long customerId);

    /**
     * Add item to wishlist
     */
    WishlistItemDTO addToWishlist(Long customerId, Long gearId);

    /**
     * Remove item from wishlist
     */
    void removeFromWishlist(Long customerId, Long gearId);

    /**
     * Check if item is in wishlist
     */
    boolean isInWishlist(Long customerId, Long gearId);

    /**
     * Get wishlist count for customer
     */
    int getWishlistCount(Long customerId);

    /**
     * Clear all wishlist items for customer
     */
    void clearWishlist(Long customerId);
}
