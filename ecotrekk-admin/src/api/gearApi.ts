import axiosInstance from './axiosInstance';

export interface Gear {
    id: string;
    gearId?: string;
    name: string;
    category: string;
    description: string;
    pricePerDay: number;
    totalQty: number;
    availableQty: number;
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

export interface GearCategoryOption {
    id: number;
    name: string;
    imageUrl?: string | null;
}

export interface GearCategoryUpsertRequest {
    name: string;
}

export interface CreateGearRequest {
    name: string;
    categoryId: number;
    description?: string;
    dailyRentPrice: number;
    depositAmount: number;
    hubId?: number;
    availableCount: number;
    active: boolean;
}

export interface UpdateGearRequest extends CreateGearRequest {}

export const getGears = (params: GearListParams) =>
    axiosInstance.get<PageResponse<Gear>>('/api/admin/gears', {
        params,
        suppressUnauthorizedEvent: true,
    });

export const getGearById = (id: string) =>
    axiosInstance.get<Gear>(`/api/admin/gears/${id}`);

export const getGearCategories = () =>
    axiosInstance.get<GearCategoryOption[]>('/api/admin/gears/categories');

export const createGearCategory = (payload: GearCategoryUpsertRequest) =>
    axiosInstance.post<GearCategoryOption>('/api/admin/gears/categories', payload);

export const updateGearCategory = (categoryId: number, payload: GearCategoryUpsertRequest) =>
    axiosInstance.put<GearCategoryOption>(`/api/admin/gears/categories/${categoryId}`, payload);

export const deleteGearCategory = (categoryId: number) =>
    axiosInstance.delete(`/api/admin/gears/categories/${categoryId}`);

export const createAdminGear = (payload: CreateGearRequest) =>
    axiosInstance.post<Gear>('/api/admin/gears', payload);

export const updateAdminGear = (id: string, payload: UpdateGearRequest) =>
    axiosInstance.put<Gear>(`/api/admin/gears/${id}`, payload);

export const createGear = (data: Omit<Gear, 'id' | 'updatedAt'>) =>
    axiosInstance.post<Gear>('/api/admin/gears', data);

export const updateGear = (id: string, data: Partial<Gear>) =>
    axiosInstance.put<Gear>(`/api/admin/gears/${id}`, data);

export const deleteGear = (id: string) =>
    axiosInstance.delete(`/api/admin/gears/${id}`);

export const patchGearStatus = (id: string, status: 'ACTIVE' | 'INACTIVE') =>
    axiosInstance.patch(`/api/admin/gears/${id}/status`, { status });
