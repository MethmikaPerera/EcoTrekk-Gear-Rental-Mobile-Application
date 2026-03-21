import { useState } from 'react';
import {
    Box, Card, Button,
    IconButton, Typography, Tooltip, Dialog, DialogTitle, DialogContent,
    DialogActions, TextField, Grid, Alert, Chip, InputAdornment, CardMedia,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import MapIcon from '@mui/icons-material/Map';
import PlaceIcon from '@mui/icons-material/Place';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import CloseIcon from '@mui/icons-material/Close';
import ImageIcon from '@mui/icons-material/Image';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import PageHeader from '../../components/PageHeader';
import ConfirmDialog from '../../components/ConfirmDialog';
import EmptyState from '../../components/EmptyState';
import { useSnackbar } from 'notistack';
import StoreIcon from '@mui/icons-material/Store';
import { AdminHub, createHub, deleteHub, fetchAdminHubs, updateHub } from '../../api/hubApi';
import { getCloudinarySignature, saveCloudinaryAsset, uploadToCloudinary } from '../../api/uploadApi';

const schema = z.object({
    name: z.string().min(2, 'Name required'),
    address: z.string().min(5, 'Address required'),
    phone: z.string().min(7, 'Valid phone required'),
    latitude: z.coerce.number().min(-90).max(90, 'Valid latitude (-90 to 90)'),
    longitude: z.coerce.number().min(-180).max(180, 'Valid longitude (-180 to 180)'),
    googleMapsUrl: z.string().url('Must be a valid URL'),
    imageUrl: z.string().optional(),
});
type HubFormData = z.infer<typeof schema>;

interface Hub extends HubFormData {
    id: number;
    active: boolean;
}

const toHubView = (hub: AdminHub): Hub => ({
    id: hub.id,
    name: hub.name,
    address: hub.address ?? '',
    phone: hub.phone ?? '',
    latitude: Number(hub.latitude),
    longitude: Number(hub.longitude),
    googleMapsUrl: hub.mapLocationUrl ?? '',
    imageUrl: hub.imageUrl ?? '',
    active: hub.active,
});

const HubListPage = () => {
    const { enqueueSnackbar } = useSnackbar();
    const queryClient = useQueryClient();
    const [formOpen, setFormOpen] = useState(false);
    const [editingHub, setEditingHub] = useState<Hub | null>(null);
    const [deleteTarget, setDeleteTarget] = useState<Hub | null>(null);
    const [mapHub, setMapHub] = useState<Hub | null>(null);
    const [saving, setSaving] = useState(false);
    const [search, setSearch] = useState('');
    const [selectedImageFile, setSelectedImageFile] = useState<File | null>(null);
    const [previewImageUrl, setPreviewImageUrl] = useState<string>('');
    const [imageRefreshToken, setImageRefreshToken] = useState<number>(Date.now());

    const { data: hubData = [], isLoading: isHubsLoading } = useQuery({
        queryKey: ['adminHubs'],
        queryFn: fetchAdminHubs,
    });

    const hubs = hubData.map(toHubView);

    const createHubMutation = useMutation({
        mutationFn: createHub,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['adminHubs'] });
        },
    });

    const updateHubMutation = useMutation({
        mutationFn: ({ id, payload }: { id: number; payload: Parameters<typeof updateHub>[1] }) => updateHub(id, payload),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['adminHubs'] });
        },
    });

    const deleteHubMutation = useMutation({
        mutationFn: deleteHub,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['adminHubs'] });
        },
    });

    const { register, handleSubmit, reset, formState: { errors }, setValue } = useForm<HubFormData>({
        resolver: zodResolver(schema) as any,
        defaultValues: { name: '', address: '', phone: '', latitude: 0, longitude: 0, googleMapsUrl: '', imageUrl: '' },
    });

    const openAdd = () => {
        reset({ name: '', address: '', phone: '', latitude: 0, longitude: 0, googleMapsUrl: '', imageUrl: '' });
        setSelectedImageFile(null);
        setPreviewImageUrl('');
        setEditingHub(null);
        setFormOpen(true);
    };

    const openEdit = (hub: Hub) => {
        reset(hub);
        setSelectedImageFile(null);
        setPreviewImageUrl(hub.imageUrl ?? '');
        setEditingHub(hub);
        setFormOpen(true);
    };

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            const file = e.target.files[0];
            setSelectedImageFile(file);
            const localUrl = URL.createObjectURL(file);
            setPreviewImageUrl(localUrl);
            setValue('imageUrl', localUrl);
        }
    };

    const onSubmit = async (data: HubFormData) => {
        try {
            setSaving(true);

            if (!data.googleMapsUrl?.trim()) {
                enqueueSnackbar('Google Maps URL is required.', { variant: 'error' });
                return;
            }

            if (!selectedImageFile && !editingHub?.imageUrl) {
                enqueueSnackbar('Hub cover image is required.', { variant: 'error' });
                return;
            }

            const fallbackImageUrl = selectedImageFile ? 'PENDING_UPLOAD' : '';

            const payload = {
                name: data.name,
                address: data.address,
                phone: data.phone,
                latitude: data.latitude,
                longitude: data.longitude,
                mapLocationUrl: data.googleMapsUrl.trim(),
                imageUrl: editingHub?.imageUrl ?? fallbackImageUrl,
                active: editingHub?.active ?? true,
            };

            if (editingHub) {
                await updateHubMutation.mutateAsync({ id: editingHub.id, payload });

                if (selectedImageFile) {
                    const signature = await getCloudinarySignature({
                        folderType: 'hubs',
                        entityId: editingHub.id,
                    });
                    const uploadResult = await uploadToCloudinary(selectedImageFile, signature);
                    await saveCloudinaryAsset({
                        folderType: 'hubs',
                        entityId: editingHub.id,
                        secureUrl: uploadResult.secure_url,
                        publicId: uploadResult.public_id,
                    });
                    await queryClient.invalidateQueries({ queryKey: ['adminHubs'] });
                    setImageRefreshToken(Date.now());
                }

                enqueueSnackbar(`"${data.name}" updated successfully.`, { variant: 'success' });
            } else {
                const createdHub = await createHubMutation.mutateAsync(payload);

                if (selectedImageFile) {
                    const signature = await getCloudinarySignature({
                        folderType: 'hubs',
                        entityId: createdHub.id,
                    });
                    const uploadResult = await uploadToCloudinary(selectedImageFile, signature);
                    await saveCloudinaryAsset({
                        folderType: 'hubs',
                        entityId: createdHub.id,
                        secureUrl: uploadResult.secure_url,
                        publicId: uploadResult.public_id,
                    });
                    await queryClient.invalidateQueries({ queryKey: ['adminHubs'] });
                    setImageRefreshToken(Date.now());
                }

                enqueueSnackbar(`Hub "${data.name}" added successfully.`, { variant: 'success' });
            }

            setFormOpen(false);
            setSelectedImageFile(null);
            setPreviewImageUrl('');
        } catch (error) {
            enqueueSnackbar('Failed to save hub. Please try again.', { variant: 'error' });
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async () => {
        if (!deleteTarget) return;
        try {
            await deleteHubMutation.mutateAsync(deleteTarget.id);
            enqueueSnackbar(`"${deleteTarget.name}" has been deleted.`, { variant: 'success' });
            setDeleteTarget(null);
        } catch (error) {
            enqueueSnackbar('Failed to delete hub. Please try again.', { variant: 'error' });
        }
    };

    const filteredHubs = hubs.filter(hub =>
        hub.name.toLowerCase().includes(search.toLowerCase()) ||
        hub.address.toLowerCase().includes(search.toLowerCase())
    );

    const getHubCardImageUrl = (imageUrl: string) => {
        const separator = imageUrl.includes('?') ? '&' : '?';
        return `${imageUrl}${separator}v=${imageRefreshToken}`;
    };

    return (
        <Box sx={{ pb: 4 }}>
            <PageHeader
                title="Rental Hubs"
                subtitle={isHubsLoading ? 'Loading hubs...' : `${hubs.length} active pickup/dropoff locations`}
                breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'Rental Hubs' }]}
                action={
                    <Button
                        variant="contained"
                        startIcon={<AddIcon />}
                        onClick={openAdd}
                        disabled={saving}
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
                            '&:disabled': {
                                opacity: 0.6,
                                cursor: 'not-allowed',
                            }
                        }}
                    >
                        {saving ? 'Saving...' : 'Add Hub'}
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
                        placeholder="Search by hub name or address..."
                        value={search}
                        autoFocus
                        onChange={(e) => setSearch(e.target.value)}
                        sx={{
                            minWidth: 320,
                            '& .MuiOutlinedInput-root': {
                                borderRadius: '12px',
                                backgroundColor: 'background.paper',
                                '&:hover fieldset': { borderColor: 'primary.main' }
                            }
                        }}
                        InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon color="action" /></InputAdornment> }}
                    />

                    {search && (
                        <Button variant="text" color="inherit" size="small" onClick={() => setSearch('')} sx={{ borderRadius: 2, fontWeight: 600 }}>
                            Clear Search
                        </Button>
                    )}

                    <Typography variant="body2" color="text.secondary" ml="auto" fontWeight={600} sx={{ py: 1, px: 2, bgcolor: 'rgba(0,0,0,0.03)', borderRadius: 2 }}>
                        {filteredHubs.length} {filteredHubs.length === 1 ? 'hub' : 'hubs'}
                    </Typography>
                </Box>
            </Card>

            <Grid container spacing={3}>
                {filteredHubs.length === 0 ? (
                    <Grid size={{ xs: 12 }}>
                        <Card sx={{ borderRadius: 2, p: 4, background: 'rgba(255, 255, 255, 0.8)', backdropFilter: 'blur(12px)', border: '1px solid rgba(0,0,0,0.04)', boxShadow: 'none' }}>
                            <EmptyState
                                icon={<StoreIcon sx={{ fontSize: 48, color: 'primary.light' }} />}
                                title={search ? "No hubs match your search" : "No hubs yet"}
                                description={search ? "Try different keywords or clear the search." : "Add your first rental hub to get started."}
                                action={!search ? { label: 'Add First Hub', onClick: openAdd } : undefined}
                            />
                        </Card>
                    </Grid>
                ) : filteredHubs.map((hub, idx) => (
                    <Grid size={{ xs: 12, sm: 6, md: 6, lg: 4, xl: 3 }} key={hub.id}>
                        <Card
                            sx={{
                                height: '100%',
                                display: 'flex',
                                flexDirection: 'column',
                                borderRadius: 3,
                                background: 'rgba(255, 255, 255, 0.8)',
                                backdropFilter: 'blur(12px)',
                                border: '1px solid rgba(0,0,0,0.04)',
                                boxShadow: '0 8px 32px rgba(0,0,0,0.03)',
                                transition: 'all 0.3s ease',
                                '&:hover': {
                                    transform: 'translateY(-4px)',
                                    boxShadow: '0 12px 40px rgba(0,0,0,0.08)',
                                    borderColor: 'rgba(31, 77, 58, 0.15)',
                                }
                            }}
                        >
                            {hub.imageUrl ? (
                                <CardMedia
                                    component="img"
                                    height="160"
                                    image={getHubCardImageUrl(hub.imageUrl)}
                                    alt={hub.name}
                                />
                            ) : (
                                <Box sx={{ height: 160, background: 'linear-gradient(135deg, #1A2E2A 0%, #2E7D5B 100%)' }} />
                            )}
                            <Box sx={{ p: 3, flexGrow: 1 }}>
                                <Box display="flex" alignItems="center" gap={2} mb={2}>
                                    <Box>
                                        <Typography variant="h6" fontWeight={800} color="text.primary" sx={{ lineHeight: 1.2 }}>
                                            {hub.name}
                                        </Typography>
                                    </Box>
                                </Box>

                                <Box display="flex" flexDirection="column" gap={1.5}>
                                    <Box display="flex" alignItems="flex-start" gap={1.5}>
                                        <PlaceIcon sx={{ color: 'text.secondary', fontSize: 20, mt: 0.2 }} />
                                        <Typography variant="body2" fontWeight={500} color="text.secondary" sx={{ lineHeight: 1.5 }}>
                                            {hub.address}
                                        </Typography>
                                    </Box>

                                    <Box display="flex" alignItems="center" gap={1.5}>
                                        <Typography variant="body2" fontWeight={700} color="text.primary">
                                            📞 {hub.phone}
                                        </Typography>
                                    </Box>

                                    <Box mt={1}>
                                        <Chip
                                            icon={<PlaceIcon />}
                                            label={`${hub.latitude.toFixed(4)}, ${hub.longitude.toFixed(4)}`}
                                            size="small"
                                            sx={{
                                                fontFamily: 'monospace',
                                                fontSize: '0.75rem',
                                                fontWeight: 600,
                                                borderRadius: '8px',
                                                backgroundColor: 'rgba(26, 46, 42, 0.06)',
                                                color: '#1A2E2A',
                                                px: 0.5,
                                                '& .MuiChip-icon': { color: 'inherit', fontSize: '1rem' }
                                            }}
                                        />
                                    </Box>
                                </Box>
                            </Box>

                            <Box
                                sx={{
                                    p: 2,
                                    borderTop: '1px solid rgba(0,0,0,0.06)',
                                    display: 'flex',
                                    justifyContent: 'space-evenly',
                                    bgcolor: 'rgba(255,255,255,0.5)'
                                }}
                            >
                                <Tooltip title="View on Map" placement="top" arrow>
                                    <IconButton
                                        onClick={() => setMapHub(hub)}
                                        sx={{
                                            color: 'info.main',
                                            bgcolor: 'rgba(2, 119, 189, 0.05)',
                                            '&:hover': { bgcolor: 'info.main', color: 'white', transform: 'scale(1.1)' },
                                            transition: 'all 0.2s',
                                        }}
                                    >
                                        <MapIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                                <Tooltip title="Edit Hub" placement="top" arrow>
                                    <IconButton
                                        onClick={() => openEdit(hub)}
                                        sx={{
                                            color: 'primary.main',
                                            bgcolor: 'rgba(31, 77, 58, 0.05)',
                                            '&:hover': { bgcolor: 'primary.main', color: 'white', transform: 'scale(1.1)' },
                                            transition: 'all 0.2s',
                                        }}
                                    >
                                        <EditIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                                <Tooltip title="Delete Hub" placement="top" arrow>
                                    <IconButton
                                        onClick={() => setDeleteTarget(hub)}
                                        sx={{
                                            color: 'error.main',
                                            bgcolor: 'rgba(211, 47, 47, 0.05)',
                                            '&:hover': { bgcolor: 'error.main', color: 'white', transform: 'scale(1.1)' },
                                            transition: 'all 0.2s',
                                        }}
                                    >
                                        <DeleteIcon fontSize="small" />
                                    </IconButton>
                                </Tooltip>
                            </Box>
                        </Card>
                    </Grid>
                ))}
            </Grid>

            {/* Add/Edit Dialog */}
            <Dialog open={formOpen} onClose={() => setFormOpen(false)} maxWidth="sm" fullWidth>
                <DialogTitle fontWeight={700}>{editingHub ? 'Edit Hub' : 'Add New Hub'}</DialogTitle>
                <Box component="form" onSubmit={handleSubmit(onSubmit as any)}>
                    <DialogContent>
                        <Grid container spacing={2}>
                            <Grid size={{ xs: 12 }}>
                                <TextField {...register('name')} label="Hub Name *" fullWidth error={!!errors.name} helperText={errors.name?.message} />
                            </Grid>
                            <Grid size={{ xs: 12 }}>
                                <TextField {...register('address')} label="Full Address *" fullWidth error={!!errors.address} helperText={errors.address?.message} />
                            </Grid>
                            <Grid size={{ xs: 12 }}>
                                <TextField {...register('phone')} label="Phone Number *" fullWidth error={!!errors.phone} helperText={errors.phone?.message} />
                            </Grid>
                            <Grid size={{ xs: 6 }}>
                                <TextField {...register('latitude')} label="Latitude *" type="number" fullWidth error={!!errors.latitude} helperText={errors.latitude?.message} inputProps={{ step: 'any' }} />
                            </Grid>
                            <Grid size={{ xs: 6 }}>
                                <TextField {...register('longitude')} label="Longitude *" type="number" fullWidth error={!!errors.longitude} helperText={errors.longitude?.message} inputProps={{ step: 'any' }} />
                            </Grid>
                            <Grid size={{ xs: 12 }}>
                                <TextField {...register('googleMapsUrl')} label="Google Maps URL *" fullWidth error={!!errors.googleMapsUrl} helperText={errors.googleMapsUrl?.message} />
                            </Grid>
                            <Grid size={{ xs: 12 }}>
                                <Typography variant="subtitle2" color="text.secondary" fontWeight={600} mb={1}>
                                    Hub Cover Image
                                </Typography>
                                <Box
                                    component="label"
                                    sx={{
                                        display: 'flex',
                                        flexDirection: 'column',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        width: '100%',
                                        height: 180,
                                        border: '2px dashed rgba(31, 77, 58, 0.3)',
                                        borderRadius: 3,
                                        bgcolor: 'rgba(31, 77, 58, 0.02)',
                                        cursor: 'pointer',
                                        transition: 'all 0.3s ease',
                                        overflow: 'hidden',
                                        position: 'relative',
                                        '&:hover': {
                                            borderColor: 'primary.main',
                                            bgcolor: 'rgba(31, 77, 58, 0.06)',
                                        }
                                    }}
                                >
                                    <input
                                        type="file"
                                        hidden
                                        accept="image/*"
                                        onChange={handleImageChange}
                                    />
                                    {previewImageUrl ? (
                                        <>
                                            <Box
                                                component="img"
                                                src={previewImageUrl}
                                                alt="Hub Preview"
                                                sx={{
                                                    width: '100%',
                                                    height: '100%',
                                                    objectFit: 'cover',
                                                }}
                                            />
                                            <Box
                                                sx={{
                                                    position: 'absolute',
                                                    inset: 0,
                                                    bgcolor: 'rgba(0,0,0,0.4)',
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    justifyContent: 'center',
                                                    opacity: 0,
                                                    transition: 'opacity 0.2s',
                                                    '&:hover': { opacity: 1 }
                                                }}
                                            >
                                                <Button
                                                    variant="contained"
                                                    startIcon={<CloudUploadIcon />}
                                                    sx={{ textTransform: 'none', borderRadius: 2, bgcolor: 'rgba(255,255,255,0.9)', color: 'text.primary', '&:hover': { bgcolor: '#fff' } }}
                                                    component="span" // Prevent button from swallowing label click
                                                >
                                                    Change Image
                                                </Button>
                                            </Box>
                                        </>
                                    ) : (
                                        <>
                                            <Box sx={{ p: 2, borderRadius: '50%', bgcolor: 'rgba(31, 77, 58, 0.08)', mb: 2 }}>
                                                <ImageIcon sx={{ fontSize: 32, color: 'primary.main', opacity: 0.8 }} />
                                            </Box>
                                            <Typography variant="body1" fontWeight={600} color="text.primary" gutterBottom>
                                                Click to upload image
                                            </Typography>
                                            <Typography variant="caption" color="text.secondary">
                                                SVG, PNG, JPG or GIF (max. 5MB)
                                            </Typography>
                                        </>
                                    )}
                                </Box>
                                {selectedImageFile && (
                                    <Box display="flex" justifyContent="space-between" alignItems="center" mt={1}>
                                       <Typography variant="caption" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                            <ImageIcon fontSize="inherit" />
                                            {selectedImageFile.name}
                                       </Typography>
                                       <IconButton 
                                            size="small" 
                                            onClick={(e) => {
                                                e.preventDefault();
                                                setSelectedImageFile(null);
                                                setPreviewImageUrl('');
                                                setValue('imageUrl', '');
                                            }}
                                            sx={{ color: 'error.main', p: 0.5 }}
                                        >
                                           <CloseIcon fontSize="small" />
                                       </IconButton>
                                    </Box>
                                )}
                                {errors.imageUrl && (
                                    <Typography variant="caption" color="error" sx={{ mt: 1, display: 'block' }}>
                                        {errors.imageUrl.message as string}
                                    </Typography>
                                )}
                            </Grid>
                            <Grid size={{ xs: 12 }}>
                                <Alert severity="info" sx={{ borderRadius: 2 }}>
                                    Use Google Maps to find precise coordinates. Right-click a location → "What's here?"
                                </Alert>
                            </Grid>
                        </Grid>
                    </DialogContent>
                    <DialogActions sx={{ px: 3, pb: 2 }}>
                        <Button onClick={() => setFormOpen(false)} variant="outlined" color="inherit">Cancel</Button>
                        <Button type="submit" variant="contained" disabled={saving}>
                            {saving ? 'Saving...' : editingHub ? 'Update Hub' : 'Add Hub'}
                        </Button>
                    </DialogActions>
                </Box>
            </Dialog>

            {/* Map Preview Dialog */}
            <Dialog open={!!mapHub} onClose={() => setMapHub(null)} maxWidth="md" fullWidth>
                <DialogTitle fontWeight={700}>
                    <Box display="flex" alignItems="center" gap={1}>
                        <PlaceIcon color="primary" />
                        {mapHub?.name}
                    </Box>
                </DialogTitle>
                <DialogContent sx={{ p: 0 }}>
                    {mapHub && (
                        <Box sx={{ height: 400, overflow: 'hidden' }}>
                            <iframe
                                title={mapHub.name}
                                width="100%"
                                height="100%"
                                style={{ border: 0 }}
                                loading="lazy"
                                src={`https://maps.google.com/maps?q=${mapHub.latitude},${mapHub.longitude}&z=15&output=embed`}
                            />
                        </Box>
                    )}
                </DialogContent>
                <DialogActions sx={{ px: 2, py: 1.5 }}>
                    <Button
                        href={mapHub ? `https://maps.google.com/?q=${mapHub.latitude},${mapHub.longitude}` : '#'}
                        target="_blank"
                        variant="outlined"
                        startIcon={<MapIcon />}
                    >
                        Open in Google Maps
                    </Button>
                    <Button onClick={() => setMapHub(null)} variant="contained">Close</Button>
                </DialogActions>
            </Dialog>

            <ConfirmDialog
                open={!!deleteTarget}
                title="Delete Hub"
                message={`Are you sure you want to delete "${deleteTarget?.name}"? Active orders at this hub may be affected.`}
                confirmLabel="Delete Hub"
                onConfirm={handleDelete}
                onCancel={() => setDeleteTarget(null)}
            />
        </Box>
    );
};

export default HubListPage;
