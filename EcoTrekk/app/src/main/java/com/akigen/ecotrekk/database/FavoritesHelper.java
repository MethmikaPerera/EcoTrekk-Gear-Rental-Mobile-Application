package com.akigen.ecotrekk.database;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.akigen.ecotrekk.api.WishlistApiHelper;
import com.akigen.ecotrekk.database.entity.FavoriteEntity;
import com.akigen.ecotrekk.database.repository.FavoritesRepository;

import java.util.List;

/**
 * Helper class for favorites/wishlist operations
 * Simplifies common favorites functionalities with server sync support
 */
public class FavoritesHelper {
    private static final String TAG = "FavoritesHelper";
    
    private static volatile FavoritesHelper instance;
    private final FavoritesRepository favoritesRepository;
    private WishlistApiHelper wishlistApiHelper;
    private Long customerId;
    
    private FavoritesHelper(Context context) {
        this.favoritesRepository = FavoritesRepository.getInstance(context);
    }
    
    /**
     * Get singleton instance
     */
    public static FavoritesHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (FavoritesHelper.class) {
                if (instance == null) {
                    instance = new FavoritesHelper(context);
                }
            }
        }
        return instance;
    }

    /**
     * Initialize with customer ID for server sync
     */
    public void initializeWithCustomerId(Long customerId) {
        this.customerId = customerId;
        if (customerId != null && customerId > 0) {
            this.wishlistApiHelper = new WishlistApiHelper(customerId);
        }
    }
    
    /**
     * Add gear to favorites (local only)
     */
    public void addToFavorites(long gearId, String gearName, String category,
                              double pricePerDay, double depositAmount,
                              String description, String imageUrl) {
        FavoriteEntity favorite = new FavoriteEntity(
            gearId, gearName, category, pricePerDay, depositAmount,
            description, imageUrl
        );
        favoritesRepository.addToFavorites(favorite);
    }

    /**
     * Add gear to favorites and sync with server
     */
    public void addToFavoritesWithServerSync(long gearId, String gearName, String category,
                                            double pricePerDay, double depositAmount,
                                            String description, String imageUrl) {
        // Add to local database
        addToFavorites(gearId, gearName, category, pricePerDay, depositAmount, description, imageUrl);

        // Sync with server
        if (wishlistApiHelper != null) {
            wishlistApiHelper.addToWishlist(gearId, new WishlistApiHelper.WishlistCallback<com.akigen.ecotrekk.dto.WishlistItemResponse>() {
                @Override
                public void onSuccess(com.akigen.ecotrekk.dto.WishlistItemResponse result) {
                    // Server sync successful
                }

                @Override
                public void onError(String message) {
                    Log.w(TAG, "Server sync add failed: " + message);
                }
            });
        }
    }
    
    /**
     * Remove gear from favorites (local only)
     */
    public void removeFromFavorites(long gearId) {
        favoritesRepository.removeFromFavorites(gearId);
    }

    /**
     * Remove gear from favorites and sync with server
     */
    public void removeFromFavoritesWithServerSync(long gearId) {
        // Remove from local database
        removeFromFavorites(gearId);

        // Sync with server
        if (wishlistApiHelper != null) {
            wishlistApiHelper.removeFromWishlist(gearId, new WishlistApiHelper.WishlistCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Server sync successful
                }

                @Override
                public void onError(String message) {
                    Log.w(TAG, "Server sync remove failed: " + message);
                }
            });
        }
    }
    
    /**
     * Check if gear is favorited
     */
    public boolean isFavorite(long gearId) {
        return favoritesRepository.isFavorite(gearId);
    }
    
    /**
     * Toggle favorite status (local only)
     */
    public void toggleFavorite(long gearId, String gearName, String category,
                              double pricePerDay, double depositAmount,
                              String description, String imageUrl) {
        FavoriteEntity favorite = new FavoriteEntity(
            gearId, gearName, category, pricePerDay, depositAmount,
            description, imageUrl
        );
        favoritesRepository.toggleFavorite(favorite);
    }

    /**
     * Toggle favorite status and sync with server
     */
    public void toggleFavoriteWithServerSync(long gearId, String gearName, String category,
                                            double pricePerDay, double depositAmount,
                                            String description, String imageUrl) {
        boolean isFavorited = isFavorite(gearId);
        
        if (isFavorited) {
            // Remove from favorites
            removeFromFavoritesWithServerSync(gearId);
        } else {
            // Add to favorites
            addToFavoritesWithServerSync(gearId, gearName, category, pricePerDay, depositAmount, description, imageUrl);
        }
    }
    
    /**
     * Get all favorite items
     */
    public List<FavoriteEntity> getAllFavorites() {
        return favoritesRepository.getAllFavorites();
    }

    /**
     * Observe all favorites for real-time UI updates
     */
    public LiveData<List<FavoriteEntity>> observeFavorites() {
        return favoritesRepository.observeAllFavorites();
    }
    
    /**
     * Get favorites by category
     */
    public List<FavoriteEntity> getFavoritesByCategory(String category) {
        return favoritesRepository.getFavoritesByCategory(category);
    }
    
    /**
     * Get total favorites count
     */
    public int getFavoriteCount() {
        return favoritesRepository.getFavoriteCount();
    }
    
    /**
     * Clear all favorites (local only)
     */
    public void clearAllFavorites() {
        favoritesRepository.clearAllFavorites();
    }

    /**
     * Replace local favorites snapshot (call from background thread)
     */
    public void replaceFavorites(List<FavoriteEntity> favorites) {
        favoritesRepository.replaceAllFavorites(favorites);
    }

    /**
     * Clear all favorites and sync with server
     */
    public void clearAllFavoritesWithServerSync() {
        clearAllFavorites();

        if (wishlistApiHelper != null) {
            wishlistApiHelper.clearWishlist(new WishlistApiHelper.WishlistCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Server sync successful
                }

                @Override
                public void onError(String message) {
                    Log.w(TAG, "Server sync clear failed: " + message);
                }
            });
        }
    }
}
