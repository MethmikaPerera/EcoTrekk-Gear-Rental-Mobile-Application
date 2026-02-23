import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Box, Card, CardContent, Table, TableBody, TableCell, TableHead, TableRow,
    TextField, Select, MenuItem, FormControl, InputLabel, Button, IconButton,
    Typography, Avatar, Checkbox, Chip, Tooltip, TablePagination,
    InputAdornment, Alert, LinearProgress,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import BlockIcon from '@mui/icons-material/Block';
import InventoryIcon from '@mui/icons-material/Inventory2';
import ImageNotSupportedIcon from '@mui/icons-material/ImageNotSupported';
import { useSnackbar } from 'notistack';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import ConfirmDialog from '../../components/ConfirmDialog';
import EmptyState from '../../components/EmptyState';

const CATEGORIES = ['Tents', 'Backpacks', 'Sleeping Bags', 'Cooking', 'Navigation', 'Clothing', 'Safety', 'Lighting'];

const mockGears = Array.from({ length: 28 }, (_, i) => ({
    id: `gear-${i + 1}`,
    name: [
        'Tent Pro X3', 'Ultra Light Backpack 60L', 'Sleeping Bag -10°C', 'Camp Chef Stove',
        'GPS Navigator', 'Waterproof Jacket', 'Headlamp 500lm', 'Trekking Poles',
        'First Aid Kit Pro', 'Bear Canister', 'Water Filter Pump', 'Solar Charger',
    ][i % 12],
    category: CATEGORIES[i % CATEGORIES.length],
    pricePerDay: [1500, 800, 600, 400, 1200, 900, 350, 500, 250, 700, 450, 1100][i % 12],
    stock: i % 7 === 0 ? 2 : i % 5 === 0 ? 0 : Math.floor(Math.random() * 20) + 3,
    status: i % 8 === 0 ? 'INACTIVE' : 'ACTIVE',
    updatedAt: new Date(Date.now() - i * 86400000).toISOString(),
}));

const GearListPage = () => {
    const navigate = useNavigate();
    const { enqueueSnackbar } = useSnackbar();
    const [gears, setGears] = useState(mockGears);
    const [search, setSearch] = useState('');
    const [categoryFilter, setCategoryFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [selected, setSelected] = useState<string[]>([]);
    const [deleteTarget, setDeleteTarget] = useState<string | null>(null);
    const [deleteLoading, setDeleteLoading] = useState(false);
    const [loading, setLoading] = useState(false);

    const filtered = gears.filter((g) => {
        const matchSearch = g.name.toLowerCase().includes(search.toLowerCase());
        const matchCat = !categoryFilter || g.category === categoryFilter;
        const matchStatus = !statusFilter || g.status === statusFilter;
        return matchSearch && matchCat && matchStatus;
    });

    const paginated = filtered.slice(page * rowsPerPage, (page + 1) * rowsPerPage);
    const lowStockCount = gears.filter((g) => g.stock > 0 && g.stock <= 3).length;

    const handleSelectAll = (checked: boolean) =>
        setSelected(checked ? paginated.map((g) => g.id) : []);

    const handleSelect = (id: string) =>
        setSelected((prev) => prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]);

    const handleDelete = async () => {
        setDeleteLoading(true);
        await new Promise((r) => setTimeout(r, 700));
        setGears((prev) => prev.filter((g) => g.id !== deleteTarget));
        setDeleteLoading(false);
        setDeleteTarget(null);
        enqueueSnackbar('Gear item deleted successfully.', { variant: 'success' });
    };

    const handleBulkActivate = async () => {
        setLoading(true);
        await new Promise((r) => setTimeout(r, 600));
        setGears((prev) => prev.map((g) => selected.includes(g.id) ? { ...g, status: 'ACTIVE' } : g));
        enqueueSnackbar(`${selected.length} item(s) activated.`, { variant: 'success' });
        setSelected([]);
        setLoading(false);
    };

    const handleBulkDeactivate = async () => {
        setLoading(true);
        await new Promise((r) => setTimeout(r, 600));
        setGears((prev) => prev.map((g) => selected.includes(g.id) ? { ...g, status: 'INACTIVE' } : g));
        enqueueSnackbar(`${selected.length} item(s) deactivated.`, { variant: 'warning' });
        setSelected([]);
        setLoading(false);
    };

    const isLowStock = (stock: number) => stock > 0 && stock <= 3;
    const isOutOfStock = (stock: number) => stock === 0;

    return (
        <Box>
            <PageHeader
                title="Gear Inventory"
                subtitle={`${gears.length} items total · ${lowStockCount} low stock`}
                breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'Gear Inventory' }]}
                action={
                    <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/inventory/gears/new')}>
                        Add New Gear
                    </Button>
                }
            />

            {lowStockCount > 0 && (
                <Alert
                    severity="warning" icon={<WarningAmberIcon />} sx={{ mb: 2.5, borderRadius: 2 }}
                    action={
                        <Button size="small" color="warning" onClick={() => setStatusFilter('ACTIVE')}>
                            Filter Low Stock
                        </Button>
                    }
                >
                    <strong>{lowStockCount} items</strong> are running low on stock (≤ 3 units). Restock soon.
                </Alert>
            )}

            {/* Filters */}
            <Card sx={{ mb: 2.5 }}>
                <CardContent sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center', pb: '12px !important' }}>
                    <TextField
                        placeholder="Search by gear name..."
                        value={search}
                        onChange={(e) => { setSearch(e.target.value); setPage(0); }}
                        sx={{ minWidth: 240 }}
                        autoFocus
                        InputProps={{
                            startAdornment: <InputAdornment position="start"><SearchIcon color="action" /></InputAdornment>,
                        }}
                    />
                    <FormControl sx={{ minWidth: 160 }}>
                        <InputLabel>Category</InputLabel>
                        <Select value={categoryFilter} label="Category" onChange={(e) => { setCategoryFilter(e.target.value); setPage(0); }}>
                            <MenuItem value="">All Categories</MenuItem>
                            {CATEGORIES.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
                        </Select>
                    </FormControl>
                    <FormControl sx={{ minWidth: 140 }}>
                        <InputLabel>Status</InputLabel>
                        <Select value={statusFilter} label="Status" onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}>
                            <MenuItem value="">All Statuses</MenuItem>
                            <MenuItem value="ACTIVE">Active</MenuItem>
                            <MenuItem value="INACTIVE">Inactive</MenuItem>
                        </Select>
                    </FormControl>
                    {(search || categoryFilter || statusFilter) && (
                        <Button variant="outlined" color="inherit" size="small" onClick={() => { setSearch(''); setCategoryFilter(''); setStatusFilter(''); }}>
                            Clear Filters
                        </Button>
                    )}
                    <Typography variant="body2" color="text.secondary" ml="auto" fontWeight={500}>
                        {filtered.length} {filtered.length === 1 ? 'result' : 'results'}
                    </Typography>
                </CardContent>
            </Card>

            {/* Bulk Action Bar */}
            {selected.length > 0 && (
                <Card sx={{ mb: 1.5, background: 'linear-gradient(90deg, #2D6A4F, #52B788)' }}>
                    <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, py: '12px !important' }}>
                        <Typography color="white" fontWeight={700} fontSize="0.9rem">
                            {selected.length} item{selected.length > 1 ? 's' : ''} selected
                        </Typography>
                        <Button size="small" variant="contained"
                            sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: '#fff', '&:hover': { bgcolor: 'rgba(255,255,255,0.3)' } }}
                            startIcon={<CheckCircleIcon />} onClick={handleBulkActivate} disabled={loading}>
                            Activate
                        </Button>
                        <Button size="small" variant="contained"
                            sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: '#fff', '&:hover': { bgcolor: 'rgba(255,255,255,0.3)' } }}
                            startIcon={<BlockIcon />} onClick={handleBulkDeactivate} disabled={loading}>
                            Deactivate
                        </Button>
                        <Button size="small" sx={{ color: 'rgba(255,255,255,0.7)', ml: 'auto' }} onClick={() => setSelected([])}>
                            Cancel
                        </Button>
                    </CardContent>
                    {loading && <LinearProgress sx={{ height: 3 }} />}
                </Card>
            )}

            {/* Table */}
            <Card>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell padding="checkbox">
                                <Checkbox
                                    indeterminate={selected.length > 0 && selected.length < paginated.length}
                                    checked={paginated.length > 0 && selected.length === paginated.length}
                                    onChange={(e) => handleSelectAll(e.target.checked)}
                                />
                            </TableCell>
                            <TableCell>Item</TableCell>
                            <TableCell>Category</TableCell>
                            <TableCell>Price / Day</TableCell>
                            <TableCell>Stock</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell>Last Updated</TableCell>
                            <TableCell align="center">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {paginated.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={8}>
                                    <EmptyState
                                        icon={<InventoryIcon sx={{ fontSize: 36, color: 'grey.400' }} />}
                                        title={search || categoryFilter || statusFilter ? 'No matching gear found' : 'No gear items yet'}
                                        description={
                                            search || categoryFilter || statusFilter
                                                ? 'Try adjusting your search or filters to find what you\'re looking for.'
                                                : 'Get started by adding your first rental gear item.'
                                        }
                                        action={!search && !categoryFilter && !statusFilter ? {
                                            label: 'Add First Gear',
                                            onClick: () => navigate('/inventory/gears/new'),
                                        } : undefined}
                                    />
                                </TableCell>
                            </TableRow>
                        ) : paginated.map((gear) => (
                            <TableRow
                                key={gear.id}
                                hover
                                selected={selected.includes(gear.id)}
                                sx={{
                                    bgcolor: isOutOfStock(gear.stock) ? 'rgba(211,47,47,0.03)' : isLowStock(gear.stock) ? 'rgba(237,108,2,0.03)' : 'inherit',
                                    '&:last-child td': { border: 0 },
                                }}
                            >
                                <TableCell padding="checkbox">
                                    <Checkbox checked={selected.includes(gear.id)} onChange={() => handleSelect(gear.id)} />
                                </TableCell>
                                <TableCell>
                                    <Box display="flex" alignItems="center" gap={1.5}>
                                        <Avatar variant="rounded" sx={{ width: 44, height: 44, bgcolor: 'grey.100', borderRadius: 1.5 }}>
                                            <ImageNotSupportedIcon sx={{ color: 'grey.400', fontSize: 20 }} />
                                        </Avatar>
                                        <Box>
                                            <Typography variant="body2" fontWeight={600}>{gear.name}</Typography>
                                            <Typography variant="caption" color="text.secondary">#{gear.id}</Typography>
                                        </Box>
                                    </Box>
                                </TableCell>
                                <TableCell>
                                    <Chip label={gear.category} size="small" variant="outlined" sx={{ borderRadius: 1 }} />
                                </TableCell>
                                <TableCell>
                                    <Typography fontWeight={700} color="primary.main">LKR {gear.pricePerDay.toLocaleString()}</Typography>
                                </TableCell>
                                <TableCell>
                                    <Box display="flex" alignItems="center" gap={0.8}>
                                        <Typography fontWeight={600} color={isOutOfStock(gear.stock) ? 'error.main' : isLowStock(gear.stock) ? 'warning.main' : 'text.primary'}>
                                            {gear.stock}
                                        </Typography>
                                        {isOutOfStock(gear.stock) && <Chip label="Out of Stock" size="small" color="error" sx={{ height: 18, fontSize: '0.65rem' }} />}
                                        {isLowStock(gear.stock) && <Chip label="Low" size="small" color="warning" sx={{ height: 18, fontSize: '0.65rem' }} />}
                                    </Box>
                                </TableCell>
                                <TableCell><StatusBadge status={gear.status} /></TableCell>
                                <TableCell>
                                    <Typography variant="body2" color="text.secondary">
                                        {new Date(gear.updatedAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}
                                    </Typography>
                                </TableCell>
                                <TableCell align="center">
                                    <Box display="flex" justifyContent="center" gap={0.5}>
                                        <Tooltip title="Edit gear" arrow>
                                            <IconButton size="small" color="primary" onClick={() => navigate(`/inventory/gears/${gear.id}/edit`)}
                                                sx={{ '&:hover': { bgcolor: 'primary.50' } }}>
                                                <EditIcon fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                        <Tooltip title="Delete gear" arrow>
                                            <IconButton size="small" color="error" onClick={() => setDeleteTarget(gear.id)}
                                                sx={{ '&:hover': { bgcolor: 'error.50' } }}>
                                                <DeleteIcon fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                    </Box>
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
                    onRowsPerPageChange={(e) => { setRowsPerPage(+e.target.value); setPage(0); }}
                    rowsPerPageOptions={[5, 10, 25]}
                    labelRowsPerPage="Rows:"
                />
            </Card>

            <ConfirmDialog
                open={!!deleteTarget}
                title="Delete Gear Item"
                message="Are you sure you want to delete this gear item? This action cannot be undone and may affect active orders."
                confirmLabel="Yes, Delete"
                cancelLabel="Keep Item"
                loading={deleteLoading}
                onConfirm={handleDelete}
                onCancel={() => setDeleteTarget(null)}
            />
        </Box>
    );
};

export default GearListPage;
