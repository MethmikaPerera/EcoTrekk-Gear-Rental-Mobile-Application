import axios from 'axios';

const API_BASE_URL = 'http://10.0.2.2:8081/api';
// Use localhost if running admin web
const isWeb = typeof window !== 'undefined';
const baseURL = isWeb ? 'http://localhost:8081/api' : API_BASE_URL;

const axiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL?.trim() || baseURL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor - attach JWT token
axiosInstance.interceptors.request.use(
    (config) => {
        if (!config.skipAuth) {
            const token = sessionStorage.getItem('ecotrekk_token');
            if (token) {
                if (config.headers.set) {
                    config.headers.set('Authorization', `Bearer ${token}`);
                } else {
                    config.headers.Authorization = `Bearer ${token}`;
                }
            }
        } else if (config.headers?.Authorization !== undefined) {
            if (config.headers.delete) {
                config.headers.delete('Authorization');
            } else {
                delete config.headers.Authorization;
            }
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor - handle 401 globally
axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
        const shouldSuppressUnauthorizedEvent = Boolean(error.config?.suppressUnauthorizedEvent);
        if (error.response?.status === 401 && !shouldSuppressUnauthorizedEvent) {
            // Dispatch a custom event — AuthContext will handle logout + redirect
            window.dispatchEvent(new Event('ecotrekk:unauthorized'));
        }
        return Promise.reject(error);
    }
);

export default axiosInstance;
