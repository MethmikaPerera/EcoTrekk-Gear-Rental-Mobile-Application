import axiosInstance from './axiosInstance';

export interface AdminUser {
    id: string;
    name: string;
    email: string;
    status: 'ACTIVE' | 'INACTIVE';
    createdAt: string;
}

export interface UserListParams {
    page?: number;
    size?: number;
    search?: string;
    status?: string;
}

export const getUsers = (params: UserListParams) =>
    axiosInstance.get<{ content: AdminUser[]; totalElements: number; totalPages: number }>('/api/admin/users', { params });

export const patchUserStatus = (id: string, status: 'ACTIVE' | 'INACTIVE') =>
    axiosInstance.patch(`/api/admin/users/${id}/status`, { status });
