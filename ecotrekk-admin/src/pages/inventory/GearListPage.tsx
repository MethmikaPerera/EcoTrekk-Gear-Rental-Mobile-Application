import { useEffect, useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import axios from 'axios';
import {
    Alert,
    Avatar, Box, Button, Card, CardContent, Chip,
    Dialog, DialogActions, DialogContent, DialogTitle,
    FormControl, InputAdornment, InputLabel,
    LinearProgress, MenuItem, Select, Stack, Switch, TablePagination, TextField, Tooltip, Typography,
} from '@mui/material';
import { alpha, styled } from '@mui/material/styles';
import SearchIcon from '@mui/icons-material/Search';
import AddIcon from '@mui/icons-material/Add';
import CategoryIcon from '@mui/icons-material/Category';
import InventoryIcon from '@mui/icons-material/Inventory2';
import ImageNotSupportedIcon from '@mui/icons-material/ImageNotSupported';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import EmptyState from '../../components/EmptyState';
import GearFormModal, { type GearFormData } from './components/GearFormModal';
import GearCategoryManagerModal from './components/GearCategoryManagerModal';
import { createAdminGear, getGearCategories, getGears, patchGearStatus, updateAdminGear } from '../../api/gearApi';
import { getCloudinarySignature, saveCloudinaryAsset, uploadToCloudinary } from '../../api/uploadApi';

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
    imageUrl?: string;
    description?: string;
    pricePerDay: number;
    depositAmount: number;
    totalQty: number;
    availableQty: number;
    status: 'ACTIVE' | 'INACTIVE';
    updatedAt: string;
};

type ApiGearLike = {
    id?: unknown;
    gearId?: unknown;
    name?: unknown;
    category?: unknown;
    categoryName?: unknown;
    imageUrl?: unknown;
    primaryImageUrl?: unknown;
    description?: unknown;
    pricePerDay?: unknown;
    dailyRentPrice?: unknown;
    depositAmount?: unknown;
    stock?: unknown;
    totalQty?: unknown;
    totalQuantity?: unknown;
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

const formatGearUpdatedDate = (updatedAt: string): string =>
    new Date(updatedAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });

const formatCurrency = (value: number): string => value.toLocaleString();

const getAvailabilityPercent = (availableQty: number, totalQty: number): number => {
    if (totalQty <= 0) {
        return 0;
    }
    return Math.min(100, Math.max(0, (availableQty / totalQty) * 100));
};

const getAvailabilityColor = (availableQty: number, totalQty: number): 'error' | 'warning' | 'success' => {
    if (totalQty <= 0 || availableQty <= 0) {
        return 'error';
    }

    const ratio = availableQty / totalQty;
    if (ratio <= 0.3) {
        return 'warning';
    }

    return 'success';
};

const mapGearFromApi = (raw: ApiGearLike): GearCardItem => ({
    id: String(raw.gearId ?? raw.id ?? ''),
    name: String(raw.name ?? 'Unnamed Gear'),
    category: String(raw.category ?? raw.categoryName ?? 'Uncategorized'),
    imageUrl: raw.imageUrl ? String(raw.imageUrl) : (raw.primaryImageUrl ? String(raw.primaryImageUrl) : undefined),
    description: raw.description ? String(raw.description) : '',
    pricePerDay: Number(raw.pricePerDay ?? raw.dailyRentPrice ?? 0),
    depositAmount: Number(raw.depositAmount ?? 0),
    totalQty: Number(raw.totalQty ?? raw.totalQuantity ?? raw.stock ?? raw.availableQty ?? raw.availableQuantity ?? raw.quantity ?? 0),
    availableQty: Number(raw.availableQty ?? raw.availableQuantity ?? raw.stock ?? raw.quantity ?? 0),
    status: toStatus(raw.status ?? (raw.isActive ? 'ACTIVE' : 'INACTIVE')),
    updatedAt: String(raw.updatedAt ?? raw.updated_at ?? new Date().toISOString()),
});

const GearListPage = () => {
    const { enqueueSnackbar } = useSnackbar();
    const [gears, setGears] = useState<GearCardItem[]>([]);
    const [search, setSearch] = useState('');
    const [categoryFilter, setCategoryFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(12);
    const [totalElements, setTotalElements] = useState(0);
    const [fetchLoading, setFetchLoading] = useState(false);
    const [fetchError, setFetchError] = useState<string | null>(null);
    const [openGearModal, setOpenGearModal] = useState(false);
    const [openCategoryModal, setOpenCategoryModal] = useState(false);
    const [savingGear, setSavingGear] = useState(false);
    const [statusUpdatingId, setStatusUpdatingId] = useState<string | null>(null);
    const [editingGearId, setEditingGearId] = useState<string | null>(null);
    const [selectedGearId, setSelectedGearId] = useState<string | null>(null);

    const { data: categoryOptions = [] } = useQuery({
        queryKey: ['gear-category-options'],
        queryFn: async () => {
            const response = await getGearCategories();
            return response.data;
        },
    });

    const isOutOfStock = (availableQty: number) => availableQty === 0;

    const categoryFilterOptions = useMemo(
        () => [...new Set(categoryOptions.map((cat) => cat.name))].sort((a, b) => a.localeCompare(b)),
        [categoryOptions]
    );

    const filtered = gears.filter((g) => {
        const matchSearch = g.name.toLowerCase().includes(search.toLowerCase());
        const matchCat = !categoryFilter || g.category === categoryFilter;
        const matchStatus = !statusFilter || g.status === statusFilter;
        return matchSearch && matchCat && matchStatus;
    });

    const paginated = filtered;
    const filteredOutOfStockCount = filtered.filter((g) => isOutOfStock(g.availableQty)).length;
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

    const selectedGear = useMemo(
        () => gears.find((gear) => gear.id === selectedGearId) ?? null,
        [gears, selectedGearId]
    );

    const gearFormInitialData: Partial<GearFormData> | undefined = editingGear
        ? {
            name: editingGear.name,
            categoryId: String(categoryOptions.find((c) => c.name === editingGear.category)?.id ?? ''),
            description: editingGear.description ?? '',
            availableCount: String(editingGear.availableQty),
            dailyRentPrice: String(editingGear.pricePerDay),
            depositAmount: String(editingGear.depositAmount ?? 0),
            isActive: editingGear.status === 'ACTIVE',
            primaryImageUrl: editingGear.imageUrl ?? '',
            imageFile: null,
        }
        : undefined;

    const handleToggleStatus = async (id: string) => {
        const currentGear = gears.find((g) => g.id === id);
        if (!currentGear) {
            return;
        }

        const nextStatus: 'ACTIVE' | 'INACTIVE' = currentGear.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';

        setStatusUpdatingId(id);
        setFetchError(null);
        setGears((prev) => prev.map((g) => (g.id === id ? { ...g, status: nextStatus } : g)));

        try {
            await patchGearStatus(id, nextStatus);
        } catch {
            setGears((prev) => prev.map((g) => (g.id === id ? { ...g, status: currentGear.status } : g)));
            setFetchError('Failed to update gear status. Please try again.');
        } finally {
            setStatusUpdatingId(null);
        }
    };

    const handleSaveGear = async (formData: GearFormData) => {
        setSavingGear(true);
        try {
            const categoryId = Number(formData.categoryId);

            if (!Number.isFinite(categoryId) || categoryId <= 0) {
                throw new Error('Please select a valid category.');
            }

            if (editingGearId) {
                const updateResponse = await updateAdminGear(editingGearId, {
                    name: formData.name,
                    categoryId,
                    description: formData.description,
                    dailyRentPrice: Number(formData.dailyRentPrice),
                    depositAmount: Number(formData.depositAmount || 0),
                    availableCount: Number(formData.availableCount),
                    active: formData.isActive,
                });

                let updated = mapGearFromApi(updateResponse.data as unknown as ApiGearLike);

                if (formData.imageFile) {
                    const signature = await getCloudinarySignature({
                        folderType: 'gears',
                        entityId: Number(editingGearId),
                    });
                    const uploadResult = await uploadToCloudinary(formData.imageFile, signature);
                    await saveCloudinaryAsset({
                        folderType: 'gears',
                        entityId: Number(editingGearId),
                        secureUrl: uploadResult.secure_url,
                        publicId: uploadResult.public_id,
                        primaryImage: true,
                        sortOrder: 0,
                    });
                    updated = { ...updated, imageUrl: uploadResult.secure_url };
                }

                setGears((prev) => prev.map((gear) => (gear.id === editingGearId ? updated : gear)));
                enqueueSnackbar('Gear updated successfully.', { variant: 'success' });
            } else {
                const createResponse = await createAdminGear({
                    name: formData.name,
                    categoryId,
                    description: formData.description,
                    dailyRentPrice: Number(formData.dailyRentPrice),
                    depositAmount: Number(formData.depositAmount || 0),
                    availableCount: Number(formData.availableCount),
                    active: formData.isActive,
                });

                const created = mapGearFromApi(createResponse.data as unknown as ApiGearLike);
                let createdWithImage = created;

                const createdData = createResponse.data as { gearId?: string | number };
                if (formData.imageFile && createdData.gearId) {
                    const gearId = Number(createdData.gearId);
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
                    createdWithImage = { ...created, imageUrl: uploadResult.secure_url };
                }

                setGears((prev) => [createdWithImage, ...prev]);
            }

            setOpenGearModal(false);
            setEditingGearId(null);
        } catch (error) {
            if (axios.isAxiosError(error)) {
                const message = (error.response?.data as { message?: string } | undefined)?.message;
                throw new Error(message || 'Failed to save gear. Please check your inputs and try again.');
            }
            throw error;
        } finally {
            setSavingGear(false);
        }
    };

    const clearFilters = () => {
        setSearch('');
        setCategoryFilter('');
        setStatusFilter('');
        setPage(0);
    };

    const hasActiveFilters = Boolean(search || categoryFilter || statusFilter);

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
                                Review global inventory levels, then perform quick row-level actions.
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
                            title={search || categoryFilter || statusFilter ? 'No matching gear found' : 'No gear items yet'}
                            description={
                                search || categoryFilter || statusFilter
                                    ? 'Try adjusting your search or filters to find what you\'re looking for.'
                                    : 'Get started by adding your first rental gear item.'
                            }
                            action={!search && !categoryFilter && !statusFilter ? {
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
                                onClick={() => setSelectedGearId(gear.id)}
                                sx={{
                                    borderColor: isOutOfStock(gear.availableQty)
                                        ? alpha('#DC2626', 0.28)
                                        : alpha('#1F4D3A', 0.12),
                                    boxShadow: 'none',
                                    overflow: 'hidden',
                                    cursor: 'pointer',
                                    transition: 'transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease',
                                    '&:hover': {
                                        transform: 'translateY(-2px)',
                                        boxShadow: `0 8px 22px ${alpha('#0F172A', 0.08)}`,
                                        borderColor: alpha('#1F4D3A', 0.28),
                                    },
                                }}
                            >
                                <CardContent sx={{ p: 1.4, '&:last-child': { pb: 1.4 } }}>
                                    <Box display="flex" gap={1.2}>
                                        <Box
                                            sx={{
                                                width: 88,
                                                aspectRatio: '1 / 1',
                                                borderRadius: 1.4,
                                                overflow: 'hidden',
                                                flexShrink: 0,
                                                bgcolor: alpha('#1F4D3A', 0.07),
                                                border: `1px solid ${alpha('#1F4D3A', 0.12)}`,
                                            }}
                                        >
                                            {gear.imageUrl ? (
                                                <Box
                                                    component="img"
                                                    src={gear.imageUrl}
                                                    alt={gear.name}
                                                    sx={{ width: '100%', height: '100%', objectFit: 'cover' }}
                                                />
                                            ) : (
                                                <Box sx={{ width: '100%', height: '100%', display: 'grid', placeItems: 'center' }}>
                                                    <Avatar
                                                        variant="rounded"
                                                        sx={{
                                                            width: 42,
                                                            height: 42,
                                                            borderRadius: 1.5,
                                                            bgcolor: alpha('#1F4D3A', 0.12),
                                                        }}
                                                    >
                                                        <ImageNotSupportedIcon sx={{ color: 'text.secondary', fontSize: 22 }} />
                                                    </Avatar>
                                                </Box>
                                            )}
                                        </Box>

                                        <Box minWidth={0} flex={1}>
                                            <Box display="flex" alignItems="flex-start" justifyContent="space-between" gap={0.8}>
                                                <Box minWidth={0}>
                                                    <Typography variant="body1" fontWeight={800} noWrap>{gear.name}</Typography>
                                                    <Typography variant="caption" color="text.secondary" noWrap>{gear.category}</Typography>
                                                </Box>
                                                <Tooltip title={gear.status === 'ACTIVE' ? 'Deactivate gear' : 'Activate gear'} arrow>
                                                    <PremiumSwitch
                                                        checked={gear.status === 'ACTIVE'}
                                                        disabled={statusUpdatingId === gear.id}
                                                        onClick={(e) => e.stopPropagation()}
                                                        onChange={() => handleToggleStatus(gear.id)}
                                                    />
                                                </Tooltip>
                                            </Box>

                                            <Stack direction="row" spacing={0.8} mt={0.7} useFlexGap flexWrap="wrap">
                                                <StatusBadge status={gear.status} />
                                                <Chip
                                                    label={`${gear.availableQty}/${gear.totalQty} in stock`}
                                                    size="small"
                                                    color={isOutOfStock(gear.availableQty) ? 'error' : 'default'}
                                                    variant="outlined"
                                                    sx={{ borderRadius: 1, fontWeight: 600 }}
                                                />
                                            </Stack>

                                            <Box display="flex" alignItems="center" justifyContent="space-between" mt={1}>
                                                <Typography variant="body2" fontWeight={700} color="primary.main">
                                                    LKR {formatCurrency(gear.pricePerDay)}
                                                </Typography>
                                                <Typography variant="caption" color="text.secondary">
                                                    Updated {formatGearUpdatedDate(gear.updatedAt)}
                                                </Typography>
                                            </Box>
                                        </Box>
                                    </Box>
                                </CardContent>
                            </Card>
                        ))}
                    </Box>
                )}

                <TablePagination
                    component="div"
                    count={totalElements}
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
                initialData={gearFormInitialData}
                loading={savingGear}
            />

            <GearCategoryManagerModal
                open={openCategoryModal}
                onClose={() => setOpenCategoryModal(false)}
            />

            <Dialog
                open={Boolean(selectedGear)}
                onClose={() => setSelectedGearId(null)}
                maxWidth="sm"
                fullWidth
            >
                {selectedGear && (
                    <>
                        <DialogTitle sx={{ fontWeight: 800 }}>Gear Details</DialogTitle>
                        <DialogContent sx={{ pt: 1 }}>
                            <Box
                                sx={{
                                    width: '100%',
                                    height: 190,
                                    borderRadius: 2,
                                    overflow: 'hidden',
                                    bgcolor: alpha('#1F4D3A', 0.06),
                                    mb: 2,
                                }}
                            >
                                {selectedGear.imageUrl ? (
                                    <Box
                                        component="img"
                                        src={selectedGear.imageUrl}
                                        alt={selectedGear.name}
                                        sx={{ width: '100%', height: '100%', objectFit: 'cover' }}
                                    />
                                ) : (
                                    <Box sx={{ width: '100%', height: '100%', display: 'grid', placeItems: 'center' }}>
                                        <ImageNotSupportedIcon sx={{ color: 'text.secondary', fontSize: 36 }} />
                                    </Box>
                                )}
                            </Box>

                            <Stack spacing={1.4}>
                                <Box>
                                    <Typography variant="h6" fontWeight={800}>{selectedGear.name}</Typography>
                                    <Stack direction="row" spacing={1} mt={0.8}>
                                        <Chip label={selectedGear.category} size="small" variant="outlined" />
                                        <StatusBadge status={selectedGear.status} />
                                    </Stack>
                                </Box>

                                <Box
                                    sx={{
                                        display: 'grid',
                                        gridTemplateColumns: { xs: '1fr', sm: 'repeat(3, minmax(0, 1fr))' },
                                        gap: 1,
                                    }}
                                >
                                    <Box sx={{ p: 1.2, borderRadius: 1.5, bgcolor: alpha('#1F4D3A', 0.05) }}>
                                        <Typography variant="caption" color="text.secondary">Daily Rental Price</Typography>
                                        <Typography fontWeight={800} color="primary.main">LKR {formatCurrency(selectedGear.pricePerDay)}</Typography>
                                    </Box>
                                    <Box sx={{ p: 1.2, borderRadius: 1.5, bgcolor: alpha('#166534', 0.06) }}>
                                        <Typography variant="caption" color="text.secondary">Deposit Amount</Typography>
                                        <Typography fontWeight={800}>LKR {formatCurrency(selectedGear.depositAmount)}</Typography>
                                    </Box>
                                    <Box sx={{ p: 1.2, borderRadius: 1.5, bgcolor: alpha('#0F172A', 0.04) }}>
                                        <Typography variant="caption" color="text.secondary">Stock Overview</Typography>
                                        <Typography fontWeight={800}>{selectedGear.availableQty} / {selectedGear.totalQty}</Typography>
                                    </Box>
                                </Box>

                                <Box sx={{ p: 1.2, borderRadius: 1.5, bgcolor: alpha('#0F172A', 0.03) }}>
                                    <Typography variant="caption" color="text.secondary">Description</Typography>
                                    <Typography variant="body2" sx={{ mt: 0.5 }}>
                                        {selectedGear.description?.trim() ? selectedGear.description : 'No description available.'}
                                    </Typography>
                                </Box>

                                <Box>
                                    <Box display="flex" justifyContent="space-between" mb={0.6}>
                                        <Typography variant="caption" color="text.secondary">Availability</Typography>
                                        <Typography variant="caption" fontWeight={700} color="text.secondary">
                                            {Math.round(getAvailabilityPercent(selectedGear.availableQty, selectedGear.totalQty))}%
                                        </Typography>
                                    </Box>
                                    <LinearProgress
                                        variant="determinate"
                                        value={getAvailabilityPercent(selectedGear.availableQty, selectedGear.totalQty)}
                                        color={getAvailabilityColor(selectedGear.availableQty, selectedGear.totalQty)}
                                        sx={{ height: 8, borderRadius: 99 }}
                                    />
                                </Box>

                                <Typography variant="caption" color="text.secondary">
                                    Last Updated: {formatGearUpdatedDate(selectedGear.updatedAt)}
                                </Typography>
                            </Stack>
                        </DialogContent>
                        <DialogActions sx={{ px: 3, pb: 2 }}>
                            <Button onClick={() => setSelectedGearId(null)}>Close</Button>
                            <Button
                                variant="contained"
                                onClick={() => {
                                    setSelectedGearId(null);
                                    setEditingGearId(selectedGear.id);
                                    setOpenGearModal(true);
                                }}
                            >
                                Edit Gear
                            </Button>
                        </DialogActions>
                    </>
                )}
            </Dialog>
        </Box>
    );
};

export default GearListPage;
