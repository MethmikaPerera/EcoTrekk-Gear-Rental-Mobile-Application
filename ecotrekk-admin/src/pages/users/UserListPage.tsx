import { useMemo, useState } from 'react';
import {
    Box, Card, Table, TableBody, TableCell, TableHead, TableRow, TextField,
    Typography, TablePagination, InputAdornment, FormControl,
    InputLabel, Select, MenuItem, Button, Tooltip, Avatar, Chip, CircularProgress,
    Stack, Switch, styled,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import PeopleIcon from '@mui/icons-material/People';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import PhoneInTalkOutlinedIcon from '@mui/icons-material/PhoneInTalkOutlined';
import { useSnackbar } from 'notistack';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import ConfirmDialog from '../../components/ConfirmDialog';
import EmptyState from '../../components/EmptyState';
import { getCustomers, patchCustomerStatus, CustomerUser } from '../../api/userApi';
import { useAuth } from '../../context/AuthContext';

interface ApiErrorBody {
    message?: string;
}

const avatarColors = ['#2D6A4F', '#D4830A', '#0277BD', '#6B3FA0', '#C62828', '#00695C'];

const PremiumSwitch = styled(Switch)(({ theme }) => ({
    width: 48,
    height: 26,
    padding: 0,
    display: 'flex',
    '&:active': {
        '& .MuiSwitch-thumb': {
            width: 28,
        },
        '& .MuiSwitch-switchBase.Mui-checked': {
            transform: 'translateX(14px)',
        },
    },
    '& .MuiSwitch-switchBase': {
        padding: 3,
        '&.Mui-checked': {
            transform: 'translateX(22px)',
            color: '#fff',
            '& + .MuiSwitch-track': {
                opacity: 1,
                backgroundColor: theme.palette.success.main,
            },
        },
        '&.Mui-focusVisible .MuiSwitch-thumb': {
            color: theme.palette.success.main,
            border: '6px solid #fff',
        },
    },
    '& .MuiSwitch-thumb': {
        boxShadow: '0 2px 6px 0 rgb(0 0 0 / 15%)',
        width: 20,
        height: 20,
        borderRadius: 10,
        transition: theme.transitions.create(['width'], {
            duration: 200,
        }),
    },
    '& .MuiSwitch-track': {
        borderRadius: 26 / 2,
        opacity: 1,
        backgroundColor: 'rgba(0,0,0,.15)',
        boxSizing: 'border-box',
    },
}));

const UserListPage = () => {
    const { user } = useAuth();
    const { enqueueSnackbar } = useSnackbar();
    const queryClient = useQueryClient();

    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [page, setPage] = useState(0);
    const [rowsPerPage] = useState(12);
    const [confirmTarget, setConfirmTarget] = useState<{ id: number; name: string; action: 'ACTIVE' | 'INACTIVE' } | null>(null);
    const [loading, setLoading] = useState(false);
    const canManageStatus = user?.role === 'SUPER_ADMIN' || user?.role === 'ADMIN';

    const { data, isLoading } = useQuery({
        queryKey: ['customers', page, rowsPerPage, search, statusFilter],
        queryFn: () => getCustomers({
            page,
            size: rowsPerPage,
            search: search || undefined,
            status: (statusFilter || undefined) as 'ACTIVE' | 'INACTIVE' | undefined,
        }),
    });

    // Fetch all matching data for KPI statistics
    const { data: statsData } = useQuery({
        queryKey: ['customers-stats', search, statusFilter],
        queryFn: () => getCustomers({
            page: 0,
            size: 9999,
            search: search || undefined,
            status: (statusFilter || undefined) as 'ACTIVE' | 'INACTIVE' | undefined,
        }),
        staleTime: 5 * 60 * 1000, // 5 minutes
    });

    const users = data?.data.content ?? [];
    const totalElements = data?.data.totalElements ?? 0;

    // Calculate stats from all matching data
    const allMatchingUsers = statsData?.data.content ?? [];
    const totalActiveCount = useMemo(() => allMatchingUsers.filter((u) => u.active).length, [allMatchingUsers]);
    const totalInactiveCount = useMemo(() => allMatchingUsers.filter((u) => !u.active).length, [allMatchingUsers]);
    const totalMatchingElements = statsData?.data.totalElements ?? totalElements;

    // For current page display
    const activeCount = useMemo(() => users.filter((u) => u.active).length, [users]);
    const inactiveCount = useMemo(() => users.filter((u) => !u.active).length, [users]);

    const handleConfirmStatusChange = async () => {
        if (!confirmTarget) return;
        setLoading(true);
        try {
            await patchCustomerStatus(confirmTarget.id, confirmTarget.action);
            if (confirmTarget.action === 'INACTIVE') {
                enqueueSnackbar(`"${confirmTarget.name}" has been deactivated.`, { variant: 'warning' });
            } else {
                enqueueSnackbar(`"${confirmTarget.name}" has been activated successfully.`, { variant: 'success' });
            }
            queryClient.invalidateQueries({ queryKey: ['customers'] });
            setConfirmTarget(null);
        } catch (error) {
            const apiError = error as AxiosError<ApiErrorBody>;
            const apiMessage = apiError.response?.data?.message;
            enqueueSnackbar(apiMessage || 'Failed to update user status.', { variant: 'error' });
        } finally {
            setLoading(false);
        }
    };

    const getInitials = (name: string) => name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2);

    return (
        <Box>
            <PageHeader
                title="User Management"
                subtitle={`${totalElements} registered customers`}
                breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'Users' }]}
            />

            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ mb: 2 }}>
                <Card sx={{ flex: 1, p: 2.2, borderRadius: 2.5, background: 'linear-gradient(135deg, #0f5032 0%, #2b7f56 100%)', color: '#fff' }}>
                    <Typography variant="caption" sx={{ opacity: 0.8, letterSpacing: 0.8 }}>TOTAL ACTIVE</Typography>
                    <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1.5 }}>
                        <Typography variant="h4" fontWeight={800}>{totalActiveCount}</Typography>
                        {totalMatchingElements > 0 && (
                            <Typography variant="body2" sx={{ opacity: 0.9 }}>
                                ({Math.round((totalActiveCount / totalMatchingElements) * 100)}%)
                            </Typography>
                        )}
                    </Box>
                </Card>
                <Card sx={{ flex: 1, p: 2.2, borderRadius: 2.5, background: 'linear-gradient(135deg, #d4830a 0%, #f2b03b 100%)', color: '#2a1e04' }}>
                    <Typography variant="caption" sx={{ opacity: 0.8, letterSpacing: 0.8 }}>TOTAL INACTIVE</Typography>
                    <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1.5 }}>
                        <Typography variant="h4" fontWeight={800}>{totalInactiveCount}</Typography>
                        {totalMatchingElements > 0 && (
                            <Typography variant="body2" sx={{ opacity: 0.7 }}>
                                ({Math.round((totalInactiveCount / totalMatchingElements) * 100)}%)
                            </Typography>
                        )}
                    </Box>
                </Card>
                <Card sx={{ flex: 1, p: 2.2, borderRadius: 2.5, background: 'linear-gradient(135deg, #f5f7fa 0%, #e6edf5 100%)' }}>
                    <Typography variant="caption" color="text.secondary" sx={{ letterSpacing: 0.8 }}>TOTAL MATCHING USERS</Typography>
                    <Typography variant="h4" fontWeight={800} color="text.primary">{totalMatchingElements}</Typography>
                </Card>
            </Stack>

            <Card sx={{ mb: 2 }}>
                <Box sx={{ p: 2, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                    <TextField
                        placeholder="Search by name or email..."
                        value={search}
                        autoFocus
                        onChange={(e) => { setSearch(e.target.value); setPage(0); }}
                        sx={{ minWidth: 260 }}
                        InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon color="action" /></InputAdornment> }}
                    />
                    <FormControl sx={{ minWidth: 150 }}>
                        <InputLabel sx={{ top: '-5px' }}>Status</InputLabel>
                        <Select value={statusFilter} label="Status" onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}>
                            <MenuItem value="">All</MenuItem>
                            <MenuItem value="ACTIVE">
                                <Box display="flex" alignItems="center" gap={1}>
                                    <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: 'success.main' }} />
                                    Active
                                </Box>
                            </MenuItem>
                            <MenuItem value="INACTIVE">
                                <Box display="flex" alignItems="center" gap={1}>
                                    <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: 'grey.400' }} />
                                    Inactive
                                </Box>
                            </MenuItem>
                        </Select>
                    </FormControl>
                    {(search || statusFilter) && (
                        <Button variant="text" color="inherit" size="small" onClick={() => { setSearch(''); setStatusFilter(''); }}>
                            Clear
                        </Button>
                    )}
                    <Typography variant="body2" color="text.secondary" ml="auto" fontWeight={500}>
                        {totalElements} {totalElements === 1 ? 'user' : 'users'}
                    </Typography>
                </Box>
            </Card>

            <Card>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>User</TableCell>
                            <TableCell>Email</TableCell>
                            <TableCell>Phone</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell>Registered</TableCell>
                            <TableCell align="center">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={6} align="center" sx={{ py: 8 }}>
                                    <CircularProgress />
                                </TableCell>
                            </TableRow>
                        ) : users.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={6}>
                                    <EmptyState
                                        icon={<PeopleIcon sx={{ fontSize: 36, color: 'grey.400' }} />}
                                        title={search || statusFilter ? 'No users match your search' : 'No users registered yet'}
                                        description={search || statusFilter ? 'Try different search terms or clear your filters.' : 'Users will appear here once they register in the app.'}
                                    />
                                </TableCell>
                            </TableRow>
                        ) : users.map((user: CustomerUser, idx) => (
                            <TableRow key={user.id} hover>
                                <TableCell>
                                    <Box display="flex" alignItems="center" gap={1.5}>
                                        <Avatar
                                            sx={{
                                                width: 40, height: 40, fontSize: '0.85rem', fontWeight: 700,
                                                bgcolor: avatarColors[idx % avatarColors.length],
                                            }}
                                            src={user.profilePictureUrl || undefined}
                                        >
                                            {getInitials(user.fullName)}
                                        </Avatar>
                                        <Box>
                                            <Typography variant="body2" fontWeight={600}>{user.fullName}</Typography>
                                            <Typography variant="caption" color="text.secondary" fontFamily="monospace">USR-{user.id}</Typography>
                                        </Box>
                                    </Box>
                                </TableCell>
                                <TableCell>
                                    <Box display="flex" alignItems="center" gap={0.8}>
                                        <MailOutlineIcon fontSize="small" color="action" />
                                        <Typography variant="body2">{user.email}</Typography>
                                    </Box>
                                </TableCell>
                                <TableCell>
                                    <Box display="flex" alignItems="center" gap={0.8}>
                                        <PhoneInTalkOutlinedIcon fontSize="small" color="action" />
                                        <Typography variant="body2" color="text.secondary">{user.phone || 'Not provided'}</Typography>
                                    </Box>
                                </TableCell>
                                <TableCell><StatusBadge status={user.active ? 'ACTIVE' : 'INACTIVE'} /></TableCell>
                                <TableCell>
                                    <Typography variant="body2" color="text.secondary">
                                        {new Date(user.createdAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}
                                    </Typography>
                                </TableCell>
                                <TableCell align="center">
                                    <Tooltip title={user.active ? 'Deactivate account' : 'Activate account'} placement="top" arrow>
                                        <Box component="span" sx={{ display: 'inline-flex' }}>
                                            <PremiumSwitch
                                                checked={user.active}
                                                disabled={!canManageStatus}
                                                onChange={(e) => setConfirmTarget({
                                                    id: user.id,
                                                    name: user.fullName,
                                                    action: e.target.checked ? 'ACTIVE' : 'INACTIVE',
                                                })}
                                                inputProps={{ 'aria-label': 'customer status toggle' }}
                                            />
                                        </Box>
                                    </Tooltip>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
                <TablePagination
                    component="div"
                    count={totalElements}
                    page={page}
                    onPageChange={(_, p) => setPage(p)}
                    rowsPerPage={rowsPerPage}
                    onRowsPerPageChange={() => { }}
                    rowsPerPageOptions={[12]}
                    labelRowsPerPage="Rows:"
                    labelDisplayedRows={({ from, to, count }) => `${from}–${to} of ${count} users`}
                />
            </Card>

            <ConfirmDialog
                open={!!confirmTarget}
                title={confirmTarget?.action === 'INACTIVE' ? 'Deactivate User Account' : 'Activate User Account'}
                message={
                    confirmTarget?.action === 'INACTIVE'
                        ? `Deactivating "${confirmTarget?.name}" will prevent them from logging in or placing new orders. You can reactivate them at any time.`
                        : `Activating "${confirmTarget?.name}" will restore their full app access.`
                }
                confirmLabel={confirmTarget?.action === 'INACTIVE' ? 'Yes, Deactivate' : 'Yes, Activate'}
                cancelLabel="Cancel"
                danger={confirmTarget?.action === 'INACTIVE'}
                loading={loading}
                onConfirm={handleConfirmStatusChange}
                onCancel={() => setConfirmTarget(null)}
            />
        </Box>
    );
};

export default UserListPage;
