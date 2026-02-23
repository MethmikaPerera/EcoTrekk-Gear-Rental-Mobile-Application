import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Box, Card, Table, TableBody, TableCell, TableHead, TableRow, TextField,
    Select, MenuItem, FormControl, InputLabel, Typography, Chip, IconButton,
    TablePagination, InputAdornment, Button, Tabs, Tab, Tooltip,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import VisibilityIcon from '@mui/icons-material/Visibility';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import EmptyState from '../../components/EmptyState';

const STATUS_TABS = ['ALL', 'PENDING', 'CONFIRMED', 'PICKED_UP', 'RETURNED', 'CANCELLED'];

const mockOrders = Array.from({ length: 40 }, (_, i) => ({
    id: `ORD-${1000 + i + 1}`,
    customer: { name: ['Saman Silva', 'Priya Perera', 'Nimal Fernando', 'Dilini Jayasena', 'Kamal Bandara', 'Amara Kumari'][i % 6], email: 'customer@email.com' },
    hubName: ['Colombo Hub', 'Kandy Hub', 'Galle Hub', 'Nuwara Hub'][i % 4],
    startDate: new Date(Date.now() - (i * 2) * 86400000).toISOString().split('T')[0],
    endDate: new Date(Date.now() + (3 - i % 5) * 86400000).toISOString().split('T')[0],
    status: STATUS_TABS.slice(1)[i % 5] as string,
    totalAmount: [2400, 1800, 4500, 900, 3600][i % 5],
    createdAt: new Date(Date.now() - i * 86400000).toISOString(),
}));

const OrderListPage = () => {
    const navigate = useNavigate();
    const [tab, setTab] = useState(0);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(0);
    const [rowsPerPage] = useState(10);

    const activeStatus = STATUS_TABS[tab];
    const filtered = mockOrders.filter((o) => {
        const matchStatus = activeStatus === 'ALL' || o.status === activeStatus;
        const matchSearch = !search ||
            o.id.toLowerCase().includes(search.toLowerCase()) ||
            o.customer.name.toLowerCase().includes(search.toLowerCase());
        return matchStatus && matchSearch;
    });

    const paginated = filtered.slice(page * rowsPerPage, (page + 1) * rowsPerPage);

    const tabCounts = STATUS_TABS.map((s) =>
        s === 'ALL' ? mockOrders.length : mockOrders.filter((o) => o.status === s).length
    );

    return (
        <Box>
            <PageHeader
                title="Order Management"
                subtitle={`${mockOrders.length} total orders`}
                breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'Orders' }]}
            />

            {/* Status Tabs */}
            <Card sx={{ mb: 2 }}>
                <Tabs
                    value={tab}
                    onChange={(_, v) => { setTab(v); setPage(0); }}
                    variant="scrollable"
                    scrollButtons="auto"
                    sx={{ px: 1, borderBottom: '1px solid rgba(0,0,0,0.08)' }}
                >
                    {STATUS_TABS.map((s, i) => (
                        <Tab
                            key={s}
                            label={
                                <Box display="flex" alignItems="center" gap={0.8}>
                                    {s === 'ALL' ? 'All Orders' : s.replace('_', ' ')}
                                    <Chip label={tabCounts[i]} size="small" sx={{ height: 18, fontSize: '0.68rem', minWidth: 24 }} />
                                </Box>
                            }
                        />
                    ))}
                </Tabs>

                <Box sx={{ p: 2, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                    <TextField
                        placeholder="Search order ID or customer..."
                        value={search}
                        onChange={(e) => { setSearch(e.target.value); setPage(0); }}
                        sx={{ minWidth: 280 }}
                        InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon color="action" /></InputAdornment> }}
                    />
                    {search && (
                        <Button variant="text" color="inherit" size="small" onClick={() => setSearch('')}>
                            Clear
                        </Button>
                    )}
                    <Typography variant="body2" color="text.secondary" ml="auto">
                        {filtered.length} results
                    </Typography>
                </Box>
            </Card>

            <Card>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Order ID</TableCell>
                            <TableCell>Customer</TableCell>
                            <TableCell>Hub</TableCell>
                            <TableCell>Start Date</TableCell>
                            <TableCell>End Date</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell align="right">Amount</TableCell>
                            <TableCell align="center">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {paginated.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={8}>
                                    <EmptyState
                                        icon={<ReceiptLongIcon sx={{ fontSize: 36, color: 'grey.400' }} />}
                                        title={search ? `No orders matching "${search}"` : 'No orders in this status'}
                                        description={search ? 'Try a different order ID or customer name.' : 'Orders will appear here once customers place rentals.'}
                                    />
                                </TableCell>
                            </TableRow>
                        ) : paginated.map((order) => (
                            <TableRow key={order.id} hover sx={{ cursor: 'pointer' }} onClick={() => navigate(`/orders/${order.id}`)}>
                                <TableCell>
                                    <Typography fontWeight={700} color="primary.main" variant="body2">{order.id}</Typography>
                                </TableCell>
                                <TableCell>
                                    <Typography variant="body2" fontWeight={600}>{order.customer.name}</Typography>
                                    <Typography variant="caption" color="text.secondary">{order.customer.email}</Typography>
                                </TableCell>
                                <TableCell>
                                    <Typography variant="body2">{order.hubName}</Typography>
                                </TableCell>
                                <TableCell><Typography variant="body2">{order.startDate}</Typography></TableCell>
                                <TableCell><Typography variant="body2">{order.endDate}</Typography></TableCell>
                                <TableCell><StatusBadge status={order.status} /></TableCell>
                                <TableCell align="right">
                                    <Typography fontWeight={700} variant="body2">LKR {order.totalAmount.toLocaleString()}</Typography>
                                </TableCell>
                                <TableCell align="center" onClick={(e) => e.stopPropagation()}>
                                    <Tooltip title="View Details">
                                        <IconButton size="small" color="primary" onClick={() => navigate(`/orders/${order.id}`)}>
                                            <VisibilityIcon fontSize="small" />
                                        </IconButton>
                                    </Tooltip>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
                <TablePagination
                    component="div"
                    count={filtered.length}
                    page={page}
                    onPageChange={(_, p) => setPage(p)}
                    rowsPerPage={rowsPerPage}
                    onRowsPerPageChange={() => { }}
                    rowsPerPageOptions={[10]}
                />
            </Card>
        </Box>
    );
};

export default OrderListPage;
