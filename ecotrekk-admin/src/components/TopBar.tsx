import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
    AppBar, Toolbar, IconButton, Typography, Avatar, Box, Menu, MenuItem,
    ListItemIcon, Divider, Chip, Tooltip,
    Dialog, DialogTitle, DialogContent, DialogActions, Button, CircularProgress,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import MenuOpenIcon from '@mui/icons-material/MenuOpen';
import LogoutIcon from '@mui/icons-material/Logout';
import PersonIcon from '@mui/icons-material/Person';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import CircleIcon from '@mui/icons-material/Circle';
import { useAuth } from '../context/AuthContext';
import { useSnackbar } from 'notistack';
import SessionTimer from './SessionTimer';
import { useWebSocket } from '../hooks/useWebSocket';
import axiosInstance from '../api/axiosInstance';

const pageTitleMap: Record<string, string> = {
    '/dashboard': 'Dashboard',
    '/inventory/gears': 'Gear Inventory',
    '/inventory/hubs': 'Rental Hubs',
    '/inventory/my-hub': 'My Hub',
    '/orders': 'Order Management',
    '/users': 'User Management',
    '/reports': 'Reports & Analytics',
    '/settings': 'Settings',
};

const roleColors: Record<string, string> = {
    SUPER_ADMIN: '#D4830A',
    INVENTORY_MANAGER: '#1F4D3A',
    ORDER_MANAGER: '#0277BD',
    SUPPORT_AGENT: '#6B7280',
};

interface TopBarProps {
    sidebarCollapsed: boolean;
    onToggleSidebar: () => void;
    onMobileMenuOpen: () => void;
}

const TopBar = ({ sidebarCollapsed, onToggleSidebar, onMobileMenuOpen }: TopBarProps) => {
    const { user, logout } = useAuth();
    const { isConnected } = useWebSocket();
    const navigate = useNavigate();
    const location = useLocation();
    const { enqueueSnackbar } = useSnackbar();

    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const [confirmOpen, setConfirmOpen] = useState(false);
    const [signingOut, setSigningOut] = useState(false);

    const pageTitle =
        pageTitleMap[location.pathname] ||
        (location.pathname.startsWith('/orders/') ? 'Order Details' : '') ||
        (location.pathname.includes('/edit') ? 'Edit Gear' : 'EcoTrekk Admin');

    const handleMenuOpen = (e: React.MouseEvent<HTMLElement>) => setAnchorEl(e.currentTarget);
    const handleMenuClose = () => setAnchorEl(null);

    const handleSignOutRequest = () => {
        handleMenuClose();
        setConfirmOpen(true);
    };

    const handleSignOutConfirm = async () => {
        setSigningOut(true);
        try {
            await axiosInstance.post('/api/auth/admin/logout');
        } catch {
            // Backend logout is best-effort — always clear local state regardless
        } finally {
            logout();
            navigate('/login');
            enqueueSnackbar('You have been signed out successfully.', { variant: 'success', autoHideDuration: 3000 });
        }
    };

    const initials = user?.name
        ? user.name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2)
        : 'AD';

    return (
        <>
            <AppBar
                position="sticky"
                elevation={0}
                sx={{
                    background: '#fff',
                    borderBottom: '1px solid rgba(0,0,0,0.08)',
                    color: 'text.primary',
                }}
            >
                <Toolbar sx={{ gap: 1 }}>
                    {/* Desktop toggle */}
                    <Tooltip title={sidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}>
                        <IconButton
                            onClick={onToggleSidebar}
                            sx={{ display: { xs: 'none', md: 'flex' }, color: 'text.secondary' }}
                        >
                            {sidebarCollapsed ? <MenuIcon /> : <MenuOpenIcon />}
                        </IconButton>
                    </Tooltip>

                    {/* Mobile menu */}
                    <IconButton
                        onClick={onMobileMenuOpen}
                        sx={{ display: { xs: 'flex', md: 'none' }, color: 'text.secondary' }}
                    >
                        <MenuIcon />
                    </IconButton>

                    <Typography variant="h6" fontWeight={700} sx={{ flexGrow: 1, color: 'text.primary', ml: 1 }}>
                        {pageTitle}
                    </Typography>

                    {/* Right side */}
                    <Box display="flex" alignItems="center" gap={1.5}>
                        <SessionTimer />

                        <Tooltip title={isConnected ? 'Realtime updates connected' : 'Realtime updates disconnected'}>
                            <Chip
                                icon={<CircleIcon sx={{ fontSize: '0.62rem !important' }} />}
                                label={isConnected ? 'Live' : 'Offline'}
                                size="small"
                                sx={{
                                    fontWeight: 700,
                                    fontSize: '0.73rem',
                                    display: { xs: 'none', sm: 'flex' },
                                    bgcolor: isConnected ? 'rgba(22,163,74,0.10)' : 'rgba(217, 66, 6, 0.12)',
                                    color: isConnected ? '#15803D' : '#b43109',
                                    border: isConnected
                                        ? '1px solid rgba(22,163,74,0.28)'
                                        : '1px solid rgba(217,119,6,0.30)',
                                    '& .MuiChip-icon': {
                                        color: isConnected ? '#22C55E' : '#f53a0b',
                                    },
                                }}
                            />
                        </Tooltip>

                        {user && (
                            <Chip
                                label={user.role.replace(/_/g, ' ')}
                                size="small"
                                sx={{
                                    fontWeight: 700,
                                    fontSize: '0.7rem',
                                    bgcolor: `${roleColors[user.role]}22`,
                                    color: roleColors[user.role] || '#6B7280',
                                    border: `1px solid ${roleColors[user.role] || '#6B7280'}44`,
                                    display: { xs: 'none', sm: 'flex' },
                                }}
                            />
                        )}

                        <Tooltip title="Account">
                            <IconButton onClick={handleMenuOpen} size="small">
                                <Avatar
                                    sx={{
                                        width: 36, height: 36,
                                        background: 'linear-gradient(135deg, #1F4D3A, #2E7D5B)',
                                        fontSize: '0.85rem',
                                        fontWeight: 700,
                                    }}
                                >
                                    {initials}
                                </Avatar>
                            </IconButton>
                        </Tooltip>
                    </Box>

                    <Menu
                        anchorEl={anchorEl}
                        open={Boolean(anchorEl)}
                        onClose={handleMenuClose}
                        PaperProps={{
                            elevation: 4,
                            sx: { minWidth: 220, borderRadius: 2, mt: 1 },
                        }}
                        transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
                    >
                        {user && (
                            <Box sx={{ px: 2, py: 1.5, borderBottom: '1px solid rgba(0,0,0,0.06)' }}>
                                <Typography fontWeight={700} fontSize="0.95rem">{user.name}</Typography>
                                <Typography fontSize="0.8rem" color="text.secondary">{user.email}</Typography>
                            </Box>
                        )}
                        <MenuItem onClick={() => { handleMenuClose(); navigate('/profile'); }} sx={{ mt: 0.5 }}>
                            <ListItemIcon><PersonIcon fontSize="small" /></ListItemIcon>
                            Profile
                            <ChevronRightIcon fontSize="small" sx={{ ml: 'auto', color: 'text.disabled' }} />
                        </MenuItem>
                        <Divider sx={{ my: 0.5 }} />
                        <MenuItem onClick={handleSignOutRequest} sx={{ color: 'error.main' }}>
                            <ListItemIcon><LogoutIcon fontSize="small" color="error" /></ListItemIcon>
                            Sign Out
                        </MenuItem>
                    </Menu>
                </Toolbar>
            </AppBar>

            {/* Sign-out confirmation dialog */}
            <Dialog
                open={confirmOpen}
                onClose={() => !signingOut && setConfirmOpen(false)}
                maxWidth="xs"
                fullWidth
                PaperProps={{ sx: { borderRadius: 3 } }}
            >
                <DialogTitle sx={{ pb: 1 }}>
                    <Box display="flex" alignItems="center" gap={1.5}>
                        <Avatar sx={{ bgcolor: 'error.light', width: 40, height: 40 }}>
                            <WarningAmberIcon sx={{ color: 'error.main', fontSize: 22 }} />
                        </Avatar>
                        <Box>
                            <Typography fontWeight={700} fontSize="1.05rem">Sign Out</Typography>
                            <Typography variant="caption" color="text.secondary">
                                You will be returned to the login screen
                            </Typography>
                        </Box>
                    </Box>
                </DialogTitle>

                <DialogContent sx={{ pt: 0 }}>
                    <Typography color="text.secondary" variant="body2">
                        Are you sure you want to sign out? Any unsaved changes will be lost.
                    </Typography>
                    {user && (
                        <Box
                            sx={{
                                mt: 2, p: 1.5, borderRadius: 2,
                                background: 'rgba(31,77,58,0.06)',
                                border: '1px solid rgba(31,77,58,0.15)',
                                display: 'flex', alignItems: 'center', gap: 1.5,
                            }}
                        >
                            <Avatar sx={{ width: 32, height: 32, bgcolor: '#1F4D3A', fontSize: '0.75rem', fontWeight: 700 }}>
                                {initials}
                            </Avatar>
                            <Box>
                                <Typography fontWeight={600} fontSize="0.85rem">{user.name}</Typography>
                                <Typography variant="caption" color="text.secondary">{user.role.replace(/_/g, ' ')}</Typography>
                            </Box>
                        </Box>
                    )}
                </DialogContent>

                <DialogActions sx={{ px: 3, pb: 2.5, gap: 1 }}>
                    <Button
                        variant="outlined"
                        onClick={() => setConfirmOpen(false)}
                        disabled={signingOut}
                        sx={{ borderRadius: 2, flex: 1 }}
                    >
                        Cancel
                    </Button>
                    <Button
                        variant="contained"
                        color="error"
                        onClick={handleSignOutConfirm}
                        disabled={signingOut}
                        startIcon={signingOut ? <CircularProgress size={16} color="inherit" /> : <LogoutIcon />}
                        sx={{ borderRadius: 2, flex: 1, fontWeight: 700 }}
                    >
                        {signingOut ? 'Signing out…' : 'Sign Out'}
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
};

export default TopBar;
