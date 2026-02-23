import { useState } from 'react';
import {
    Box, Card, Table, TableBody, TableCell, TableHead, TableRow, Button,
    IconButton, Typography, Tooltip, Dialog, DialogTitle, DialogContent,
    DialogActions, TextField, Grid, Alert, Chip,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import MapIcon from '@mui/icons-material/Map';
import PlaceIcon from '@mui/icons-material/Place';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import PageHeader from '../../components/PageHeader';
import ConfirmDialog from '../../components/ConfirmDialog';
import EmptyState from '../../components/EmptyState';
import { useSnackbar } from 'notistack';
import StoreIcon from '@mui/icons-material/Store';

const schema = z.object({
    name: z.string().min(2, 'Name required'),
    address: z.string().min(5, 'Address required'),
    phone: z.string().min(7, 'Valid phone required'),
    latitude: z.coerce.number().min(-90).max(90, 'Valid latitude (-90 to 90)'),
    longitude: z.coerce.number().min(-180).max(180, 'Valid longitude (-180 to 180)'),
});
type HubFormData = z.infer<typeof schema>;

interface Hub extends HubFormData { id: string; }

const mockHubs: Hub[] = [
    { id: 'hub-1', name: 'Colombo Central Hub', address: '45 Galle Rd, Colombo 03', phone: '+94112345678', latitude: 6.9271, longitude: 79.8612 },
    { id: 'hub-2', name: 'Kandy Mountain Hub', address: '12 Temple St, Kandy', phone: '+94812345678', latitude: 7.2906, longitude: 80.6337 },
    { id: 'hub-3', name: 'Galle Fort Hub', address: '8 Church St, Galle Fort', phone: '+94912345678', latitude: 6.0328, longitude: 80.2168 },
    { id: 'hub-4', name: 'Nuwara Eliya Hub', address: '3 Park Rd, Nuwara Eliya', phone: '+94522345678', latitude: 6.9497, longitude: 80.7891 },
];

const HubListPage = () => {
    const { enqueueSnackbar } = useSnackbar();
    const [hubs, setHubs] = useState<Hub[]>(mockHubs);
    const [formOpen, setFormOpen] = useState(false);
    const [editingHub, setEditingHub] = useState<Hub | null>(null);
    const [deleteTarget, setDeleteTarget] = useState<Hub | null>(null);
    const [mapHub, setMapHub] = useState<Hub | null>(null);
    const [saving, setSaving] = useState(false);

    const { register, handleSubmit, reset, formState: { errors } } = useForm<HubFormData>({
        resolver: zodResolver(schema) as any,
        defaultValues: { name: '', address: '', phone: '', latitude: 0, longitude: 0 },
    });

    const openAdd = () => { reset({ name: '', address: '', phone: '', latitude: 0, longitude: 0 }); setEditingHub(null); setFormOpen(true); };
    const openEdit = (hub: Hub) => { reset(hub); setEditingHub(hub); setFormOpen(true); };

    const onSubmit = async (data: HubFormData) => {
        setSaving(true);
        await new Promise((r) => setTimeout(r, 700));
        if (editingHub) {
            setHubs((prev) => prev.map((h) => h.id === editingHub.id ? { ...h, ...data } : h));
            enqueueSnackbar(`"${data.name}" updated successfully.`, { variant: 'success' });
        } else {
            setHubs((prev) => [...prev, { id: `hub-${Date.now()}`, ...data }]);
            enqueueSnackbar(`Hub "${data.name}" added successfully.`, { variant: 'success' });
        }
        setSaving(false);
        setFormOpen(false);
    };

    const handleDelete = async () => {
        if (!deleteTarget) return;
        await new Promise((r) => setTimeout(r, 500));
        enqueueSnackbar(`"${deleteTarget.name}" has been deleted.`, { variant: 'success' });
        setHubs((prev) => prev.filter((h) => h.id !== deleteTarget.id));
        setDeleteTarget(null);
    };

    return (
        <Box>
            <PageHeader
                title="Rental Hubs"
                subtitle={`${hubs.length} active pickup/dropoff locations`}
                breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'Rental Hubs' }]}
                action={
                    <Button variant="contained" startIcon={<AddIcon />} onClick={openAdd}>
                        Add Hub
                    </Button>
                }
            />

            <Card>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Hub Name</TableCell>
                            <TableCell>Address</TableCell>
                            <TableCell>Phone</TableCell>
                            <TableCell>Coordinates</TableCell>
                            <TableCell align="center">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {hubs.length === 0 ? (
                            <TableRow><TableCell colSpan={5}><EmptyState icon={<StoreIcon sx={{ fontSize: 36, color: 'grey.400' }} />} title="No hubs yet" description="Add your first rental hub to get started." action={{ label: 'Add First Hub', onClick: openAdd }} /></TableCell></TableRow>
                        ) : hubs.map((hub) => (
                            <TableRow key={hub.id} hover>
                                <TableCell>
                                    <Box display="flex" alignItems="center" gap={1}>
                                        <PlaceIcon sx={{ color: 'primary.main', fontSize: 18 }} />
                                        <Typography fontWeight={600}>{hub.name}</Typography>
                                    </Box>
                                </TableCell>
                                <TableCell>
                                    <Typography variant="body2" color="text.secondary">{hub.address}</Typography>
                                </TableCell>
                                <TableCell>{hub.phone}</TableCell>
                                <TableCell>
                                    <Chip
                                        label={`${hub.latitude.toFixed(4)}, ${hub.longitude.toFixed(4)}`}
                                        size="small"
                                        variant="outlined"
                                        sx={{ fontFamily: 'monospace', fontSize: '0.75rem' }}
                                    />
                                </TableCell>
                                <TableCell align="center">
                                    <Box display="flex" justifyContent="center" gap={0.5}>
                                        <Tooltip title="View on Map">
                                            <IconButton size="small" color="info" onClick={() => setMapHub(hub)}>
                                                <MapIcon fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                        <Tooltip title="Edit">
                                            <IconButton size="small" color="primary" onClick={() => openEdit(hub)}>
                                                <EditIcon fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                        <Tooltip title="Delete">
                                            <IconButton size="small" color="error" onClick={() => setDeleteTarget(hub)}>
                                                <DeleteIcon fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                    </Box>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </Card>

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
