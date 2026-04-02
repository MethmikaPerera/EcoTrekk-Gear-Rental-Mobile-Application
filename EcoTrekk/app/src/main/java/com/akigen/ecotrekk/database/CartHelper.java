package com.akigen.ecotrekk.database;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.akigen.ecotrekk.database.entity.CartItemEntity;
import com.akigen.ecotrekk.database.repository.CartRepository;

import java.util.List;

/**
 * Helper class for cart operations
 * Simplifies common cart functionalities
 */
public class CartHelper {
    
    private static volatile CartHelper instance;
    private final CartRepository cartRepository;
    
    private CartHelper(Context context) {
        this.cartRepository = CartRepository.getInstance(context);
    }
    
    /**
     * Get singleton instance
     */
    public static CartHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (CartHelper.class) {
                if (instance == null) {
                    instance = new CartHelper(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * Add gear to cart with all details
     */
    public void addToCart(long gearId, String gearName, String category,
                         int quantity, long startDate, long endDate,
                         double pricePerDay, double depositAmount,
                         long hubId, String hubName, String imageUrl) {
        CartItemEntity cartItem = new CartItemEntity(
            gearId, gearName, category, quantity, startDate, endDate,
            pricePerDay, depositAmount, hubId, hubName, imageUrl
        );
        cartRepository.addCartItem(cartItem);
    }
    
    /**
     * Get all cart items
     */
    public java.util.List<CartItemEntity> getCartItems() {
        return cartRepository.getAllCartItems();
    }
    
    /**
     * Get cart total price
     */
    public double getCartTotal() {
        return cartRepository.getCartTotal();
    }
    
    /**
     * Get total deposit amount
     */
    public double getTotalDeposit() {
        return cartRepository.getTotalDeposits();
    }
    
    /**
     * Get total items count
     */
    public int getItemCount() {
        return cartRepository.getCartItemCount();
    }
    
    /**
     * Update item quantity
     */
    public void updateItemQuantity(long cartItemId, int newQuantity) {
        new Thread(() -> {
            CartItemEntity item = cartRepository.getCartItem(cartItemId);
            if (item != null) {
                item.setQuantity(newQuantity);
                cartRepository.updateCartItem(item);
            }
        }).start();
    }
    
    /**
     * Remove item from cart
     */
    public void removeItem(long cartItemId) {
        cartRepository.removeCartItem(cartItemId);
    }
    
    /**
     * Remove gear from cart by gear ID
     */
    public void removeGear(long gearId) {
        cartRepository.removeGearFromCart(gearId);
    }
    
    /**
     * Clear entire cart
     */
    public void clearCart() {
        cartRepository.clearCart();
    }
    
    /**
     * Check if gear is in cart
     */
    public boolean isInCart(long gearId) {
        return cartRepository.isGearInCart(gearId);
    }

    /**
     * Observe all cart items as LiveData (for reactive UI updates)
     */
    public LiveData<List<CartItemEntity>> observeCartItems() {
        return cartRepository.observeAllCartItems();
    }

    /**
     * Replace all cart items atomically (clear existing, insert new list)
     */
    public void replaceCartItems(List<CartItemEntity> items) {
        cartRepository.replaceAllCartItems(items);
    }
}
