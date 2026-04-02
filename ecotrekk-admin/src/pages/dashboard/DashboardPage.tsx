import { useNavigate } from 'react-router-dom';
import {
    Grid, Card, CardContent, Typography, Box, Button, Avatar,
    Table, TableBody, TableCell, TableHead, TableRow, Paper, Alert,
} from '@mui/material';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import PendingActionsIcon from '@mui/icons-material/PendingActions';
import InventoryIcon from '@mui/icons-material/Inventory2';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import AddIcon from '@mui/icons-material/Add';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import WbSunnyIcon from '@mui/icons-material/WbSunny';
import NightsStayIcon from '@mui/icons-material/NightsStay';
import LightModeIcon from '@mui/icons-material/LightMode';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';
import { useQuery } from '@tanstack/react-query';
import './DashboardPage.css';
import StatusBadge from '../../components/StatusBadge';
import { useAuth } from '../../context/AuthContext';
import { getMetricsSummary } from '../../api/metricsApi';
import { getOrders, type OrderStatus } from '../../api/orderApi';

type DashboardRecentOrder = {
    id: string;
    customer: string;
    gear: string;
    status: OrderStatus;
    total: number;
};

// ─── Helpers ─────────────────────────────────────────────────────────────────
const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return { text: 'Good morning', icon: <WbSunnyIcon sx={{ color: '#F4A261', fontSize: 22 }} /> };
    if (hour < 17) return { text: 'Good afternoon', icon: <LightModeIcon sx={{ color: '#D4830A', fontSize: 22 }} /> };
    return { text: 'Good evening', icon: <NightsStayIcon sx={{ color: '#52B788', fontSize: 22 }} /> };
};

const formatDate = () =>
    new Date().toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });

// ─── Stat Card ────────────────────────────────────────────────────────────────
interface StatCardProps {
    title: string;
    value: string | number;
    icon: React.ReactNode;
    color: string;
    prefix?: string;
    onClick?: () => void;
}

const StatCard = ({ title, value, icon, color, prefix, onClick }: StatCardProps) => (
    <Card
        sx={{
            height: '100%',
            position: 'relative',
            overflow: 'hidden',
            cursor: onClick ? 'pointer' : 'default',
            transition: 'transform 0.2s ease, box-shadow 0.2s ease',
            '&:hover': onClick
                ? {
                    transform: 'translateY(-2px)',
                    boxShadow: '0 10px 20px rgba(10, 48, 41, 0.1)',
                }
                : {},
        }}
        onClick={onClick}
    >
        <Box
            sx={{
                position: 'absolute',
                right: -18,
                top: -18,
                width: 108,
                height: 108,
                borderRadius: '50%',
                background: `${color}18`,
            }}
        />
        <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', p: 2.5 }}>
            <Box>
                <Typography variant="caption" color="text.secondary" fontWeight={600} textTransform="uppercase" letterSpacing="0.06em">
                    {title}
                </Typography>
                <Typography variant="h4" fontWeight={800} mt={0.5} color="text.primary">
                    {prefix}{typeof value === 'number' ? value.toLocaleString() : value}
                </Typography>
            </Box>
            <Avatar sx={{ bgcolor: `${color}18`, width: 52, height: 52 }}>
                <Box sx={{ color }}>{icon}</Box>
            </Avatar>
        </CardContent>
    </Card>
);

// ─── Dashboard Page ───────────────────────────────────────────────────────────
const DashboardPage = () => {
    const navigate = useNavigate();
    const { user } = useAuth();
    const greeting = getGreeting();

    const {
        data: metricsResponse,
        isLoading: isMetricsLoading,
        isError: isMetricsError,
    } = useQuery({
        queryKey: ['dashboard-metrics'],
        queryFn: getMetricsSummary,
    });

    const {
        data: recentOrdersResponse,
        isLoading: isRecentOrdersLoading,
        isError: isRecentOrdersError,
    } = useQuery({
        queryKey: ['dashboard-recent-orders'],
        queryFn: () => getOrders({ page: 0, size: 5 }),
    });

    const metrics = metricsResponse?.data;
    const recentOrders: DashboardRecentOrder[] = (recentOrdersResponse?.data.content || []).map((order) => ({
        id: order.id,
        customer: order.customer.name,
        gear: order.items[0]?.gearName || 'Multiple items',
        status: order.status,
        total: Number(order.totalAmount),
    }));

    const dailyOrderCounts = (metrics?.ordersThisWeek || []).map((item) => ({
        day: item.date,
        count: item.count,
    }));

    return (
        <Box className="dashboard-shell">
            <Card
                sx={{
                    mb: 3,
                    background: 'linear-gradient(135deg, #1A2E2A 0%, #1F4D3A 60%, #2E7D5B 100%)',
                    color: '#fff',
                    overflow: 'hidden',
                    position: 'relative',
                }}
            >
                <Box
                    sx={{
                        position: 'absolute',
                        inset: 0,
                        opacity: 0.06,
                        backgroundImage: 'radial-gradient(circle at 2px 2px, white 1px, transparent 0)',
                        backgroundSize: '28px 28px',
                    }}
                />
                <CardContent sx={{ p: 3, position: 'relative' }}>
                    <Box display="flex" justifyContent="space-between" alignItems="flex-start" flexWrap="wrap" gap={2}>
                        <Box>
                            <Box display="flex" alignItems="center" gap={1} mb={0.5}>
                                {greeting.icon}
                                <Typography variant="body1" sx={{ color: 'rgba(255,255,255,0.8)', fontWeight: 500 }}>
                                    {greeting.text}
                                </Typography>
                            </Box>
                            <Typography
                                variant="h4"
                                fontWeight={800}
                                color="#fff"
                                mb={0.6}
                                sx={{ letterSpacing: '-0.01em' }}
                            >
                                {user?.name || 'Admin'}
                            </Typography>
                            <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.7)' }}>
                                {formatDate()} · Here's a quick overview of your day.
                            </Typography>
                        </Box>
                        <Box display="flex" gap={1.5} flexWrap="wrap">
                            <Button
                                variant="contained"
                                startIcon={<AddIcon />}
                                onClick={() => navigate('/inventory/gears')}
                                sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: '#fff', '&:hover': { bgcolor: 'rgba(255,255,255,0.3)' }, backdropFilter: 'blur(4px)', fontWeight: 700 }}
                            >
                                Add Gear
                            </Button>
                            <Button
                                variant="outlined"
                                endIcon={<ArrowForwardIcon />}
                                onClick={() => navigate('/orders')}
                                sx={{ borderColor: 'rgba(255,255,255,0.4)', color: '#fff', '&:hover': { borderColor: '#fff', bgcolor: 'rgba(255,255,255,0.1)' } }}
                            >
                                View Orders
                            </Button>
                        </Box>
                    </Box>
                </CardContent>
            </Card>

            {(isMetricsError || isRecentOrdersError) && (
                <Alert severity="error" sx={{ mb: 2.5, borderRadius: 2 }}>
                    Some dashboard data could not be loaded. Showing available data.
                </Alert>
            )}

            {(metrics?.pendingOrders || 0) > 0 && (
                <Alert
                    severity="warning"
                    icon={<WarningAmberRoundedIcon />}
                    sx={{ mb: 2.5, borderRadius: 2 }}
                    action={
                        <Button color="warning" size="small" onClick={() => navigate('/orders?status=PENDING')}>
                            Review
                        </Button>
                    }
                >
                    <strong>{metrics?.pendingOrders || 0} orders</strong> are waiting for your confirmation.
                </Alert>
            )}

            <Grid container spacing={2.5} mb={3}>
                <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
                    <StatCard
                        title="Orders Today" value={metrics?.totalOrdersToday || 0}
                        icon={<ShoppingCartIcon />} color="#1F4D3A"
                        onClick={() => navigate('/orders')}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
                    <StatCard
                        title="Pending Orders" value={metrics?.pendingOrders || 0}
                        icon={<PendingActionsIcon />} color="#D4830A"
                        onClick={() => navigate('/orders')}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
                    <StatCard
                        title="Low Stock Items" value={metrics?.lowStockItems || 0}
                        icon={<InventoryIcon />} color="#D32F2F"
                        onClick={() => navigate('/inventory/gears')}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
                    <StatCard
                        title="Revenue Today" value={metrics?.revenueToday || 0}
                        icon={<AttachMoneyIcon />} color="#0277BD" prefix="LKR "
                    />
                </Grid>
            </Grid>

            <Grid container spacing={2.5}>
                <Grid size={{ xs: 12, md: 5 }}>
                    <Card>
                        <CardContent>
                            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                                <Box>
                                    <Typography variant="h6" fontWeight={700}>Daily Orders</Typography>
                                    <Typography variant="caption" color="text.secondary">
                                        {isMetricsLoading ? 'Loading weekly analytics...' : 'Order count for each day of the week'}
                                    </Typography>
                                </Box>
                                <Button size="small" onClick={() => navigate('/orders')} endIcon={<ArrowForwardIcon />}>
                                    All Orders
                                </Button>
                            </Box>
                            <ResponsiveContainer width="100%" height={220}>
                                <BarChart data={dailyOrderCounts}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#eef2f1" />
                                    <XAxis dataKey="day" tick={{ fontSize: 12 }} />
                                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                                    <Tooltip formatter={(value) => [`${value} orders`, 'Count']} />
                                    <Bar dataKey="count" fill="#2E7D5B" radius={[6, 6, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid size={{ xs: 12, md: 7 }}>
                    <Card>
                        <CardContent sx={{ pb: '16px !important' }}>
                            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                                <Box>
                                    <Typography variant="h6" fontWeight={700}>Recent Orders</Typography>
                                    <Typography variant="caption" color="text.secondary">
                                        {isRecentOrdersLoading ? 'Loading recent orders...' : 'Latest 5 rental orders'}
                                    </Typography>
                                </Box>
                                <Button size="small" variant="outlined" endIcon={<ArrowForwardIcon />} onClick={() => navigate('/orders')}>
                                    View All
                                </Button>
                            </Box>
                            <Paper variant="outlined" sx={{ borderRadius: 2, overflow: 'hidden', borderColor: 'rgba(13, 47, 39, 0.14)' }}>
                                <Table size="small">
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Order ID</TableCell>
                                            <TableCell>Customer</TableCell>
                                            <TableCell>Status</TableCell>
                                            <TableCell align="right">Total</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {recentOrders.map((o) => (
                                            <TableRow
                                                key={o.id} hover
                                                onClick={() => navigate(`/orders/${o.id}`)}
                                                sx={{
                                                    cursor: 'pointer',
                                                    '&:hover': {
                                                        backgroundColor: 'rgba(16, 75, 61, 0.05)',
                                                    },
                                                }}
                                            >
                                                <TableCell>
                                                    <Typography variant="body2" fontWeight={700} color="primary.main">{o.id}</Typography>
                                                </TableCell>
                                                <TableCell>
                                                    <Typography variant="body2">{o.customer}</Typography>
                                                    <Typography variant="caption" color="text.secondary">{o.gear}</Typography>
                                                </TableCell>
                                                <TableCell><StatusBadge status={o.status} /></TableCell>
                                                <TableCell align="right">
                                                    <Typography variant="body2" fontWeight={600}>LKR {o.total.toLocaleString()}</Typography>
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                        {!isRecentOrdersLoading && recentOrders.length === 0 && (
                                            <TableRow>
                                                <TableCell colSpan={4} align="center">
                                                    <Typography variant="body2" color="text.secondary">No recent orders found.</Typography>
                                                </TableCell>
                                            </TableRow>
                                        )}
                                    </TableBody>
                                </Table>
                            </Paper>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>
        </Box>
    );
};

export default DashboardPage;
