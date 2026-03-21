package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.upload.request.CloudinaryAssetSaveRequest;
import com.akigen.ecotrekk_backend.entity.Customer;
import com.akigen.ecotrekk_backend.entity.Gear;
import com.akigen.ecotrekk_backend.entity.GearCategory;
import com.akigen.ecotrekk_backend.entity.GearImage;
import com.akigen.ecotrekk_backend.entity.Hub;
import com.akigen.ecotrekk_backend.repository.CustomerRepository;
import com.akigen.ecotrekk_backend.repository.GearCategoryRepository;
import com.akigen.ecotrekk_backend.repository.GearImageRepository;
import com.akigen.ecotrekk_backend.repository.GearRepository;
import com.akigen.ecotrekk_backend.repository.HubRepository;
import com.akigen.ecotrekk_backend.service.MediaAssetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MediaAssetServiceImpl implements MediaAssetService {

    private final GearRepository gearRepository;
    private final GearImageRepository gearImageRepository;
    private final HubRepository hubRepository;
    private final CustomerRepository customerRepository;
    private final GearCategoryRepository gearCategoryRepository;

    @Override
    @Transactional
    public void saveUploadedAsset(CloudinaryAssetSaveRequest request) {
        String folderType = request.getFolderType().trim().toLowerCase();

        switch (folderType) {
            case "gears" -> saveGearImage(request);
            case "hubs" -> saveHubImage(request);
            case "customers" -> saveCustomerProfileImage(request);
            case "categories" -> saveCategoryImage(request);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported folderType: " + request.getFolderType());
        }
    }

    private void saveGearImage(CloudinaryAssetSaveRequest request) {
        Gear gear = gearRepository.findById(request.getEntityId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gear not found"));

        GearImage gearImage = GearImage.builder()
                .gear(gear)
                .imageUrl(request.getSecureUrl())
                .primary(Boolean.TRUE.equals(request.getPrimaryImage()))
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .build();

        gearImageRepository.save(gearImage);
    }

    private void saveHubImage(CloudinaryAssetSaveRequest request) {
        Hub hub = hubRepository.findById(request.getEntityId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hub not found"));

        hub.setImageUrl(request.getSecureUrl());
        hubRepository.save(hub);
    }

    private void saveCustomerProfileImage(CloudinaryAssetSaveRequest request) {
        Customer customer = customerRepository.findById(request.getEntityId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        customer.setProfilePictureUrl(request.getSecureUrl());
        customerRepository.save(customer);
    }

    private void saveCategoryImage(CloudinaryAssetSaveRequest request) {
        GearCategory category = gearCategoryRepository.findById(request.getEntityId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        category.setImageUrl(request.getSecureUrl());
        gearCategoryRepository.save(category);
    }
}
