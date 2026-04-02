package com.akigen.ecotrekk_backend.controller.customer;

import com.akigen.ecotrekk_backend.dto.response.WishlistItemDTO;
import com.akigen.ecotrekk_backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * Get all wishlist items for the current customer
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<List<WishlistItemDTO>> getWishlist(@PathVariable Long customerId) {
        List<WishlistItemDTO> wishlist = wishlistService.getCustomerWishlist(customerId);
        return ResponseEntity.ok(wishlist);
    }

    /**
     * Add a gear to the wishlist
     */
    @PostMapping("/{customerId}/add/{gearId}")
    public ResponseEntity<WishlistItemDTO> addToWishlist(
            @PathVariable Long customerId,
            @PathVariable Long gearId) {
        WishlistItemDTO item = wishlistService.addToWishlist(customerId, gearId);
        return ResponseEntity.ok(item);
    }

    /**
     * Remove a gear from the wishlist
     */
    @DeleteMapping("/{customerId}/{gearId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable Long customerId,
            @PathVariable Long gearId) {
        wishlistService.removeFromWishlist(customerId, gearId);
        return ResponseEntity.ok().build();
    }

    /**
     * Check if a gear is in the wishlist
     */
    @GetMapping("/{customerId}/check/{gearId}")
    public ResponseEntity<Boolean> isInWishlist(
            @PathVariable Long customerId,
            @PathVariable Long gearId) {
        boolean exists = wishlistService.isInWishlist(customerId, gearId);
        return ResponseEntity.ok(exists);
    }

    /**
     * Get wishlist count for the customer
     */
    @GetMapping("/{customerId}/count")
    public ResponseEntity<Integer> getWishlistCount(@PathVariable Long customerId) {
        int count = wishlistService.getWishlistCount(customerId);
        return ResponseEntity.ok(count);
    }

    /**
     * Clear all wishlist items for the customer
     */
    @DeleteMapping("/{customerId}/clear")
    public ResponseEntity<Void> clearWishlist(@PathVariable Long customerId) {
        wishlistService.clearWishlist(customerId);
        return ResponseEntity.ok().build();
    }
}
