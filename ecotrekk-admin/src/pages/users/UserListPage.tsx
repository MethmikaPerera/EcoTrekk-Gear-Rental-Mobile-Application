import { useState } from 'react';
import {
    Box, Card, Table, TableBody, TableCell, TableHead, TableRow, TextField,
    Typography, IconButton, TablePagination, InputAdornment, FormControl,
    InputLabel, Select, MenuItem, Button, Tooltip, Avatar, Chip,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import BlockIcon from '@mui/icons-material/Block';
import PeopleIcon from '@mui/icons-material/People';
import { useSnackbar } from 'notistack';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import ConfirmDialog from '../../components/ConfirmDialog';
import EmptyState from '../../components/EmptyState';

const mockUsers = Array.from({ length: 35 }, (_, i) => ({
    id: `usr-${100 + i}`,
    name: ['Saman Silva', 'Priya Perera', 'Nimal Fernando', 'Dilini Jayasena', 'Kamal Bandara', 'Amara Kumari'][i % 6],
    email: `user${i + 1}@example.com`,
    phone: `+9471${String(Math.floor(1000000 + Math.random() * 9000000))}`,
    status: i % 7 === 0 ? 'INACTIVE' : 'ACTIVE',
    totalOrders: Math.floor(Math.random() * 15),
    createdAt: new Date(Date.now() - i * 5 * 86400000).toISOString(),
}));

const avatarColors = ['#2D6A4F', '#D4830A', '#0277BD', '#6B3FA0', '#C62828', '#00695C'];

const UserListPage = () => {
    const { enqueueSnackbar } = useSnackbar();
    const [users, setUsers] = useState(mockUsers);
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [page, setPage] = useState(0);
    const [rowsPerPage] = useState(10);
    const [confirmTarget, setConfirmTarget] = useState<{ id: string; name: string; action: 'ACTIVE' | 'INACTIVE' } | null>(null);
    const [loading, setLoading] = useState(false);

    const filtered = users.filter((u) => {
        const matchSearch = !search ||
            u.name.toLowerCase().includes(search.toLowerCase()) ||
            u.email.toLowerCase().includes(search.toLowerCase());
        const matchStatus = !statusFilter || u.status === statusFilter;
        return matchSearch && matchStatus;
    });

    const paginated = filtered.slice(page * rowsPerPage, (page + 1) * rowsPerPage);

    const handleConfirmStatusChange = async () => {
        if (!confirmTarget) return;
        setLoading(true);
        await new Promise((r) => setTimeout(r, 600));
        setUsers((prev) => prev.map((u) => u.id === confirmTarget.id ? { ...u, status: confirmTarget.action } : u));
        setLoading(false);
        if (confirmTarget.action === 'INACTIVE') {
            enqueueSnackbar(`"${confirmTarget.name}" has been deactivated.`, { variant: 'warning' });
        } else {
            enqueueSnackbar(`"${confirmTarget.name}" has been activated successfully.`, { variant: 'success' });
        }
        setConfirmTarget(null);
    };

    const getInitials = (name: string) => name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2);
    const activeCount = users.filter((u) => u.status === 'ACTIVE').length;
    const inactiveCount = users.filter((u) => u.status === 'INACTIVE').length;

    return (
        <Box>
            <PageHeader
                title="User Management"
                subtitle={`${activeCount} active · ${inactiveCount} inactive`}
                breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'Users' }]}
            />

            {/* Filters */}
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
                        <InputLabel>Status</InputLabel>
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
                        {filtered.length} {filtered.length === 1 ? 'user' : 'users'}
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
                            <TableCell>Orders</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell>Registered</TableCell>
                            <TableCell align="center">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {paginated.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={7}>
                                    <EmptyState
                                        icon={<PeopleIcon sx={{ fontSize: 36, color: 'grey.400' }} />}
                                        title={search || statusFilter ? 'No users match your search' : 'No users registered yet'}
                                        description={search || statusFilter ? 'Try different search terms or clear your filters.' : 'Users will appear here once they register in the app.'}
                                    />
                                </TableCell>
                            </TableRow>
                        ) : paginated.map((user, idx) => (
                            <TableRow key={user.id} hover>
                                <TableCell>
                                    <Box display="flex" alignItems="center" gap={1.5}>
                                        <Avatar
                                            sx={{
                                                width: 40, height: 40, fontSize: '0.85rem', fontWeight: 700,
                                                bgcolor: avatarColors[idx % avatarColors.length],
                                            }}
                                        >
                                            {getInitials(user.name)}
                                        </Avatar>
                                        <Box>
                                            <Typography variant="body2" fontWeight={600}>{user.name}</Typography>
                                            <Typography variant="caption" color="text.secondary" fontFamily="monospace">{user.id}</Typography>
                                        </Box>
                                    </Box>
                                </TableCell>
                                <TableCell><Typography variant="body2">{user.email}</Typography></TableCell>
                                <TableCell><Typography variant="body2" color="text.secondary">{user.phone}</Typography></TableCell>
                                <TableCell>
                                    <Chip label={`${user.totalOrders} orders`} size="small" variant="outlined" sx={{ borderRadius: 1, fontSize: '0.72rem' }} />
                                </TableCell>
                                <TableCell><StatusBadge status={user.status} /></TableCell>
                                <TableCell>
                                    <Typography variant="body2" color="text.secondary">
                                        {new Date(user.createdAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}
                                    </Typography>
                                </TableCell>
                                <TableCell align="center">
                                    {user.status === 'ACTIVE' ? (
                                        <Tooltip title="Deactivate user — prevents login" arrow>
                                            <IconButton size="small" color="error"
                                                onClick={() => setConfirmTarget({ id: user.id, name: user.name, action: 'INACTIVE' })}
                                                sx={{ '&:hover': { bgcolor: 'error.50' } }}>
                                                <BlockIcon fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                    ) : (
                                        <Tooltip title="Activate user — restores access" arrow>
                                            <IconButton size="small" color="success"
                                                onClick={() => setConfirmTarget({ id: user.id, name: user.name, action: 'ACTIVE' })}
                                                sx={{ '&:hover': { bgcolor: 'success.50' } }}>
                                                <CheckCircleIcon fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                    )}
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
