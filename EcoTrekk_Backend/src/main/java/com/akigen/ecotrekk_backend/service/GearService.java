package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.request.GearCreateRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.GearCategoryUpsertRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.GearAdminListItemDTO;
import com.akigen.ecotrekk_backend.dto.response.GearCategoryOptionDTO;
import com.akigen.ecotrekk_backend.dto.response.GearPageResponseDTO;

import java.util.List;

public interface GearService {
    GearAdminListItemDTO createAdminGear(GearCreateRequestDTO request);

    List<GearCategoryOptionDTO> getGearCategories();

    GearCategoryOptionDTO createGearCategory(GearCategoryUpsertRequestDTO request);

    GearCategoryOptionDTO updateGearCategory(Long categoryId, GearCategoryUpsertRequestDTO request);

    void deleteGearCategory(Long categoryId);

    GearPageResponseDTO getAdminGears(int page, int size, String search, String category, String status, String hub, String sort);
}
