package com.akigen.ecotrekk_backend.controller;

import com.akigen.ecotrekk_backend.dto.upload.request.CloudinarySignatureRequest;
import com.akigen.ecotrekk_backend.dto.upload.response.CloudinarySignatureResponse;
import com.akigen.ecotrekk_backend.repository.HubManagerRepository;
import com.akigen.ecotrekk_backend.service.CloudinarySignedUploadService;
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

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadSignatureController {

    private final CloudinarySignedUploadService cloudinarySignedUploadService;
    private final HubManagerRepository hubManagerRepository;

    @PostMapping("/signature")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<CloudinarySignatureResponse> generateSignature(
            @Valid @RequestBody CloudinarySignatureRequest request,
            Authentication authentication
    ) {
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));

        if (!isSuperAdmin) {
            Long adminUserId = Long.parseLong(authentication.getName());
            String folderType = request.getFolderType() == null ? "" : request.getFolderType().trim().toLowerCase();
            if (!"hubs".equals(folderType) && !"gears".equals(folderType) && !"categories".equals(folderType)) {
                throw new ResponseStatusException(FORBIDDEN, "Admins can only upload images for hubs, gears, or categories");
            }
            if ("hubs".equals(folderType)) {
                if (request.getEntityId() == null) {
                    throw new ResponseStatusException(BAD_REQUEST, "entityId is required for hub image uploads");
                }
                boolean assigned = hubManagerRepository.existsByHubIdAndAdminUserId(request.getEntityId(), adminUserId);
                if (!assigned) {
                    throw new ResponseStatusException(FORBIDDEN, "You can only upload images for your assigned hub");
                }
            }
        }

        return ResponseEntity.ok(cloudinarySignedUploadService.generateUploadSignature(request));
    }
}
