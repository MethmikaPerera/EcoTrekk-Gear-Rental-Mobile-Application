import axiosInstance from './axiosInstance';

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PICKED_UP' | 'RETURNED' | 'CANCELLED';
export type PaymentMethod = 'CARD' | 'PAY_AT_HUB';
export type PaymentStatus = 'UNPAID' | 'PAID' | 'REFUNDED';

export interface OrderItem {
    id: string;
    gearId: string;
    gearName: string;
    imageUrl?: string;
    quantity: number;
    pricePerDay: number;
    depositAmount?: number;
    depositTotal?: number;
    refundableDeposit?: number;
    totalPrice: number;
}

export interface OrderCustomer {
    id: string;
    name: string;
    email: string;
    phone: string;
}

export interface Order {
    id: string;
    customer: OrderCustomer;
    hubId: string;
    hubName: string;
    startDate: string;
    endDate: string;
    status: OrderStatus;
    paymentMethod: PaymentMethod;
    paymentStatus: PaymentStatus;
    items: OrderItem[];
    subtotal?: number;
    depositTotal?: number;
    refundableDeposit?: number;
    totalAmount: number;
    createdAt: string;
    statusHistory?: { status: OrderStatus; timestamp: string; note?: string }[];
}

export interface OrderListParams {
    page?: number;
    size?: number;
    status?: string;
    search?: string;
    startDate?: string;
    endDate?: string;
}

export const getOrders = (params: OrderListParams) =>
    axiosInstance.get<{ content: Order[]; totalElements: number; totalPages: number }>('/api/admin/orders', { params });

export const getOrderById = (id: string) =>
    axiosInstance.get<Order>(`/api/admin/orders/${id}`);

export const patchOrderStatus = (id: string, status: OrderStatus, note?: string) =>
    axiosInstance.patch(`/api/admin/orders/${id}/status`, { status, note });

export const patchOrderPaymentStatus = (id: string, paid: boolean) =>
    axiosInstance.patch(`/api/admin/orders/${id}/payment-status`, { paid });
