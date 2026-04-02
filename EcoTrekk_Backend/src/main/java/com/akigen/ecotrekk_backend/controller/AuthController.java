package com.akigen.ecotrekk_backend.controller;

import com.akigen.ecotrekk_backend.dto.request.AdminLoginRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.CustomerFirebaseLoginRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.CustomerProfileUpdateRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.CustomerRegisterRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.DeviceFcmTokenRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminLoginResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.CustomerAuthResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.CustomerRegisterResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.DeviceFcmTokenResponseDTO;
import com.akigen.ecotrekk_backend.service.AuthService;
import com.akigen.ecotrekk_backend.service.CustomerDeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final CustomerDeviceTokenService deviceTokenService;

    @PostMapping("/admin/login")
    public ResponseEntity<AdminLoginResponseDTO> adminLogin(@Valid @RequestBody AdminLoginRequestDTO request) {
        return ResponseEntity.ok(authService.adminLogin(request));
    }

    @PostMapping("/admin/logout")
    public ResponseEntity<Map<String, String>> adminLogout() {
        // JWT is stateless — the actual token is invalidated client-side.
        // This endpoint exists for audit purposes and future token blacklisting.
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/customer/register")
    public ResponseEntity<CustomerRegisterResponseDTO> customerRegister(
            @Valid @RequestBody CustomerRegisterRequestDTO request) {
        return ResponseEntity.ok(authService.registerCustomer(request));
    }

    @PostMapping("/customer/firebase")
    public ResponseEntity<CustomerAuthResponseDTO> customerFirebaseLogin(
            @Valid @RequestBody CustomerFirebaseLoginRequestDTO request) {
        return ResponseEntity.ok(authService.customerLoginWithFirebase(request));
    }

    @PostMapping(value = "/customer/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomerAuthResponseDTO> updateCustomerProfileImage(
            @RequestPart("idToken") String idToken,
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(authService.updateCustomerProfileImage(idToken, image));
    }

    @PostMapping("/customer/profile")
    public ResponseEntity<CustomerAuthResponseDTO> updateCustomerProfile(
            @Valid @RequestBody CustomerProfileUpdateRequestDTO request) {
        return ResponseEntity.ok(authService.updateCustomerProfile(request));
    }
    
    // Device FCM Token Endpoints
    
    @PostMapping("/customer/{customerId}/device-token")
    public ResponseEntity<DeviceFcmTokenResponseDTO> registerDeviceToken(
            @PathVariable Long customerId,
            @Valid @RequestBody DeviceFcmTokenRequestDTO request) {
        DeviceFcmTokenResponseDTO response = deviceTokenService.registerDeviceToken(customerId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/customer/{customerId}/device-tokens")
    public ResponseEntity<List<DeviceFcmTokenResponseDTO>> getActiveDeviceTokens(
            @PathVariable Long customerId) {
        List<DeviceFcmTokenResponseDTO> tokens = deviceTokenService.getActiveDeviceTokens(customerId);
        return ResponseEntity.ok(tokens);
    }
    
    @DeleteMapping("/customer/{customerId}/device-token/{deviceId}")
    public ResponseEntity<Void> unregisterDeviceToken(
            @PathVariable Long customerId,
            @PathVariable String deviceId) {
        deviceTokenService.unregisterDeviceToken(customerId, deviceId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/customer/{customerId}/device-tokens/logout")
    public ResponseEntity<Map<String, String>> deactivateAllDeviceTokens(
            @PathVariable Long customerId) {
        deviceTokenService.deactivateAllDeviceTokens(customerId);
        return ResponseEntity.ok(Map.of("message", "All device tokens deactivated"));
    }
}
