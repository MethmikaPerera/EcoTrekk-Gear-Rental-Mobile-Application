package com.akigen.ecotrekk_backend.controller.admin;

import com.akigen.ecotrekk_backend.dto.response.AdminMetricsSummaryDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminReportsAnalyticsDTO;
import com.akigen.ecotrekk_backend.service.AdminMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/metrics")
@RequiredArgsConstructor
public class AdminMetricsController {

    private final AdminMetricsService adminMetricsService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','STAFF')")
    public ResponseEntity<AdminMetricsSummaryDTO> getSummary(Authentication authentication) {
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SUPER_ADMIN".equals(authority.getAuthority()));

        Long adminUserId = null;
        if (!isSuperAdmin) {
            adminUserId = Long.parseLong(authentication.getName());
        }

        return ResponseEntity.ok(adminMetricsService.getSummary(adminUserId, isSuperAdmin));
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','STAFF')")
    public ResponseEntity<AdminReportsAnalyticsDTO> getAnalytics(
            Authentication authentication,
            @RequestParam(defaultValue = "THIS_MONTH") String period
    ) {
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SUPER_ADMIN".equals(authority.getAuthority()));

        Long adminUserId = null;
        if (!isSuperAdmin) {
            adminUserId = Long.parseLong(authentication.getName());
        }

        return ResponseEntity.ok(adminMetricsService.getAnalytics(adminUserId, isSuperAdmin, period));
    }
}
