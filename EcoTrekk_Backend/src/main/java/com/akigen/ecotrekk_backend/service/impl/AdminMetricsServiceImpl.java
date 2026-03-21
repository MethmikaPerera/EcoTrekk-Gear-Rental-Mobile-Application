package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.response.AdminMetricsSummaryDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminReportsAnalyticsDTO;
import com.akigen.ecotrekk_backend.entity.Invoice;
import com.akigen.ecotrekk_backend.entity.InvoiceItem;
import com.akigen.ecotrekk_backend.enums.InvoiceStatus;
import com.akigen.ecotrekk_backend.repository.HubInventoryRepository;
import com.akigen.ecotrekk_backend.repository.HubManagerRepository;
import com.akigen.ecotrekk_backend.repository.InvoiceItemRepository;
import com.akigen.ecotrekk_backend.repository.InvoiceRepository;
import com.akigen.ecotrekk_backend.service.AdminMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminMetricsServiceImpl implements AdminMetricsService {

    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final DateTimeFormatter WEEK_LABEL_FORMATTER = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH_DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final HubInventoryRepository hubInventoryRepository;
    private final HubManagerRepository hubManagerRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminMetricsSummaryDTO getSummary(Long adminUserId, boolean superAdmin) {
        List<Long> scopedHubIds = resolveHubIds(adminUserId, superAdmin);
        if (!superAdmin && scopedHubIds.isEmpty()) {
            return buildEmptySummary();
        }

        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);

        Instant todayStart = today.atStartOfDay(zone).toInstant();
        Instant tomorrowStart = today.plusDays(1).atStartOfDay(zone).toInstant();
        Instant weekStart = today.minusDays(6).atStartOfDay(zone).toInstant();
        Instant monthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant();

        List<Invoice> invoicesToday = findInvoicesInRange(todayStart, tomorrowStart, scopedHubIds);
        List<Invoice> invoicesThisWeek = findInvoicesInRange(weekStart, tomorrowStart, scopedHubIds);
        List<Invoice> invoicesThisMonth = findInvoicesInRange(monthStart, tomorrowStart, scopedHubIds);

        long pendingOrders = superAdmin
                ? invoiceRepository.countByStatus(InvoiceStatus.PENDING)
                : invoiceRepository.countByStatusAndHubIdIn(InvoiceStatus.PENDING, scopedHubIds);

        long lowStockItems = superAdmin
                ? hubInventoryRepository.countByAvailableQtyLessThanEqual(LOW_STOCK_THRESHOLD)
                : hubInventoryRepository.countByHubIdInAndAvailableQtyLessThanEqual(scopedHubIds, LOW_STOCK_THRESHOLD);

        return AdminMetricsSummaryDTO.builder()
                .totalOrdersToday(invoicesToday.size())
                .pendingOrders(pendingOrders)
                .lowStockItems(lowStockItems)
                .revenueToday(sumRevenue(invoicesToday))
                .revenueThisMonth(sumRevenue(invoicesThisMonth))
                .ordersThisWeek(buildDailyOrderCounts(invoicesThisWeek, today, zone))
                .revenueThisWeek(buildDailyRevenue(invoicesThisWeek, today, zone))
                .topRentedGears(buildTopRentedGears(invoicesThisWeek))
                .hubPerformance(buildHubPerformance(invoicesThisWeek))
                .build();
    }

            @Override
            @Transactional(readOnly = true)
            public AdminReportsAnalyticsDTO getAnalytics(Long adminUserId, boolean superAdmin, String period) {
            ReportPeriod reportPeriod = resolvePeriod(period);
            List<Long> scopedHubIds = resolveHubIds(adminUserId, superAdmin);

            ZoneId zone = ZoneId.systemDefault();
            LocalDate today = LocalDate.now(zone);
            DateRange range = resolveCurrentRange(reportPeriod, today);
            DateRange previousRange = resolvePreviousRange(range);

            if (!superAdmin && scopedHubIds.isEmpty()) {
                return buildEmptyAnalytics(reportPeriod, range);
            }

            List<Invoice> currentInvoices = findInvoicesInRange(
                range.startInclusive().atStartOfDay(zone).toInstant(),
                range.endExclusive().atStartOfDay(zone).toInstant(),
                scopedHubIds
            );
            List<Invoice> previousInvoices = findInvoicesInRange(
                previousRange.startInclusive().atStartOfDay(zone).toInstant(),
                previousRange.endExclusive().atStartOfDay(zone).toInstant(),
                scopedHubIds
            );

            long totalOrders = currentInvoices.size();
            BigDecimal totalRevenue = sumRevenue(currentInvoices);
            BigDecimal previousRevenue = sumRevenue(previousInvoices);
            BigDecimal averageOrderValue = totalOrders == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
            long activeCustomers = currentInvoices.stream()
                .map(invoice -> invoice.getCustomer().getId())
                .distinct()
                .count();

            return AdminReportsAnalyticsDTO.builder()
                .period(reportPeriod.name())
                .rangeStart(range.startInclusive().toString())
                .rangeEnd(range.endExclusive().minusDays(1).toString())
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .activeCustomers(activeCustomers)
                .orderTrendPercent(calculateTrendPercentage(totalOrders, previousInvoices.size()))
                .revenueTrendPercent(calculateRevenueTrendPercentage(totalRevenue, previousRevenue))
                .orderSeries(buildOrderSeries(currentInvoices, reportPeriod, range, zone))
                .revenueSeries(buildRevenueSeries(currentInvoices, reportPeriod, range, zone))
                .statusBreakdown(buildStatusBreakdown(currentInvoices))
                .topRentedGears(buildTopRentedGearsForAnalytics(currentInvoices))
                .hubPerformance(buildHubPerformanceForAnalytics(currentInvoices))
                .build();
            }

    private List<Long> resolveHubIds(Long adminUserId, boolean superAdmin) {
        if (superAdmin) {
            return List.of();
        }

        return hubManagerRepository.findByAdminUserId(adminUserId).stream()
                .map(hubManager -> hubManager.getHub().getId())
                .distinct()
                .toList();
    }

    private List<Invoice> findInvoicesInRange(Instant startInclusive, Instant endExclusive, List<Long> scopedHubIds) {
        if (scopedHubIds.isEmpty()) {
            return invoiceRepository.findByCreatedAtBetween(startInclusive, endExclusive);
        }
        return invoiceRepository.findByHubIdInAndCreatedAtBetween(scopedHubIds, startInclusive, endExclusive);
    }

    private ReportPeriod resolvePeriod(String period) {
        if (period == null || period.isBlank()) {
            return ReportPeriod.THIS_MONTH;
        }

        try {
            return ReportPeriod.valueOf(period.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ex) {
            return ReportPeriod.THIS_MONTH;
        }
    }

    private DateRange resolveCurrentRange(ReportPeriod period, LocalDate today) {
        return switch (period) {
            case THIS_WEEK -> new DateRange(today.minusDays(6), today.plusDays(1));
            case THIS_MONTH -> new DateRange(today.withDayOfMonth(1), today.plusDays(1));
            case LAST_3_MONTHS -> new DateRange(today.withDayOfMonth(1).minusMonths(2), today.plusDays(1));
            case THIS_YEAR -> new DateRange(today.withDayOfYear(1), today.plusDays(1));
        };
    }

    private DateRange resolvePreviousRange(DateRange currentRange) {
        long spanDays = ChronoUnit.DAYS.between(currentRange.startInclusive(), currentRange.endExclusive());
        LocalDate previousEndExclusive = currentRange.startInclusive();
        LocalDate previousStartInclusive = previousEndExclusive.minusDays(spanDays);
        return new DateRange(previousStartInclusive, previousEndExclusive);
    }

    private BigDecimal sumRevenue(List<Invoice> invoices) {
        return invoices.stream()
                .filter(invoice -> invoice.getStatus() != InvoiceStatus.CANCELLED)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<AdminReportsAnalyticsDTO.OrderSeriesPointDTO> buildOrderSeries(
            List<Invoice> invoices,
            ReportPeriod period,
            DateRange range,
            ZoneId zone
    ) {
        List<LocalDate> bucketDates = buildBucketDates(period, range);
        Map<LocalDate, Long> countsByBucket = new LinkedHashMap<>();
        for (LocalDate bucketDate : bucketDates) {
            countsByBucket.put(bucketDate, 0L);
        }

        for (Invoice invoice : invoices) {
            LocalDate createdDate = invoice.getCreatedAt().atZone(zone).toLocalDate();
            LocalDate bucketDate = period.isMonthly() ? createdDate.withDayOfMonth(1) : createdDate;
            if (countsByBucket.containsKey(bucketDate)) {
                countsByBucket.merge(bucketDate, 1L, Long::sum);
            }
        }

        List<AdminReportsAnalyticsDTO.OrderSeriesPointDTO> points = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> entry : countsByBucket.entrySet()) {
            points.add(AdminReportsAnalyticsDTO.OrderSeriesPointDTO.builder()
                    .label(formatBucketLabel(entry.getKey(), period))
                    .count(entry.getValue())
                    .build());
        }
        return points;
    }

    private List<AdminReportsAnalyticsDTO.RevenueSeriesPointDTO> buildRevenueSeries(
            List<Invoice> invoices,
            ReportPeriod period,
            DateRange range,
            ZoneId zone
    ) {
        List<LocalDate> bucketDates = buildBucketDates(period, range);
        Map<LocalDate, BigDecimal> revenueByBucket = new LinkedHashMap<>();
        for (LocalDate bucketDate : bucketDates) {
            revenueByBucket.put(bucketDate, BigDecimal.ZERO);
        }

        for (Invoice invoice : invoices) {
            if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
                continue;
            }

            LocalDate createdDate = invoice.getCreatedAt().atZone(zone).toLocalDate();
            LocalDate bucketDate = period.isMonthly() ? createdDate.withDayOfMonth(1) : createdDate;
            if (revenueByBucket.containsKey(bucketDate)) {
                revenueByBucket.merge(bucketDate, invoice.getTotalAmount(), BigDecimal::add);
            }
        }

        List<AdminReportsAnalyticsDTO.RevenueSeriesPointDTO> points = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : revenueByBucket.entrySet()) {
            points.add(AdminReportsAnalyticsDTO.RevenueSeriesPointDTO.builder()
                    .label(formatBucketLabel(entry.getKey(), period))
                    .amount(entry.getValue())
                    .build());
        }
        return points;
    }

    private List<LocalDate> buildBucketDates(ReportPeriod period, DateRange range) {
        List<LocalDate> bucketDates = new ArrayList<>();

        if (period.isMonthly()) {
            LocalDate cursor = range.startInclusive().withDayOfMonth(1);
            LocalDate lastDate = range.endExclusive().minusDays(1).withDayOfMonth(1);
            while (!cursor.isAfter(lastDate)) {
                bucketDates.add(cursor);
                cursor = cursor.plusMonths(1);
            }
            return bucketDates;
        }

        LocalDate cursor = range.startInclusive();
        while (cursor.isBefore(range.endExclusive())) {
            bucketDates.add(cursor);
            cursor = cursor.plusDays(1);
        }

        return bucketDates;
    }

    private String formatBucketLabel(LocalDate date, ReportPeriod period) {
        return switch (period) {
            case THIS_WEEK -> date.format(WEEK_LABEL_FORMATTER);
            case THIS_MONTH -> date.format(MONTH_DAY_LABEL_FORMATTER);
            case LAST_3_MONTHS, THIS_YEAR -> date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        };
    }

    private List<AdminReportsAnalyticsDTO.StatusBreakdownDTO> buildStatusBreakdown(List<Invoice> invoices) {
        Map<InvoiceStatus, Long> countsByStatus = new HashMap<>();
        for (Invoice invoice : invoices) {
            countsByStatus.merge(invoice.getStatus(), 1L, Long::sum);
        }

        return Arrays.stream(InvoiceStatus.values())
                .map(status -> AdminReportsAnalyticsDTO.StatusBreakdownDTO.builder()
                        .status(status.name())
                        .count(countsByStatus.getOrDefault(status, 0L))
                        .build())
                .toList();
    }

    private List<AdminReportsAnalyticsDTO.TopRentedGearDTO> buildTopRentedGearsForAnalytics(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return List.of();
        }

        Set<Long> invoiceIds = invoices.stream().map(Invoice::getId).collect(java.util.stream.Collectors.toSet());
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceIdIn(new ArrayList<>(invoiceIds));

        Map<String, Long> countsByGear = new HashMap<>();
        for (InvoiceItem item : items) {
            countsByGear.merge(item.getGear().getName(), (long) item.getQty(), Long::sum);
        }

        return countsByGear.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(6)
                .map(entry -> AdminReportsAnalyticsDTO.TopRentedGearDTO.builder()
                        .name(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .toList();
    }

    private List<AdminReportsAnalyticsDTO.HubPerformanceDTO> buildHubPerformanceForAnalytics(List<Invoice> invoices) {
        Map<String, Long> orderCountByHub = new HashMap<>();
        for (Invoice invoice : invoices) {
            orderCountByHub.merge(invoice.getHub().getName(), 1L, Long::sum);
        }

        return orderCountByHub.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> AdminReportsAnalyticsDTO.HubPerformanceDTO.builder()
                        .name(entry.getKey())
                        .orders(entry.getValue())
                        .build())
                .toList();
    }

    private double calculateTrendPercentage(long currentValue, long previousValue) {
        if (previousValue == 0L) {
            return currentValue == 0L ? 0.0 : 100.0;
        }
        return ((currentValue - previousValue) * 100.0) / previousValue;
    }

    private double calculateRevenueTrendPercentage(BigDecimal currentValue, BigDecimal previousValue) {
        if (previousValue.compareTo(BigDecimal.ZERO) == 0) {
            return currentValue.compareTo(BigDecimal.ZERO) == 0 ? 0.0 : 100.0;
        }

        BigDecimal delta = currentValue.subtract(previousValue);
        return delta.multiply(BigDecimal.valueOf(100))
                .divide(previousValue, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private List<AdminMetricsSummaryDTO.DailyOrderCountDTO> buildDailyOrderCounts(List<Invoice> invoices, LocalDate today, ZoneId zone) {
        Map<LocalDate, Long> countsByDate = new HashMap<>();
        for (Invoice invoice : invoices) {
            LocalDate createdDate = invoice.getCreatedAt().atZone(zone).toLocalDate();
            countsByDate.merge(createdDate, 1L, Long::sum);
        }

        List<AdminMetricsSummaryDTO.DailyOrderCountDTO> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            result.add(AdminMetricsSummaryDTO.DailyOrderCountDTO.builder()
                    .date(date.format(WEEK_LABEL_FORMATTER))
                    .count(countsByDate.getOrDefault(date, 0L))
                    .build());
        }
        return result;
    }

    private List<AdminMetricsSummaryDTO.DailyRevenueDTO> buildDailyRevenue(List<Invoice> invoices, LocalDate today, ZoneId zone) {
        Map<LocalDate, BigDecimal> revenueByDate = new HashMap<>();
        for (Invoice invoice : invoices) {
            if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
                continue;
            }
            LocalDate createdDate = invoice.getCreatedAt().atZone(zone).toLocalDate();
            revenueByDate.merge(createdDate, invoice.getTotalAmount(), BigDecimal::add);
        }

        List<AdminMetricsSummaryDTO.DailyRevenueDTO> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            result.add(AdminMetricsSummaryDTO.DailyRevenueDTO.builder()
                    .date(date.format(WEEK_LABEL_FORMATTER))
                    .amount(revenueByDate.getOrDefault(date, BigDecimal.ZERO))
                    .build());
        }
        return result;
    }

    private List<AdminMetricsSummaryDTO.TopRentedGearDTO> buildTopRentedGears(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return List.of();
        }

        Set<Long> invoiceIds = invoices.stream().map(Invoice::getId).collect(java.util.stream.Collectors.toSet());
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceIdIn(new ArrayList<>(invoiceIds));

        Map<String, Long> countsByGear = new HashMap<>();
        for (InvoiceItem item : items) {
            countsByGear.merge(item.getGear().getName(), (long) item.getQty(), Long::sum);
        }

        return countsByGear.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(entry -> AdminMetricsSummaryDTO.TopRentedGearDTO.builder()
                        .name(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .toList();
    }

    private List<AdminMetricsSummaryDTO.HubPerformanceDTO> buildHubPerformance(List<Invoice> invoices) {
        Map<String, Long> orderCountByHub = new HashMap<>();
        for (Invoice invoice : invoices) {
            orderCountByHub.merge(invoice.getHub().getName(), 1L, Long::sum);
        }

        return orderCountByHub.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> AdminMetricsSummaryDTO.HubPerformanceDTO.builder()
                        .name(entry.getKey())
                        .orders(entry.getValue())
                        .build())
                .toList();
    }

    private AdminMetricsSummaryDTO buildEmptySummary() {
        List<AdminMetricsSummaryDTO.DailyOrderCountDTO> zeroOrderSeries = new ArrayList<>();
        List<AdminMetricsSummaryDTO.DailyRevenueDTO> zeroRevenueSeries = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            zeroOrderSeries.add(AdminMetricsSummaryDTO.DailyOrderCountDTO.builder()
                    .date(date.format(WEEK_LABEL_FORMATTER))
                    .count(0)
                    .build());
            zeroRevenueSeries.add(AdminMetricsSummaryDTO.DailyRevenueDTO.builder()
                    .date(date.format(WEEK_LABEL_FORMATTER))
                    .amount(BigDecimal.ZERO)
                    .build());
        }

        return AdminMetricsSummaryDTO.builder()
                .totalOrdersToday(0)
                .pendingOrders(0)
                .lowStockItems(0)
                .revenueToday(BigDecimal.ZERO)
                .revenueThisMonth(BigDecimal.ZERO)
                .ordersThisWeek(zeroOrderSeries)
                .revenueThisWeek(zeroRevenueSeries)
                .topRentedGears(List.of())
                .hubPerformance(List.of())
                .build();
    }

    private AdminReportsAnalyticsDTO buildEmptyAnalytics(ReportPeriod period, DateRange range) {
        List<AdminReportsAnalyticsDTO.OrderSeriesPointDTO> orderSeries = new ArrayList<>();
        List<AdminReportsAnalyticsDTO.RevenueSeriesPointDTO> revenueSeries = new ArrayList<>();

        for (LocalDate bucketDate : buildBucketDates(period, range)) {
            orderSeries.add(AdminReportsAnalyticsDTO.OrderSeriesPointDTO.builder()
                    .label(formatBucketLabel(bucketDate, period))
                    .count(0)
                    .build());
            revenueSeries.add(AdminReportsAnalyticsDTO.RevenueSeriesPointDTO.builder()
                    .label(formatBucketLabel(bucketDate, period))
                    .amount(BigDecimal.ZERO)
                    .build());
        }

        List<AdminReportsAnalyticsDTO.StatusBreakdownDTO> statusBreakdown = Arrays.stream(InvoiceStatus.values())
                .map(status -> AdminReportsAnalyticsDTO.StatusBreakdownDTO.builder()
                        .status(status.name())
                        .count(0)
                        .build())
                .toList();

        return AdminReportsAnalyticsDTO.builder()
                .period(period.name())
                .rangeStart(range.startInclusive().toString())
                .rangeEnd(range.endExclusive().minusDays(1).toString())
                .totalOrders(0)
                .totalRevenue(BigDecimal.ZERO)
                .averageOrderValue(BigDecimal.ZERO)
                .activeCustomers(0)
                .orderTrendPercent(0.0)
                .revenueTrendPercent(0.0)
                .orderSeries(orderSeries)
                .revenueSeries(revenueSeries)
                .statusBreakdown(statusBreakdown)
                .topRentedGears(List.of())
                .hubPerformance(List.of())
                .build();
    }

    private enum ReportPeriod {
        THIS_WEEK,
        THIS_MONTH,
        LAST_3_MONTHS,
        THIS_YEAR;

        private boolean isMonthly() {
            return this == LAST_3_MONTHS || this == THIS_YEAR;
        }
    }

    private record DateRange(LocalDate startInclusive, LocalDate endExclusive) {
    }
}
