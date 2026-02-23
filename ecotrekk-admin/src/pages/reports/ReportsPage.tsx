import {
    Box, Card, CardContent, Grid, Typography, FormControl, InputLabel, Select, MenuItem,
} from '@mui/material';
import {
    LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
    ResponsiveContainer, PieChart, Pie, Cell, Legend,
} from 'recharts';
import PageHeader from '../../components/PageHeader';

const weeklyOrders = [
    { week: 'Week 1', orders: 38 }, { week: 'Week 2', orders: 52 },
    { week: 'Week 3', orders: 47 }, { week: 'Week 4', orders: 63 },
    { week: 'Week 5', orders: 71 }, { week: 'Week 6', orders: 58 },
    { week: 'Week 7', orders: 84 }, { week: 'Week 8', orders: 77 },
];

const revenueTrend = [
    { month: 'Jul', revenue: 28000 }, { month: 'Aug', revenue: 35000 },
    { month: 'Sep', revenue: 32000 }, { month: 'Oct', revenue: 41000 },
    { month: 'Nov', revenue: 38000 }, { month: 'Dec', revenue: 55000 },
    { month: 'Jan', revenue: 47000 }, { month: 'Feb', revenue: 48200 },
];

const topGear = [
    { name: 'Tent Pro X3', count: 87 }, { name: 'Backpack 60L', count: 74 },
    { name: 'Sleeping Bag', count: 63 }, { name: 'Trekking Poles', count: 51 },
    { name: 'Camp Stove', count: 44 }, { name: 'Headlamp', count: 38 },
];

const hubPie = [
    { name: 'Colombo', value: 42, color: '#1F4D3A' },
    { name: 'Kandy', value: 28, color: '#0F766E' },
    { name: 'Galle', value: 18, color: '#65A30D' },
    { name: 'Nuwara Eliya', value: 12, color: '#92400E' },
];

const ReportsPage = () => {
    return (
        <Box>
            <PageHeader
                title="Reports & Analytics"
                subtitle="Operational insights and performance metrics"
                breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'Reports' }]}
                action={
                    <FormControl sx={{ minWidth: 140 }}>
                        <InputLabel>Period</InputLabel>
                        <Select defaultValue="thisMonth" label="Period">
                            <MenuItem value="thisWeek">This Week</MenuItem>
                            <MenuItem value="thisMonth">This Month</MenuItem>
                            <MenuItem value="last3months">Last 3 Months</MenuItem>
                            <MenuItem value="thisYear">This Year</MenuItem>
                        </Select>
                    </FormControl>
                }
            />

            {/* Summary Chips */}
            <Grid container spacing={2} mb={3}>
                {[
                    { label: 'Total Orders', value: '490', color: '#1F4D3A' },
                    { label: 'Total Revenue', value: 'LKR 324,200', color: '#0F766E' },
                    { label: 'Avg Order Value', value: 'LKR 2,450', color: '#334155' },
                    { label: 'Active Customers', value: '312', color: '#92400E' },
                ].map((s) => (
                    <Grid key={s.label} size={{ xs: 6, md: 3 }}>
                        <Card>
                            <CardContent sx={{ p: 2, pb: '16px !important' }}>
                                <Typography variant="caption" color="text.secondary" fontWeight={600} textTransform="uppercase">
                                    {s.label}
                                </Typography>
                                <Typography variant="h5" fontWeight={800} mt={0.5} sx={{ color: s.color }}>
                                    {s.value}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>

            <Grid container spacing={2.5} mb={2.5}>
                {/* Weekly Orders Bar */}
                <Grid size={{ xs: 12, lg: 7 }}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" fontWeight={700} mb={0.5}>Orders Per Week</Typography>
                            <Typography variant="caption" color="text.secondary">Total rental orders placed per week</Typography>
                            <ResponsiveContainer width="100%" height={240}>
                                <BarChart data={weeklyOrders} barSize={24} style={{ marginTop: 16 }}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                                    <XAxis dataKey="week" tick={{ fontSize: 12 }} />
                                    <YAxis tick={{ fontSize: 12 }} />
                                    <Tooltip contentStyle={{ borderRadius: 8 }} />
                                    <Bar dataKey="orders" fill="#2E7D5B" radius={[6, 6, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </CardContent>
                    </Card>
                </Grid>

                {/* Hub Pie */}
                <Grid size={{ xs: 12, lg: 5 }}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" fontWeight={700} mb={0.5}>Orders by Hub</Typography>
                            <Typography variant="caption" color="text.secondary">Percentage distribution by rental hub</Typography>
                            <ResponsiveContainer width="100%" height={240}>
                                <PieChart>
                                    <Pie data={hubPie} cx="50%" cy="50%" outerRadius={85} paddingAngle={3} dataKey="value" label={({ name, percent }: { name?: string; percent?: number }) => `${name ?? ''} ${((percent ?? 0) * 100).toFixed(0)}%`} labelLine={false}>
                                        {hubPie.map((entry, idx) => <Cell key={idx} fill={entry.color} />)}
                                    </Pie>
                                    <Tooltip formatter={(v) => [`${v} orders`]} contentStyle={{ borderRadius: 8 }} />
                                </PieChart>
                            </ResponsiveContainer>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            <Grid container spacing={2.5}>
                {/* Revenue Trend Line */}
                <Grid size={{ xs: 12, lg: 7 }}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" fontWeight={700} mb={0.5}>Revenue Trend</Typography>
                            <Typography variant="caption" color="text.secondary">Monthly revenue in LKR</Typography>
                            <ResponsiveContainer width="100%" height={230}>
                                <LineChart data={revenueTrend} style={{ marginTop: 16 }}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                                    <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                                    <YAxis tick={{ fontSize: 12 }} tickFormatter={(v) => `${(v / 1000).toFixed(0)}k`} />
                                    <Tooltip formatter={(v) => [`LKR ${(v as number).toLocaleString()}`]} contentStyle={{ borderRadius: 8 }} />
                                    <Line type="monotone" dataKey="revenue" stroke="#D4830A" strokeWidth={3} dot={{ fill: '#D4830A', r: 5 }} />
                                </LineChart>
                            </ResponsiveContainer>
                        </CardContent>
                    </Card>
                </Grid>

                {/* Top Gear Bar */}
                <Grid size={{ xs: 12, lg: 5 }}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" fontWeight={700} mb={0.5}>Top Rented Gear</Typography>
                            <Typography variant="caption" color="text.secondary">Items with highest rental frequency</Typography>
                            <ResponsiveContainer width="100%" height={230}>
                                <BarChart data={topGear} layout="vertical" barSize={16} style={{ marginTop: 16 }}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                                    <XAxis type="number" tick={{ fontSize: 11 }} />
                                    <YAxis dataKey="name" type="category" tick={{ fontSize: 11 }} width={90} />
                                    <Tooltip contentStyle={{ borderRadius: 8 }} />
                                    <Bar dataKey="count" fill="#1F4D3A" radius={[0, 6, 6, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>
        </Box>
    );
};

export default ReportsPage;
