import axios from 'axios';
import axiosInstance from './axiosInstance';

export type FolderType = 'gears' | 'hubs' | 'customers' | 'categories';

export interface CloudinarySignatureRequest {
    folderType: FolderType;
    entityId?: number;
}

export interface CloudinarySignatureResponse {
    cloudName: string;
    apiKey: string;
    timestamp: number;
    folder: string;
    signature: string;
}

export interface CloudinaryUploadResult {
    secure_url: string;
    public_id: string;
}

export interface CloudinaryAssetSaveRequest {
    secureUrl: string;
    publicId: string;
    folderType: FolderType;
    entityId: number;
    primaryImage?: boolean;
    sortOrder?: number;
}

export const getCloudinarySignature = async (
    payload: CloudinarySignatureRequest
): Promise<CloudinarySignatureResponse> => {
    const response = await axiosInstance.post<CloudinarySignatureResponse>('/api/uploads/signature', payload);
    return response.data;
};

export const uploadToCloudinary = async (
    file: File,
    signature: CloudinarySignatureResponse
): Promise<CloudinaryUploadResult> => {
    if (
        !signature.cloudName ||
        !signature.apiKey ||
        signature.cloudName.includes('${') ||
        signature.apiKey.includes('${')
    ) {
        throw new Error('Cloudinary config is missing on backend. Ask backend to set CLOUDINARY_CLOUD_NAME and CLOUDINARY_API_KEY.');
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('api_key', signature.apiKey);
    formData.append('timestamp', String(signature.timestamp));
    formData.append('folder', signature.folder);
    formData.append('signature', signature.signature);

    const endpoint = `https://api.cloudinary.com/v1_1/${signature.cloudName}/image/upload`;
    const response = await axios.post<CloudinaryUploadResult>(endpoint, formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });

    return response.data;
};

export const saveCloudinaryAsset = async (payload: CloudinaryAssetSaveRequest): Promise<void> => {
    await axiosInstance.post('/api/media-assets', payload);
};
