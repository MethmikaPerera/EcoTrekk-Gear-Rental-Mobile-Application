package com.akigen.ecotrekk_backend.controller;

import com.akigen.ecotrekk_backend.dto.request.AdminLoginRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.CustomerFirebaseLoginRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.CustomerRegisterRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminLoginResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.CustomerAuthResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.CustomerRegisterResponseDTO;
import com.akigen.ecotrekk_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

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
}
