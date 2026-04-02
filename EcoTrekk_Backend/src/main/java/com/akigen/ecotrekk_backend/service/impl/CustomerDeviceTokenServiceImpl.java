package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.request.DeviceFcmTokenRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.DeviceFcmTokenResponseDTO;
import com.akigen.ecotrekk_backend.entity.Customer;
import com.akigen.ecotrekk_backend.entity.CustomerDeviceToken;
import com.akigen.ecotrekk_backend.repository.CustomerDeviceTokenRepository;
import com.akigen.ecotrekk_backend.repository.CustomerRepository;
import com.akigen.ecotrekk_backend.service.CustomerDeviceTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class CustomerDeviceTokenServiceImpl implements CustomerDeviceTokenService {
    
    private final CustomerDeviceTokenRepository deviceTokenRepository;
    private final CustomerRepository customerRepository;
    
    public CustomerDeviceTokenServiceImpl(CustomerDeviceTokenRepository deviceTokenRepository,
                                         CustomerRepository customerRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.customerRepository = customerRepository;
    }
    
    @Override
    public DeviceFcmTokenResponseDTO registerDeviceToken(Long customerId, DeviceFcmTokenRequestDTO request) {
        // Verify customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        
        // Check if device token already exists
        var existingToken = deviceTokenRepository.findByCustomerIdAndDeviceId(customerId, request.getDeviceId());
        
        CustomerDeviceToken token;
        if (existingToken.isPresent()) {
            // Update existing token
            token = existingToken.get();
            token.setFcmToken(request.getFcmToken());
            token.setIsActive(true);
            token.setLastUpdated(LocalDateTime.now());
            log.info("Updating device token for customer: {} device: {}", customerId, request.getDeviceId());
        } else {
            // Create new token
            token = CustomerDeviceToken.builder()
                    .customer(customer)
                    .deviceId(request.getDeviceId())
                    .fcmToken(request.getFcmToken())
                    .deviceModel(request.getDeviceModel())
                    .osVersion(request.getOsVersion())
                    .isActive(true)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            log.info("Registering new device token for customer: {} device: {}", customerId, request.getDeviceId());
        }
        
        token = deviceTokenRepository.save(token);
        return mapToDTO(token);
    }
    
    @Override
    public List<DeviceFcmTokenResponseDTO> getActiveDeviceTokens(Long customerId) {
        // Verify customer exists
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        
        List<CustomerDeviceToken> tokens = deviceTokenRepository.findByCustomerIdAndIsActiveTrue(customerId);
        return tokens.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public void unregisterDeviceToken(Long customerId, String deviceId) {
        CustomerDeviceToken token = deviceTokenRepository.findByCustomerIdAndDeviceId(customerId, deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device token not found"));
        
        deviceTokenRepository.delete(token);
        log.info("Device token deleted for customer: {} device: {}", customerId, deviceId);
    }
    
    @Override
    public void deactivateAllDeviceTokens(Long customerId) {
        deviceTokenRepository.deactivateAllByCustomerId(customerId);
        log.info("All device tokens deactivated for customer: {}", customerId);
    }
    
    @Override
    public Long getCustomerIdByFcmToken(String fcmToken) {
        return deviceTokenRepository.findByFcmToken(fcmToken)
                .map(token -> token.getCustomer().getId())
                .orElse(null);
    }
    
    private DeviceFcmTokenResponseDTO mapToDTO(CustomerDeviceToken token) {
        return DeviceFcmTokenResponseDTO.builder()
                .id(token.getId())
                .deviceId(token.getDeviceId())
                .deviceModel(token.getDeviceModel())
                .osVersion(token.getOsVersion())
                .isActive(token.getIsActive())
                .lastUpdated(token.getLastUpdated())
                .build();
    }
}
