import { useNavigate, useLocation } from 'react-router-dom';
import {
    Box, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText,
    Typography, Tooltip, Divider, IconButton,
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import PeopleIcon from '@mui/icons-material/People';
import BarChartIcon from '@mui/icons-material/BarChart';

import ListAltIcon from '@mui/icons-material/ListAlt';

import HubIcon from '@mui/icons-material/Store';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import { useAuth, AdminRole } from '../context/AuthContext';

const DRAWER_WIDTH = 260;
const DRAWER_COLLAPSED_WIDTH = 72;

interface NavItem {
    label: string;
    icon: React.ReactNode;
    path?: string;
    requiredRole?: AdminRole[];
}

const navItems: NavItem[] = [
    { label: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
    { label: 'Gears', icon: <ListAltIcon />, path: '/inventory/gears' },
    { label: 'Rental Hubs', icon: <HubIcon />, path: '/inventory/hubs', requiredRole: ['SUPER_ADMIN'] },
    { label: 'My Hub', icon: <HubIcon />, path: '/inventory/my-hub', requiredRole: ['ADMIN'] },
    { label: 'Orders', icon: <ShoppingCartIcon />, path: '/orders' },
    { label: 'Users', icon: <PeopleIcon />, path: '/users' },
    { label: 'Admins', icon: <AdminPanelSettingsIcon />, path: '/admins', requiredRole: ['SUPER_ADMIN'] },
    { label: 'Reports', icon: <BarChartIcon />, path: '/reports' },

];

interface SidebarProps {
    collapsed: boolean;
    onToggle: () => void;
    mobileOpen: boolean;
    onMobileClose: () => void;
}

const Sidebar = ({ collapsed, onToggle, mobileOpen, onMobileClose }: SidebarProps) => {
    const navigate = useNavigate();
    const location = useLocation();
    const { user } = useAuth();

    const isActive = (path?: string) =>
        path ? location.pathname === path || location.pathname.startsWith(path + '/') : false;

    const handleNav = (path: string) => {
        navigate(path);
        onMobileClose();
    };

    const renderNavItem = (item: NavItem, depth = 0) => {
        if (item.requiredRole && user && !item.requiredRole.includes(user.role)) return null;


        const active = isActive(item.path);
        return (
            <Tooltip key={item.label} title={collapsed ? item.label : ''} placement="right">
                <ListItem disablePadding sx={{ display: 'block' }}>
                    <ListItemButton
                        onClick={() => item.path && handleNav(item.path)}
                        sx={{
                            minHeight: 48,
                            px: collapsed ? 2.5 : 2,
                            mx: 1,
                            borderRadius: 2,
                            mb: 0.5,
                            backgroundColor: active ? 'rgba(46,125,91,0.2)' : 'transparent',
                            borderLeft: active ? '3px solid #A8D5BA' : '3px solid transparent',
                            '&:hover': { backgroundColor: 'rgba(46,125,91,0.12)' },
                        }}
                    >
                        <ListItemIcon
                            sx={{
                                minWidth: 0,
                                mr: collapsed ? 0 : 2,
                                justifyContent: 'center',
                                color: active ? '#A8D5BA' : 'rgba(255,255,255,0.65)',
                            }}
                        >
                            {item.icon}
                        </ListItemIcon>
                        {!collapsed && (
                            <ListItemText
                                primary={item.label}
                                primaryTypographyProps={{
                                    fontSize: '0.9rem',
                                    fontWeight: active ? 700 : 400,
                                    color: active ? '#fff' : 'rgba(255,255,255,0.8)',
                                }}
                            />
                        )}
                    </ListItemButton>
                </ListItem>
            </Tooltip>
        );
    };

    const drawerContent = (
        <Box
            sx={{
                height: '100%',
                background: 'linear-gradient(180deg, #1A2E2A 0%, #1F4D3A 60%, #1A2E2A 100%)',
                display: 'flex',
                flexDirection: 'column',
                overflow: 'hidden',
            }}
        >
            {/* Logo Area */}
            <Box
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: collapsed ? 'center' : 'space-between',
                    px: collapsed ? 1 : 2.5,
                    py: 2.5,
                    minHeight: 72,
                    borderBottom: '1px solid rgba(255,255,255,0.08)',
                }}
            >
                <Box display="flex" alignItems="center" gap={1.2}>
                    <img
                        src="/EcoTrekk-Logo.png"
                        alt="EcoTrekk Logo"
                        style={{ width: 36, height: 36, objectFit: 'contain', flexShrink: 0, borderRadius: 8 }}
                    />
                    {!collapsed && (
                        <Box>
                            <Typography fontWeight={800} fontSize="1.05rem" color="#fff" lineHeight={1}>
                                EcoTrekk
                            </Typography>
                            <Typography fontSize="0.68rem" color="rgba(255,255,255,0.55)" fontWeight={500} letterSpacing="0.08em">
                                ADMIN CONSOLE
                            </Typography>
                        </Box>
                    )}
                </Box>
            </Box>

            {/* Nav Items */}
            <Box sx={{ flex: 1, overflowY: 'auto', overflowX: 'hidden', py: 1, '&::-webkit-scrollbar': { width: 4 }, '&::-webkit-scrollbar-thumb': { background: 'rgba(255,255,255,0.15)', borderRadius: 2 } }}>
                <List disablePadding>
                    {navItems.map((item) => renderNavItem(item))}
                </List>
            </Box>

            {/* Footer */}
            <Box sx={{ borderTop: '1px solid rgba(255,255,255,0.08)', py: 2, px: collapsed ? 1 : 2.5 }}>
                {!collapsed && user && (
                    <Box>
                        <Typography fontSize="0.75rem" color="rgba(255,255,255,0.5)" fontWeight={600} textTransform="uppercase" letterSpacing="0.06em">
                            Signed in as
                        </Typography>
                        <Typography fontSize="0.85rem" color="#fff" fontWeight={600} mt={0.3} noWrap>
                            {user.name}
                        </Typography>
                        <Typography fontSize="0.72rem" color="rgba(255,255,255,0.5)" noWrap>
                            {user.role.replace('_', ' ')}
                        </Typography>
                    </Box>
                )}
            </Box>
        </Box>
    );

    return (
        <>
            {/* Desktop Sidebar */}
            <Drawer
                variant="permanent"
                sx={{
                    display: { xs: 'none', md: 'block' },
                    width: collapsed ? DRAWER_COLLAPSED_WIDTH : DRAWER_WIDTH,
                    flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: collapsed ? DRAWER_COLLAPSED_WIDTH : DRAWER_WIDTH,
                        transition: 'width 0.25s ease',
                        overflowX: 'hidden',
                        border: 'none',
                        boxShadow: '4px 0 24px rgba(0,0,0,0.12)',
                    },
                }}
            >
                {drawerContent}
            </Drawer>

            {/* Mobile Drawer */}
            <Drawer
                variant="temporary"
                open={mobileOpen}
                onClose={onMobileClose}
                ModalProps={{ keepMounted: true }}
                sx={{
                    display: { xs: 'block', md: 'none' },
                    '& .MuiDrawer-paper': { width: DRAWER_WIDTH, border: 'none' },
                }}
            >
                {drawerContent}
            </Drawer>
        </>
    );
};

export default Sidebar;
