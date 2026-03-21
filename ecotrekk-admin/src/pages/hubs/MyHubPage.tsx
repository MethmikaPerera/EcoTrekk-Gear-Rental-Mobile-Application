import { ChangeEvent, useState } from 'react';
import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    CardMedia,
    Chip,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Grid,
    IconButton,
    InputAdornment,
    TextField,
    Tooltip,
    Typography,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import StorefrontIcon from '@mui/icons-material/Storefront';
import PlaceIcon from '@mui/icons-material/Place';
import PhoneIcon from '@mui/icons-material/Phone';
import MapIcon from '@mui/icons-material/Map';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import ImageIcon from '@mui/icons-material/Image';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import SearchIcon from '@mui/icons-material/Search';
import DomainDisabledIcon from '@mui/icons-material/DomainDisabled';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useSnackbar } from 'notistack';
import PageHeader from '../../components/PageHeader';
import EmptyState from '../../components/EmptyState';
import { fetchMyAssignedHub, updateMyAssignedHub } from '../../api/hubApi';
import { getCloudinarySignature, saveCloudinaryAsset, uploadToCloudinary } from '../../api/uploadApi';

const schema = z.object({
    name: z.string().min(2, 'Hub name is required'),
    address: z.string().min(5, 'Address is required'),
    phone: z.string().min(7, 'Valid phone is required'),
    latitude: z.coerce.number().min(-90, 'Latitude must be between -90 and 90').max(90, 'Latitude must be between -90 and 90'),
    longitude: z.coerce.number().min(-180, 'Longitude must be between -180 and 180').max(180, 'Longitude must be between -180 and 180'),
    mapLocationUrl: z.string().url('Enter a valid URL'),
    imageUrl: z.string().optional(),
});

type HubFormData = z.infer<typeof schema>;

const MyHubPage = () => {
    const { enqueueSnackbar } = useSnackbar();
    const queryClient = useQueryClient();
    const [editOpen, setEditOpen] = useState(false);
    const [saving, setSaving] = useState(false);
    const [selectedImageFile, setSelectedImageFile] = useState<File | null>(null);
    const [previewImageUrl, setPreviewImageUrl] = useState('');

    const { data: assignedHub, isLoading: isHubLoading, isError: isHubError, error: hubError } = useQuery({
        queryKey: ['myAssignedHub'],
        queryFn: fetchMyAssignedHub,
        retry: false,
    });

    const updateHubMutation = useMutation({
        mutationFn: updateMyAssignedHub,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['myAssignedHub'] });
        },
    });

    const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<HubFormData>({
        resolver: zodResolver(schema) as any,
        defaultValues: {
            name: '',
            address: '',
            phone: '',
            latitude: 0,
            longitude: 0,
            mapLocationUrl: '',
            imageUrl: '',
        },
    });

    const openEdit = () => {
        if (!assignedHub) return;
        reset({
            name: assignedHub.name,
            address: assignedHub.address,
            phone: assignedHub.phone ?? '',
            latitude: assignedHub.latitude,
            longitude: assignedHub.longitude,
            mapLocationUrl: assignedHub.mapLocationUrl ?? '',
            imageUrl: assignedHub.imageUrl ?? '',
        });
        setSelectedImageFile(null);
        setPreviewImageUrl(assignedHub.imageUrl ?? '');
        setEditOpen(true);
    };

    const handleImageChange = (e: ChangeEvent<HTMLInputElement>) => {
        if (!e.target.files || e.target.files.length === 0) return;
        const file = e.target.files[0];
        setSelectedImageFile(file);
        const localUrl = URL.createObjectURL(file);
        setPreviewImageUrl(localUrl);
        setValue('imageUrl', localUrl);
    };

    const onSubmit = async (formData: HubFormData) => {
        if (!assignedHub) return;

        try {
            setSaving(true);

            if (!formData.mapLocationUrl?.trim()) {
                enqueueSnackbar('Google Maps URL is required.', { variant: 'error' });
                return;
            }

            if (!selectedImageFile && !assignedHub.imageUrl) {
                enqueueSnackbar('Hub cover image is required.', { variant: 'error' });
                return;
            }

            const fallbackImageUrl = selectedImageFile ? 'PENDING_UPLOAD' : '';

            await updateHubMutation.mutateAsync({
                name: formData.name,
                address: formData.address,
                phone: formData.phone,
                latitude: formData.latitude,
                longitude: formData.longitude,
                mapLocationUrl: formData.mapLocationUrl.trim(),
                imageUrl: assignedHub.imageUrl || fallbackImageUrl,
            });

            if (selectedImageFile) {
                const signature = await getCloudinarySignature({
                    folderType: 'hubs',
                    entityId: assignedHub.id,
                });

                const uploadResult = await uploadToCloudinary(selectedImageFile, signature);
                await saveCloudinaryAsset({
                    folderType: 'hubs',
                    entityId: assignedHub.id,
                    secureUrl: uploadResult.secure_url,
                    publicId: uploadResult.public_id,
                });
                queryClient.invalidateQueries({ queryKey: ['myAssignedHub'] });
            }

            enqueueSnackbar('Hub details updated successfully.', { variant: 'success' });
            setEditOpen(false);
            setSelectedImageFile(null);
            setPreviewImageUrl('');
        } catch {
            enqueueSnackbar('Failed to update hub details. Please try again.', { variant: 'error' });
        } finally {
            setSaving(false);
        }
    };

    if (isHubLoading) {
        return (
            <Box sx={{ minHeight: '50vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <CircularProgress color="primary" />
            </Box>
        );
    }

    if (isHubError) {
        const axiosError = hubError as AxiosError<{ message?: string }>;
        const isNotFound = axiosError.response?.status === 404;
        return (
            <Box sx={{ pb: 4 }}>
                <PageHeader
                    title="My Hub"
                    subtitle="Hub details for your manager account"
                    breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'My Hub' }]}
                />

                <Card sx={{ borderRadius: 3, p: 4, border: '1px solid rgba(0,0,0,0.06)', boxShadow: '0 8px 32px rgba(0,0,0,0.03)' }}>
                    <EmptyState
                        icon={<DomainDisabledIcon sx={{ fontSize: 48, color: 'warning.main' }} />}
                        title={isNotFound ? 'No hub assigned' : 'Hub details unavailable'}
                        description={
                            isNotFound
                                ? 'Your account is not linked to a rental hub yet. Please contact a super admin.'
                                : 'Your assigned hub details could not be loaded right now. Please try again later.'
                        }
                    />
                </Card>
            </Box>
        );
    }

    if (!assignedHub) {
        return (
            <Box sx={{ pb: 4 }}>
                <PageHeader
                    title="My Hub"
                    subtitle="Hub details for your manager account"
                    breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'My Hub' }]}
                />

                <Card sx={{ borderRadius: 3, p: 4, border: '1px solid rgba(0,0,0,0.06)', boxShadow: '0 8px 32px rgba(0,0,0,0.03)' }}>
                    <EmptyState
                        icon={<DomainDisabledIcon sx={{ fontSize: 48, color: 'warning.main' }} />}
                        title="Hub details unavailable"
                        description="Your assigned hub details could not be loaded right now. Please try again later."
                    />
                </Card>
            </Box>
        );
    }

    return (
        <Box sx={{ pb: 4 }}>
            <PageHeader
                title="My Hub"
                subtitle="Manage your assigned rental hub profile"
                breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'My Hub' }]}
                action={
                    <Button
                        variant="contained"
                        startIcon={<EditIcon />}
                        onClick={openEdit}
                        sx={{
                            borderRadius: '12px',
                            px: 2.5,
                            py: 1,
                            fontWeight: 700,
                            textTransform: 'none',
                            background: 'linear-gradient(135deg, #1F4D3A 0%, #2E7D5B 100%)',
                            boxShadow: '0 6px 18px rgba(31,77,58,0.28)',
                        }}
                    >
                        Edit Hub
                    </Button>
                }
            />

            <Grid container spacing={3}>
                <Grid size={{ xs: 12, lg: 8 }}>
                    <Card sx={{ borderRadius: 4, overflow: 'hidden', boxShadow: '0 14px 42px rgba(0,0,0,0.07)', border: '1px solid rgba(0,0,0,0.05)' }}>
                        {assignedHub.imageUrl ? (
                            <CardMedia component="img" height="220" image={assignedHub.imageUrl} alt={assignedHub.name} />
                        ) : (
                            <Box sx={{ height: 220, background: 'linear-gradient(120deg, #1A2E2A 0%, #2E7D5B 70%)' }} />
                        )}
                        <CardContent sx={{ p: 3.2 }}>
                            <Box display="flex" justifyContent="space-between" alignItems="flex-start" gap={2} mb={2.5}>
                                <Box>
                                    <Typography variant="h4" fontWeight={900} color="text.primary" sx={{ lineHeight: 1.1 }}>
                                        {assignedHub.name}
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary" fontFamily="monospace" sx={{ mt: 0.6 }}>
                                        Hub ID: {assignedHub.id}
                                    </Typography>
                                </Box>
                                <Chip
                                    label={assignedHub.active ? 'Operational' : 'Inactive'}
                                    color={assignedHub.active ? 'success' : 'default'}
                                    sx={{ fontWeight: 800, borderRadius: 2 }}
                                />
                            </Box>

                            <Grid container spacing={2.2}>
                                <Grid size={{ xs: 12, md: 6 }}>
                                    <Card sx={{ borderRadius: 3, border: '1px solid rgba(0,0,0,0.06)', height: '100%' }}>
                                        <CardContent>
                                            <Box display="flex" alignItems="center" gap={1.2} mb={1}>
                                                <PlaceIcon color="primary" />
                                                <Typography fontWeight={800}>Address</Typography>
                                            </Box>
                                            <Typography variant="body2" color="text.secondary">{assignedHub.address}</Typography>
                                        </CardContent>
                                    </Card>
                                </Grid>

                                <Grid size={{ xs: 12, md: 6 }}>
                                    <Card sx={{ borderRadius: 3, border: '1px solid rgba(0,0,0,0.06)', height: '100%' }}>
                                        <CardContent>
                                            <Box display="flex" alignItems="center" gap={1.2} mb={1}>
                                                <PhoneIcon color="primary" />
                                                <Typography fontWeight={800}>Contact</Typography>
                                            </Box>
                                            <Typography variant="body2" color="text.secondary">
                                                {assignedHub.phone || 'N/A'}
                                            </Typography>
                                        </CardContent>
                                    </Card>
                                </Grid>

                                <Grid size={{ xs: 12 }}>
                                    <Card sx={{ borderRadius: 3, border: '1px solid rgba(0,0,0,0.06)' }}>
                                        <CardContent>
                                            <Box display="flex" alignItems="center" gap={1.2} mb={1.2}>
                                                <StorefrontIcon color="primary" />
                                                <Typography fontWeight={800}>Geo Position</Typography>
                                            </Box>
                                            <Typography variant="body2" color="text.secondary" fontFamily="monospace">
                                                {assignedHub.latitude.toFixed(6)}, {assignedHub.longitude.toFixed(6)}
                                            </Typography>
                                            <Box mt={2} display="flex" gap={1.2} flexWrap="wrap">
                                                <Button
                                                    variant="outlined"
                                                    startIcon={<MapIcon />}
                                                    href={assignedHub.mapLocationUrl || `https://maps.google.com/?q=${assignedHub.latitude},${assignedHub.longitude}`}
                                                    target="_blank"
                                                >
                                                    Open in Google Maps
                                                </Button>
                                                <Tooltip title="Copy coordinates">
                                                    <Button
                                                        variant="text"
                                                        color="inherit"
                                                        onClick={() => navigator.clipboard.writeText(`${assignedHub.latitude}, ${assignedHub.longitude}`)}
                                                        startIcon={<SearchIcon />}
                                                    >
                                                        Copy Coordinates
                                                    </Button>
                                                </Tooltip>
                                            </Box>
                                        </CardContent>
                                    </Card>
                                </Grid>
                            </Grid>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid size={{ xs: 12, lg: 4 }}>
                    <Card sx={{ borderRadius: 4, border: '1px solid rgba(0,0,0,0.06)', boxShadow: '0 10px 36px rgba(0,0,0,0.05)' }}>
                        <CardContent>
                            <Typography variant="h6" fontWeight={900} sx={{ mb: 1.2 }}>Hub Manager Actions</Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 2.5 }}>
                                Keep your hub listing accurate. Updated contact, map, and photo details are shown to users.
                            </Typography>
                            <Button fullWidth variant="contained" startIcon={<EditIcon />} onClick={openEdit} sx={{ borderRadius: 2, mb: 1.2 }}>
                                Update Hub Details
                            </Button>
                            <Button
                                fullWidth
                                variant="outlined"
                                startIcon={<MapIcon />}
                                href={assignedHub.mapLocationUrl || `https://maps.google.com/?q=${assignedHub.latitude},${assignedHub.longitude}`}
                                target="_blank"
                                sx={{ borderRadius: 2 }}
                            >
                                Verify Location
                            </Button>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            <Dialog open={editOpen} onClose={() => !saving && setEditOpen(false)} fullWidth maxWidth="md">
                <DialogTitle fontWeight={800}>Edit My Hub</DialogTitle>
                <Box component="form" onSubmit={handleSubmit(onSubmit as any)}>
                    <DialogContent>
                        <Grid container spacing={2}>
                            <Grid size={{ xs: 12, md: 6 }}>
                                <TextField {...register('name')} label="Hub Name *" fullWidth error={!!errors.name} helperText={errors.name?.message} />
                            </Grid>
                            <Grid size={{ xs: 12, md: 6 }}>
                                <TextField {...register('phone')} label="Phone *" fullWidth error={!!errors.phone} helperText={errors.phone?.message} />
                            </Grid>

                            <Grid size={{ xs: 12 }}>
                                <TextField {...register('address')} label="Address *" fullWidth error={!!errors.address} helperText={errors.address?.message} />
                            </Grid>

                            <Grid size={{ xs: 12, md: 6 }}>
                                <TextField {...register('latitude')} type="number" label="Latitude *" fullWidth inputProps={{ step: 'any' }} error={!!errors.latitude} helperText={errors.latitude?.message} />
                            </Grid>
                            <Grid size={{ xs: 12, md: 6 }}>
                                <TextField {...register('longitude')} type="number" label="Longitude *" fullWidth inputProps={{ step: 'any' }} error={!!errors.longitude} helperText={errors.longitude?.message} />
                            </Grid>

                            <Grid size={{ xs: 12 }}>
                                <TextField
                                    {...register('mapLocationUrl')}
                                    label="Google Maps URL *"
                                    fullWidth
                                    error={!!errors.mapLocationUrl}
                                    helperText={errors.mapLocationUrl?.message}
                                    InputProps={{ startAdornment: <InputAdornment position="start"><MapIcon fontSize="small" color="action" /></InputAdornment> }}
                                />
                            </Grid>

                            <Grid size={{ xs: 12 }}>
                                <Typography variant="subtitle2" color="text.secondary" fontWeight={700} mb={1}>Hub Image</Typography>
                                <Box
                                    component="label"
                                    sx={{
                                        width: '100%',
                                        height: 190,
                                        border: '2px dashed rgba(31,77,58,0.3)',
                                        borderRadius: 3,
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        overflow: 'hidden',
                                        cursor: 'pointer',
                                        bgcolor: 'rgba(31,77,58,0.03)',
                                        position: 'relative',
                                        '&:hover': { borderColor: 'primary.main' },
                                    }}
                                >
                                    <input type="file" hidden accept="image/*" onChange={handleImageChange} />
                                    {previewImageUrl ? (
                                        <Box component="img" src={previewImageUrl} alt="Hub preview" sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                                    ) : (
                                        <Box textAlign="center">
                                            <CloudUploadIcon sx={{ fontSize: 36, color: 'primary.main', mb: 1 }} />
                                            <Typography variant="body2" fontWeight={700}>Click to upload new image</Typography>
                                            <Typography variant="caption" color="text.secondary">PNG, JPG, WEBP up to 5MB</Typography>
                                        </Box>
                                    )}
                                </Box>
                                {selectedImageFile && (
                                    <Box mt={1} display="flex" alignItems="center" justifyContent="space-between">
                                        <Typography variant="caption" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 0.6 }}>
                                            <ImageIcon fontSize="inherit" />
                                            {selectedImageFile.name}
                                        </Typography>
                                        <IconButton
                                            size="small"
                                            onClick={(e) => {
                                                e.preventDefault();
                                                setSelectedImageFile(null);
                                                setPreviewImageUrl(assignedHub?.imageUrl || '');
                                                setValue('imageUrl', assignedHub?.imageUrl || '');
                                            }}
                                        >
                                            <CloseIcon fontSize="small" />
                                        </IconButton>
                                    </Box>
                                )}
                            </Grid>

                            <Grid size={{ xs: 12 }}>
                                <Alert severity="info" sx={{ borderRadius: 2 }}>
                                    Changes are applied directly to your hub profile. Double-check coordinates and map URL before saving.
                                </Alert>
                            </Grid>
                        </Grid>
                    </DialogContent>

                    <DialogActions sx={{ p: 2.5 }}>
                        <Button variant="outlined" color="inherit" onClick={() => setEditOpen(false)} disabled={saving}>Cancel</Button>
                        <Button type="submit" variant="contained" startIcon={<SaveIcon />} disabled={saving}>
                            {saving ? 'Saving...' : 'Save Changes'}
                        </Button>
                    </DialogActions>
                </Box>
            </Dialog>
        </Box>
    );
};

export default MyHubPage;
