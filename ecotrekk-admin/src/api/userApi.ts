import axiosInstance from './axiosInstance';

export interface AdminUser {
    id: string;
    username: string;
    fullName: string;
    email: string;
    role: string;
    active: boolean;
    createdAt: string;
    hubId?: number | null;
}

export interface UserListParams {
    page?: number;
    size?: number;
    search?: string;
    role?: string;
}

export interface CustomerListParams {
    page?: number;
    size?: number;
    search?: string;
    status?: 'ACTIVE' | 'INACTIVE';
}

export interface CustomerUser {
    id: number;
    fullName: string;
    email: string;
    phone?: string | null;
    profilePictureUrl?: string | null;
    active: boolean;
    createdAt: string;
}

export interface CustomerPageResponse {
    content: CustomerUser[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export const getUsers = (params: UserListParams) =>
    axiosInstance.get<{ content: AdminUser[]; totalElements: number; totalPages: number }>('/api/admin/users', { params });

export const getCustomers = (params: CustomerListParams) =>
    axiosInstance.get<CustomerPageResponse>('/api/admin/customers', { params });

export const patchUserStatus = (id: string, status: 'ACTIVE' | 'INACTIVE') =>
    axiosInstance.patch(`/api/admin/users/${id}/status`, { status });

export const patchCustomerStatus = (id: number, status: 'ACTIVE' | 'INACTIVE') =>
    axiosInstance.patch(`/api/admin/customers/${id}/status`, { status });

export const createAdmin = async (adminData: { username: string; fullName: string; email: string; password?: string; role: string; hubId?: number | null }): Promise<AdminUser> => {
    const response = await axiosInstance.post<AdminUser>('/api/admin/users', adminData);
    return response.data;
};

export const updateAdmin = async (id: string, adminData: { username: string; fullName: string; email: string; role: string; hubId?: number | null }): Promise<AdminUser> => {
    const response = await axiosInstance.put<AdminUser>(`/api/admin/users/${id}`, adminData);
    return response.data;
};

export const getProfile = async (): Promise<AdminUser> => {
    const response = await axiosInstance.get<AdminUser>('/api/admin/profile/me');
    return response.data;
};

export const changePassword = async (data: { currentPassword: string; newPassword: string }): Promise<void> => {
    await axiosInstance.put('/api/admin/profile/password', data);
};
