package com.akigen.ecotrekk_backend.controller;

import com.akigen.ecotrekk_backend.dto.upload.request.CloudinaryAssetSaveRequest;
import com.akigen.ecotrekk_backend.repository.HubManagerRepository;
import com.akigen.ecotrekk_backend.service.MediaAssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/media-assets")
@RequiredArgsConstructor
public class MediaAssetController {

    private final MediaAssetService mediaAssetService;
    private final HubManagerRepository hubManagerRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> saveUploadedAsset(
            @Valid @RequestBody CloudinaryAssetSaveRequest request,
            Authentication authentication) {
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));

        if (!isSuperAdmin) {
            Long adminUserId = Long.parseLong(authentication.getName());
            String folderType = request.getFolderType() == null ? "" : request.getFolderType().trim().toLowerCase();
            if (!"hubs".equals(folderType) && !"gears".equals(folderType) && !"categories".equals(folderType)) {
                throw new ResponseStatusException(FORBIDDEN, "Admins can only save images for hubs, gears, or categories");
            }
            if ("hubs".equals(folderType)) {
                boolean assigned = hubManagerRepository.existsByHubIdAndAdminUserId(request.getEntityId(), adminUserId);
                if (!assigned) {
                    throw new ResponseStatusException(FORBIDDEN, "You can only update images for your assigned hub");
                }
            }
        }

        mediaAssetService.saveUploadedAsset(request);
        return ResponseEntity.ok().build();
    }
}
