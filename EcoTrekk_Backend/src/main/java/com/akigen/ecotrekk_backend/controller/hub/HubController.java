package com.akigen.ecotrekk_backend.controller.hub;

import com.akigen.ecotrekk_backend.dto.response.HubResponseDTO;
import com.akigen.ecotrekk_backend.service.HubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hubs")
@RequiredArgsConstructor
public class HubController {

    private final HubService hubService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<HubResponseDTO>> getAllActiveHubs() {
        return ResponseEntity.ok(hubService.getAllActiveHubs());
    }
}
