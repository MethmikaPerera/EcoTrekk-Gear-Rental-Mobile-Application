package com.akigen.ecotrekk_backend.controller.admin;

import com.akigen.ecotrekk_backend.dto.request.ChangePasswordRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminRegisterResponseDTO;
import com.akigen.ecotrekk_backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final AdminService adminService;

    @GetMapping("/me")
    public ResponseEntity<AdminRegisterResponseDTO> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        AdminRegisterResponseDTO profile = adminService.getProfile(username);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changeMyPassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        String username = authentication.getName();
        adminService.changePassword(username, request);
        return ResponseEntity.ok().build();
    }
}
