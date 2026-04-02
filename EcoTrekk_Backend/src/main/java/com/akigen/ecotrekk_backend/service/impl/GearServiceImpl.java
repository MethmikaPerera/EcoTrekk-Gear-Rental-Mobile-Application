package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.request.GearCategoryUpsertRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.GearCreateRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.GearAdminListItemDTO;
import com.akigen.ecotrekk_backend.dto.response.GearCategoryOptionDTO;
import com.akigen.ecotrekk_backend.dto.response.GearPageResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.PublicFeaturedGearDTO;
import com.akigen.ecotrekk_backend.entity.Gear;
import com.akigen.ecotrekk_backend.entity.GearCategory;
import com.akigen.ecotrekk_backend.entity.GearImage;
import com.akigen.ecotrekk_backend.entity.Inventory;
import com.akigen.ecotrekk_backend.repository.GearCategoryRepository;
import com.akigen.ecotrekk_backend.repository.GearImageRepository;
import com.akigen.ecotrekk_backend.repository.GearRepository;
import com.akigen.ecotrekk_backend.repository.InventoryRepository;
import com.akigen.ecotrekk_backend.service.GearService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class GearServiceImpl implements GearService {

    private final InventoryRepository inventoryRepository;
    private final GearRepository gearRepository;
    private final GearCategoryRepository gearCategoryRepository;
    private final GearImageRepository gearImageRepository;

    @Override
    @Transactional
    public GearAdminListItemDTO createAdminGear(GearCreateRequestDTO request) {
        try {
            GearCategory category = gearCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found with ID: " + request.getCategoryId()));

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

            Inventory inventory = Inventory.builder()
                    .gear(savedGear)
                    .totalQty(availableCount)
                    .availableQty(availableCount)
                    .build();

            Inventory savedInventory = inventoryRepository.save(inventory);

            return GearAdminListItemDTO.builder()
                    .id(savedInventory.getId())
                    .gearId(savedGear.getId())
                    .name(savedGear.getName())
                    .category(category.getName())
                    .imageUrl(resolveGearImageUrl(savedGear))
                    .description(savedGear.getDescription())
                    .pricePerDay(savedGear.getDailyRentPrice())
                    .depositAmount(savedGear.getDepositAmount())
                    .totalQty(savedInventory.getTotalQty())
                    .availableQty(savedInventory.getAvailableQty())
                    .status(savedGear.isActive() ? "ACTIVE" : "INACTIVE")
                    .updatedAt(savedGear.getUpdatedAt())
                    .build();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid gear payload or database constraint violation", ex);
        }
    }

    @Override
    @Transactional
    public GearAdminListItemDTO updateAdminGear(Long gearId, GearCreateRequestDTO request) {
        try {
            Gear gear = gearRepository.findById(gearId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Gear not found"));

            GearCategory category = gearCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found with ID: " + request.getCategoryId()));

            int availableCount = request.getAvailableCount();
            if (availableCount < 0) {
                throw new ResponseStatusException(BAD_REQUEST, "Available count must be non-negative");
            }

            gear.setCategory(category);
            gear.setName(request.getName().trim());
            gear.setDescription(request.getDescription());
            gear.setDailyRentPrice(request.getDailyRentPrice());
            gear.setDepositAmount(request.getDepositAmount() == null ? BigDecimal.ZERO : request.getDepositAmount());
            gear.setActive(request.getActive() == null || request.getActive());

            Gear updatedGear = gearRepository.save(gear);

            Inventory inventory = inventoryRepository.findByGearId(gearId).stream()
                    .findFirst()
                    .orElseGet(() -> Inventory.builder().gear(updatedGear).build());

            inventory.setTotalQty(availableCount);
            inventory.setAvailableQty(availableCount);
            Inventory updatedInventory = inventoryRepository.save(inventory);

            return GearAdminListItemDTO.builder()
                    .id(updatedInventory.getId())
                    .gearId(updatedGear.getId())
                    .name(updatedGear.getName())
                    .category(category.getName())
                    .imageUrl(resolveGearImageUrl(updatedGear))
                    .description(updatedGear.getDescription())
                    .pricePerDay(updatedGear.getDailyRentPrice())
                    .depositAmount(updatedGear.getDepositAmount())
                    .totalQty(updatedInventory.getTotalQty())
                    .availableQty(updatedInventory.getAvailableQty())
                    .status(updatedGear.isActive() ? "ACTIVE" : "INACTIVE")
                    .updatedAt(updatedGear.getUpdatedAt())
                    .build();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid gear payload or database constraint violation", ex);
        }
    }

    @Override
    @Transactional
    public void updateGearStatus(Long gearId, boolean active) {
        Gear gear = gearRepository.findById(gearId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Gear not found"));

        gear.setActive(active);
        gearRepository.save(gear);
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
    @Transactional(readOnly = true)
    public List<PublicFeaturedGearDTO> getPublicFeaturedGears(int limit) {
        int cappedLimit = Math.max(1, Math.min(limit, 12));

        return gearRepository.findByActiveTrue().stream()
                .sorted((first, second) -> second.getUpdatedAt().compareTo(first.getUpdatedAt()))
                .limit(cappedLimit)
                .map(this::mapToPublicGearDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicFeaturedGearDTO> getPublicGears() {
        return gearRepository.findByActiveTrue().stream()
                .sorted((first, second) -> second.getUpdatedAt().compareTo(first.getUpdatedAt()))
                .map(this::mapToPublicGearDto)
                .toList();
    }

    private PublicFeaturedGearDTO mapToPublicGearDto(Gear gear) {
        int availableQty = inventoryRepository.findByGearId(gear.getId()).stream()
                .mapToInt(inv -> inv.getAvailableQty())
                .sum();

        return PublicFeaturedGearDTO.builder()
                .id(gear.getId())
                .name(gear.getName())
                .category(gear.getCategory().getName())
                .description(gear.getDescription())
                .pricePerDay(gear.getDailyRentPrice())
                .depositAmount(gear.getDepositAmount())
                .imageUrl(resolveGearImageUrl(gear))
                .availableQty(availableQty)
                .build();
    }

    private String resolveGearImageUrl(Gear gear) {
        List<GearImage> primaryImages = gearImageRepository.findByGearIdAndPrimaryTrue(gear.getId());
        if (!primaryImages.isEmpty()) {
            return primaryImages.get(0).getImageUrl();
        }

        List<GearImage> orderedImages = gearImageRepository.findByGearIdOrderBySortOrderAsc(gear.getId());
        if (!orderedImages.isEmpty()) {
            return orderedImages.get(0).getImageUrl();
        }

        return gear.getCategory().getImageUrl();
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
            String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);

        Page<GearAdminListItemDTO> result =
                inventoryRepository.searchAdminInventory(
                        normalize(search),
                        normalize(category),
                        normalizeStatus(status),
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
