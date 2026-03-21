import { useState, useEffect } from 'react';
import {
    Box, Card, CardContent, Typography, Grid, TextField, Button,
    Avatar, Skeleton, Divider, InputAdornment, IconButton,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import { useQuery, useMutation } from '@tanstack/react-query';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { getProfile, changePassword } from '../../api/userApi';
import { useAuth } from '../../context/AuthContext';
import PersonIcon from '@mui/icons-material/Person';
import LockResetIcon from '@mui/icons-material/LockReset';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import VpnKeyIcon from '@mui/icons-material/VpnKey';
import EmailIcon from '@mui/icons-material/Email';
import BadgeIcon from '@mui/icons-material/Badge';
import DomainIcon from '@mui/icons-material/Domain';
import PageHeader from '../../components/PageHeader';

const passwordSchema = z.object({
    currentPassword: z.string().min(1, 'Current password is required'),
    newPassword: z.string().min(6, 'New password must be at least 6 characters'),
    confirmPassword: z.string().min(1, 'Please confirm your new password'),
}).refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
});

type PasswordFormData = z.infer<typeof passwordSchema>;

const AdminProfilePage = () => {
    const { enqueueSnackbar } = useSnackbar();
    const { user: authUser } = useAuth();
    
    const [showCurrentPassword, setShowCurrentPassword] = useState(false);
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const {
        control,
        handleSubmit,
        reset,
        formState: { errors, isValid }
    } = useForm<PasswordFormData>({
        resolver: zodResolver(passwordSchema),
        defaultValues: {
            currentPassword: '',
            newPassword: '',
            confirmPassword: '',
        },
        mode: 'onChange',
    });

    const { data: profile, isLoading, error } = useQuery({
        queryKey: ['adminProfile'],
        queryFn: getProfile,
    });

    const passwordMutation = useMutation({
        mutationFn: changePassword,
        onSuccess: () => {
            enqueueSnackbar('Password changed successfully!', { variant: 'success' });
            reset();
        },
        onError: (err: any) => {
            enqueueSnackbar(err.response?.data?.message || 'Failed to change password', { variant: 'error' });
        }
    });

    const onSubmit = (data: PasswordFormData) => {
        passwordMutation.mutate({
            currentPassword: data.currentPassword,
            newPassword: data.newPassword,
        });
    };

    if (error) {
        return <Typography color="error">Failed to load profile details.</Typography>;
    }

    const roleColors: Record<string, string> = {
        SUPER_ADMIN: '#D4830A',
        ADMIN: '#0277BD',
        STAFF: '#1F4D3A',
    };

    const displayRole = profile?.role || authUser?.role || 'STAFF';

    return (
        <Box sx={{ p: { xs: 2, md: 4 }, maxWidth: 1000, margin: '0 auto' }}>
            <PageHeader 
                title="My Profile" 
                subtitle="Manage your personal details and security settings"
            />

            <Grid container spacing={4} sx={{ mt: 1 }}>
                
                {/* Details Section */}
                <Grid size={{ xs: 12, md: 5 }}>
                    <Card sx={{ 
                        borderRadius: 3, 
                        boxShadow: '0px 10px 30px rgba(0,0,0,0.05)',
                        background: 'rgba(255, 255, 255, 0.8)',
                        backdropFilter: 'blur(12px)',
                        border: '1px solid rgba(255, 255, 255, 0.3)',
                        height: '100%'
                    }}>
                        <CardContent sx={{ p: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                            <Avatar 
                                sx={{ 
                                    width: 100, 
                                    height: 100, 
                                    bgcolor: 'primary.main',
                                    mb: 2,
                                    fontSize: '2.5rem',
                                    fontWeight: 'bold',
                                    boxShadow: '0 8px 24px rgba(31, 77, 58, 0.2)'
                                }}
                            >
                                {profile ? profile.fullName.charAt(0).toUpperCase() : <PersonIcon fontSize="large" />}
                            </Avatar>
                            
                            <Typography variant="h5" fontWeight={800} gutterBottom>
                                {isLoading ? <Skeleton width={150} /> : profile?.fullName}
                            </Typography>
                            
                            <Box 
                                sx={{ 
                                    bgcolor: `${roleColors[displayRole]}22`, 
                                    color: roleColors[displayRole],
                                    px: 2, py: 0.5, 
                                    borderRadius: '12px',
                                    fontSize: '0.85rem',
                                    fontWeight: 700,
                                    mb: 4,
                                    border: `1px solid ${roleColors[displayRole]}44`
                                }}
                            >
                                {displayRole.replace('_', ' ')}
                            </Box>

                            <Divider sx={{ width: '100%', mb: 3 }} />

                            <Box sx={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                                <Box display="flex" alignItems="center" gap={2}>
                                    <Avatar sx={{ width: 40, height: 40, bgcolor: 'rgba(0,0,0,0.04)', color: 'text.secondary' }}>
                                        <BadgeIcon fontSize="small" />
                                    </Avatar>
                                    <Box>
                                        <Typography variant="caption" color="text.secondary" fontWeight={600}>USERNAME</Typography>
                                        <Typography variant="body2" fontWeight={500}>
                                            {isLoading ? <Skeleton width={100} /> : profile?.username}
                                        </Typography>
                                    </Box>
                                </Box>

                                <Box display="flex" alignItems="center" gap={2}>
                                    <Avatar sx={{ width: 40, height: 40, bgcolor: 'rgba(0,0,0,0.04)', color: 'text.secondary' }}>
                                        <EmailIcon fontSize="small" />
                                    </Avatar>
                                    <Box>
                                        <Typography variant="caption" color="text.secondary" fontWeight={600}>EMAIL ADDRESS</Typography>
                                        <Typography variant="body2" fontWeight={500}>
                                            {isLoading ? <Skeleton width={150} /> : profile?.email}
                                        </Typography>
                                    </Box>
                                </Box>

                                {(displayRole === 'ADMIN' || displayRole === 'STAFF') && (
                                    <Box display="flex" alignItems="center" gap={2}>
                                        <Avatar sx={{ width: 40, height: 40, bgcolor: 'rgba(0,0,0,0.04)', color: 'text.secondary' }}>
                                            <DomainIcon fontSize="small" />
                                        </Avatar>
                                        <Box>
                                            <Typography variant="caption" color="text.secondary" fontWeight={600}>ASSIGNED HUB</Typography>
                                            <Typography variant="body2" fontWeight={500}>
                                                {isLoading ? 
                                                    <Skeleton width={120} /> : 
                                                    (profile?.hubId ? `Hub ID: ${profile.hubId}` : 'Not Assigned')
                                                }
                                            </Typography>
                                        </Box>
                                    </Box>
                                )}
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>

                {/* Security Section */}
                <Grid size={{ xs: 12, md: 7 }}>
                    <Card sx={{ 
                        borderRadius: 3, 
                        boxShadow: '0px 10px 30px rgba(0,0,0,0.05)',
                        background: 'rgba(255, 255, 255, 0.8)',
                        backdropFilter: 'blur(12px)',
                        border: '1px solid rgba(255, 255, 255, 0.3)',
                    }}>
                        <CardContent sx={{ p: 4 }}>
                            <Box display="flex" alignItems="center" gap={1.5} mb={3}>
                                <Avatar sx={{ bgcolor: 'rgba(31, 77, 58, 0.1)', color: 'primary.main' }}>
                                    <VpnKeyIcon />
                                </Avatar>
                                <Typography variant="h6" fontWeight={700}>Security & Password</Typography>
                            </Box>
                            
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>
                                Ensure your account is using a long, random password to stay secure. 
                                We recommend using a password manager.
                            </Typography>

                            <form onSubmit={handleSubmit(onSubmit)}>
                                <Box display="flex" flexDirection="column" gap={3}>
                                    <Controller
                                        name="currentPassword"
                                        control={control}
                                        render={({ field }: { field: any }) => (
                                            <TextField
                                                {...field}
                                                label="Current Password"
                                                type={showCurrentPassword ? 'text' : 'password'}
                                                fullWidth
                                                variant="outlined"
                                                error={!!errors.currentPassword}
                                                helperText={errors.currentPassword?.message}
                                                InputProps={{
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <IconButton
                                                                onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                                                                edge="end"
                                                            >
                                                                {showCurrentPassword ? <VisibilityOff /> : <Visibility />}
                                                            </IconButton>
                                                        </InputAdornment>
                                                    ),
                                                }}
                                            />
                                        )}
                                    />
                                    
                                    <Divider sx={{ my: 1 }} />

                                    <Controller
                                        name="newPassword"
                                        control={control}
                                        render={({ field }: { field: any }) => (
                                            <TextField
                                                {...field}
                                                label="New Password"
                                                type={showNewPassword ? 'text' : 'password'}
                                                fullWidth
                                                variant="outlined"
                                                error={!!errors.newPassword}
                                                helperText={errors.newPassword?.message}
                                                InputProps={{
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <IconButton
                                                                onClick={() => setShowNewPassword(!showNewPassword)}
                                                                edge="end"
                                                            >
                                                                {showNewPassword ? <VisibilityOff /> : <Visibility />}
                                                            </IconButton>
                                                        </InputAdornment>
                                                    ),
                                                }}
                                            />
                                        )}
                                    />

                                    <Controller
                                        name="confirmPassword"
                                        control={control}
                                        render={({ field }: { field: any }) => (
                                            <TextField
                                                {...field}
                                                label="Confirm New Password"
                                                type={showConfirmPassword ? 'text' : 'password'}
                                                fullWidth
                                                variant="outlined"
                                                error={!!errors.confirmPassword}
                                                helperText={errors.confirmPassword?.message}
                                                InputProps={{
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <IconButton
                                                                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                                                edge="end"
                                                            >
                                                                {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                                                            </IconButton>
                                                        </InputAdornment>
                                                    ),
                                                }}
                                            />
                                        )}
                                    />

                                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
                                        <Button
                                            type="submit"
                                            variant="contained"
                                            color="primary"
                                            size="large"
                                            disabled={!isValid || passwordMutation.isPending}
                                            startIcon={<LockResetIcon />}
                                            sx={{ borderRadius: 2, px: 4, fontWeight: 700 }}
                                        >
                                            {passwordMutation.isPending ? 'Updating...' : 'Update Password'}
                                        </Button>
                                    </Box>
                                </Box>
                            </form>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>
        </Box>
    );
};

export default AdminProfilePage;
