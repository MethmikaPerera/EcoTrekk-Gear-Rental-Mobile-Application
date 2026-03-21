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
public class AdminMetricsSummaryDTO {

    private long totalOrdersToday;
    private long pendingOrders;
    private long lowStockItems;
    private BigDecimal revenueToday;
    private BigDecimal revenueThisMonth;
    private List<DailyOrderCountDTO> ordersThisWeek;
    private List<DailyRevenueDTO> revenueThisWeek;
    private List<TopRentedGearDTO> topRentedGears;
    private List<HubPerformanceDTO> hubPerformance;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyOrderCountDTO {
        private String date;
        private long count;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRevenueDTO {
        private String date;
        private BigDecimal amount;
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
