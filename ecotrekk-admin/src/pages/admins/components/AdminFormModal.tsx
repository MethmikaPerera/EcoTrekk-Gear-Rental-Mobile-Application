import {
    Dialog, DialogTitle, DialogContent, DialogActions,
    Button, TextField, Box, MenuItem, FormControl,
    InputLabel, Select, IconButton, Typography, FormHelperText, CircularProgress
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useForm, Controller, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchActiveHubs } from '../../../api/hubApi';

const getAdminSchema = (isEditMode: boolean) => z.object({
    name: z.string().min(2, 'Name is required'),
    email: z.string().email('Valid email is required'),
    password: isEditMode ? z.string().optional() : z.string().min(6, 'Password must be at least 6 characters'),
    role: z.string().min(1, 'Role is required'), 
    hubId: z.number().optional().nullable(),
}).refine(data => {
    if ((data.role === 'ADMIN' || data.role === 'STAFF') && !data.hubId) {
        return false;
    }
    return true;
}, {
    message: "Hub selection is required for Admins and Staff",
    path: ["hubId"],
});

export type AdminFormData = z.infer<ReturnType<typeof getAdminSchema>> & { id?: string };

interface AdminFormModalProps {
    open: boolean;
    onClose: () => void;
    onSubmit: (data: AdminFormData) => void;
    roles: string[];
    initialData?: AdminFormData | null;
}

const AdminFormModal = ({ open, onClose, onSubmit, roles, initialData }: AdminFormModalProps) => {
    const isEditMode = !!initialData;
    const { control, handleSubmit, reset, formState: { errors } } = useForm<AdminFormData>({
        resolver: zodResolver(getAdminSchema(isEditMode)),
        defaultValues: {
            name: '',
            email: '',
            password: '',
            role: 'STAFF',
            hubId: null
        },
    });

    useEffect(() => {
        if (open) {
            if (initialData) {
                reset({ ...initialData, password: '' });
            } else {
                reset({
                    name: '',
                    email: '',
                    password: '',
                    role: 'STAFF',
                    hubId: null
                });
            }
        }
    }, [open, initialData, reset]);

    const watchedRole = useWatch({ control, name: 'role' });
    const showHubSelect = watchedRole === 'ADMIN' || watchedRole === 'STAFF';

    const { data: hubs = [], isLoading: hubsLoading } = useQuery({
        queryKey: ['activeHubs'],
        queryFn: fetchActiveHubs,
        enabled: open && showHubSelect, // Only fetch if modal is open and dropdown is shown
        staleTime: 5 * 60 * 1000,
    });

    const handleFormSubmit = (data: AdminFormData) => {
        // If super admin, ensure hubId is not sent
        if (data.role === 'SUPER_ADMIN') {
            data.hubId = null;
        }
        if (isEditMode && initialData) {
            data.id = initialData.id;
        }
        onSubmit(data);
    };

    const handleClose = () => {
        reset();
        onClose();
    };

    return (
        <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
            <DialogTitle sx={{ m: 0, p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="h6" fontWeight={700}>{isEditMode ? 'Edit Admin' : 'Add New Admin'}</Typography>
                <IconButton onClick={handleClose} size="small">
                    <CloseIcon />
                </IconButton>
            </DialogTitle>
            
            <DialogContent dividers>
                <Box component="form" noValidate sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                    <Controller
                        name="name"
                        control={control}
                        render={({ field }) => (
                            <TextField
                                {...field}
                                label="Full Name"
                                fullWidth
                                error={!!errors.name}
                                helperText={errors.name?.message}
                            />
                        )}
                    />
                    
                    <Controller
                        name="email"
                        control={control}
                        render={({ field }) => (
                            <TextField
                                {...field}
                                label="Email Address"
                                type="email"
                                fullWidth
                                error={!!errors.email}
                                helperText={errors.email?.message}
                            />
                        )}
                    />

                    {!isEditMode && (
                        <Controller
                            name="password"
                            control={control}
                            render={({ field }) => (
                                <TextField
                                    {...field}
                                    label="Temporary Password"
                                    type="password"
                                    fullWidth
                                    error={!!errors.password}
                                    helperText={errors.password?.message}
                                />
                            )}
                        />
                    )}

                    <Controller
                        name="role"
                        control={control}
                        render={({ field }) => (
                            <TextField
                                {...field}
                                select
                                fullWidth
                                label="Admin Role"
                                error={!!errors.role}
                                helperText={errors.role?.message}
                            >
                                {roles.length === 0 ? (
                                    <MenuItem disabled>Loading roles...</MenuItem>
                                ) : (
                                    roles.map(r => (
                                        <MenuItem key={r} value={r}>
                                            {r === 'SUPER_ADMIN' ? 'Super Admin' : r === 'ADMIN' ? 'Admin' : 'Staff'}
                                        </MenuItem>
                                    ))
                                )}
                            </TextField>
                        )}
                    />

                    {showHubSelect && (
                        <Controller
                            name="hubId"
                            control={control}
                            render={({ field }) => (
                            <TextField
                                {...field}
                                select
                                fullWidth
                                label="Assign Hub"
                                value={field.value ?? ''}
                                error={!!errors.hubId}
                                helperText={errors.hubId?.message}
                            >
                                {hubsLoading ? (
                                    <MenuItem disabled sx={{ justifyContent: 'center' }}>
                                        <CircularProgress size={20} sx={{ mr: 1 }}/> Loading hubs...
                                    </MenuItem>
                                ) : hubs.length === 0 ? (
                                    <MenuItem disabled sx={{ justifyContent: 'center' }}>No active hubs found</MenuItem>
                                ) : (
                                    hubs.map(h => (
                                        <MenuItem key={h.id} value={h.id}>
                                            {h.name} ({h.address})
                                        </MenuItem>
                                    ))
                                )}
                            </TextField>
                            )}
                        />
                    )}
                </Box>
            </DialogContent>
            <DialogActions sx={{ p: 2 }}>
                <Button onClick={handleClose} color="inherit">Cancel</Button>
                <Button onClick={handleSubmit(handleFormSubmit)} variant="contained" color="primary">
                    {isEditMode ? 'Save Changes' : 'Create Admin'}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default AdminFormModal;
