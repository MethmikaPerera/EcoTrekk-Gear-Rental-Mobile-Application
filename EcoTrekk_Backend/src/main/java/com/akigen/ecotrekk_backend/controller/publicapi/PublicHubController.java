package com.akigen.ecotrekk_backend.controller.publicapi;

import com.akigen.ecotrekk_backend.dto.response.PublicHubResponseDTO;
import com.akigen.ecotrekk_backend.service.HubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/hubs")
@RequiredArgsConstructor
public class PublicHubController {

    private final HubService hubService;

    @GetMapping
    public ResponseEntity<List<PublicHubResponseDTO>> getPublicActiveHubs() {
        return ResponseEntity.ok(hubService.getAllPublicActiveHubs());
    }
}
