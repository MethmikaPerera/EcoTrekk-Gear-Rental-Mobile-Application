import { useMemo, useState } from 'react';
import {
    Alert,
    Box,
    Card,
    CardContent,
    Chip,
    CircularProgress,
    Divider,
    FormControl,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    Stack,
    Typography,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import {
    LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
    ResponsiveContainer, PieChart, Pie, Cell, Legend,
} from 'recharts';
import PageHeader from '../../components/PageHeader';
import { getReportsAnalytics, type ReportPeriod } from '../../api/metricsApi';

const PERIOD_OPTIONS: { value: ReportPeriod; label: string }[] = [
    { value: 'THIS_WEEK', label: 'This Week' },
    { value: 'THIS_MONTH', label: 'This Month' },
    { value: 'LAST_3_MONTHS', label: 'Last 3 Months' },
    { value: 'THIS_YEAR', label: 'This Year' },
];

const HUB_COLORS = ['#1F4D3A', '#0F766E', '#65A30D', '#1D4ED8', '#92400E', '#BE123C'];
const STATUS_COLORS: Record<string, string> = {
    PENDING: '#F59E0B',
    CONFIRMED: '#2563EB',
    PICKED_UP: '#0F766E',
    RETURNED: '#16A34A',
    CANCELLED: '#DC2626',
};

const formatCurrency = (value: number) => `LKR ${value.toLocaleString()}`;

const formatPeriodRange = (start: string, end: string) => {
    const startDate = new Date(start);
    const endDate = new Date(end);
    return `${startDate.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })} - ${endDate.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}`;
};

const formatTrend = (value: number) => `${value >= 0 ? '+' : ''}${value.toFixed(1)}%`;

const prettyStatus = (status: string) =>
    status.toLowerCase().split('_').map((part) => part[0].toUpperCase() + part.slice(1)).join(' ');

const ReportsPage = () => {
    const [period, setPeriod] = useState<ReportPeriod>('THIS_MONTH');

    const {
        data: analyticsResponse,
        isLoading,
        isError,
    } = useQuery({
        queryKey: ['reports-analytics', period],
        queryFn: () => getReportsAnalytics(period),
    });

    const analytics = analyticsResponse?.data;

    const hubPie = useMemo(
        () =>
            (analytics?.hubPerformance || []).map((hub, index) => ({
                name: hub.name,
                value: hub.orders,
                color: HUB_COLORS[index % HUB_COLORS.length],
            })),
        [analytics],
    );

    const statusPie = useMemo(
        () =>
            (analytics?.statusBreakdown || [])
                .filter((item) => item.count > 0)
                .map((item) => ({
                    name: prettyStatus(item.status),
                    value: item.count,
                    color: STATUS_COLORS[item.status] || '#64748B',
                })),
        [analytics],
    );

    const summaryCards = [
        {
            label: 'Total Orders',
            value: analytics?.totalOrders ?? 0,
            color: '#1F4D3A',
            helper: formatTrend(analytics?.orderTrendPercent ?? 0),
        },
        {
            label: 'Total Revenue',
            value: formatCurrency(analytics?.totalRevenue ?? 0),
            color: '#0F766E',
            helper: formatTrend(analytics?.revenueTrendPercent ?? 0),
        },
        {
            label: 'Avg Order Value',
            value: formatCurrency(analytics?.averageOrderValue ?? 0),
            color: '#334155',
        },
        {
            label: 'Active Customers',
            value: analytics?.activeCustomers ?? 0,
            color: '#92400E',
        },
    ];

    return (
        <Box>
            <PageHeader
                title="Reports & Analytics"
                subtitle="Operational insights and performance metrics"
                breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'Reports' }]}
                action={
                    <FormControl sx={{ minWidth: 140 }}>
                        <InputLabel>Period</InputLabel>
                        <Select
                            value={period}
                            label="Period"
                            onChange={(event) => setPeriod(event.target.value as ReportPeriod)}
                        >
                            {PERIOD_OPTIONS.map((option) => (
                                <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                }
            />

            {isError && (
                <Alert severity="error" sx={{ mb: 2 }}>
                    Analytics could not be loaded at the moment. Please try again.
                </Alert>
            )}

            {analytics && (
                <Card sx={{ mb: 2.5, background: 'linear-gradient(120deg, #ECF7F2 0%, #EFF6FF 100%)', border: '1px solid #DDECE6' }}>
                    <CardContent sx={{ py: 1.8, '&:last-child': { pb: 1.8 } }}>
                        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.2} alignItems={{ xs: 'flex-start', sm: 'center' }}>
                            <Chip label={PERIOD_OPTIONS.find((option) => option.value === period)?.label} color="primary" size="small" />
                            <Typography variant="body2" color="text.secondary">
                                {formatPeriodRange(analytics.rangeStart, analytics.rangeEnd)}
                            </Typography>
                        </Stack>
                    </CardContent>
                </Card>
            )}

            {isLoading ? (
                <Card>
                    <CardContent sx={{ py: 8, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                        <CircularProgress />
                    </CardContent>
                </Card>
            ) : (
                <>
                    <Grid container spacing={2} mb={3}>
                        {summaryCards.map((s) => (
                            <Grid key={s.label} size={{ xs: 12, sm: 6, lg: 3 }}>
                                <Card>
                                    <CardContent sx={{ p: 2.2, pb: '18px !important' }}>
                                        <Typography variant="caption" color="text.secondary" fontWeight={700} textTransform="uppercase" letterSpacing="0.06em">
                                            {s.label}
                                        </Typography>
                                        <Typography variant="h5" fontWeight={800} mt={0.7} sx={{ color: s.color }}>
                                            {typeof s.value === 'number' ? s.value.toLocaleString() : s.value}
                                        </Typography>
                                        {s.helper && (
                                            <Typography
                                                variant="caption"
                                                sx={{
                                                    mt: 0.8,
                                                    display: 'inline-flex',
                                                    fontWeight: 700,
                                                    color: s.helper.startsWith('+') ? 'success.main' : 'error.main',
                                                }}
                                            >
                                                {s.helper} vs previous period
                                            </Typography>
                                        )}
                                    </CardContent>
                                </Card>
                            </Grid>
                        ))}
                    </Grid>

                    <Grid container spacing={2.5} mb={2.5}>
                        <Grid size={{ xs: 12, lg: 7 }}>
                            <Card>
                                <CardContent>
                                    <Typography variant="h6" fontWeight={700} mb={0.5}>Orders Trend</Typography>
                                    <Typography variant="caption" color="text.secondary">Order volume across the selected period</Typography>
                                    <ResponsiveContainer width="100%" height={250}>
                                        <BarChart data={analytics?.orderSeries || []} barSize={22} style={{ marginTop: 16 }}>
                                            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                                            <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                                            <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                                            <Tooltip contentStyle={{ borderRadius: 8 }} formatter={(value) => [`${value} orders`, 'Orders']} />
                                            <Bar dataKey="count" fill="#2E7D5B" radius={[6, 6, 0, 0]} />
                                        </BarChart>
                                    </ResponsiveContainer>
                                </CardContent>
                            </Card>
                        </Grid>

                        <Grid size={{ xs: 12, lg: 5 }}>
                            <Card>
                                <CardContent>
                                    <Typography variant="h6" fontWeight={700} mb={0.5}>Orders by Hub</Typography>
                                    <Typography variant="caption" color="text.secondary">Hub contribution to total orders</Typography>
                                    <ResponsiveContainer width="100%" height={250}>
                                        <PieChart>
                                            <Pie
                                                data={hubPie}
                                                cx="50%"
                                                cy="50%"
                                                outerRadius={82}
                                                paddingAngle={3}
                                                dataKey="value"
                                                label={({ name, percent }: { name?: string; percent?: number }) => `${name ?? ''} ${((percent ?? 0) * 100).toFixed(0)}%`}
                                                labelLine={false}
                                            >
                                                {hubPie.map((entry, idx) => <Cell key={idx} fill={entry.color} />)}
                                            </Pie>
                                            <Tooltip formatter={(value) => [`${value} orders`, 'Orders']} contentStyle={{ borderRadius: 8 }} />
                                            <Legend verticalAlign="bottom" />
                                        </PieChart>
                                    </ResponsiveContainer>
                                </CardContent>
                            </Card>
                        </Grid>
                    </Grid>

                    <Grid container spacing={2.5}>
                        <Grid size={{ xs: 12, lg: 7 }}>
                            <Card>
                                <CardContent>
                                    <Typography variant="h6" fontWeight={700} mb={0.5}>Revenue Trend</Typography>
                                    <Typography variant="caption" color="text.secondary">Revenue progression for the selected period</Typography>
                                    <ResponsiveContainer width="100%" height={230}>
                                        <LineChart data={analytics?.revenueSeries || []} style={{ marginTop: 16 }}>
                                            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                                            <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                                            <YAxis tick={{ fontSize: 12 }} tickFormatter={(value) => `${(Number(value) / 1000).toFixed(0)}k`} />
                                            <Tooltip formatter={(value) => [formatCurrency(Number(value)), 'Revenue']} contentStyle={{ borderRadius: 8 }} />
                                            <Line type="monotone" dataKey="amount" stroke="#D4830A" strokeWidth={3} dot={{ fill: '#D4830A', r: 4 }} />
                                        </LineChart>
                                    </ResponsiveContainer>
                                </CardContent>
                            </Card>
                        </Grid>

                        <Grid size={{ xs: 12, lg: 5 }}>
                            <Card sx={{ mb: 2.5 }}>
                                <CardContent>
                                    <Typography variant="h6" fontWeight={700} mb={0.5}>Top Rented Gear</Typography>
                                    <Typography variant="caption" color="text.secondary">Most frequently rented items</Typography>
                                    <ResponsiveContainer width="100%" height={230}>
                                        <BarChart data={analytics?.topRentedGears || []} layout="vertical" barSize={16} style={{ marginTop: 16 }}>
                                            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                                            <XAxis type="number" tick={{ fontSize: 11 }} />
                                            <YAxis dataKey="name" type="category" tick={{ fontSize: 11 }} width={120} />
                                            <Tooltip contentStyle={{ borderRadius: 8 }} formatter={(value) => [`${value} rentals`, 'Count']} />
                                            <Bar dataKey="count" fill="#1F4D3A" radius={[0, 6, 6, 0]} />
                                        </BarChart>
                                    </ResponsiveContainer>
                                </CardContent>
                            </Card>

                            <Card>
                                <CardContent>
                                    <Typography variant="h6" fontWeight={700} mb={0.5}>Order Status Mix</Typography>
                                    <Typography variant="caption" color="text.secondary">Distribution by current order status</Typography>
                                    <Divider sx={{ my: 1.5 }} />
                                    {statusPie.length === 0 ? (
                                        <Typography variant="body2" color="text.secondary">No status distribution data available for this period.</Typography>
                                    ) : (
                                        <Stack spacing={1}>
                                            {statusPie.map((item) => (
                                                <Stack key={item.name} direction="row" justifyContent="space-between" alignItems="center">
                                                    <Stack direction="row" alignItems="center" spacing={1}>
                                                        <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: item.color }} />
                                                        <Typography variant="body2">{item.name}</Typography>
                                                    </Stack>
                                                    <Typography variant="body2" fontWeight={700}>{item.value}</Typography>
                                                </Stack>
                                            ))}
                                        </Stack>
                                    )}
                                </CardContent>
                            </Card>
                        </Grid>
                    </Grid>
                </>
            )}
        </Box>
    );
};

export default ReportsPage;
