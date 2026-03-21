package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.response.AdminMetricsSummaryDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminReportsAnalyticsDTO;

public interface AdminMetricsService {
    AdminMetricsSummaryDTO getSummary(Long adminUserId, boolean superAdmin);

    AdminReportsAnalyticsDTO getAnalytics(Long adminUserId, boolean superAdmin, String period);
}
