import { useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
    Box,
    Card,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    TextField,
    Typography,
    Chip,
    IconButton,
    TablePagination,
    InputAdornment,
    Button,
    Tabs,
    Tab,
    Tooltip,
    CircularProgress,
    Stack,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import VisibilityIcon from '@mui/icons-material/Visibility';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import PendingActionsIcon from '@mui/icons-material/PendingActions';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import LocalShippingOutlinedIcon from '@mui/icons-material/LocalShippingOutlined';
import AssignmentTurnedInOutlinedIcon from '@mui/icons-material/AssignmentTurnedInOutlined';
import CancelOutlinedIcon from '@mui/icons-material/CancelOutlined';
import { useQuery } from '@tanstack/react-query';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import EmptyState from '../../components/EmptyState';
import { getOrders, OrderStatus } from '../../api/orderApi';

const STATUS_TABS: Array<'ALL' | OrderStatus> = ['ALL', 'PENDING', 'CONFIRMED', 'PICKED_UP', 'RETURNED', 'CANCELLED'];

const statusIconMap: Record<string, React.ReactNode> = {
    PENDING: <PendingActionsIcon fontSize="small" />,
    CONFIRMED: <CheckCircleOutlineIcon fontSize="small" />,
    PICKED_UP: <LocalShippingOutlinedIcon fontSize="small" />,
    RETURNED: <AssignmentTurnedInOutlinedIcon fontSize="small" />,
    CANCELLED: <CancelOutlinedIcon fontSize="small" />,
};

const getDayCount = (start: string, end: string) => {
    const startDate = new Date(start).getTime();
    const endDate = new Date(end).getTime();
    return Math.max(1, Math.ceil((endDate - startDate) / 86400000));
};

const OrderListPage = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const initialStatus = (searchParams.get('status') || '').toUpperCase();
    const initialTab = Math.max(0, STATUS_TABS.findIndex((s) => s === initialStatus));

    const [tab, setTab] = useState(initialTab);
    const [search, setSearch] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [page, setPage] = useState(0);
    const [rowsPerPage] = useState(10);

    const activeStatus = STATUS_TABS[tab];

    const { data, isLoading } = useQuery({
        queryKey: ['orders', page, rowsPerPage, activeStatus, search, startDate, endDate],
        queryFn: () =>
            getOrders({
                page,
                size: rowsPerPage,
                status: activeStatus === 'ALL' ? undefined : activeStatus,
                search: search || undefined,
                startDate: startDate || undefined,
                endDate: endDate || undefined,
            }),
    });

    const { data: countsData } = useQuery({
        queryKey: ['orders-counts', search, startDate, endDate],
        queryFn: () =>
            getOrders({
                page: 0,
                size: 9999,
                search: search || undefined,
                startDate: startDate || undefined,
                endDate: endDate || undefined,
            }),
        staleTime: 60_000,
    });

    const orders = data?.data.content ?? [];
    const totalElements = data?.data.totalElements ?? 0;
    const allMatchingOrders = useMemo(() => countsData?.data.content ?? [], [countsData?.data.content]);

    const tabCounts = useMemo(
        () =>
            STATUS_TABS.map((status) =>
                status === 'ALL' ? allMatchingOrders.length : allMatchingOrders.filter((order) => order.status === status).length
            ),
        [allMatchingOrders]
    );

    const kpis = useMemo(
        () => ({
            pending: allMatchingOrders.filter((order) => order.status === 'PENDING').length,
            active: allMatchingOrders.filter((order) => order.status === 'CONFIRMED' || order.status === 'PICKED_UP').length,
            closed: allMatchingOrders.filter((order) => order.status === 'RETURNED' || order.status === 'CANCELLED').length,
        }),
        [allMatchingOrders]
    );

    const hasFilters = Boolean(search || startDate || endDate);

    return (
        <Box>
            <PageHeader
                title="Order Management"
                subtitle={`${totalElements} orders in current view`}
                breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'Orders' }]}
            />

            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ mb: 2 }}>
                <Card sx={{ flex: 1, p: 2.2, borderRadius: 2.5, background: 'linear-gradient(135deg, #4f2d1f 0%, #8f4d2f 100%)', color: '#fff' }}>
                    <Typography variant="caption" sx={{ opacity: 0.82, letterSpacing: 0.9 }}>PENDING APPROVAL</Typography>
                    <Typography variant="h4" fontWeight={800}>{kpis.pending}</Typography>
                </Card>
                <Card sx={{ flex: 1, p: 2.2, borderRadius: 2.5, background: 'linear-gradient(135deg, #075985 0%, #0ea5e9 100%)', color: '#fff' }}>
                    <Typography variant="caption" sx={{ opacity: 0.82, letterSpacing: 0.9 }}>ACTIVE RENTALS</Typography>
                    <Typography variant="h4" fontWeight={800}>{kpis.active}</Typography>
                </Card>
                <Card sx={{ flex: 1, p: 2.2, borderRadius: 2.5, background: 'linear-gradient(135deg, #fef3c7 0%, #fde68a 100%)', color: '#78350f' }}>
                    <Typography variant="caption" sx={{ letterSpacing: 0.9 }}>CLOSED ORDERS</Typography>
                    <Typography variant="h4" fontWeight={800}>{kpis.closed}</Typography>
                </Card>
            </Stack>

            <Card sx={{ mb: 2 }}>
                <Tabs
                    value={tab}
                    onChange={(_, value) => {
                        setTab(value);
                        setPage(0);
                    }}
                    variant="scrollable"
                    scrollButtons="auto"
                    sx={{ px: 1, borderBottom: '1px solid rgba(0,0,0,0.08)' }}
                >
                    {STATUS_TABS.map((status, index) => (
                        <Tab
                            key={status}
                            label={
                                <Box display="flex" alignItems="center" gap={0.8}>
                                    {status === 'ALL' ? 'All Orders' : status.replace('_', ' ')}
                                    <Chip label={tabCounts[index]} size="small" sx={{ height: 18, fontSize: '0.68rem', minWidth: 24 }} />
                                </Box>
                            }
                        />
                    ))}
                </Tabs>

                <Box sx={{ p: 2, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                    <TextField
                        placeholder="Search order ID, customer, email..."
                        value={search}
                        onChange={(event) => {
                            setSearch(event.target.value);
                            setPage(0);
                        }}
                        sx={{ minWidth: 280 }}
                        InputProps={{
                            startAdornment: (
                                <InputAdornment position="start">
                                    <SearchIcon color="action" />
                                </InputAdornment>
                            ),
                        }}
                    />

                    <TextField
                        type="date"
                        size="small"
                        label="Start Date"
                        InputLabelProps={{ shrink: true }}
                        value={startDate}
                        onChange={(event) => {
                            setStartDate(event.target.value);
                            setPage(0);
                        }}
                    />

                    <TextField
                        type="date"
                        size="small"
                        label="End Date"
                        InputLabelProps={{ shrink: true }}
                        value={endDate}
                        onChange={(event) => {
                            setEndDate(event.target.value);
                            setPage(0);
                        }}
                    />

                    {hasFilters && (
                        <Button
                            variant="text"
                            color="inherit"
                            size="small"
                            onClick={() => {
                                setSearch('');
                                setStartDate('');
                                setEndDate('');
                                setPage(0);
                            }}
                        >
                            Clear
                        </Button>
                    )}

                    <Typography variant="body2" color="text.secondary" ml="auto" fontWeight={500}>
                        {totalElements} results
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
                            <TableCell>Schedule</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell align="right">Amount</TableCell>
                            <TableCell align="center">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={7} align="center" sx={{ py: 8 }}>
                                    <CircularProgress />
                                </TableCell>
                            </TableRow>
                        ) : orders.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={7}>
                                    <EmptyState
                                        icon={<ReceiptLongIcon sx={{ fontSize: 36, color: 'grey.400' }} />}
                                        title={hasFilters ? 'No orders match your filters' : 'No orders found'}
                                        description={hasFilters ? 'Try broadening your date range or clearing search terms.' : 'Orders will appear here once customers place rentals.'}
                                    />
                                </TableCell>
                            </TableRow>
                        ) : (
                            orders.map((order) => (
                                <TableRow key={order.id} hover sx={{ cursor: 'pointer' }} onClick={() => navigate(`/orders/${order.id}`)}>
                                    <TableCell>
                                        <Typography fontWeight={700} color="primary.main" variant="body2">{order.id}</Typography>
                                        <Typography variant="caption" color="text.secondary">{new Date(order.createdAt).toLocaleDateString('en-GB')}</Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2" fontWeight={600}>{order.customer.name}</Typography>
                                        <Typography variant="caption" color="text.secondary">{order.customer.email}</Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2" fontWeight={500}>{order.hubName}</Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2">{order.startDate} to {order.endDate}</Typography>
                                        <Typography variant="caption" color="text.secondary">{getDayCount(order.startDate, order.endDate)} day rental</Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Box display="flex" alignItems="center" gap={1}>
                                            {statusIconMap[order.status]}
                                            <StatusBadge status={order.status} />
                                        </Box>
                                    </TableCell>
                                    <TableCell align="right">
                                        <Typography fontWeight={700} variant="body2">LKR {Number(order.totalAmount).toLocaleString()}</Typography>
                                    </TableCell>
                                    <TableCell align="center" onClick={(event) => event.stopPropagation()}>
                                        <Tooltip title="View Details">
                                            <IconButton size="small" color="primary" onClick={() => navigate(`/orders/${order.id}`)}>
                                                <VisibilityIcon fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>

                <TablePagination
                    component="div"
                    count={totalElements}
                    page={page}
                    onPageChange={(_, nextPage) => setPage(nextPage)}
                    rowsPerPage={rowsPerPage}
                    onRowsPerPageChange={() => {}}
                    rowsPerPageOptions={[10]}
                    labelRowsPerPage="Rows:"
                />
            </Card>
        </Box>
    );
};

export default OrderListPage;
