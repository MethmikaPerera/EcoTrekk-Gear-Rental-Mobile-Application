package com.akigen.ecotrekk.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.akigen.ecotrekk.database.entity.CartItemEntity;

import java.util.List;

/**
 * DAO for CartItem entity
 */
@Dao
public interface CartItemDao {
    
    @Insert
    long insertCartItem(CartItemEntity cartItem);
    
    @Insert
    long[] insertCartItems(CartItemEntity... cartItems);

    @Insert
    void insertCartItemsList(List<CartItemEntity> cartItems);
    
    @Update
    void updateCartItem(CartItemEntity cartItem);
    
    @Delete
    void deleteCartItem(CartItemEntity cartItem);
    
    @Query("SELECT * FROM cart_items ORDER BY addedAt DESC")
    List<CartItemEntity> getAllCartItems();

    @Query("SELECT * FROM cart_items ORDER BY addedAt DESC")
    LiveData<List<CartItemEntity>> observeAllCartItems();
    
    @Query("SELECT * FROM cart_items WHERE id = :cartItemId LIMIT 1")
    CartItemEntity getCartItemById(long cartItemId);
    
    @Query("SELECT * FROM cart_items WHERE gearId = :gearId LIMIT 1")
    CartItemEntity getCartItemByGearId(long gearId);
    
    @Query("SELECT COUNT(*) FROM cart_items")
    int getCartItemCount();
    
    @Query("DELETE FROM cart_items WHERE id = :cartItemId")
    void deleteCartItemById(long cartItemId);
    
    @Query("DELETE FROM cart_items")
    void clearCart();
    
    @Query("SELECT SUM(quantity) FROM cart_items")
    int getTotalQuantity();
    
    @Query("SELECT * FROM cart_items WHERE hubId = :hubId")
    List<CartItemEntity> getCartItemsByHub(long hubId);
}
