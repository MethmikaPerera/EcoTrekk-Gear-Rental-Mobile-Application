package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.request.GearCreateRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.GearCategoryUpsertRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.GearAdminListItemDTO;
import com.akigen.ecotrekk_backend.dto.response.GearCategoryOptionDTO;
import com.akigen.ecotrekk_backend.dto.response.GearPageResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.PublicFeaturedGearDTO;

import java.util.List;

public interface GearService {
    GearAdminListItemDTO createAdminGear(GearCreateRequestDTO request);

    GearAdminListItemDTO updateAdminGear(Long gearId, GearCreateRequestDTO request);

    void updateGearStatus(Long gearId, boolean active);

    List<GearCategoryOptionDTO> getGearCategories();

    GearCategoryOptionDTO createGearCategory(GearCategoryUpsertRequestDTO request);

    GearCategoryOptionDTO updateGearCategory(Long categoryId, GearCategoryUpsertRequestDTO request);

    void deleteGearCategory(Long categoryId);

    List<PublicFeaturedGearDTO> getPublicFeaturedGears(int limit);

    List<PublicFeaturedGearDTO> getPublicGears();

    GearPageResponseDTO getAdminGears(int page, int size, String search, String category, String status, String sort);
}
