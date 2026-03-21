package com.akigen.ecotrekk_backend.controller.admin;

import com.akigen.ecotrekk_backend.dto.request.HubUpsertRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.HubAdminResponseDTO;
import com.akigen.ecotrekk_backend.service.HubService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/hubs")
@RequiredArgsConstructor
public class AdminHubController {

    private final HubService hubService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<HubAdminResponseDTO>> getAllHubs() {
        return ResponseEntity.ok(hubService.getAllHubsForAdmin());
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<HubAdminResponseDTO> getMyHub(Authentication authentication) {
        Long adminUserId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(hubService.getHubForAdminUser(adminUserId));
    }

    @PutMapping("/my")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HubAdminResponseDTO> updateMyHub(
            Authentication authentication,
            @Valid @RequestBody HubUpsertRequestDTO request) {
        Long adminUserId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(hubService.updateHubForAdminUser(adminUserId, request));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<HubAdminResponseDTO> createHub(@Valid @RequestBody HubUpsertRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hubService.createHub(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<HubAdminResponseDTO> updateHub(
            @PathVariable Long id,
            @Valid @RequestBody HubUpsertRequestDTO request) {
        return ResponseEntity.ok(hubService.updateHub(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteHub(@PathVariable Long id) {
        hubService.deleteHub(id);
        return ResponseEntity.noContent().build();
    }
}
