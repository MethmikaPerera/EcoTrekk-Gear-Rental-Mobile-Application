package com.akigen.ecotrekk_backend.controller.admin;

import com.akigen.ecotrekk_backend.dto.request.AdminStatusUpdateRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.GearCreateRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.GearCategoryUpsertRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.GearAdminListItemDTO;
import com.akigen.ecotrekk_backend.dto.response.GearCategoryOptionDTO;
import com.akigen.ecotrekk_backend.dto.response.GearPageResponseDTO;
import com.akigen.ecotrekk_backend.service.GearService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/admin/gears")
@RequiredArgsConstructor
public class AdminGearController {

    private final GearService gearService;

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','STAFF')")
    public ResponseEntity<List<GearCategoryOptionDTO>> getGearCategories() {
        return ResponseEntity.ok(gearService.getGearCategories());
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<GearCategoryOptionDTO> createGearCategory(
            @Valid @RequestBody GearCategoryUpsertRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gearService.createGearCategory(request));
    }

    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<GearCategoryOptionDTO> updateGearCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody GearCategoryUpsertRequestDTO request
    ) {
        return ResponseEntity.ok(gearService.updateGearCategory(categoryId, request));
    }

    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> deleteGearCategory(@PathVariable Long categoryId) {
        gearService.deleteGearCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<GearAdminListItemDTO> createGear(@Valid @RequestBody GearCreateRequestDTO request) {
        return ResponseEntity.ok(gearService.createAdminGear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<GearAdminListItemDTO> updateGear(
            @PathVariable Long id,
            @Valid @RequestBody GearCreateRequestDTO request
    ) {
        return ResponseEntity.ok(gearService.updateAdminGear(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> updateGearStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminStatusUpdateRequestDTO request
    ) {
        String status = request.getStatus() == null ? "" : request.getStatus().trim();
        if (!"ACTIVE".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status)) {
            throw new org.springframework.web.server.ResponseStatusException(BAD_REQUEST, "Status must be ACTIVE or INACTIVE");
        }

        gearService.updateGearStatus(id, "ACTIVE".equalsIgnoreCase(status));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','STAFF')")
    public ResponseEntity<GearPageResponseDTO> getGears(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(gearService.getAdminGears(page, size, search, category, status, sort));
    }
}
