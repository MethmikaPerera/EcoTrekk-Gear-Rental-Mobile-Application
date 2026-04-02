package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.CustomerDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerDeviceTokenRepository extends JpaRepository<CustomerDeviceToken, Long> {
    
    /**
     * Find token by customer and device ID (for updates)
     */
    Optional<CustomerDeviceToken> findByCustomerIdAndDeviceId(Long customerId, String deviceId);
    
    /**
     * Find by FCM token (for webhook validation)
     */
    Optional<CustomerDeviceToken> findByFcmToken(String fcmToken);
    
    /**
     * Get all active tokens for a customer (for notifications)
     */
    List<CustomerDeviceToken> findByCustomerIdAndIsActiveTrue(Long customerId);
    
    /**
     * Get all tokens for a customer (regardless of active status)
     */
    List<CustomerDeviceToken> findAllByCustomerId(Long customerId);
    
    /**
     * Deactivate all tokens for a customer (logout flow)
     */
    @Modifying
    @Query("UPDATE CustomerDeviceToken c SET c.isActive = false WHERE c.customer.id = :customerId")
    void deactivateAllByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * Delete all inactive tokens older than specified timestamp (cleanup)
     */
    @Modifying
    @Query("DELETE FROM CustomerDeviceToken c WHERE c.isActive = false AND c.updatedAt < :cutoffDate")
    int deleteOldInactiveTokens(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
