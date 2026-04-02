package com.akigen.ecotrekk.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.akigen.ecotrekk.database.dao.CartItemDao;
import com.akigen.ecotrekk.database.dao.FavoriteDao;
import com.akigen.ecotrekk.database.dao.UserDao;
import com.akigen.ecotrekk.database.entity.CartItemEntity;
import com.akigen.ecotrekk.database.entity.FavoriteEntity;
import com.akigen.ecotrekk.database.entity.UserEntity;

/**
 * Room database for EcoTrekk application
 * Handles local storage for cart, user sessions, and favorites
 */
@Database(
    entities = {
        UserEntity.class,
        CartItemEntity.class,
        FavoriteEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class EcoTrekkDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "ecotrekk_db";
    private static volatile EcoTrekkDatabase instance;
    
    public abstract UserDao userDao();
    public abstract CartItemDao cartItemDao();
    public abstract FavoriteDao favoriteDao();
    
    /**
     * Get singleton instance of EcoTrekkDatabase
     */
    public static EcoTrekkDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (EcoTrekkDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.getApplicationContext(),
                        EcoTrekkDatabase.class,
                        DATABASE_NAME
                    ).build();
                }
            }
        }
        return instance;
    }
}
