package com.akigen.ecotrekk.database.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.akigen.ecotrekk.database.EcoTrekkDatabase;
import com.akigen.ecotrekk.database.dao.CartItemDao;
import com.akigen.ecotrekk.database.entity.CartItemEntity;

import java.util.List;

/**
 * Repository for managing shopping cart items
 */
public class CartRepository {
    
    private static volatile CartRepository instance;
    private final CartItemDao cartItemDao;
    
    private CartRepository(Context context) {
        EcoTrekkDatabase database = EcoTrekkDatabase.getInstance(context);
        this.cartItemDao = database.cartItemDao();
    }
    
    /**
     * Get singleton instance
     */
    public static CartRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (CartRepository.class) {
                if (instance == null) {
                    instance = new CartRepository(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * Add item to cart
     */
    public void addCartItem(CartItemEntity cartItem) {
        new Thread(() -> {
            // Check if gear already in cart, if so update quantity
            CartItemEntity existing = cartItemDao.getCartItemByGearId(cartItem.getGearId());
            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + cartItem.getQuantity());
                cartItemDao.updateCartItem(existing);
            } else {
                cartItemDao.insertCartItem(cartItem);
            }
        }).start();
    }
    
    /**
     * Get all cart items
     */
    public List<CartItemEntity> getAllCartItems() {
        return cartItemDao.getAllCartItems();
    }
    
    /**
     * Get cart item by ID
     */
    public CartItemEntity getCartItem(long cartItemId) {
        return cartItemDao.getCartItemById(cartItemId);
    }
    
    /**
     * Update cart item quantity or other details
     */
    public void updateCartItem(CartItemEntity cartItem) {
        new Thread(() -> cartItemDao.updateCartItem(cartItem)).start();
    }
    
    /**
     * Remove item from cart
     */
    public void removeCartItem(long cartItemId) {
        new Thread(() -> cartItemDao.deleteCartItemById(cartItemId)).start();
    }
    
    /**
     * Remove specific gear from cart
     */
    public void removeGearFromCart(long gearId) {
        new Thread(() -> {
            CartItemEntity item = cartItemDao.getCartItemByGearId(gearId);
            if (item != null) {
                cartItemDao.deleteCartItem(item);
            }
        }).start();
    }
    
    /**
     * Get total quantity of items in cart
     */
    public int getCartItemCount() {
        return cartItemDao.getCartItemCount();
    }
    
    /**
     * Get total quantity (sum of all quantities)
     */
    public int getTotalQuantity() {
        Integer total = cartItemDao.getTotalQuantity();
        return total != null ? total : 0;
    }
    
    /**
     * Get cart items for specific hub
     */
    public List<CartItemEntity> getCartItemsByHub(long hubId) {
        return cartItemDao.getCartItemsByHub(hubId);
    }
    
    /**
     * Calculate total cart price
     */
    public double getCartTotal() {
        List<CartItemEntity> items = getAllCartItems();
        double total = 0;
        for (CartItemEntity item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }
    
    /**
     * Calculate total deposits
     */
    public double getTotalDeposits() {
        List<CartItemEntity> items = getAllCartItems();
        double total = 0;
        for (CartItemEntity item : items) {
            total += (item.getDepositAmount() * item.getQuantity());
        }
        return total;
    }
    
    /**
     * Clear entire cart
     */
    public void clearCart() {
        new Thread(cartItemDao::clearCart).start();
    }
    
    /**
     * Check if gear already in cart
     */
    public boolean isGearInCart(long gearId) {
        CartItemEntity item = cartItemDao.getCartItemByGearId(gearId);
        return item != null;
    }

    /**
     * Observe all cart items as LiveData
     */
    public LiveData<List<CartItemEntity>> observeAllCartItems() {
        return cartItemDao.observeAllCartItems();
    }

    /**
     * Replace all cart items atomically (clear then insert)
     */
    public void replaceAllCartItems(List<CartItemEntity> items) {
        new Thread(() -> {
            cartItemDao.clearCart();
            if (items != null && !items.isEmpty()) {
                cartItemDao.insertCartItemsList(items);
            }
        }).start();
    }
}
