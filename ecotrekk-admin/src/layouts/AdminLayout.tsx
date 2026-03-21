import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Box } from '@mui/material';
import Sidebar from '../components/Sidebar';
import TopBar from '../components/TopBar';
import { useRealtimeSync } from '../hooks/useRealtimeSync';

const DRAWER_WIDTH = 260;
const DRAWER_COLLAPSED_WIDTH = 72;

const AdminLayout = () => {
    const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
    const [mobileOpen, setMobileOpen] = useState(false);
    
    // Initialize real-time data sync
    useRealtimeSync();

    const currentWidth = sidebarCollapsed ? DRAWER_COLLAPSED_WIDTH : DRAWER_WIDTH;

    return (
        <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
            <Sidebar
                collapsed={sidebarCollapsed}
                onToggle={() => setSidebarCollapsed((p) => !p)}
                mobileOpen={mobileOpen}
                onMobileClose={() => setMobileOpen(false)}
            />
            <Box
                sx={{
                    flex: 1,
                    display: 'flex',
                    flexDirection: 'column',
                    minWidth: 0,
                    ml: { md: 0 },
                    transition: 'margin 0.25s ease',
                }}
            >
                <TopBar
                    sidebarCollapsed={sidebarCollapsed}
                    onToggleSidebar={() => setSidebarCollapsed((p) => !p)}
                    onMobileMenuOpen={() => setMobileOpen(true)}
                />
                <Box
                    component="main"
                    sx={{
                        flex: 1,
                        p: { xs: 2, sm: 3 },
                        maxWidth: '100%',
                        overflow: 'auto',
                    }}
                >
                    <Outlet />
                </Box>
            </Box>
        </Box>
    );
};

export default AdminLayout;
