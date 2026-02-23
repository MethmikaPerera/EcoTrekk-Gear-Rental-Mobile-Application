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
