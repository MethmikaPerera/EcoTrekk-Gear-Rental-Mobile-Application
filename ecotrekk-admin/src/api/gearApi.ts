import axiosInstance from './axiosInstance';

export interface Gear {
    id: string;
    name: string;
    category: string;
    description: string;
    pricePerDay: number;
    stock: number;
    status: 'ACTIVE' | 'INACTIVE';
    imageUrl?: string;
    videoUrl?: string;
    updatedAt: string;
}

export interface GearListParams {
    page?: number;
    size?: number;
    search?: string;
    category?: string;
    status?: string;
    sort?: string;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

export const getGears = (params: GearListParams) =>
    axiosInstance.get<PageResponse<Gear>>('/api/admin/gears', { params });

export const getGearById = (id: string) =>
    axiosInstance.get<Gear>(`/api/admin/gears/${id}`);

export const createGear = (data: Omit<Gear, 'id' | 'updatedAt'>) =>
    axiosInstance.post<Gear>('/api/admin/gears', data);

export const updateGear = (id: string, data: Partial<Gear>) =>
    axiosInstance.put<Gear>(`/api/admin/gears/${id}`, data);

export const deleteGear = (id: string) =>
    axiosInstance.delete(`/api/admin/gears/${id}`);

export const patchGearStatus = (id: string, status: 'ACTIVE' | 'INACTIVE') =>
    axiosInstance.patch(`/api/admin/gears/${id}/status`, { status });
