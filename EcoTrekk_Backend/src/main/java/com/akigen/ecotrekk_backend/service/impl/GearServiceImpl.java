package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.request.GearCreateRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.GearCategoryUpsertRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.GearAdminListItemDTO;
import com.akigen.ecotrekk_backend.dto.response.GearCategoryOptionDTO;
import com.akigen.ecotrekk_backend.dto.response.GearPageResponseDTO;
import com.akigen.ecotrekk_backend.entity.Gear;
import com.akigen.ecotrekk_backend.entity.GearCategory;
import com.akigen.ecotrekk_backend.entity.Hub;
import com.akigen.ecotrekk_backend.entity.HubInventory;
import com.akigen.ecotrekk_backend.repository.GearCategoryRepository;
import com.akigen.ecotrekk_backend.repository.GearRepository;
import com.akigen.ecotrekk_backend.repository.HubRepository;
import com.akigen.ecotrekk_backend.repository.HubInventoryRepository;
import com.akigen.ecotrekk_backend.service.GearService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GearServiceImpl implements GearService {

    private final HubInventoryRepository hubInventoryRepository;
    private final GearRepository gearRepository;
    private final GearCategoryRepository gearCategoryRepository;
    private final HubRepository hubRepository;

    @Override
    @Transactional
    public GearAdminListItemDTO createAdminGear(GearCreateRequestDTO request) {
    GearCategory category = gearCategoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found with ID: " + request.getCategoryId()));

    Hub hub = hubRepository.findById(request.getHubId())
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Hub not found with ID: " + request.getHubId()));

    int availableCount = request.getAvailableCount();
    if (availableCount < 0) {
        throw new ResponseStatusException(BAD_REQUEST, "Available count must be non-negative");
    }

    Gear gear = Gear.builder()
        .category(category)
        .name(request.getName().trim())
        .description(request.getDescription())
        .dailyRentPrice(request.getDailyRentPrice())
        .depositAmount(request.getDepositAmount() == null ? BigDecimal.ZERO : request.getDepositAmount())
        .active(request.getActive() == null || request.getActive())
        .build();

    Gear savedGear = gearRepository.save(gear);

    HubInventory inventory = HubInventory.builder()
        .hub(hub)
        .gear(savedGear)
        .totalQty(availableCount)
        .availableQty(availableCount)
        .build();

    HubInventory savedInventory = hubInventoryRepository.save(inventory);

    return GearAdminListItemDTO.builder()
        .id(savedInventory.getId())
        .gearId(savedGear.getId())
        .name(savedGear.getName())
        .category(category.getName())
        .description(savedGear.getDescription())
        .pricePerDay(savedGear.getDailyRentPrice())
        .stock(savedInventory.getAvailableQty())
        .status(savedGear.isActive() ? "ACTIVE" : "INACTIVE")
        .hubId(hub.getId())
        .hubName(hub.getName())
        .updatedAt(savedGear.getUpdatedAt())
        .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GearCategoryOptionDTO> getGearCategories() {
    return gearCategoryRepository.findAll().stream()
        .map(category -> GearCategoryOptionDTO.builder()
            .id(category.getId())
            .name(category.getName())
            .imageUrl(category.getImageUrl())
            .build())
        .toList();
    }

    @Override
    @Transactional
    public GearCategoryOptionDTO createGearCategory(GearCategoryUpsertRequestDTO request) {
        String normalizedName = normalizeCategoryName(request.getName());

        if (gearCategoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(CONFLICT, "Category already exists");
        }

        GearCategory savedCategory = gearCategoryRepository.save(
                GearCategory.builder()
                        .name(normalizedName)
                        .build()
        );

        return GearCategoryOptionDTO.builder()
                .id(savedCategory.getId())
                .name(savedCategory.getName())
            .imageUrl(savedCategory.getImageUrl())
                .build();
    }

    @Override
    @Transactional
    public GearCategoryOptionDTO updateGearCategory(Long categoryId, GearCategoryUpsertRequestDTO request) {
        GearCategory category = gearCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));

        String normalizedName = normalizeCategoryName(request.getName());

        gearCategoryRepository.findByNameIgnoreCase(normalizedName)
                .filter(existing -> !existing.getId().equals(categoryId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(CONFLICT, "Category already exists");
                });

        category.setName(normalizedName);
        GearCategory updatedCategory = gearCategoryRepository.save(category);

        return GearCategoryOptionDTO.builder()
                .id(updatedCategory.getId())
                .name(updatedCategory.getName())
            .imageUrl(updatedCategory.getImageUrl())
                .build();
    }

    @Override
    @Transactional
    public void deleteGearCategory(Long categoryId) {
        GearCategory category = gearCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));

        if (gearRepository.countByCategoryId(categoryId) > 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot delete category that is assigned to existing gears");
        }

        gearCategoryRepository.delete(category);
    }

    @Override
    public GearPageResponseDTO getAdminGears(
            int page,
            int size,
            String search,
            String category,
            String status,
            String hub,
            String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);

        Page<com.akigen.ecotrekk_backend.dto.response.GearAdminListItemDTO> result =
                hubInventoryRepository.searchAdminGearInventory(
                        normalize(search),
                        normalize(category),
                        normalizeStatus(status),
                        normalize(hub),
                        pageable
                );

        return GearPageResponseDTO.builder()
                .content(result.getContent())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .size(result.getSize())
                .number(result.getNumber())
                .build();
    }

    private Pageable buildPageable(int page, int size, String sort) {
        return PageRequest.of(page, size);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeStatus(String status) {
        String normalized = normalize(status);
        if (normalized == null) {
            return null;
        }

        if ("ACTIVE".equalsIgnoreCase(normalized)) {
            return "ACTIVE";
        }
        if ("INACTIVE".equalsIgnoreCase(normalized)) {
            return "INACTIVE";
        }
        return null;
    }

    private String normalizeCategoryName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Category name is required");
        }

        String normalizedName = name.trim();
        if (normalizedName.length() > 80) {
            throw new ResponseStatusException(BAD_REQUEST, "Category name cannot exceed 80 characters");
        }

        return normalizedName;
    }
}
