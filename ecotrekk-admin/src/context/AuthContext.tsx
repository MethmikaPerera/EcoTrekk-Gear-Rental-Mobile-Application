import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { jwtDecode } from 'jwt-decode';

export type AdminRole = 'SUPER_ADMIN' | 'INVENTORY_MANAGER' | 'ORDER_MANAGER' | 'SUPPORT_AGENT';

export interface AdminUser {
    id: string;
    name: string;
    email: string;
    role: AdminRole;
    token: string;
}

interface DecodedToken {
    sub: string;
    name?: string;
    email?: string;
    role?: AdminRole;
    exp: number;
}

interface AuthContextType {
    user: AdminUser | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (token: string) => void;
    logout: () => void;
    hasRole: (roles: AdminRole | AdminRole[]) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [user, setUser] = useState<AdminUser | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const token = sessionStorage.getItem('ecotrekk_token');
        if (token) {
            try {
                const decoded = jwtDecode<DecodedToken>(token);
                if (decoded.exp * 1000 > Date.now()) {
                    setUser({
                        id: decoded.sub,
                        name: decoded.name || 'Admin',
                        email: decoded.email || '',
                        role: decoded.role || 'SUPPORT_AGENT',
                        token,
                    });
                } else {
                    sessionStorage.removeItem('ecotrekk_token');
                }
            } catch {
                sessionStorage.removeItem('ecotrekk_token');
            }
        }
        setIsLoading(false);
    }, []);

    const login = (token: string) => {
        try {
            const decoded = jwtDecode<DecodedToken>(token);
            const adminUser: AdminUser = {
                id: decoded.sub,
                name: decoded.name || 'Admin',
                email: decoded.email || '',
                role: decoded.role || 'SUPPORT_AGENT',
                token,
            };
            sessionStorage.setItem('ecotrekk_token', token);
            setUser(adminUser);
        } catch {
            console.error('Invalid token');
        }
    };

    const logout = () => {
        sessionStorage.removeItem('ecotrekk_token');
        setUser(null);
    };

    const hasRole = (roles: AdminRole | AdminRole[]) => {
        if (!user) return false;
        const roleArray = Array.isArray(roles) ? roles : [roles];
        return roleArray.includes(user.role);
    };

    return (
        <AuthContext.Provider value={{ user, isAuthenticated: !!user, isLoading, login, logout, hasRole }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth must be used within AuthProvider');
    return ctx;
};
