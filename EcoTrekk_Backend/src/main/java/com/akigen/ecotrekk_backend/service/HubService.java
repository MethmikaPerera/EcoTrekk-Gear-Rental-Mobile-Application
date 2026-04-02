package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.request.HubUpsertRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.HubAdminResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.PublicHubResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.HubResponseDTO;
import java.util.List;

public interface HubService {
    List<HubResponseDTO> getAllActiveHubs();

    List<PublicHubResponseDTO> getAllPublicActiveHubs();

    List<HubAdminResponseDTO> getAllHubsForAdmin();

    HubAdminResponseDTO getHubForAdminUser(Long adminUserId);

    HubAdminResponseDTO updateHubForAdminUser(Long adminUserId, HubUpsertRequestDTO request);

    HubAdminResponseDTO createHub(HubUpsertRequestDTO request);

    HubAdminResponseDTO updateHub(Long hubId, HubUpsertRequestDTO request);

    void deleteHub(Long hubId);
}
