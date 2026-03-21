package com.akigen.ecotrekk_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportsAnalyticsDTO {

    private String period;
    private String rangeStart;
    private String rangeEnd;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private long activeCustomers;
    private double orderTrendPercent;
    private double revenueTrendPercent;
    private List<OrderSeriesPointDTO> orderSeries;
    private List<RevenueSeriesPointDTO> revenueSeries;
    private List<StatusBreakdownDTO> statusBreakdown;
    private List<TopRentedGearDTO> topRentedGears;
    private List<HubPerformanceDTO> hubPerformance;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSeriesPointDTO {
        private String label;
        private long count;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueSeriesPointDTO {
        private String label;
        private BigDecimal amount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusBreakdownDTO {
        private String status;
        private long count;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopRentedGearDTO {
        private String name;
        private long count;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HubPerformanceDTO {
        private String name;
        private long orders;
    }
}