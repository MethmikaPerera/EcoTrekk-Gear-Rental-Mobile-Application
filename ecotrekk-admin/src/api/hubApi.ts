import axiosInstance from './axiosInstance';

export interface HubResponse {
    id: number;
    name: string;
    address: string;
}

export interface AdminHub {
    id: number;
    name: string;
    address: string;
    phone: string;
    latitude: number;
    longitude: number;
    mapLocationUrl?: string | null;
    imageUrl?: string | null;
    active: boolean;
}

export interface HubUpsertPayload {
    name: string;
    address: string;
    phone: string;
    latitude: number;
    longitude: number;
    mapLocationUrl: string;
    imageUrl: string;
    active?: boolean;
}

export const fetchActiveHubs = async (): Promise<HubResponse[]> => {
    const response = await axiosInstance.get<HubResponse[]>('/api/hubs');
    return response.data;
};

export const fetchAdminHubs = async (): Promise<AdminHub[]> => {
    const response = await axiosInstance.get<AdminHub[]>('/api/admin/hubs');
    return response.data;
};

export const fetchMyAssignedHub = async (): Promise<AdminHub> => {
    const response = await axiosInstance.get<AdminHub>('/api/admin/hubs/my');
    return response.data;
};

export const updateMyAssignedHub = async (payload: HubUpsertPayload): Promise<AdminHub> => {
    const response = await axiosInstance.put<AdminHub>('/api/admin/hubs/my', payload);
    return response.data;
};

export const createHub = async (payload: HubUpsertPayload): Promise<AdminHub> => {
    const response = await axiosInstance.post<AdminHub>('/api/admin/hubs', payload);
    return response.data;
};

export const updateHub = async (id: number, payload: HubUpsertPayload): Promise<AdminHub> => {
    const response = await axiosInstance.put<AdminHub>(`/api/admin/hubs/${id}`, payload);
    return response.data;
};

export const deleteHub = async (id: number): Promise<void> => {
    await axiosInstance.delete(`/api/admin/hubs/${id}`);
};
