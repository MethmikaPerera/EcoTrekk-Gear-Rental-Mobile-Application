package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.request.HubUpsertRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.HubAdminResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.HubResponseDTO;
import com.akigen.ecotrekk_backend.entity.Hub;
import com.akigen.ecotrekk_backend.entity.HubManager;
import com.akigen.ecotrekk_backend.repository.HubManagerRepository;
import com.akigen.ecotrekk_backend.repository.HubRepository;
import com.akigen.ecotrekk_backend.service.HubService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class HubServiceImpl implements HubService {

    private final HubRepository hubRepository;
    private final HubManagerRepository hubManagerRepository;

    @Override
    public List<HubResponseDTO> getAllActiveHubs() {
        return hubRepository.findByActiveTrue().stream()
                .map(hub -> HubResponseDTO.builder()
                        .id(hub.getId())
                        .name(hub.getName())
                .address(hub.getAddress())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HubAdminResponseDTO> getAllHubsForAdmin() {
        return hubRepository.findAll().stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HubAdminResponseDTO getHubForAdminUser(Long adminUserId) {
        HubManager hubManager = hubManagerRepository.findFirstByAdminUserId(adminUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No hub assigned for this user"));
        return toAdminResponse(hubManager.getHub());
    }

    @Override
    @Transactional
    public HubAdminResponseDTO updateHubForAdminUser(Long adminUserId, HubUpsertRequestDTO request) {
        HubManager hubManager = hubManagerRepository.findFirstByAdminUserId(adminUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No hub assigned for this user"));

        Hub hub = hubManager.getHub();
        hub.setName(request.getName().trim());
        hub.setAddress(request.getAddress().trim());
        hub.setPhone(request.getPhone().trim());
        hub.setLatitude(request.getLatitude());
        hub.setLongitude(request.getLongitude());
        hub.setMapLocationUrl(request.getMapLocationUrl());
        hub.setImageUrl(request.getImageUrl());

        return toAdminResponse(hubRepository.save(hub));
    }

    @Override
    @Transactional
    public HubAdminResponseDTO createHub(HubUpsertRequestDTO request) {
        Hub hub = Hub.builder()
                .name(request.getName().trim())
                .address(request.getAddress().trim())
                .phone(request.getPhone().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .mapLocationUrl(request.getMapLocationUrl())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() == null || request.getActive())
                .build();

        return toAdminResponse(hubRepository.save(hub));
    }

    @Override
    @Transactional
    public HubAdminResponseDTO updateHub(Long hubId, HubUpsertRequestDTO request) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Hub not found with ID: " + hubId));

        hub.setName(request.getName().trim());
        hub.setAddress(request.getAddress().trim());
        hub.setPhone(request.getPhone().trim());
        hub.setLatitude(request.getLatitude());
        hub.setLongitude(request.getLongitude());
        hub.setMapLocationUrl(request.getMapLocationUrl());
        hub.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) {
            hub.setActive(request.getActive());
        }

        return toAdminResponse(hubRepository.save(hub));
    }

    @Override
    @Transactional
    public void deleteHub(Long hubId) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Hub not found with ID: " + hubId));
        hubRepository.delete(hub);
    }

    private HubAdminResponseDTO toAdminResponse(Hub hub) {
        return HubAdminResponseDTO.builder()
                .id(hub.getId())
                .name(hub.getName())
                .address(hub.getAddress())
                .phone(hub.getPhone())
                .latitude(hub.getLatitude())
                .longitude(hub.getLongitude())
                .mapLocationUrl(hub.getMapLocationUrl())
                .imageUrl(hub.getImageUrl())
                .active(hub.isActive())
                .build();
    }
}
