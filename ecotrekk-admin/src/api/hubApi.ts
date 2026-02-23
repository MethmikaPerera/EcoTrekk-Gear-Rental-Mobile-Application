import axiosInstance from './axiosInstance';

export interface Hub {
    id: string;
    name: string;
    address: string;
    phone: string;
    latitude: number;
    longitude: number;
}

export const getHubs = () =>
    axiosInstance.get<Hub[]>('/api/admin/hubs');

export const createHub = (data: Omit<Hub, 'id'>) =>
    axiosInstance.post<Hub>('/api/admin/hubs', data);

export const updateHub = (id: string, data: Omit<Hub, 'id'>) =>
    axiosInstance.put<Hub>(`/api/admin/hubs/${id}`, data);

export const deleteHub = (id: string) =>
    axiosInstance.delete(`/api/admin/hubs/${id}`);
