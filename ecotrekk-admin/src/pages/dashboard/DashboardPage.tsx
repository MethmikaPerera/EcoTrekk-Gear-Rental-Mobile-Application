import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Grid, Card, CardContent, Typography, Box, Button, Avatar,
    Table, TableBody, TableCell, TableHead, TableRow, Paper, Alert, Chip,
} from '@mui/material';
import {
    LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid,
    Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend,
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
import StatusBadge from '../../components/StatusBadge';
import { useAuth } from '../../context/AuthContext';

// ─── Mock Data ────────────────────────────────────────────────────────────────
const mockMetrics = {
    totalOrdersToday: 24,
    pendingOrders: 8,
    lowStockItems: 5,
    revenueToday: 3840,
};

const weeklyOrders = [
    { date: 'Mon', orders: 12 }, { date: 'Tue', orders: 19 },
    { date: 'Wed', orders: 15 }, { date: 'Thu', orders: 27 },
    { date: 'Fri', orders: 32 }, { date: 'Sat', orders: 41 },
    { date: 'Sun', orders: 24 },
];

const revenueData = [
    { month: 'Sep', revenue: 32000 }, { month: 'Oct', revenue: 38500 },
    { month: 'Nov', revenue: 42000 }, { month: 'Dec', revenue: 55000 },
    { month: 'Jan', revenue: 46000 }, { month: 'Feb', revenue: 48200 },
];

const hubPerformance = [
    { name: 'Colombo Hub', value: 42, color: '#1F4D3A' },
    { name: 'Kandy Hub', value: 28, color: '#0F766E' },
    { name: 'Galle Hub', value: 18, color: '#65A30D' },
    { name: 'Nuwara Hub', value: 12, color: '#92400E' },
];

const recentOrders = [
    { id: 'ORD-1042', customer: 'Saman Silva', gear: 'Tent Pro X3', status: 'CONFIRMED', total: 2400 },
    { id: 'ORD-1041', customer: 'Priya Perera', gear: 'Sleeping Bag', status: 'PENDING', total: 1200 },
    { id: 'ORD-1040', customer: 'Nimal Fernando', gear: 'Backpack 60L', status: 'PICKED_UP', total: 900 },
    { id: 'ORD-1039', customer: 'Dilini Jayasena', gear: 'Trekking Poles', status: 'RETURNED', total: 600 },
    { id: 'ORD-1038', customer: 'Kamal Bandara', gear: 'Camping Stove', status: 'CANCELLED', total: 450 },
];

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
    trend?: number;
    prefix?: string;
    onClick?: () => void;
}

const StatCard = ({ title, value, icon, color, trend, prefix, onClick }: StatCardProps) => (
    <Card
        sx={{
            height: '100%', position: 'relative', overflow: 'hidden',
            cursor: onClick ? 'pointer' : 'default',
            transition: 'transform 0.15s, box-shadow 0.15s',
            '&:hover': onClick ? { transform: 'translateY(-2px)', boxShadow: '0 6px 20px rgba(0,0,0,0.12)' } : {},
        }}
        onClick={onClick}
    >
        <Box
            sx={{
                position: 'absolute', right: -16, top: -16, width: 90, height: 90,
                borderRadius: '50%', background: `${color}14`,
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
                {trend !== undefined && (
                    <Box display="flex" alignItems="center" gap={0.5} mt={0.5}>
                        {trend >= 0
                            ? <TrendingUpIcon sx={{ fontSize: 14, color: 'success.main' }} />
                            : <TrendingDownIcon sx={{ fontSize: 14, color: 'error.main' }} />}
                        <Typography variant="caption" color={trend >= 0 ? 'success.main' : 'error.main'} fontWeight={700}>
                            {Math.abs(trend)}% vs yesterday
                        </Typography>
                    </Box>
                )}
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

    return (
        <Box>
            {/* ── Welcome Banner ── */}
            <Card
                sx={{
                    mb: 3,
                    background: 'linear-gradient(135deg, #1A2E2A 0%, #1F4D3A 60%, #2E7D5B 100%)',
                    color: '#fff',
                    overflow: 'hidden',
                    position: 'relative',
                }}
            >
                {/* Background pattern */}
                <Box
                    sx={{
                        position: 'absolute', inset: 0, opacity: 0.06,
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
                            <Typography variant="h5" fontWeight={800} color="#fff" mb={0.5}>
                                {user?.name || 'Admin'} 👋
                            </Typography>
                            <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.7)' }}>
                                {formatDate()} · Here's what's happening with EcoTrekk today.
                            </Typography>
                        </Box>
                        <Box display="flex" gap={1.5} flexWrap="wrap">
                            <Button
                                variant="contained"
                                startIcon={<AddIcon />}
                                onClick={() => navigate('/inventory/gears/new')}
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

            {/* ── Pending alert ── */}
            {mockMetrics.pendingOrders > 0 && (
                <Alert
                    severity="warning"
                    sx={{ mb: 2.5, borderRadius: 2 }}
                    action={
                        <Button color="warning" size="small" onClick={() => navigate('/orders?status=PENDING')}>
                            Review
                        </Button>
                    }
                >
                    <strong>{mockMetrics.pendingOrders} orders</strong> are waiting for your confirmation.
                </Alert>
            )}

            {/* ── Stat Cards ── */}
            <Grid container spacing={2.5} mb={3}>
                <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
                    <StatCard
                        title="Orders Today" value={mockMetrics.totalOrdersToday}
                        icon={<ShoppingCartIcon />} color="#1F4D3A" trend={12}
                        onClick={() => navigate('/orders')}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
                    <StatCard
                        title="Pending Orders" value={mockMetrics.pendingOrders}
                        icon={<PendingActionsIcon />} color="#D4830A" trend={-5}
                        onClick={() => navigate('/orders')}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
                    <StatCard
                        title="Low Stock Items" value={mockMetrics.lowStockItems}
                        icon={<InventoryIcon />} color="#D32F2F"
                        onClick={() => navigate('/inventory/gears')}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
                    <StatCard
                        title="Revenue Today" value={mockMetrics.revenueToday}
                        icon={<AttachMoneyIcon />} color="#0277BD" prefix="LKR " trend={8}
                    />
                </Grid>
            </Grid>

            {/* ── Charts Row ── */}
            <Grid container spacing={2.5} mb={3}>
                <Grid size={{ xs: 12, lg: 8 }}>
                    <Card sx={{ height: '100%' }}>
                        <CardContent>
                            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                                <Box>
                                    <Typography variant="h6" fontWeight={700}>Orders This Week</Typography>
                                    <Typography variant="caption" color="text.secondary">Daily rental orders placed</Typography>
                                </Box>
                                <Button size="small" endIcon={<ArrowForwardIcon />} onClick={() => navigate('/orders')}>
                                    All Orders
                                </Button>
                            </Box>
                            <ResponsiveContainer width="100%" height={220}>
                                <LineChart data={weeklyOrders}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                                    <XAxis dataKey="date" tick={{ fontSize: 12 }} />
                                    <YAxis tick={{ fontSize: 12 }} />
                                    <Tooltip contentStyle={{ borderRadius: 8, border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} />
                                    <Line type="monotone" dataKey="orders" stroke="#1F4D3A" strokeWidth={3} dot={{ fill: '#1F4D3A', r: 5 }} activeDot={{ r: 7 }} />
                                </LineChart>
                            </ResponsiveContainer>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid size={{ xs: 12, lg: 4 }}>
                    <Card sx={{ height: '100%' }}>
                        <CardContent>
                            <Typography variant="h6" fontWeight={700} mb={0.5}>Hub Performance</Typography>
                            <Typography variant="caption" color="text.secondary">Orders by rental hub</Typography>
                            <ResponsiveContainer width="100%" height={220}>
                                <PieChart>
                                    <Pie data={hubPerformance} cx="50%" cy="50%" innerRadius={55} outerRadius={80} paddingAngle={4} dataKey="value">
                                        {hubPerformance.map((entry, index) => (
                                            <Cell key={index} fill={entry.color} />
                                        ))}
                                    </Pie>
                                    <Tooltip formatter={(v) => [`${v} orders`]} contentStyle={{ borderRadius: 8 }} />
                                    <Legend iconType="circle" iconSize={10} formatter={(v) => <span style={{ fontSize: 12 }}>{v}</span>} />
                                </PieChart>
                            </ResponsiveContainer>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            {/* ── Bottom Row ── */}
            <Grid container spacing={2.5}>
                <Grid size={{ xs: 12, lg: 5 }}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" fontWeight={700} mb={0.5}>Revenue Trend</Typography>
                            <Typography variant="caption" color="text.secondary">Last 6 months (LKR)</Typography>
                            <ResponsiveContainer width="100%" height={200}>
                                <BarChart data={revenueData} barSize={28}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                                    <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                                    <YAxis tick={{ fontSize: 12 }} tickFormatter={(v) => `${(v / 1000).toFixed(0)}k`} />
                                    <Tooltip formatter={(v) => [`LKR ${(v as number).toLocaleString()}`]} contentStyle={{ borderRadius: 8 }} />
                                    <Bar dataKey="revenue" fill="#2E7D5B" radius={[6, 6, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid size={{ xs: 12, lg: 7 }}>
                    <Card>
                        <CardContent sx={{ pb: '16px !important' }}>
                            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                                <Box>
                                    <Typography variant="h6" fontWeight={700}>Recent Orders</Typography>
                                    <Typography variant="caption" color="text.secondary">Latest 5 rental orders</Typography>
                                </Box>
                                <Button size="small" variant="outlined" endIcon={<ArrowForwardIcon />} onClick={() => navigate('/orders')}>
                                    View All
                                </Button>
                            </Box>
                            <Paper variant="outlined" sx={{ borderRadius: 2, overflow: 'hidden' }}>
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
                                                sx={{ cursor: 'pointer' }}
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
