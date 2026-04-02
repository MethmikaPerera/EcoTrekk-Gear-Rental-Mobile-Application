package com.akigen.ecotrekk.database;

import android.content.Context;

import com.akigen.ecotrekk.database.repository.CartRepository;
import com.akigen.ecotrekk.database.repository.FavoritesRepository;
import com.akigen.ecotrekk.database.repository.UserRepository;

/**
 * Central manager for all database operations
 * Provides easy access to all repositories
 */
public class DatabaseManager {
    
    private static volatile DatabaseManager instance;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final FavoritesRepository favoritesRepository;
    
    private DatabaseManager(Context context) {
        this.userRepository = UserRepository.getInstance(context);
        this.cartRepository = CartRepository.getInstance(context);
        this.favoritesRepository = FavoritesRepository.getInstance(context);
    }
    
    /**
     * Get singleton instance
     */
    public static DatabaseManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * Get user repository
     */
    public UserRepository getUserRepository() {
        return userRepository;
    }
    
    /**
     * Get cart repository
     */
    public CartRepository getCartRepository() {
        return cartRepository;
    }
    
    /**
     * Get favorites repository
     */
    public FavoritesRepository getFavoritesRepository() {
        return favoritesRepository;
    }
}
