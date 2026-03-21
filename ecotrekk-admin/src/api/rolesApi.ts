import axiosInstance from './axiosInstance';

export const getAdminRoles = async (): Promise<string[]> => {
    const response = await axiosInstance.get('/api/admin/roles');
    return response.data;
};
