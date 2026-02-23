import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    Box, Grid, Card, CardContent, Typography, Button, Divider,
    Table, TableBody, TableCell, TableHead, TableRow, Stepper, Step,
    StepLabel, StepContent, Paper, Alert, Chip,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import AssignmentReturnIcon from '@mui/icons-material/AssignmentReturn';
import PersonIcon from '@mui/icons-material/Person';
import PlaceIcon from '@mui/icons-material/Place';
import InfoIcon from '@mui/icons-material/Info';
import { useSnackbar } from 'notistack';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import ConfirmDialog from '../../components/ConfirmDialog';

const statusSteps = ['PENDING', 'CONFIRMED', 'PICKED_UP', 'RETURNED'];

const getMockOrder = (id: string) => ({
    id,
    customer: { id: 'usr-42', name: 'Saman Silva', email: 'saman@gmail.com', phone: '+94712345678' },
    hubId: 'hub-1', hubName: 'Colombo Central Hub',
    startDate: '2026-02-22', endDate: '2026-02-25',
    status: 'CONFIRMED' as 'PENDING' | 'CONFIRMED' | 'PICKED_UP' | 'RETURNED' | 'CANCELLED',
    totalAmount: 4500,
    createdAt: '2026-02-20T08:30:00Z',
    items: [
        { id: 'oi-1', gearName: 'Tent Pro X3', quantity: 1, pricePerDay: 1500, totalPrice: 4500 },
        { id: 'oi-2', gearName: 'Sleeping Bag -10°C', quantity: 2, pricePerDay: 600, totalPrice: 1800 },
    ],
    statusHistory: [
        { status: 'PENDING', timestamp: '2026-02-20T08:30:00Z', note: 'Order placed by customer' },
        { status: 'CONFIRMED', timestamp: '2026-02-20T09:15:00Z', note: 'Confirmed by admin' },
    ],
});

const actionConfig: Record<string, { label: string; color: 'primary' | 'success' | 'error' | 'warning'; nextStatus: string; icon: React.ReactNode; successMsg: string }[]> = {
    PENDING: [
        { label: 'Confirm Order', color: 'success', nextStatus: 'CONFIRMED', icon: <CheckCircleIcon />, successMsg: 'Order confirmed! Customer will be notified.' },
        { label: 'Cancel Order', color: 'error', nextStatus: 'CANCELLED', icon: <CancelIcon />, successMsg: 'Order has been cancelled.' },
    ],
    CONFIRMED: [
        { label: 'Mark Picked Up', color: 'primary', nextStatus: 'PICKED_UP', icon: <LocalShippingIcon />, successMsg: 'Order marked as picked up.' },
        { label: 'Cancel Order', color: 'error', nextStatus: 'CANCELLED', icon: <CancelIcon />, successMsg: 'Order has been cancelled.' },
    ],
    PICKED_UP: [
        { label: 'Mark as Returned', color: 'success', nextStatus: 'RETURNED', icon: <AssignmentReturnIcon />, successMsg: 'Gear returned successfully. Order complete!' },
    ],
    CANCELLED: [],
    RETURNED: [],
};

const dueDateInfo = (endDate: string, status: string) => {
    if (status === 'RETURNED' || status === 'CANCELLED') return null;
    const diff = Math.ceil((new Date(endDate).getTime() - Date.now()) / 86400000);
    if (diff < 0) return { label: `Overdue by ${Math.abs(diff)} day(s)`, color: 'error' as const };
    if (diff === 0) return { label: 'Due today', color: 'warning' as const };
    if (diff <= 2) return { label: `Due in ${diff} day(s)`, color: 'warning' as const };
    return null;
};

const OrderDetailPage = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { enqueueSnackbar } = useSnackbar();
    const [order, setOrder] = useState(getMockOrder(id || 'ORD-1001'));
    const [confirmAction, setConfirmAction] = useState<{ label: string; nextStatus: string; successMsg: string } | null>(null);
    const [loading, setLoading] = useState(false);

    const currentStepIdx = statusSteps.indexOf(order.status);
    const isCancelled = order.status === 'CANCELLED';
    const actions = actionConfig[order.status] || [];
    const dueInfo = dueDateInfo(order.endDate, order.status);

    const handleStatusChange = async () => {
        if (!confirmAction) return;
        setLoading(true);
        await new Promise((r) => setTimeout(r, 800));
        setOrder((prev) => ({
            ...prev,
            status: confirmAction.nextStatus as typeof prev.status,
            statusHistory: [
                ...prev.statusHistory,
                { status: confirmAction.nextStatus, timestamp: new Date().toISOString(), note: `Updated by admin` },
            ],
        }));
        setLoading(false);
        enqueueSnackbar(confirmAction.successMsg, {
            variant: confirmAction.nextStatus === 'CANCELLED' ? 'warning' : 'success',
        });
        setConfirmAction(null);
    };

    return (
        <Box>
            <PageHeader
                title={`Order ${order.id}`}
                breadcrumbs={[
                    { label: 'Home', to: '/dashboard' },
                    { label: 'Orders', to: '/orders' },
                    { label: order.id },
                ]}
                action={
                    <Box display="flex" gap={1} flexWrap="wrap">
                        {actions.map((action) => (
                            <Button
                                key={action.nextStatus}
                                variant="contained"
                                color={action.color}
                                startIcon={action.icon}
                                onClick={() => setConfirmAction(action)}
                            >
                                {action.label}
                            </Button>
                        ))}
                        <Button variant="outlined" startIcon={<ArrowBackIcon />} onClick={() => navigate('/orders')}>
                            Back to Orders
                        </Button>
                    </Box>
                }
            />

            {dueInfo && (
                <Alert severity={dueInfo.color} sx={{ mb: 2.5, borderRadius: 2 }} icon={<InfoIcon />}>
                    <strong>{dueInfo.label}</strong> — this rental ends on {order.endDate}.
                </Alert>
            )}

            {(order.status === 'RETURNED' || order.status === 'CANCELLED') && (
                <Alert
                    severity={order.status === 'RETURNED' ? 'success' : 'error'}
                    sx={{ mb: 2.5, borderRadius: 2 }}
                >
                    {order.status === 'RETURNED'
                        ? 'This order is complete. All gear has been returned.'
                        : 'This order was cancelled and cannot be modified.'}
                </Alert>
            )}

            <Grid container spacing={2.5}>
                {/* Left */}
                <Grid size={{ xs: 12, lg: 8 }}>
                    {/* Summary */}
                    <Card sx={{ mb: 2.5 }}>
                        <CardContent>
                            <Box display="flex" justifyContent="space-between" alignItems="flex-start" flexWrap="wrap" gap={1} mb={2}>
                                <Box>
                                    <Typography variant="h6" fontWeight={700}>Order Summary</Typography>
                                    <Typography variant="caption" color="text.secondary">
                                        Created {new Date(order.createdAt).toLocaleString('en-GB', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })}
                                    </Typography>
                                </Box>
                                <StatusBadge status={order.status} size="medium" />
                            </Box>
                            <Grid container spacing={2}>
                                <Grid size={{ xs: 6, sm: 3 }}>
                                    <Typography variant="caption" color="text.secondary" fontWeight={600} textTransform="uppercase">Start Date</Typography>
                                    <Typography fontWeight={600}>{order.startDate}</Typography>
                                </Grid>
                                <Grid size={{ xs: 6, sm: 3 }}>
                                    <Typography variant="caption" color="text.secondary" fontWeight={600} textTransform="uppercase">End Date</Typography>
                                    <Typography fontWeight={600}>{order.endDate}</Typography>
                                </Grid>
                                <Grid size={{ xs: 6, sm: 3 }}>
                                    <Typography variant="caption" color="text.secondary" fontWeight={600} textTransform="uppercase">Duration</Typography>
                                    <Typography fontWeight={600}>
                                        {Math.max(1, Math.ceil((new Date(order.endDate).getTime() - new Date(order.startDate).getTime()) / 86400000))} day(s)
                                    </Typography>
                                </Grid>
                                <Grid size={{ xs: 6, sm: 3 }}>
                                    <Typography variant="caption" color="text.secondary" fontWeight={600} textTransform="uppercase">Hub</Typography>
                                    <Box display="flex" alignItems="center" gap={0.5}>
                                        <PlaceIcon sx={{ fontSize: 14, color: 'primary.main' }} />
                                        <Typography fontWeight={600} fontSize="0.9rem">{order.hubName}</Typography>
                                    </Box>
                                </Grid>
                            </Grid>
                        </CardContent>
                    </Card>

                    {/* Items */}
                    <Card sx={{ mb: 2.5 }}>
                        <CardContent>
                            <Typography variant="h6" fontWeight={700} mb={2}>Rental Items ({order.items.length})</Typography>
                            <Paper variant="outlined" sx={{ borderRadius: 2, overflow: 'hidden' }}>
                                <Table size="small">
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Item</TableCell>
                                            <TableCell align="center">Qty</TableCell>
                                            <TableCell align="right">Price/Day</TableCell>
                                            <TableCell align="right">Subtotal</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {order.items.map((item) => (
                                            <TableRow key={item.id}>
                                                <TableCell>
                                                    <Typography variant="body2" fontWeight={600}>{item.gearName}</Typography>
                                                </TableCell>
                                                <TableCell align="center">
                                                    <Chip label={item.quantity} size="small" sx={{ minWidth: 28 }} />
                                                </TableCell>
                                                <TableCell align="right">LKR {item.pricePerDay.toLocaleString()}</TableCell>
                                                <TableCell align="right">
                                                    <Typography fontWeight={700}>LKR {item.totalPrice.toLocaleString()}</Typography>
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                        <TableRow sx={{ bgcolor: 'grey.50' }}>
                                            <TableCell colSpan={3} align="right">
                                                <Typography fontWeight={700}>Total Payable</Typography>
                                            </TableCell>
                                            <TableCell align="right">
                                                <Typography fontWeight={800} color="primary.main" fontSize="1.05rem">
                                                    LKR {order.totalAmount.toLocaleString()}
                                                </Typography>
                                            </TableCell>
                                        </TableRow>
                                    </TableBody>
                                </Table>
                            </Paper>
                        </CardContent>
                    </Card>

                    {/* Timeline */}
                    <Card>
                        <CardContent>
                            <Typography variant="h6" fontWeight={700} mb={2}>Status History</Typography>
                            {isCancelled ? (
                                <Alert severity="error" sx={{ borderRadius: 2 }}>Order was cancelled.</Alert>
                            ) : (
                                <Stepper activeStep={currentStepIdx === -1 ? 0 : currentStepIdx} orientation="vertical">
                                    {statusSteps.map((step, idx) => {
                                        const hist = order.statusHistory.find((h) => h.status === step);
                                        return (
                                            <Step key={step} completed={idx <= currentStepIdx}>
                                                <StepLabel>
                                                    <Typography fontWeight={idx <= currentStepIdx ? 700 : 500} color={idx <= currentStepIdx ? 'text.primary' : 'text.disabled'}>
                                                        {step.replace('_', ' ')}
                                                    </Typography>
                                                </StepLabel>
                                                <StepContent>
                                                    {hist && (
                                                        <Box>
                                                            <Typography variant="body2" color="text.secondary">{hist.note}</Typography>
                                                            <Typography variant="caption" color="text.disabled">
                                                                {new Date(hist.timestamp).toLocaleString('en-GB', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })}
                                                            </Typography>
                                                        </Box>
                                                    )}
                                                </StepContent>
                                            </Step>
                                        );
                                    })}
                                </Stepper>
                            )}
                        </CardContent>
                    </Card>
                </Grid>

                {/* Right */}
                <Grid size={{ xs: 12, lg: 4 }}>
                    <Card>
                        <CardContent>
                            <Box display="flex" alignItems="center" gap={1} mb={2}>
                                <PersonIcon color="primary" />
                                <Typography variant="h6" fontWeight={700}>Customer Details</Typography>
                            </Box>
                            <Divider sx={{ mb: 2 }} />
                            {[
                                { label: 'Full Name', value: order.customer.name },
                                { label: 'Email', value: order.customer.email },
                                { label: 'Phone', value: order.customer.phone },
                                { label: 'Customer ID', value: order.customer.id, mono: true },
                            ].map((f) => (
                                <Box key={f.label} mb={1.5}>
                                    <Typography variant="caption" color="text.secondary" fontWeight={600} textTransform="uppercase">{f.label}</Typography>
                                    <Typography fontWeight={600} fontFamily={f.mono ? 'monospace' : undefined} fontSize={f.mono ? '0.85rem' : undefined}>
                                        {f.value}
                                    </Typography>
                                </Box>
                            ))}
                            <Divider sx={{ my: 2 }} />
                            <Button fullWidth variant="outlined" size="small" onClick={() => navigate('/users')}>
                                View Customer Profile
                            </Button>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            <ConfirmDialog
                open={!!confirmAction}
                title={confirmAction?.label || ''}
                message={
                    confirmAction?.nextStatus === 'CANCELLED'
                        ? 'Are you sure you want to cancel this order? The customer will be notified. This action cannot be undone.'
                        : `Confirm updating status to "${confirmAction?.nextStatus?.replace('_', ' ').toLowerCase()}"?`
                }
                confirmLabel={confirmAction?.label || ''}
                loading={loading}
                danger={confirmAction?.nextStatus === 'CANCELLED'}
                onConfirm={handleStatusChange}
                onCancel={() => setConfirmAction(null)}
            />
        </Box>
    );
};

export default OrderDetailPage;
