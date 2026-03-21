import axiosInstance from './axiosInstance';

export interface MetricsSummary {
    totalOrdersToday: number;
    pendingOrders: number;
    lowStockItems: number;
    revenueToday: number;
    revenueThisMonth: number;
    ordersThisWeek: { date: string; count: number }[];
    revenueThisWeek: { date: string; amount: number }[];
    topRentedGears: { name: string; count: number }[];
    hubPerformance: { name: string; orders: number }[];
}

export const getMetricsSummary = () =>
    axiosInstance.get<MetricsSummary>('/api/admin/metrics/summary');

export type ReportPeriod = 'THIS_WEEK' | 'THIS_MONTH' | 'LAST_3_MONTHS' | 'THIS_YEAR';

export interface ReportsAnalytics {
    period: ReportPeriod;
    rangeStart: string;
    rangeEnd: string;
    totalOrders: number;
    totalRevenue: number;
    averageOrderValue: number;
    activeCustomers: number;
    orderTrendPercent: number;
    revenueTrendPercent: number;
    orderSeries: { label: string; count: number }[];
    revenueSeries: { label: string; amount: number }[];
    statusBreakdown: { status: string; count: number }[];
    topRentedGears: { name: string; count: number }[];
    hubPerformance: { name: string; orders: number }[];
}

export const getReportsAnalytics = (period: ReportPeriod) =>
    axiosInstance.get<ReportsAnalytics>('/api/admin/metrics/analytics', {
        params: { period },
    });
