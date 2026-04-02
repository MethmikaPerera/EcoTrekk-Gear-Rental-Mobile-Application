package com.akigen.ecotrekk.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.akigen.ecotrekk.database.entity.FavoriteEntity;

import java.util.List;

/**
 * DAO for Favorite entity
 */
@Dao
public interface FavoriteDao {
    
    @Insert
    long insertFavorite(FavoriteEntity favorite);

    @Insert
    long[] insertFavorites(List<FavoriteEntity> favorites);
    
    @Update
    void updateFavorite(FavoriteEntity favorite);
    
    @Delete
    void deleteFavorite(FavoriteEntity favorite);
    
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    List<FavoriteEntity> getAllFavorites();

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    LiveData<List<FavoriteEntity>> observeAllFavorites();
    
    @Query("SELECT * FROM favorites WHERE id = :favoriteId LIMIT 1")
    FavoriteEntity getFavoriteById(long favoriteId);
    
    @Query("SELECT * FROM favorites WHERE gearId = :gearId LIMIT 1")
    FavoriteEntity getFavoriteByGearId(long gearId);
    
    @Query("SELECT COUNT(*) FROM favorites")
    int getFavoriteCount();
    
    @Query("DELETE FROM favorites WHERE id = :favoriteId")
    void deleteFavoriteById(long favoriteId);
    
    @Query("DELETE FROM favorites WHERE gearId = :gearId")
    void deleteFavoriteByGearId(long gearId);

    @Query("DELETE FROM favorites")
    void clearFavorites();
    
    @Query("SELECT * FROM favorites WHERE category = :category ORDER BY addedAt DESC")
    List<FavoriteEntity> getFavoritesByCategory(String category);
    
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM favorites WHERE gearId = :gearId")
    boolean isFavorite(long gearId);
}
