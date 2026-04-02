package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.request.DeviceFcmTokenRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.DeviceFcmTokenResponseDTO;
import java.util.List;

public interface CustomerDeviceTokenService {
    
    /**
     * Register or update a device FCM token
     */
    DeviceFcmTokenResponseDTO registerDeviceToken(Long customerId, DeviceFcmTokenRequestDTO request);
    
    /**
     * Get all active device tokens for a customer
     */
    List<DeviceFcmTokenResponseDTO> getActiveDeviceTokens(Long customerId);
    
    /**
     * Unregister a specific device
     */
    void unregisterDeviceToken(Long customerId, String deviceId);
    
    /**
     * Deactivate all device tokens (logout)
     */
    void deactivateAllDeviceTokens(Long customerId);
    
    /**
     * Get customer ID by FCM token (for internal use)
     */
    Long getCustomerIdByFcmToken(String fcmToken);
}
