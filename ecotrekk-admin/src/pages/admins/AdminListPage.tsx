import { useState } from 'react';
import {
    Box, Card, Table, TableBody, TableCell, TableHead, TableRow, TextField,
    Typography, IconButton, TablePagination, InputAdornment, FormControl,
    InputLabel, Select, MenuItem, Button, Tooltip, Avatar, Chip, CircularProgress,
    Switch, styled
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import AddIcon from '@mui/icons-material/Add';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import FilterListIcon from '@mui/icons-material/FilterList';
import InventoryIcon from '@mui/icons-material/Inventory';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import SupportAgentIcon from '@mui/icons-material/SupportAgent';
import EditIcon from '@mui/icons-material/Edit';
import { useSnackbar } from 'notistack';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import ConfirmDialog from '../../components/ConfirmDialog';
import EmptyState from '../../components/EmptyState';
import AdminFormModal from './components/AdminFormModal';
import { AdminUser, AdminRole } from '../../context/AuthContext';
import { getAdminRoles } from '../../api/rolesApi';
import { createAdmin, getUsers, patchUserStatus, updateAdmin } from '../../api/userApi';
import { AdminFormData } from './components/AdminFormModal';

const avatarColors = ['#1A2E2A', '#D4830A', '#0277BD', '#6B3FA0'];

const formatRole = (role: string) => {
    return role.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join(' ');
};

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

const AdminListPage = () => {
    const { enqueueSnackbar } = useSnackbar();
    const queryClient = useQueryClient();

    // Fetch roles from backend
    const { data: roles = [], isLoading: isLoadingRoles } = useQuery({
        queryKey: ['admin-roles'],
        queryFn: getAdminRoles,
    });

    const [search, setSearch] = useState('');
    const [roleFilter, setRoleFilter] = useState('');
    const [page, setPage] = useState(0);
    const [rowsPerPage] = useState(10);
    const [confirmTarget, setConfirmTarget] = useState<{ id: string; name: string; action: 'ACTIVE' | 'INACTIVE' } | null>(null);
    const [loading, setLoading] = useState(false);
    const [isFormModalOpen, setIsFormModalOpen] = useState(false);
    const [editingAdmin, setEditingAdmin] = useState<AdminFormData | null>(null);

    const { data: usersData, isLoading: isLoadingUsers } = useQuery({
        queryKey: ['admin-users', page, rowsPerPage, search, roleFilter],
        queryFn: () => getUsers({ page, size: rowsPerPage, search: search || undefined, role: roleFilter || undefined })
    });

    const admins = usersData?.data?.content || [];
    const totalElements = usersData?.data?.totalElements || 0;

    const handleConfirmStatusChange = async () => {
        if (!confirmTarget) return;
        setLoading(true);
        try {
            await patchUserStatus(confirmTarget.id, confirmTarget.action);
            setLoading(false);
            if (confirmTarget.action === 'INACTIVE') {
                enqueueSnackbar(`"${confirmTarget.name}" has been deactivated.`, { variant: 'warning' });
            } else {
                enqueueSnackbar(`"${confirmTarget.name}" has been activated successfully.`, { variant: 'success' });
            }
            setConfirmTarget(null);
            queryClient.invalidateQueries({ queryKey: ['admin-users'] });
        } catch (error: any) {
            console.error('Failed to change admin status:', error);
            setLoading(false);
            enqueueSnackbar(error.response?.data?.message || 'Failed to update admin status. Please try again.', { variant: 'error' });
        }
    };

    const handleFormSubmit = async (data: any) => {
        try {
            if (editingAdmin && data.id) {
                enqueueSnackbar('Updating admin account...', { variant: 'info' });
                await updateAdmin(data.id, {
                    username: data.username || data.name.toLowerCase().replace(/\s+/g, '_') + '_' + Math.floor(Math.random() * 100),
                    fullName: data.name,
                    email: data.email,
                    role: data.role,
                    hubId: data.hubId ?? null
                });
                enqueueSnackbar(`Admin account for ${data.name} updated successfully!`, { variant: 'success' });
            } else {
                enqueueSnackbar('Creating admin account...', { variant: 'info' });
                await createAdmin({
                    username: data.name.toLowerCase().replace(/\s+/g, '_') + '_' + Math.floor(Math.random() * 1000), // Simple username generator
                    fullName: data.name,
                    email: data.email,
                    password: data.password || 'EcoTrekk@123', // Default or from form if it existed
                    role: data.role,
                    hubId: data.hubId ?? null
                });
                enqueueSnackbar(`Admin account for ${data.name} created successfully!`, { variant: 'success' });
            }

            handleCloseModal();
            queryClient.invalidateQueries({ queryKey: ['admin-users'] });
        } catch (error: any) {
            console.error('Failed to save admin:', error);
            const errorMessage = error.response?.data?.message || 'Failed to save admin. Please try again.';
            enqueueSnackbar(errorMessage, { variant: 'error' });
        }
    };

    const handleOpenAddModal = () => {
        setEditingAdmin(null);
        setIsFormModalOpen(true);
    };

    const handleOpenEditModal = async (admin: any) => {
        setEditingAdmin({
            id: String(admin.id),
            name: admin.fullName,
            email: admin.email,
            password: '', // password intentionally blank for edit state
            role: admin.role,
            hubId: admin.hubId ?? null // Note: we need hubId from admin object if provided by endpoint,
            // we assume `admin.hubId` exists, if not we must load it or adjust backend
        });
        setIsFormModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsFormModalOpen(false);
        setEditingAdmin(null);
    };

    const getInitials = (name: string) => name ? name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2) : 'A';

    return (
        <Box sx={{ pb: 4 }}>
            <PageHeader
                title="Admin Management"
                subtitle="Manage system administrators and staff access"
                breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'Admins' }]}
                action={
                    <Button
                        variant="contained"
                        startIcon={<AddIcon />}
                        onClick={handleOpenAddModal}
                        sx={{
                            borderRadius: '12px',
                            px: 3,
                            py: 1,
                            fontWeight: 700,
                            textTransform: 'none',
                            background: 'linear-gradient(135deg, #1F4D3A 0%, #2E7D5B 100%)',
                            boxShadow: '0 4px 14px 0 rgba(46, 125, 91, 0.39)',
                            '&:hover': {
                                transform: 'translateY(-1px)',
                                boxShadow: '0 6px 20px rgba(46, 125, 91, 0.23)',
                            },
                        }}
                    >
                        Add Admin
                    </Button>
                }
            />

            {/* Filters */}
            <Card sx={{
                mb: 3,
                borderRadius: 2,
                border: '1px solid rgba(0,0,0,0.04)',
                boxShadow: '0 8px 32px rgba(0,0,0,0.03)',
                background: 'rgba(255, 255, 255, 0.8)',
                backdropFilter: 'blur(12px)',
            }}>
                <Box sx={{ p: 2.5, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                    <TextField
                        placeholder="Search by name or email..."
                        value={search}
                        autoFocus
                        onChange={(e) => { setSearch(e.target.value); setPage(0); }}
                        sx={{
                            minWidth: 280,
                            '& .MuiOutlinedInput-root': {
                                borderRadius: '12px',
                                backgroundColor: 'background.paper',
                                '&:hover fieldset': { borderColor: 'primary.main' }
                            }
                        }}
                        InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon color="action" /></InputAdornment> }}
                    />
                    <FormControl sx={{ minWidth: 220 }} size="small">
                        <InputLabel>Role Filter</InputLabel>
                        <Select
                            value={roleFilter}
                            label="Role Filter"
                            onChange={(e) => { setRoleFilter(e.target.value); setPage(0); }}
                            sx={{ borderRadius: '12px', backgroundColor: 'background.paper' }}
                            renderValue={(selected) => {
                                if (!selected) return <Box display="flex" alignItems="center" gap={1}><FilterListIcon fontSize="small" color="action" /> All Roles</Box>;
                                const roleMap: Record<string, { label: string, icon: any, color: string }> = {
                                    'SUPER_ADMIN': { label: 'Super Admin', icon: <AdminPanelSettingsIcon fontSize="small" />, color: 'error.main' },
                                    'ADMIN': { label: 'Admin', icon: <InventoryIcon fontSize="small" />, color: 'primary.main' },
                                    'STAFF': { label: 'Staff', icon: <SupportAgentIcon fontSize="small" />, color: 'warning.main' },
                                };
                                const role = roleMap[selected] || { label: selected, icon: <FilterListIcon fontSize="small" />, color: 'inherit' };
                                return (
                                    <Box display="flex" alignItems="center" gap={1} sx={{ color: role.color, fontWeight: 600 }}>
                                        {role.icon} {role.label}
                                    </Box>
                                );
                            }}
                        >
                            <MenuItem value="">
                                <Box display="flex" alignItems="center" gap={1}>
                                    <FilterListIcon fontSize="small" color="action" />
                                    <Typography variant="body2" fontWeight={500}>All Roles</Typography>
                                </Box>
                            </MenuItem>
                            {isLoadingRoles ? (
                                <MenuItem disabled>Loading roles...</MenuItem>
                            ) : (
                                roles.map((roleKey) => {
                                    const roleMap: Record<string, { label: string, icon: any, color: string }> = {
                                        'SUPER_ADMIN': { label: 'Super Admin', icon: <AdminPanelSettingsIcon fontSize="small" color="error" />, color: 'error.main' },
                                        'ADMIN': { label: 'Admin', icon: <InventoryIcon fontSize="small" color="primary" />, color: 'primary.main' },
                                        'STAFF': { label: 'Staff', icon: <SupportAgentIcon fontSize="small" color="warning" />, color: 'warning.main' },
                                    };
                                    const role = roleMap[roleKey] || { label: roleKey, icon: <FilterListIcon fontSize="small" />, color: 'inherit' };
                                    return (
                                        <MenuItem key={roleKey} value={roleKey}>
                                            <Box display="flex" alignItems="center" gap={1.5}>
                                                {role.icon}
                                                <Typography variant="body2" fontWeight={600}>{role.label}</Typography>
                                            </Box>
                                        </MenuItem>
                                    );
                                })
                            )}
                        </Select>
                    </FormControl>
                    {(search || roleFilter) && (
                        <Button variant="text" color="inherit" size="small" onClick={() => { setSearch(''); setRoleFilter(''); }} sx={{ borderRadius: 2, fontWeight: 600 }}>
                            Clear Filters
                        </Button>
                    )}
                    <Typography variant="body2" color="text.secondary" ml="auto" fontWeight={600} sx={{ py: 1, px: 2, bgcolor: 'rgba(0,0,0,0.03)', borderRadius: 2 }}>
                        {totalElements} {totalElements === 1 ? 'admin' : 'admins'}
                    </Typography>
                </Box>
            </Card>

            <Card sx={{ borderRadius: 2, boxShadow: '0 8px 32px rgba(0,0,0,0.03)', border: '1px solid rgba(0,0,0,0.04)', overflow: 'hidden' }}>
                <Table sx={{ minWidth: 800 }}>
                    <TableHead>
                        <TableRow sx={{ backgroundColor: 'rgba(26, 46, 42, 0.02)' }}>
                            <TableCell sx={{ fontWeight: 700, color: 'text.secondary', py: 2 }}>Admin User</TableCell>
                            <TableCell sx={{ fontWeight: 700, color: 'text.secondary', py: 2 }}>Contact Email</TableCell>
                            <TableCell sx={{ fontWeight: 700, color: 'text.secondary', py: 2 }}>Role Access</TableCell>
                            <TableCell sx={{ fontWeight: 700, color: 'text.secondary', py: 2 }}>Account Status</TableCell>
                            <TableCell sx={{ fontWeight: 700, color: 'text.secondary', py: 2 }}>Date Added</TableCell>
                            <TableCell align="center" sx={{ fontWeight: 700, color: 'text.secondary', py: 2 }}>Status</TableCell>
                            <TableCell align="center" sx={{ fontWeight: 700, color: 'text.secondary', py: 2 }}></TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {isLoadingUsers ? (
                            <TableRow>
                                <TableCell colSpan={5} align="center" sx={{ py: 8 }}>
                                    <CircularProgress sx={{ color: 'primary.main' }} />
                                    <Typography variant="body1" color="text.secondary" sx={{ mt: 2, fontWeight: 500 }}>
                                        Loading administrators...
                                    </Typography>
                                </TableCell>
                            </TableRow>
                        ) : admins.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={5} sx={{ borderBottom: 'none' }}>
                                    <EmptyState
                                        icon={<AdminPanelSettingsIcon sx={{ fontSize: 48, color: 'primary.light' }} />}
                                        title={search || roleFilter ? 'No admins match your search' : 'No administrators found'}
                                        description={search || roleFilter ? 'Try different search terms or clear your filters.' : 'Click "Add Admin" to create a new staff account.'}
                                    />
                                </TableCell>
                            </TableRow>
                        ) : admins.map((admin: any, idx: number) => (
                            <TableRow
                                key={admin.id}
                                hover
                                sx={{
                                    transition: 'all 0.2s',
                                    '&:hover': {
                                        transform: 'translateY(-2px)',
                                        boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                                        backgroundColor: 'rgba(255,255,255,1)',
                                        zIndex: 1,
                                        position: 'relative'
                                    }
                                }}
                            >
                                <TableCell>
                                    <Box display="flex" alignItems="center" gap={2}>
                                        <Avatar
                                            sx={{
                                                width: 44, height: 44, fontSize: '1rem', fontWeight: 800,
                                                bgcolor: avatarColors[idx % avatarColors.length],
                                                boxShadow: '0 4px 10px rgba(0,0,0,0.1)'
                                            }}
                                        >
                                            {getInitials(admin.fullName)}
                                        </Avatar>
                                        <Box>
                                            <Typography variant="body1" fontWeight={700} color="text.primary">{admin.fullName}</Typography>
                                            <Typography variant="caption" color="text.secondary" fontFamily="monospace" sx={{ letterSpacing: 0.5 }}>{admin.username || admin.id}</Typography>
                                        </Box>
                                    </Box>
                                </TableCell>
                                <TableCell><Typography variant="body2" fontWeight={500} color="text.secondary">{admin.email}</Typography></TableCell>
                                <TableCell>
                                    <Chip
                                        icon={admin.role === 'SUPER_ADMIN' ? <AdminPanelSettingsIcon /> : admin.role === 'ADMIN' ? <InventoryIcon /> : <SupportAgentIcon />}
                                        label={formatRole(admin.role)}
                                        size="small"
                                        sx={{
                                            borderRadius: '8px',
                                            fontSize: '0.75rem',
                                            fontWeight: 700,
                                            px: 1,
                                            backgroundColor: admin.role === 'SUPER_ADMIN' ? 'rgba(211, 47, 47, 0.1)' : admin.role === 'ADMIN' ? 'rgba(2, 119, 189, 0.1)' : 'rgba(212, 131, 10, 0.1)',
                                            color: admin.role === 'SUPER_ADMIN' ? '#d32f2f' : admin.role === 'ADMIN' ? '#0277bd' : '#d4830a',
                                            '& .MuiChip-icon': { color: 'inherit', fontSize: '1.1rem' }
                                        }}
                                    />
                                </TableCell>
                                <TableCell><StatusBadge status={admin.active ? 'ACTIVE' : 'INACTIVE'} /></TableCell>
                                <TableCell>
                                    <Typography variant="body2" color="text.secondary" fontWeight={500}>
                                        {admin.createdAt ? new Date(admin.createdAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) : 'N/A'}
                                    </Typography>
                                </TableCell>
                                <TableCell align="center">
                                    <Tooltip title={admin.active ? "Deactivate account" : "Activate account"} placement="top" arrow>
                                        <Box component="span" sx={{ display: 'inline-flex' }}>
                                            <PremiumSwitch
                                                checked={admin.active}
                                                onChange={(e) => setConfirmTarget({
                                                    id: admin.id,
                                                    name: admin.fullName,
                                                    action: e.target.checked ? 'ACTIVE' : 'INACTIVE'
                                                })}
                                                inputProps={{ 'aria-label': 'admin status toggle' }}
                                            />
                                        </Box>
                                    </Tooltip>
                                </TableCell>
                                <TableCell align='center'>
                                    <Box display="flex" justifyContent="center" alignItems="center" gap={1}>
                                        <Tooltip title="Edit admin account" placement="top" arrow>
                                            <IconButton
                                                size="small"
                                                onClick={() => handleOpenEditModal(admin)}
                                                sx={{
                                                    color: 'primary.main',
                                                    bgcolor: 'rgba(2, 119, 189, 0.05)',
                                                    '&:hover': { bgcolor: 'primary.main', color: 'white', transform: 'scale(1.1)' },
                                                    transition: 'all 0.2s',
                                                }}
                                            >
                                                <EditIcon fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                    </Box>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>

                <Box sx={{ borderTop: '1px solid rgba(0,0,0,0.05)', bgcolor: 'rgba(255,255,255,0.8)' }}>
                    <TablePagination
                        component="div"
                        count={totalElements}
                        page={page}
                        onPageChange={(_, p) => setPage(p)}
                        rowsPerPage={rowsPerPage}
                        onRowsPerPageChange={() => { }}
                        rowsPerPageOptions={[10]}
                        labelRowsPerPage="Rows per page:"
                        labelDisplayedRows={({ from, to, count }) => `${from}–${to} of ${count}`}
                        sx={{
                            '.MuiTablePagination-selectLabel, .MuiTablePagination-displayedRows': {
                                fontWeight: 500, color: 'text.secondary'
                            }
                        }}
                    />
                </Box>
            </Card>

            <ConfirmDialog
                open={!!confirmTarget}
                title={confirmTarget?.action === 'INACTIVE' ? 'Deactivate Admin Account' : 'Activate Admin Account'}
                message={
                    confirmTarget?.action === 'INACTIVE'
                        ? `Are you absolutely sure you want to deactivate "${confirmTarget?.name}"? They will be immediately logged out and lose all access to the admin console.`
                        : `Are you sure you want to restore access for "${confirmTarget?.name}"?`
                }
                confirmLabel={confirmTarget?.action === 'INACTIVE' ? 'Deactivate Account' : 'Restore Access'}
                cancelLabel="Cancel"
                danger={confirmTarget?.action === 'INACTIVE'}
                loading={loading}
                onConfirm={handleConfirmStatusChange}
                onCancel={() => setConfirmTarget(null)}
            />

            <AdminFormModal
                open={isFormModalOpen}
                onClose={handleCloseModal}
                onSubmit={handleFormSubmit}
                roles={roles}
                initialData={editingAdmin}
            />
        </Box>
    );
};

export default AdminListPage;
