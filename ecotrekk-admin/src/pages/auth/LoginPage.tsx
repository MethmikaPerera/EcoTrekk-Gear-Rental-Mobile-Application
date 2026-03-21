import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
    Box, Card, CardContent, Typography, TextField, Button,
    InputAdornment, IconButton, CircularProgress, Alert, Divider,
} from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import axios from 'axios';
import { useAuth } from '../../context/AuthContext';
import axiosInstance from '../../api/axiosInstance';

const schema = z.object({
    email: z.string().email('Enter a valid email'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
});
type FormData = z.infer<typeof schema>;

const LoginPage = () => {
    const { login } = useAuth();
    const navigate = useNavigate();
    const [showPass, setShowPass] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const { register, handleSubmit, setValue, watch, formState: { errors } } = useForm<FormData>({
        resolver: zodResolver(schema),
    });
    const watchEmail = watch('email', '');
    const watchPassword = watch('password', '');

    const onSubmit = async (data: FormData) => {
        setLoading(true);
        setError('');
        try {
            const res = await axiosInstance.post('/api/auth/admin/login', data, {
                skipAuth: true,
            });
            login(res.data.token);
            navigate('/dashboard');
        } catch (err) {
            if (axios.isAxiosError(err)) {
                const message = err.response?.data?.message || 'Something went wrong.';
                setError(message);
            } else {
                setError('Something went wrong. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box
            sx={{
                minHeight: '100vh',
                background: 'linear-gradient(135deg, #1A2E2A 0%, #1F4D3A 40%, #2E7D5B 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                p: 2,
            }}
        >
            {/* Background pattern */}
            <Box
                sx={{
                    position: 'absolute', inset: 0, opacity: 0.04,
                    backgroundImage: 'radial-gradient(circle at 2px 2px, white 1px, transparent 0)',
                    backgroundSize: '32px 32px',
                }}
            />

            <Card
                sx={{
                    width: '100%', maxWidth: 440,
                    borderRadius: 4, overflow: 'hidden',
                    boxShadow: '0 24px 64px rgba(0,0,0,0.35)',
                    position: 'relative',
                }}
            >
                {/* Top accent bar */}
                <Box sx={{ height: 4, background: 'linear-gradient(90deg, #1F4D3A, #A8D5BA)' }} />

                <CardContent sx={{ p: 4 }}>
                    {/* Logo */}
                    <Box display="flex" flexDirection="column" alignItems="center" mb={3.5}>
                        <img
                            src="/EcoTrekk-Logo.png"
                            alt="EcoTrekk Logo"
                            style={{ width: 72, height: 72, objectFit: 'contain', marginBottom: 16, borderRadius: 16 }}
                        />
                        <Typography variant="h5" fontWeight={800} color="text.primary">
                            EcoTrekk Admin
                        </Typography>
                        <Typography variant="body2" color="text.secondary" mt={0.5}>
                            Sign in to your admin console
                        </Typography>
                    </Box>

                    {error && (
                        <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>
                            {error}
                        </Alert>
                    )}

                    <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
                        <TextField
                            {...register('email')}
                            label="Email Address"
                            type="email"
                            fullWidth
                            size="medium"
                            error={!!errors.email}
                            helperText={errors.email?.message}
                            sx={{ mb: 2 }}
                            autoComplete="email"
                            autoFocus
                            InputLabelProps={{ shrink: !!watchEmail }}
                        />
                        <TextField
                            {...register('password')}
                            label="Password"
                            type={showPass ? 'text' : 'password'}
                            fullWidth
                            size="medium"
                            error={!!errors.password}
                            helperText={errors.password?.message}
                            sx={{ mb: 3 }}
                            autoComplete="current-password"
                            InputLabelProps={{ shrink: !!watchPassword }}
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton onClick={() => setShowPass((p) => !p)} edge="end" tabIndex={-1}>
                                            {showPass ? <VisibilityOffIcon /> : <VisibilityIcon />}
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            }}
                        />
                        <Button
                            type="submit"
                            variant="contained"
                            fullWidth
                            size="large"
                            disabled={loading}
                            startIcon={loading ? <CircularProgress size={18} color="inherit" /> : <LockOutlinedIcon />}
                            sx={{
                                py: 1.5, borderRadius: 2, fontWeight: 700, fontSize: '1rem',
                                background: 'linear-gradient(135deg, #1F4D3A, #2E7D5B)',
                                '&:hover': { background: 'linear-gradient(135deg, #1A2E2A, #1F4D3A)' },
                            }}
                        >
                            {loading ? 'Signing in...' : 'Sign In'}
                        </Button>
                    </Box>

                    <Divider sx={{ my: 3 }}>
                        <Typography variant="caption" color="text.disabled" fontWeight={600}>
                            DEMO CREDENTIALS
                        </Typography>
                    </Divider>

                    <Box
                        sx={{
                            background: 'rgba(45,106,79,0.06)',
                            border: '1px solid rgba(45,106,79,0.15)',
                            borderRadius: 2, p: 2,
                            cursor: 'pointer',
                            '&:hover': { background: 'rgba(45,106,79,0.1)' },
                            transition: 'background 0.2s',
                        }}
                        onClick={() => {
                            setValue('email', 'admin@ecotrekk.com', { shouldValidate: true, shouldDirty: true, shouldTouch: true });
                            setValue('password', 'admin123', { shouldValidate: true, shouldDirty: true, shouldTouch: true });
                        }}
                    >
                        <Typography variant="body2" fontWeight={600} color="primary.dark">
                            🌿 Super Admin Demo
                        </Typography>
                        <Typography variant="caption" color="text.secondary" display="block" mt={0.5}>
                            admin@ecotrekk.com / admin123
                        </Typography>
                        <Typography variant="caption" color="primary.main" fontWeight={600}>
                            Click to auto-fill →
                        </Typography>
                    </Box>
                </CardContent >
            </Card >
        </Box >
    );
};

export default LoginPage;
