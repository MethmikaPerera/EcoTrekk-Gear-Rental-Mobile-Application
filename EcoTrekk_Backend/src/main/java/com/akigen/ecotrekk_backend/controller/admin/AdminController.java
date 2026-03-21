package com.akigen.ecotrekk_backend.controller.admin;

import com.akigen.ecotrekk_backend.dto.request.AdminRegisterRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.AdminStatusUpdateRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminRegisterResponseDTO;
import com.akigen.ecotrekk_backend.enums.AdminRole;
import com.akigen.ecotrekk_backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<String>> getAdminRoles() {
        List<String> roles = Arrays.stream(AdminRole.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminRegisterResponseDTO> createAdmin(@Valid @RequestBody AdminRegisterRequestDTO request) {
        AdminRegisterResponseDTO response = adminService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.akigen.ecotrekk_backend.dto.response.AdminUserPageResponseDTO> getAdmins(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AdminRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminService.getAdmins(search, role, page, size));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminRegisterResponseDTO> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody com.akigen.ecotrekk_backend.dto.request.AdminUpdateRequestDTO request) {
        AdminRegisterResponseDTO response = adminService.updateAdmin(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> updateAdminStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminStatusUpdateRequestDTO request) {
        boolean active = "ACTIVE".equalsIgnoreCase(request.getStatus());
        adminService.updateAdminStatus(id, active);
        return ResponseEntity.ok().build();
    }
}
