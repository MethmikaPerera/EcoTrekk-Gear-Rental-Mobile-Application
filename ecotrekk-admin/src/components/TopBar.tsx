import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
    AppBar, Toolbar, IconButton, Typography, Avatar, Box, Menu, MenuItem,
    ListItemIcon, Divider, Chip, Tooltip,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import MenuOpenIcon from '@mui/icons-material/MenuOpen';
import LogoutIcon from '@mui/icons-material/Logout';
import PersonIcon from '@mui/icons-material/Person';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { useAuth } from '../context/AuthContext';
import SessionTimer from './SessionTimer';

const pageTitleMap: Record<string, string> = {
    '/dashboard': 'Dashboard',
    '/inventory/gears': 'Gear Inventory',
    '/inventory/gears/new': 'Add New Gear',
    '/inventory/hubs': 'Rental Hubs',
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
    const navigate = useNavigate();
    const location = useLocation();
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

    const pageTitle =
        pageTitleMap[location.pathname] ||
        (location.pathname.startsWith('/orders/') ? 'Order Details' : '') ||
        (location.pathname.includes('/edit') ? 'Edit Gear' : 'EcoTrekk Admin');

    const handleMenuOpen = (e: React.MouseEvent<HTMLElement>) => setAnchorEl(e.currentTarget);
    const handleMenuClose = () => setAnchorEl(null);
    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const initials = user?.name
        ? user.name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2)
        : 'AD';

    return (
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
                    <MenuItem onClick={handleMenuClose} sx={{ mt: 0.5 }}>
                        <ListItemIcon><PersonIcon fontSize="small" /></ListItemIcon>
                        Profile
                        <ChevronRightIcon fontSize="small" sx={{ ml: 'auto', color: 'text.disabled' }} />
                    </MenuItem>
                    <Divider sx={{ my: 0.5 }} />
                    <MenuItem onClick={handleLogout} sx={{ color: 'error.main' }}>
                        <ListItemIcon><LogoutIcon fontSize="small" color="error" /></ListItemIcon>
                        Sign Out
                    </MenuItem>
                </Menu>
            </Toolbar>
        </AppBar>
    );
};

export default TopBar;
