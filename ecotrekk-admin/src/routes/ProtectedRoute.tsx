import { Navigate, Outlet } from 'react-router-dom';
import { useAuth, AdminRole } from '../context/AuthContext';
import { Box, CircularProgress, Typography } from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';

interface ProtectedRouteProps {
    requiredRoles?: AdminRole[];
}

const ProtectedRoute = ({ requiredRoles }: ProtectedRouteProps) => {
    const { isAuthenticated, isLoading, hasRole } = useAuth();

    if (isLoading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
                <CircularProgress color="primary" />
            </Box>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (requiredRoles && !hasRole(requiredRoles)) {
        return (
            <Box
                display="flex"
                flexDirection="column"
                justifyContent="center"
                alignItems="center"
                minHeight="60vh"
                gap={2}
            >
                <LockOutlinedIcon sx={{ fontSize: 64, color: 'error.main', opacity: 0.7 }} />
                <Typography variant="h5" fontWeight={700} color="error.main">
                    Access Denied
                </Typography>
                <Typography variant="body1" color="text.secondary" textAlign="center">
                    You don't have permission to access this page.
                    <br />
                    Contact your system administrator.
                </Typography>
            </Box>
        );
    }

    return <Outlet />;
};

export default ProtectedRoute;
