package com.akigen.ecotrekk_backend.controller.admin;

import com.akigen.ecotrekk_backend.dto.request.AdminStatusUpdateRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminCustomerPageResponseDTO;
import com.akigen.ecotrekk_backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final AdminService adminService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','STAFF')")
    public ResponseEntity<AdminCustomerPageResponseDTO> getCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminService.getCustomers(search, status, page, size));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> updateCustomerStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminStatusUpdateRequestDTO request
    ) {
        boolean active = "ACTIVE".equalsIgnoreCase(request.getStatus());
        adminService.updateCustomerStatus(id, active);
        return ResponseEntity.ok().build();
    }
}