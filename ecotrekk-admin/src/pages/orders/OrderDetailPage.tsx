import { useMemo, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    Box,
    Grid,
    Card,
    CardContent,
    Typography,
    Button,
    Divider,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Stepper,
    Step,
    StepLabel,
    StepContent,
    Paper,
    Alert,
    Chip,
    CircularProgress,
    Tooltip,
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
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import ConfirmDialog from '../../components/ConfirmDialog';
import { getOrderById, Order, OrderStatus, patchOrderPaymentStatus, patchOrderStatus } from '../../api/orderApi';

const statusSteps: OrderStatus[] = ['PENDING', 'CONFIRMED', 'PICKED_UP', 'RETURNED'];

type ActionConfig = {
    label: string;
    color: 'primary' | 'success' | 'error' | 'warning';
    nextStatus: OrderStatus;
    icon: React.ReactNode;
    successMsg: string;
};

type UiActionConfig = ActionConfig & {
    disabled: boolean;
    disabledReason?: string;
};

const actionConfig: Record<OrderStatus, ActionConfig[]> = {
    PENDING: [
        { label: 'Confirm Order', color: 'success', nextStatus: 'CONFIRMED', icon: <CheckCircleIcon />, successMsg: 'Order confirmed.' },
        { label: 'Cancel Order', color: 'error', nextStatus: 'CANCELLED', icon: <CancelIcon />, successMsg: 'Order cancelled.' },
    ],
    CONFIRMED: [
        { label: 'Mark Picked Up', color: 'primary', nextStatus: 'PICKED_UP', icon: <LocalShippingIcon />, successMsg: 'Order marked as picked up.' },
        { label: 'Cancel Order', color: 'error', nextStatus: 'CANCELLED', icon: <CancelIcon />, successMsg: 'Order cancelled.' },
    ],
    PICKED_UP: [
        { label: 'Mark as Returned', color: 'success', nextStatus: 'RETURNED', icon: <AssignmentReturnIcon />, successMsg: 'Order marked as returned.' },
    ],
    RETURNED: [],
    CANCELLED: [],
};

const MS_PER_DAY = 86_400_000;

const toUtcDateOnly = (value: string): number => {
    const [year, month, day] = value.split('T')[0].split('-').map(Number);
    if (!year || !month || !day) {
        return NaN;
    }
    return Date.UTC(year, month - 1, day);
};

const getInclusiveRentalDays = (start: string, end: string): number => {
    const startDate = toUtcDateOnly(start);
    const endDate = toUtcDateOnly(end);
    if (Number.isNaN(startDate) || Number.isNaN(endDate)) {
        return 1;
    }
    return Math.max(1, Math.floor((endDate - startDate) / MS_PER_DAY) + 1);
};

const dueDateInfo = (endDate: string, status: OrderStatus) => {
    if (status === 'RETURNED' || status === 'CANCELLED') {
        return null;
    }

    const diff = Math.ceil((new Date(endDate).getTime() - Date.now()) / 86400000);
    if (diff < 0) {
        return { label: `Overdue by ${Math.abs(diff)} day(s)`, color: 'error' as const };
    }
    if (diff === 0) {
        return { label: 'Due today', color: 'warning' as const };
    }
    if (diff <= 2) {
        return { label: `Due in ${diff} day(s)`, color: 'warning' as const };
    }

    return null;
};

const formatPaymentMethod = (method: string) => {
    if (method === 'PAY_AT_HUB') {
        return 'Pay at Hub';
    }
    if (method === 'CARD') {
        return 'Card';
    }
    return method;
};

const OrderDetailPage = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { enqueueSnackbar } = useSnackbar();
    const queryClient = useQueryClient();

    const [confirmAction, setConfirmAction] = useState<ActionConfig | null>(null);
    const [confirmPaid, setConfirmPaid] = useState<boolean | null>(null);

    const { data, isLoading } = useQuery({
        queryKey: ['order', id],
        queryFn: () => getOrderById(id || ''),
        enabled: Boolean(id),
    });

    const mutation = useMutation({
        mutationFn: ({ nextStatus }: { nextStatus: OrderStatus }) => patchOrderStatus(id || '', nextStatus),
        onSuccess: (_, variables) => {
            const message = confirmAction?.successMsg || `Order status updated to ${variables.nextStatus}.`;
            enqueueSnackbar(message, { variant: variables.nextStatus === 'CANCELLED' ? 'warning' : 'success' });
            setConfirmAction(null);
            queryClient.invalidateQueries({ queryKey: ['order', id] });
            queryClient.invalidateQueries({ queryKey: ['orders'] });
        },
        onError: () => {
            enqueueSnackbar('Failed to update order status.', { variant: 'error' });
        },
    });

    const paymentMutation = useMutation({
        mutationFn: ({ paid }: { paid: boolean }) => patchOrderPaymentStatus(id || '', paid),
        onSuccess: (_, variables) => {
            enqueueSnackbar(variables.paid ? 'Payment marked as paid.' : 'Payment marked as unpaid.', {
                variant: variables.paid ? 'success' : 'warning',
            });
            setConfirmPaid(null);
            queryClient.invalidateQueries({ queryKey: ['order', id] });
            queryClient.invalidateQueries({ queryKey: ['orders'] });
        },
        onError: () => {
            enqueueSnackbar('Failed to update payment status.', { variant: 'error' });
        },
    });

    const order = data?.data as Order | undefined;

    const historyMap = useMemo(() => {
        const map = new Map<string, { timestamp: string; note?: string }>();
        const historyEntries = order?.statusHistory || [];
        historyEntries.forEach((entry) => {
            map.set(entry.status, { timestamp: entry.timestamp, note: entry.note });
        });
        return map;
    }, [order?.statusHistory]);

    if (isLoading) {
        return (
            <Box display="flex" justifyContent="center" py={8}>
                <CircularProgress />
            </Box>
        );
    }

    if (!order) {
        return (
            <Alert severity="error" sx={{ borderRadius: 2 }}>
                Unable to load order details.
            </Alert>
        );
    }

    const currentStepIdx = statusSteps.indexOf(order.status);
    const isCancelled = order.status === 'CANCELLED';
    const actions: UiActionConfig[] = (actionConfig[order.status] || []).map((action) => {
        if (action.nextStatus === 'PICKED_UP' && order.paymentStatus !== 'PAID') {
            return {
                ...action,
                disabled: true,
                disabledReason: 'Payment must be marked as paid before pickup.',
            };
        }

        if (action.nextStatus === 'CANCELLED') {
            if (order.status !== 'PENDING') {
                return {
                    ...action,
                    disabled: true,
                    disabledReason: 'Only pending orders can be cancelled.',
                };
            }
            if (order.paymentStatus === 'PAID') {
                return {
                    ...action,
                    disabled: true,
                    disabledReason: 'Paid orders cannot be cancelled.',
                };
            }
        }

        return {
            ...action,
            disabled: false,
        };
    });
    const dueInfo = dueDateInfo(order.endDate, order.status);
    const isPayAtHubOrder = order.paymentMethod === 'PAY_AT_HUB';
    const isPaid = order.paymentStatus === 'PAID';
    const paymentStatusColor = isPaid ? 'success' : 'warning';
    const canUpdatePayment = isPayAtHubOrder && !isCancelled;

    const durationDays = getInclusiveRentalDays(order.startDate, order.endDate);

    const itemSubtotal = (order.items || []).reduce((acc, item) => acc + Number(item.totalPrice || 0), 0);
    const itemDepositTotal = (order.items || []).reduce((acc, item) => {
        const lineDeposit = item.depositTotal != null
            ? Number(item.depositTotal)
            : Number(item.depositAmount ?? item.refundableDeposit ?? 0) * Number(item.quantity || 1);
        return acc + lineDeposit;
    }, 0);

    const subtotal = order.subtotal != null && Number(order.subtotal) > 0
        ? Number(order.subtotal)
        : itemSubtotal;

    const refundableDeposit = order.depositTotal != null && Number(order.depositTotal) > 0
        ? Number(order.depositTotal)
        : (order.refundableDeposit != null && Number(order.refundableDeposit) > 0
            ? Number(order.refundableDeposit)
            : (itemDepositTotal > 0 ? itemDepositTotal : Math.max(0, Number(order.totalAmount || 0) - subtotal)));

    const totalPayable = subtotal + refundableDeposit;

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
                            <Tooltip key={action.nextStatus} title={action.disabledReason || ''} disableHoverListener={!action.disabled}>
                                <span>
                                    <Button
                                        variant="contained"
                                        color={action.color}
                                        startIcon={action.icon}
                                        disabled={action.disabled}
                                        onClick={() => setConfirmAction(action)}
                                    >
                                        {action.label}
                                    </Button>
                                </span>
                            </Tooltip>
                        ))}
                        <Button variant="outlined" startIcon={<ArrowBackIcon />} onClick={() => navigate('/orders')}>
                            Back to Orders
                        </Button>
                        {canUpdatePayment && (
                            <Button
                                variant={isPaid ? 'outlined' : 'contained'}
                                color={isPaid ? 'warning' : 'success'}
                                onClick={() => setConfirmPaid(!isPaid)}
                            >
                                {isPaid ? 'Mark as Unpaid' : 'Mark as Paid'}
                            </Button>
                        )}
                    </Box>
                }
            />

            {dueInfo && (
                <Alert severity={dueInfo.color} sx={{ mb: 2.5, borderRadius: 2 }} icon={<InfoIcon />}>
                    <strong>{dueInfo.label}</strong> rental end date is {order.endDate}.
                </Alert>
            )}

            {order.status === 'CONFIRMED' && order.paymentStatus !== 'PAID' && (
                <Alert severity="warning" sx={{ mb: 2.5, borderRadius: 2 }}>
                    This order cannot be marked as picked up until payment status is marked as paid.
                </Alert>
            )}

            {(order.status === 'RETURNED' || order.status === 'CANCELLED') && (
                <Alert severity={order.status === 'RETURNED' ? 'success' : 'error'} sx={{ mb: 2.5, borderRadius: 2 }}>
                    {order.status === 'RETURNED'
                        ? 'This order is complete. All gear has been returned.'
                        : 'This order was cancelled and cannot be modified.'}
                </Alert>
            )}

            <Grid container spacing={2.5}>
                <Grid size={{ xs: 12, lg: 8 }}>
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
                                    <Typography fontWeight={600}>{durationDays} day(s)</Typography>
                                </Grid>
                                <Grid size={{ xs: 6, sm: 3 }}>
                                    <Typography variant="caption" color="text.secondary" fontWeight={600} textTransform="uppercase">Hub</Typography>
                                    <Box display="flex" alignItems="center" gap={0.5}>
                                        <PlaceIcon sx={{ fontSize: 14, color: 'primary.main' }} />
                                        <Typography fontWeight={600} fontSize="0.9rem">{order.hubName}</Typography>
                                    </Box>
                                </Grid>
                                <Grid size={{ xs: 6, sm: 3 }}>
                                    <Typography variant="caption" color="text.secondary" fontWeight={600} textTransform="uppercase">Payment</Typography>
                                    <Typography fontWeight={600}>{formatPaymentMethod(order.paymentMethod)}</Typography>
                                </Grid>
                                <Grid size={{ xs: 6, sm: 3 }}>
                                    <Typography variant="caption" color="text.secondary" fontWeight={600} textTransform="uppercase">Payment Status</Typography>
                                    <Box mt={0.5}>
                                        <Chip
                                            size="small"
                                            color={paymentStatusColor}
                                            label={order.paymentStatus}
                                        />
                                    </Box>
                                </Grid>
                            </Grid>
                        </CardContent>
                    </Card>

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
                                            <TableCell align="right">Refundable Deposit</TableCell>
                                            <TableCell align="right">Subtotal</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {order.items.map((item) => {
                                            const itemDeposit = item.depositTotal != null
                                                ? Number(item.depositTotal)
                                                : Number(item.depositAmount ?? item.refundableDeposit ?? 0) * Number(item.quantity || 1);
                                            const unitDeposit = item.depositAmount ?? item.refundableDeposit;
                                            return (
                                                <TableRow key={item.id}>
                                                    <TableCell>
                                                        <Typography variant="body2" fontWeight={600}>{item.gearName}</Typography>
                                                    </TableCell>
                                                    <TableCell align="center">
                                                        <Chip label={item.quantity} size="small" sx={{ minWidth: 28 }} />
                                                    </TableCell>
                                                    <TableCell align="right">LKR {Number(item.pricePerDay).toLocaleString()}</TableCell>
                                                    <TableCell align="right">
                                                        <Typography variant="body2" fontWeight={600} color="text.secondary">
                                                            LKR {itemDeposit.toLocaleString()}
                                                        </Typography>
                                                        {item.quantity > 1 && unitDeposit ? (
                                                            <Typography variant="caption" color="text.disabled" display="block">
                                                                (LKR {Number(unitDeposit).toLocaleString()} / item)
                                                            </Typography>
                                                        ) : null}
                                                    </TableCell>
                                                    <TableCell align="right">
                                                        <Typography fontWeight={700}>LKR {Number(item.totalPrice).toLocaleString()}</Typography>
                                                    </TableCell>
                                                </TableRow>
                                            );
                                        })}
                                        <TableRow sx={{ bgcolor: 'grey.50' }}>
                                            <TableCell colSpan={4} align="right">
                                                <Typography variant="body2" color="text.secondary" fontWeight={600}>Rental Subtotal</Typography>
                                            </TableCell>
                                            <TableCell align="right">
                                                <Typography fontWeight={600}>
                                                    LKR {subtotal.toLocaleString()}
                                                </Typography>
                                            </TableCell>
                                        </TableRow>
                                        <TableRow sx={{ bgcolor: 'grey.50' }}>
                                            <TableCell colSpan={4} align="right">
                                                <Box display="inline-flex" alignItems="center" gap={0.5}>
                                                    <Typography variant="body2" color="text.secondary" fontWeight={600}>Refundable Deposit</Typography>
                                                    <Tooltip title="Refundable upon safe return of the gear.">
                                                        <InfoIcon sx={{ fontSize: 16, color: 'text.secondary', cursor: 'help' }} />
                                                    </Tooltip>
                                                </Box>
                                            </TableCell>
                                            <TableCell align="right">
                                                <Typography fontWeight={600} color="info.main">
                                                    LKR {refundableDeposit.toLocaleString()}
                                                </Typography>
                                            </TableCell>
                                        </TableRow>
                                        <TableRow sx={{ bgcolor: 'grey.100' }}>
                                            <TableCell colSpan={4} align="right">
                                                <Typography fontWeight={700}>Total Payable</Typography>
                                            </TableCell>
                                            <TableCell align="right">
                                                <Typography fontWeight={800} color="primary.main" fontSize="1.05rem">
                                                    LKR {totalPayable.toLocaleString()}
                                                </Typography>
                                            </TableCell>
                                        </TableRow>
                                    </TableBody>
                                </Table>
                            </Paper>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardContent>
                            <Typography variant="h6" fontWeight={700} mb={2}>Status History</Typography>
                            {isCancelled ? (
                                <Alert severity="error" sx={{ borderRadius: 2 }}>Order was cancelled.</Alert>
                            ) : (
                                <Stepper activeStep={currentStepIdx === -1 ? 0 : currentStepIdx} orientation="vertical">
                                    {statusSteps.map((step, index) => {
                                        const history = historyMap.get(step);
                                        return (
                                            <Step key={step} completed={index <= currentStepIdx}>
                                                <StepLabel>
                                                    <Typography fontWeight={index <= currentStepIdx ? 700 : 500} color={index <= currentStepIdx ? 'text.primary' : 'text.disabled'}>
                                                        {step.replace('_', ' ')}
                                                    </Typography>
                                                </StepLabel>
                                                <StepContent>
                                                    {history && (
                                                        <Box>
                                                            <Typography variant="body2" color="text.secondary">{history.note || 'Status updated'}</Typography>
                                                            <Typography variant="caption" color="text.disabled">
                                                                {new Date(history.timestamp).toLocaleString('en-GB', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })}
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
                                { label: 'Phone', value: order.customer.phone || 'Not provided' },
                                { label: 'Customer ID', value: order.customer.id, mono: true },
                            ].map((field) => (
                                <Box key={field.label} mb={1.5}>
                                    <Typography variant="caption" color="text.secondary" fontWeight={600} textTransform="uppercase">{field.label}</Typography>
                                    <Typography
                                        fontWeight={600}
                                        fontFamily={field.mono ? 'monospace' : undefined}
                                        fontSize={field.mono ? '0.85rem' : undefined}
                                    >
                                        {field.value}
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
                open={Boolean(confirmAction)}
                title={confirmAction?.label || ''}
                message={
                    confirmAction?.nextStatus === 'CANCELLED'
                        ? 'Are you sure you want to cancel this order? This action cannot be undone.'
                        : `Confirm status update to "${confirmAction?.nextStatus.replace('_', ' ')}"?`
                }
                confirmLabel={confirmAction?.label || ''}
                loading={mutation.isPending}
                danger={confirmAction?.nextStatus === 'CANCELLED'}
                onConfirm={() => {
                    if (!confirmAction) {
                        return;
                    }
                    mutation.mutate({ nextStatus: confirmAction.nextStatus });
                }}
                onCancel={() => setConfirmAction(null)}
            />

            <ConfirmDialog
                open={confirmPaid !== null}
                title={confirmPaid ? 'Mark as Paid' : 'Mark as Unpaid'}
                message={
                    confirmPaid
                        ? 'Confirm customer payment has been received for this Pay at Hub order.'
                        : 'Confirm this order should be marked as unpaid.'
                }
                confirmLabel={confirmPaid ? 'Mark Paid' : 'Mark Unpaid'}
                loading={paymentMutation.isPending}
                danger={confirmPaid === false}
                onConfirm={() => {
                    if (confirmPaid === null) {
                        return;
                    }
                    paymentMutation.mutate({ paid: confirmPaid });
                }}
                onCancel={() => setConfirmPaid(null)}
            />
        </Box>
    );
};

export default OrderDetailPage;
