package com.akigen.ecotrekk.database.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.akigen.ecotrekk.database.EcoTrekkDatabase;
import com.akigen.ecotrekk.database.dao.FavoriteDao;
import com.akigen.ecotrekk.database.entity.FavoriteEntity;

import java.util.List;

/**
 * Repository for managing favorite/wishlist items
 */
public class FavoritesRepository {
    
    private static volatile FavoritesRepository instance;
    private final FavoriteDao favoriteDao;
    
    private FavoritesRepository(Context context) {
        EcoTrekkDatabase database = EcoTrekkDatabase.getInstance(context);
        this.favoriteDao = database.favoriteDao();
    }
    
    /**
     * Get singleton instance
     */
    public static FavoritesRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (FavoritesRepository.class) {
                if (instance == null) {
                    instance = new FavoritesRepository(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * Add gear to favorites
     */
    public void addToFavorites(FavoriteEntity favorite) {
        new Thread(() -> {
            // Check if already favorited
            if (!favoriteDao.isFavorite(favorite.getGearId())) {
                favoriteDao.insertFavorite(favorite);
            }
        }).start();
    }
    
    /**
     * Remove gear from favorites
     */
    public void removeFromFavorites(long gearId) {
        new Thread(() -> favoriteDao.deleteFavoriteByGearId(gearId)).start();
    }
    
    /**
     * Get all favorite items
     */
    public List<FavoriteEntity> getAllFavorites() {
        return favoriteDao.getAllFavorites();
    }

    /**
     * Observe all favorites as LiveData for real-time UI updates
     */
    public LiveData<List<FavoriteEntity>> observeAllFavorites() {
        return favoriteDao.observeAllFavorites();
    }
    
    /**
     * Get favorite by gear ID
     */
    public FavoriteEntity getFavoriteByGearId(long gearId) {
        return favoriteDao.getFavoriteByGearId(gearId);
    }
    
    /**
     * Get favorites by category
     */
    public List<FavoriteEntity> getFavoritesByCategory(String category) {
        return favoriteDao.getFavoritesByCategory(category);
    }
    
    /**
     * Check if gear is favorited
     */
    public boolean isFavorite(long gearId) {
        return favoriteDao.isFavorite(gearId);
    }
    
    /**
     * Get total number of favorites
     */
    public int getFavoriteCount() {
        return favoriteDao.getFavoriteCount();
    }
    
    /**
     * Update favorite item
     */
    public void updateFavorite(FavoriteEntity favorite) {
        new Thread(() -> favoriteDao.updateFavorite(favorite)).start();
    }
    
    /**
     * Toggle favorite status
     */
    public void toggleFavorite(FavoriteEntity favorite) {
        new Thread(() -> {
            if (favoriteDao.isFavorite(favorite.getGearId())) {
                favoriteDao.deleteFavoriteByGearId(favorite.getGearId());
            } else {
                favoriteDao.insertFavorite(favorite);
            }
        }).start();
    }
    
    /**
     * Clear all favorites
     */
    public void clearAllFavorites() {
        new Thread(favoriteDao::clearFavorites).start();
    }

    /**
     * Replace full local favorites snapshot (synchronous, call from background thread)
     */
    public void replaceAllFavorites(List<FavoriteEntity> favorites) {
        favoriteDao.clearFavorites();
        if (favorites == null || favorites.isEmpty()) {
            return;
        }
        favoriteDao.insertFavorites(favorites);
    }
}
