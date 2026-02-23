import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
    Box, Card, CardContent, Grid, TextField, Typography, Button,
    FormControl, InputLabel, Select, MenuItem, FormHelperText,
    Divider, ToggleButtonGroup, ToggleButton, Alert, CircularProgress,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveIcon from '@mui/icons-material/Save';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PageHeader from '../../components/PageHeader';

const CATEGORIES = ['Tents', 'Backpacks', 'Sleeping Bags', 'Cooking', 'Navigation', 'Clothing', 'Safety', 'Lighting'];

const schema = z.object({
    name: z.string().min(3, 'Name must be at least 3 characters'),
    category: z.string().min(1, 'Category is required'),
    description: z.string().optional(),
    pricePerDay: z.coerce.number().positive('Price must be greater than 0'),
    stock: z.coerce.number().int().min(0, 'Stock cannot be negative'),
    status: z.enum(['ACTIVE', 'INACTIVE']),
    imageUrl: z.string().url('Must be a valid URL').optional().or(z.literal('')),
    videoUrl: z.string().url('Must be a valid URL').optional().or(z.literal('')),
});

type FormData = z.infer<typeof schema>;

// Mock existing gear for edit
const mockGearData: Record<string, FormData> = {
    'gear-1': {
        name: 'Tent Pro X3', category: 'Tents',
        description: 'Professional 3-person tent with waterproof fly and aluminum poles. Ideal for 3-season camping.',
        pricePerDay: 1500, stock: 12, status: 'ACTIVE',
        imageUrl: '', videoUrl: '',
    },
};

const GearFormPage = () => {
    const navigate = useNavigate();
    const { id } = useParams();
    const isEdit = Boolean(id);

    const {
        register, handleSubmit, control, reset, watch, formState: { errors, isSubmitting, isSubmitSuccessful },
    } = useForm<FormData>({
        resolver: zodResolver(schema) as any,
        defaultValues: {
            name: '', category: '', description: '', pricePerDay: 0, stock: 0,
            status: 'ACTIVE', imageUrl: '', videoUrl: '',
        },
    });

    useEffect(() => {
        if (isEdit && id && mockGearData[id]) {
            reset(mockGearData[id]);
        }
    }, [id, isEdit, reset]);

    const onSubmit = async (data: FormData) => {
        await new Promise((r) => setTimeout(r, 1000)); // mock API call
        console.log('Saved:', data);
        setTimeout(() => navigate('/inventory/gears'), 800);
    };

    return (
        <Box>
            <PageHeader
                title={isEdit ? 'Edit Gear' : 'Add New Gear'}
                subtitle={isEdit ? `Editing item ID: ${id}` : 'Create a new rental gear item'}
                breadcrumbs={[
                    { label: 'Home', to: '/dashboard' },
                    { label: 'Gear Inventory', to: '/inventory/gears' },
                    { label: isEdit ? 'Edit' : 'Add New' },
                ]}
                action={
                    <Button startIcon={<ArrowBackIcon />} variant="outlined" onClick={() => navigate('/inventory/gears')}>
                        Back to List
                    </Button>
                }
            />

            {isSubmitSuccessful && (
                <Alert severity="success" icon={<CheckCircleIcon />} sx={{ mb: 2, borderRadius: 2 }}>
                    Gear item {isEdit ? 'updated' : 'created'} successfully! Redirecting...
                </Alert>
            )}

            <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
                <Grid container spacing={2.5}>
                    {/* Left Column */}
                    <Grid size={{ xs: 12, lg: 8 }}>
                        <Card sx={{ mb: 2.5 }}>
                            <CardContent sx={{ p: 3 }}>
                                <Typography variant="subtitle1" fontWeight={700} mb={2} color="text.primary">
                                    Basic Information
                                </Typography>
                                <Grid container spacing={2}>
                                    <Grid size={{ xs: 12 }}>
                                        <TextField
                                            {...register('name')}
                                            label="Gear Name *"
                                            fullWidth
                                            error={!!errors.name}
                                            helperText={errors.name?.message}
                                            placeholder="e.g. Tent Pro X3"
                                        />
                                    </Grid>
                                    <Grid size={{ xs: 12, sm: 6 }}>
                                        <FormControl fullWidth error={!!errors.category}>
                                            <InputLabel>Category *</InputLabel>
                                            <Controller
                                                name="category"
                                                control={control}
                                                render={({ field }) => (
                                                    <Select {...field} label="Category *">
                                                        {CATEGORIES.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
                                                    </Select>
                                                )}
                                            />
                                            {errors.category && <FormHelperText>{errors.category.message}</FormHelperText>}
                                        </FormControl>
                                    </Grid>
                                    <Grid size={{ xs: 12 }}>
                                        <TextField
                                            {...register('description')}
                                            label="Description"
                                            fullWidth
                                            multiline
                                            rows={4}
                                            error={!!errors.description}
                                            helperText={errors.description?.message || 'Describe the gear, features, and conditions of use'}
                                            placeholder="Describe the gear item..."
                                        />
                                    </Grid>
                                </Grid>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardContent sx={{ p: 3 }}>
                                <Typography variant="subtitle1" fontWeight={700} mb={2} color="text.primary">
                                    Media
                                </Typography>
                                <Grid container spacing={2}>
                                    <Grid size={{ xs: 12 }}>
                                        <TextField
                                            {...register('imageUrl')}
                                            label="Image URL"
                                            fullWidth
                                            error={!!errors.imageUrl}
                                            helperText={errors.imageUrl?.message || 'Link to the gear image (HTTPS recommended)'}
                                            placeholder="https://example.com/gear-image.jpg"
                                        />
                                    </Grid>
                                    <Grid size={{ xs: 12 }}>
                                        <TextField
                                            {...register('videoUrl')}
                                            label="Video URL (Optional)"
                                            fullWidth
                                            error={!!errors.videoUrl}
                                            helperText={errors.videoUrl?.message || 'YouTube or Vimeo link for gear demo video'}
                                            placeholder="https://youtube.com/watch?v=..."
                                        />
                                    </Grid>
                                </Grid>
                            </CardContent>
                        </Card>
                    </Grid>

                    {/* Right Column */}
                    <Grid size={{ xs: 12, lg: 4 }}>
                        <Card sx={{ mb: 2.5 }}>
                            <CardContent sx={{ p: 3 }}>
                                <Typography variant="subtitle1" fontWeight={700} mb={2} color="text.primary">
                                    Pricing & Stock
                                </Typography>
                                <Grid container spacing={2}>
                                    <Grid size={{ xs: 12 }}>
                                        <TextField
                                            {...register('pricePerDay')}
                                            label="Price Per Day (LKR) *"
                                            type="number"
                                            fullWidth
                                            inputProps={{ min: 0 }}
                                            error={!!errors.pricePerDay}
                                            helperText={errors.pricePerDay?.message}
                                        />
                                    </Grid>
                                    <Grid size={{ xs: 12 }}>
                                        <TextField
                                            {...register('stock')}
                                            label="Stock Quantity *"
                                            type="number"
                                            fullWidth
                                            inputProps={{ min: 0 }}
                                            error={!!errors.stock}
                                            helperText={errors.stock?.message}
                                        />
                                    </Grid>
                                </Grid>
                            </CardContent>
                        </Card>

                        <Card sx={{ mb: 2.5 }}>
                            <CardContent sx={{ p: 3 }}>
                                <Typography variant="subtitle1" fontWeight={700} mb={2} color="text.primary">
                                    Status
                                </Typography>
                                <Controller
                                    name="status"
                                    control={control}
                                    render={({ field }) => (
                                        <ToggleButtonGroup
                                            value={field.value}
                                            exclusive
                                            onChange={(_, v) => v && field.onChange(v)}
                                            fullWidth
                                        >
                                            <ToggleButton value="ACTIVE" sx={{ fontWeight: 700, '&.Mui-selected': { bgcolor: 'success.light', color: 'success.dark' } }}>
                                                Active
                                            </ToggleButton>
                                            <ToggleButton value="INACTIVE" sx={{ fontWeight: 700, '&.Mui-selected': { bgcolor: 'grey.200', color: 'text.secondary' } }}>
                                                Inactive
                                            </ToggleButton>
                                        </ToggleButtonGroup>
                                    )}
                                />
                                <Typography variant="caption" color="text.secondary" mt={1} display="block">
                                    Inactive gear will not appear in customer listings.
                                </Typography>
                            </CardContent>
                        </Card>

                        <Button
                            type="submit"
                            variant="contained"
                            fullWidth
                            size="large"
                            disabled={isSubmitting}
                            startIcon={isSubmitting ? <CircularProgress size={18} color="inherit" /> : <SaveIcon />}
                            sx={{ py: 1.5, fontWeight: 700, borderRadius: 2 }}
                        >
                            {isSubmitting ? 'Saving...' : isEdit ? 'Update Gear' : 'Create Gear'}
                        </Button>
                    </Grid>
                </Grid>
            </Box>
        </Box>
    );
};

export default GearFormPage;
