package com.akigen.ecotrekk.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.akigen.ecotrekk.database.entity.UserEntity;

/**
 * DAO for User entity
 */
@Dao
public interface UserDao {
    
    @Insert
    long insertUser(UserEntity user);
    
    @Update
    void updateUser(UserEntity user);
    
    @Delete
    void deleteUser(UserEntity user);
    
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    UserEntity getUserById(String userId);
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);
    
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    UserEntity getActiveUser();
    
    @Query("DELETE FROM users WHERE userId = :userId")
    void deleteUserById(String userId);
    
    @Query("UPDATE users SET isLoggedIn = 0 WHERE isLoggedIn = 1")
    void logoutAll();
}
