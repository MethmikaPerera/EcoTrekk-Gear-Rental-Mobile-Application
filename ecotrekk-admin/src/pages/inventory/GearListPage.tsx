import { useEffect, useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
    Alert,
    Avatar, Box, Button, Card, CardContent, Chip,
    FormControl, IconButton, InputAdornment, InputLabel,
    MenuItem, Select, Stack, Switch, TablePagination, TextField, Tooltip, Typography,
} from '@mui/material';
import { alpha, styled } from '@mui/material/styles';
import SearchIcon from '@mui/icons-material/Search';
import AddIcon from '@mui/icons-material/Add';
import CategoryIcon from '@mui/icons-material/Category';
import EditIcon from '@mui/icons-material/Edit';
import InventoryIcon from '@mui/icons-material/Inventory2';
import ImageNotSupportedIcon from '@mui/icons-material/ImageNotSupported';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import EmptyState from '../../components/EmptyState';
import GearFormModal, { type GearFormData } from './components/GearFormModal';
import GearCategoryManagerModal from './components/GearCategoryManagerModal';
import { createAdminGear, getGearCategories, getGears } from '../../api/gearApi';
import { fetchActiveHubs } from '../../api/hubApi';
import { getCloudinarySignature, saveCloudinaryAsset, uploadToCloudinary } from '../../api/uploadApi';

const HUBS = ['Colombo Hub', 'Kandy Hub', 'Galle Hub', 'Ella Hub'];

const selectFieldSx = {
    '& .MuiInputLabel-root': {
        transform: 'translate(14px, 9px) scale(1)',
    },
    '& .MuiInputLabel-shrink': {
        transform: 'translate(14px, -9px) scale(0.75)',
    },
    '& .MuiSelect-select': {
        display: 'flex',
        alignItems: 'center',
        minHeight: '1.4375em',
    },
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

type GearCardItem = {
    id: string;
    name: string;
    category: string;
    hub: string;
    pricePerDay: number;
    stock: number;
    status: 'ACTIVE' | 'INACTIVE';
    updatedAt: string;
};

type ApiGearLike = {
    id?: unknown;
    name?: unknown;
    category?: unknown;
    categoryName?: unknown;
    hub?: unknown;
    hubName?: unknown;
    pricePerDay?: unknown;
    dailyRentPrice?: unknown;
    stock?: unknown;
    availableQty?: unknown;
    availableQuantity?: unknown;
    quantity?: unknown;
    status?: unknown;
    isActive?: unknown;
    updatedAt?: unknown;
    updated_at?: unknown;
};

const toStatus = (value: unknown): 'ACTIVE' | 'INACTIVE' =>
    value === 'INACTIVE' ? 'INACTIVE' : 'ACTIVE';

const mapGearFromApi = (raw: ApiGearLike): GearCardItem => ({
    id: String(raw.id ?? ''),
    name: String(raw.name ?? 'Unnamed Gear'),
    category: String(raw.category ?? raw.categoryName ?? 'Uncategorized'),
    hub: String(raw.hub ?? raw.hubName ?? 'N/A'),
    pricePerDay: Number(raw.pricePerDay ?? raw.dailyRentPrice ?? 0),
    stock: Number(raw.stock ?? raw.availableQty ?? raw.availableQuantity ?? raw.quantity ?? 0),
    status: toStatus(raw.status ?? (raw.isActive ? 'ACTIVE' : 'INACTIVE')),
    updatedAt: String(raw.updatedAt ?? raw.updated_at ?? new Date().toISOString()),
});

const GearListPage = () => {
    const [gears, setGears] = useState<GearCardItem[]>([]);
    const [search, setSearch] = useState('');
    const [categoryFilter, setCategoryFilter] = useState('');
    const [hubFilter, setHubFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(12);
    const [totalElements, setTotalElements] = useState(0);
    const [fetchLoading, setFetchLoading] = useState(false);
    const [fetchError, setFetchError] = useState<string | null>(null);
    const [openGearModal, setOpenGearModal] = useState(false);
    const [openCategoryModal, setOpenCategoryModal] = useState(false);
    const [savingGear, setSavingGear] = useState(false);
    const [editingGearId, setEditingGearId] = useState<string | null>(null);

    const { data: categoryOptions = [] } = useQuery({
        queryKey: ['gear-category-options'],
        queryFn: async () => {
            const response = await getGearCategories();
            return response.data;
        },
    });

    const { data: hubOptions = [] } = useQuery({
        queryKey: ['active-hub-options'],
        queryFn: fetchActiveHubs,
    });

    const isOutOfStock = (stock: number) => stock === 0;

    const categoryFilterOptions = useMemo(
        () => [...new Set(categoryOptions.map((cat) => cat.name))].sort((a, b) => a.localeCompare(b)),
        [categoryOptions]
    );

    const filtered = gears.filter((g) => {
        const matchSearch = g.name.toLowerCase().includes(search.toLowerCase());
        const matchCat = !categoryFilter || g.category === categoryFilter;
        const matchHub = !hubFilter || g.hub === hubFilter;
        const matchStatus = !statusFilter || g.status === statusFilter;
        return matchSearch && matchCat && matchHub && matchStatus;
    });

    const paginated = filtered;
    const filteredOutOfStockCount = filtered.filter((g) => isOutOfStock(g.stock)).length;
    const filteredActiveCount = filtered.filter((g) => g.status === 'ACTIVE').length;

    useEffect(() => {
        const fetchGearData = async () => {
            setFetchLoading(true);
            setFetchError(null);

            try {
                const response = await getGears({
                    page,
                    size: rowsPerPage,
                    search: search || undefined,
                    category: categoryFilter || undefined,
                    status: statusFilter || undefined,
                });

                const pageData = response.data;
                const content = Array.isArray(pageData?.content) ? pageData.content : [];
                setGears(content.map((item) => mapGearFromApi(item as unknown as ApiGearLike)));
                setTotalElements(Number(pageData?.totalElements ?? content.length));
            } catch {
                setFetchError('Failed to load gear data from backend.');
                setGears([]);
                setTotalElements(0);
            } finally {
                setFetchLoading(false);
            }
        };

        fetchGearData();
    }, [page, rowsPerPage, search, categoryFilter, statusFilter]);

    const editingGear = useMemo(
        () => gears.find((gear) => gear.id === editingGearId) ?? null,
        [gears, editingGearId]
    );

    const gearFormInitialData: Partial<GearFormData> | undefined = editingGear
        ? {
            name: editingGear.name,
            categoryId: String(categoryOptions.find((c) => c.name === editingGear.category)?.id ?? ''),
            description: '',
            hub: String(hubOptions.find((h) => h.name === editingGear.hub)?.id ?? ''),
            availableCount: String(editingGear.stock),
            dailyRentPrice: String(editingGear.pricePerDay),
            depositAmount: '0',
            isActive: editingGear.status === 'ACTIVE',
            primaryImageUrl: '',
            imageFile: null,
        }
        : undefined;

    const handleToggleStatus = (id: string) => {
        setGears((prev) =>
            prev.map((g) =>
                g.id === id
                    ? { ...g, status: g.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' }
                    : g
            )
        );
    };

    const handleSaveGear = async (formData: GearFormData) => {
        setSavingGear(true);
        try {
            if (editingGearId) {
                setGears((prev) =>
                    prev.map((gear) =>
                        gear.id === editingGearId
                            ? {
                                ...gear,
                                name: formData.name,
                                category: categoryOptions.find((c) => String(c.id) === formData.categoryId)?.name || gear.category,
                                hub: hubOptions.find((h) => String(h.id) === formData.hub)?.name || gear.hub,
                                pricePerDay: Number(formData.dailyRentPrice),
                                stock: Number(formData.availableCount),
                                status: formData.isActive ? 'ACTIVE' : 'INACTIVE',
                                updatedAt: new Date().toISOString(),
                            }
                            : gear
                    )
                );
            } else {
                const hubId = Number(formData.hub);
                const categoryId = Number(formData.categoryId);

                const createResponse = await createAdminGear({
                    name: formData.name,
                    categoryId,
                    description: formData.description,
                    dailyRentPrice: Number(formData.dailyRentPrice),
                    depositAmount: Number(formData.depositAmount || 0),
                    hubId,
                    availableCount: Number(formData.availableCount),
                    active: formData.isActive,
                });

                const created = mapGearFromApi(createResponse.data as unknown as ApiGearLike);

                if (formData.imageFile && (createResponse.data as any)?.gearId) {
                    const gearId = Number((createResponse.data as any).gearId);
                    const signature = await getCloudinarySignature({
                        folderType: 'gears',
                        entityId: gearId,
                    });
                    const uploadResult = await uploadToCloudinary(formData.imageFile, signature);
                    await saveCloudinaryAsset({
                        folderType: 'gears',
                        entityId: gearId,
                        secureUrl: uploadResult.secure_url,
                        publicId: uploadResult.public_id,
                        primaryImage: true,
                        sortOrder: 0,
                    });
                }

                setGears((prev) => [created, ...prev]);
            }

            setOpenGearModal(false);
            setEditingGearId(null);
        } finally {
            setSavingGear(false);
        }
    };

    const clearFilters = () => {
        setSearch('');
        setCategoryFilter('');
        setHubFilter('');
        setStatusFilter('');
        setPage(0);
    };

    const hasActiveFilters = Boolean(search || categoryFilter || hubFilter || statusFilter);

    return (
        <Box>
            <PageHeader
                title="Gear Inventory"
                subtitle={`${gears.length} items total`}
                breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'Gear Inventory' }]}
                action={
                    <Stack direction="row" spacing={1}>
                        <Button
                            variant="outlined"
                            startIcon={<CategoryIcon />}
                            onClick={() => setOpenCategoryModal(true)}
                        >
                            Manage Categories
                        </Button>
                        <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditingGearId(null); setOpenGearModal(true); }}>
                            Add New Gear
                        </Button>
                    </Stack>
                }
            />

            <Card sx={{ mb: 2.5 }}>
                <CardContent sx={{ p: 2.5 }}>
                    <Stack
                        direction={{ xs: 'column', sm: 'row' }}
                        justifyContent="space-between"
                        alignItems={{ xs: 'flex-start', sm: 'center' }}
                        spacing={1}
                        mb={1.75}
                    >
                        <Typography variant="subtitle1" fontWeight={700}>
                            Filter And Refine
                        </Typography>
                        <Chip
                            color="primary"
                            variant="outlined"
                            label={`${filtered.length} ${filtered.length === 1 ? 'result' : 'results'}`}
                            sx={{ fontWeight: 700 }}
                        />
                    </Stack>

                    <Box
                        sx={{
                            display: 'grid',
                            gridTemplateColumns: {
                                xs: '1fr',
                                sm: 'repeat(2, minmax(0, 1fr))',
                                lg: 'minmax(280px, 1.8fr) repeat(3, minmax(150px, 1fr)) auto',
                            },
                            gap: 1.5,
                            alignItems: 'center',
                        }}
                    >
                        <TextField
                            placeholder="Search by gear name..."
                            value={search}
                            onChange={(e) => { setSearch(e.target.value); setPage(0); }}
                            autoFocus
                            sx={{
                                gridColumn: {
                                    xs: '1 / -1',
                                    lg: 'auto',
                                },
                            }}
                            InputProps={{
                                startAdornment: <InputAdornment position="start"><SearchIcon color="action" /></InputAdornment>,
                            }}
                        />
                        <FormControl sx={selectFieldSx}>
                            <InputLabel>Category</InputLabel>
                            <Select value={categoryFilter} label="Category" onChange={(e) => { setCategoryFilter(e.target.value); setPage(0); }}>
                                <MenuItem value="">All Categories</MenuItem>
                                {categoryFilterOptions.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
                            </Select>
                        </FormControl>
                        <FormControl sx={selectFieldSx}>
                            <InputLabel>Hub</InputLabel>
                            <Select value={hubFilter} label="Hub" onChange={(e) => { setHubFilter(e.target.value); setPage(0); }}>
                                <MenuItem value="">All Hubs</MenuItem>
                                {HUBS.map((hub) => <MenuItem key={hub} value={hub}>{hub}</MenuItem>)}
                            </Select>
                        </FormControl>
                        <FormControl sx={selectFieldSx}>
                            <InputLabel>Status</InputLabel>
                            <Select value={statusFilter} label="Status" onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}>
                                <MenuItem value="">All Statuses</MenuItem>
                                <MenuItem value="ACTIVE">Active</MenuItem>
                                <MenuItem value="INACTIVE">Inactive</MenuItem>
                            </Select>
                        </FormControl>

                        <Button
                            variant="outlined"
                            color="inherit"
                            size="small"
                            onClick={clearFilters}
                            disabled={!hasActiveFilters}
                            sx={{
                                minHeight: 40,
                                whiteSpace: 'nowrap',
                            }}
                        >
                            Clear Filters
                        </Button>
                    </Box>

                    {hasActiveFilters && (
                        <Stack direction="row" spacing={1} mt={1.75} useFlexGap flexWrap="wrap">
                            {search && (
                                <Chip
                                    label={`Search: ${search}`}
                                    size="small"
                                    onDelete={() => { setSearch(''); setPage(0); }}
                                />
                            )}
                            {categoryFilter && (
                                <Chip
                                    label={`Category: ${categoryFilter}`}
                                    size="small"
                                    onDelete={() => { setCategoryFilter(''); setPage(0); }}
                                />
                            )}
                            {hubFilter && (
                                <Chip
                                    label={`Hub: ${hubFilter}`}
                                    size="small"
                                    onDelete={() => { setHubFilter(''); setPage(0); }}
                                />
                            )}
                            {statusFilter && (
                                <Chip
                                    label={`Status: ${statusFilter}`}
                                    size="small"
                                    onDelete={() => { setStatusFilter(''); setPage(0); }}
                                />
                            )}
                        </Stack>
                    )}
                </CardContent>
            </Card>

            <Card>
                <Box
                    sx={{
                        px: 2.5,
                        pt: 2,
                        pb: 1.5,
                        borderBottom: `1px solid ${alpha('#1F4D3A', 0.1)}`,
                        background: `linear-gradient(180deg, ${alpha('#1F4D3A', 0.03)} 0%, rgba(255,255,255,0) 100%)`,
                    }}
                >
                    <Stack
                        direction={{ xs: 'column', md: 'row' }}
                        justifyContent="space-between"
                        alignItems={{ xs: 'flex-start', md: 'center' }}
                        spacing={1.2}
                    >
                        <Box>
                            <Typography variant="subtitle1" fontWeight={700}>
                                Gear Catalog
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                Review stock and availability per hub, then perform quick row-level actions.
                            </Typography>
                        </Box>
                        <Stack direction="row" spacing={0.8} useFlexGap flexWrap="wrap">
                            <Chip label={`${filtered.length} Visible`} size="small" variant="outlined" />
                            <Chip label={`${filteredActiveCount} Active`} size="small" color="success" variant="outlined" />
                            <Chip label={`${filteredOutOfStockCount} Out`} size="small" color="error" variant="outlined" />
                        </Stack>
                    </Stack>
                </Box>

                {fetchError && (
                    <Box sx={{ p: 2, pb: 0 }}>
                        <Alert severity="error">{fetchError}</Alert>
                    </Box>
                )}

                {fetchLoading ? (
                    <Box sx={{ p: 3 }}>
                        <Typography variant="body2" color="text.secondary">Loading gear data...</Typography>
                    </Box>
                ) : paginated.length === 0 ? (
                    <Box sx={{ p: 2 }}>
                        <EmptyState
                            icon={<InventoryIcon sx={{ fontSize: 36, color: 'grey.400' }} />}
                            title={search || categoryFilter || hubFilter || statusFilter ? 'No matching gear found' : 'No gear items yet'}
                            description={
                                search || categoryFilter || hubFilter || statusFilter
                                    ? 'Try adjusting your search or filters to find what you\'re looking for.'
                                    : 'Get started by adding your first rental gear item.'
                            }
                            action={!search && !categoryFilter && !hubFilter && !statusFilter ? {
                                label: 'Add First Gear',
                                onClick: () => { setEditingGearId(null); setOpenGearModal(true); },
                            } : undefined}
                        />
                    </Box>
                ) : (
                    <Box
                        sx={{
                            p: 2,
                            display: 'grid',
                            gridTemplateColumns: {
                                xs: '1fr',
                                md: 'repeat(2, minmax(0, 1fr))',
                                xl: 'repeat(3, minmax(0, 1fr))',
                            },
                            gap: 1.5,
                        }}
                    >
                        {paginated.map((gear) => (
                            <Card
                                key={gear.id}
                                variant="outlined"
                                sx={{
                                    borderColor: isOutOfStock(gear.stock)
                                        ? alpha('#DC2626', 0.28)
                                        : alpha('#1F4D3A', 0.12),
                                    boxShadow: 'none',
                                }}
                            >
                                <CardContent sx={{ p: 1.75, '&:last-child': { pb: 1.75 } }}>
                                    <Box>
                                        {/* Top: Avatar/Name and Switch */}
                                        <Box display="flex" alignItems="flex-start" justifyContent="space-between" gap={1}>
                                            <Box display="flex" alignItems="center" gap={1.25}>
                                                <Avatar
                                                    variant="rounded"
                                                    sx={{
                                                        width: 52,
                                                        height: 52,
                                                        borderRadius: 1.5,
                                                        bgcolor: alpha('#1F4D3A', 0.08),
                                                        border: `1px solid ${alpha('#1F4D3A', 0.16)}`,
                                                    }}
                                                >
                                                    <ImageNotSupportedIcon sx={{ color: 'text.secondary', fontSize: 22 }} />
                                                </Avatar>
                                                <Box minWidth={0}>
                                                    <Typography variant="body2" fontWeight={700} noWrap>{gear.name}</Typography>
                                                </Box>
                                            </Box>
                                            <Tooltip title={gear.status === 'ACTIVE' ? 'Deactivate gear' : 'Activate gear'} arrow>
                                                <PremiumSwitch
                                                    checked={gear.status === 'ACTIVE'}
                                                    onChange={() => handleToggleStatus(gear.id)}
                                                />
                                            </Tooltip>
                                        </Box>

                                        {/* Chips */}
                                        <Stack direction="row" spacing={0.8} useFlexGap flexWrap="wrap" mt={1.25}>
                                            <Chip label={gear.category} size="small" variant="outlined" sx={{ borderRadius: 1 }} />
                                            <Chip
                                                label={gear.hub}
                                                size="small"
                                                sx={{
                                                    fontWeight: 600,
                                                    bgcolor: alpha('#2563EB', 0.1),
                                                    color: '#1E3A8A',
                                                    borderRadius: 1,
                                                }}
                                            />
                                            <StatusBadge status={gear.status} />
                                        </Stack>

                                        {/* Price and Stock */}
                                        <Box
                                            sx={{
                                                mt: 1.25,
                                                display: 'grid',
                                                gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
                                                gap: 0.8,
                                            }}
                                        >
                                            <Box>
                                                <Typography variant="caption" color="text.secondary">Price / Day</Typography>
                                                <Typography fontWeight={700} color="primary.main">LKR {gear.pricePerDay.toLocaleString()}</Typography>
                                            </Box>
                                            <Box>
                                                <Typography variant="caption" color="text.secondary">Availability</Typography>
                                                <Typography
                                                    fontWeight={700}
                                                    color={isOutOfStock(gear.stock) ? 'error.main' : 'text.primary'}
                                                >
                                                    {gear.stock}
                                                </Typography>
                                            </Box>
                                        </Box>

                                        {/* Bottom: Updated date and Edit button */}
                                        <Box display="flex" alignItems="center" justifyContent="space-between" mt={1.5}>
                                            <Typography variant="caption" color="text.secondary">
                                                Updated {new Date(gear.updatedAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}
                                            </Typography>
                                            <Tooltip title="Edit gear" arrow>
                                                <IconButton
                                                    size="small"
                                                    color="primary"
                                                    onClick={() => { setEditingGearId(gear.id); setOpenGearModal(true); }}
                                                    sx={{ '&:hover': { bgcolor: alpha('#1F4D3A', 0.08) } }}
                                                >
                                                    <EditIcon fontSize="small" />
                                                </IconButton>
                                            </Tooltip>
                                        </Box>
                                    </Box>
                                </CardContent>
                            </Card>
                        ))}
                    </Box>
                )}

                <TablePagination
                    component="div"
                    count={hubFilter ? filtered.length : totalElements}
                    page={page}
                    onPageChange={(_, p) => setPage(p)}
                    rowsPerPage={rowsPerPage}
                    onRowsPerPageChange={(e) => { setRowsPerPage(+e.target.value); setPage(0); }}
                    rowsPerPageOptions={[6, 12, 24]}
                    labelRowsPerPage="Rows:"
                />
            </Card>

            <GearFormModal
                key={`${editingGearId ?? 'add'}-${openGearModal ? 'open' : 'closed'}`}
                open={openGearModal}
                mode={editingGearId ? 'edit' : 'add'}
                onClose={() => { setOpenGearModal(false); setEditingGearId(null); }}
                onSubmit={handleSaveGear}
                categories={categoryOptions.map((cat) => ({ id: String(cat.id), name: cat.name }))}
                hubs={(hubOptions.length > 0 ? hubOptions : HUBS.map((name, idx) => ({ id: idx + 1, name }))).map((hub) => ({ id: String(hub.id), name: hub.name }))}
                initialData={gearFormInitialData}
                loading={savingGear}
            />

            <GearCategoryManagerModal
                open={openCategoryModal}
                onClose={() => setOpenCategoryModal(false)}
            />
        </Box>
    );
};

export default GearListPage;
