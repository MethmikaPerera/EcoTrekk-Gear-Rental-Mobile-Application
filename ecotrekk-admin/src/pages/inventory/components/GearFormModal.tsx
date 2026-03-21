import { useMemo, useState } from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Button,
    Box,
    MenuItem,
    InputAdornment,
    Typography,
    IconButton,
} from '@mui/material';
import { alpha } from '@mui/material/styles';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import ImageIcon from '@mui/icons-material/Image';
import CloseIcon from '@mui/icons-material/Close';

export interface GearFormData {
    name: string;
    categoryId: string;
    description: string;
    hub: string;
    availableCount: string;
    dailyRentPrice: string;
    depositAmount: string;
    isActive: boolean;
    primaryImageUrl: string;
    imageFile: File | null;
}

interface GearFormModalProps {
    open: boolean;
    mode?: 'add' | 'edit';
    onClose: () => void;
    onSubmit: (formData: GearFormData) => Promise<void>;
    categories: Array<{ id: string; name: string }>;
    hubs: Array<{ id: string; name: string }>;
    initialData?: Partial<GearFormData>;
    loading?: boolean;
}

const emptyForm: GearFormData = {
    name: '',
    categoryId: '',
    description: '',
    hub: '',
    availableCount: '0',
    dailyRentPrice: '',
    depositAmount: '0',
    isActive: true,
    primaryImageUrl: '',
    imageFile: null,
};

const GearFormModal = ({
    open,
    mode = 'add',
    onClose,
    onSubmit,
    categories,
    hubs,
    initialData,
    loading = false,
}: GearFormModalProps) => {
    const [formData, setFormData] = useState<GearFormData>({
        ...emptyForm,
        ...(mode === 'edit' && initialData ? initialData : {}),
    });
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [selectedImageName, setSelectedImageName] = useState('');
    const [previewImageUrl, setPreviewImageUrl] = useState(
        mode === 'edit' && initialData?.primaryImageUrl ? initialData.primaryImageUrl : ''
    );

    const uiText = useMemo(() => {
        if (mode === 'edit') {
            return {
                title: 'Edit Gear',
                submit: loading ? 'Saving...' : 'Save Changes',
                info: 'Update core gear details. Manage hub inventory quantities separately.',
            };
        }

        return {
            title: 'Add New Gear',
            submit: loading ? 'Adding...' : 'Add Gear',
        };
    }, [mode, loading]);

    const validate = (): boolean => {
        const newErrors: Record<string, string> = {};

        if (!formData.name.trim()) {
            newErrors.name = 'Gear name is required';
        } else if (formData.name.length > 140) {
            newErrors.name = 'Name cannot exceed 140 characters';
        }

        if (!formData.categoryId) {
            newErrors.categoryId = 'Category is required';
        }

        if (!formData.hub) {
            newErrors.hub = 'Hub is required';
        }

        if (formData.availableCount === '') {
            newErrors.availableCount = 'Available count is required';
        } else if (isNaN(Number(formData.availableCount)) || Number(formData.availableCount) < 0) {
            newErrors.availableCount = 'Available count must be a valid non-negative number';
        }

        if (!formData.dailyRentPrice) {
            newErrors.dailyRentPrice = 'Daily rent price is required';
        } else if (isNaN(Number(formData.dailyRentPrice)) || Number(formData.dailyRentPrice) < 0) {
            newErrors.dailyRentPrice = 'Price must be a valid non-negative number';
        }

        if (formData.depositAmount && (isNaN(Number(formData.depositAmount)) || Number(formData.depositAmount) < 0)) {
            newErrors.depositAmount = 'Deposit must be a valid non-negative number';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (field: keyof GearFormData) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const target = e.target as HTMLInputElement;
        const value = target.type === 'checkbox' ? target.checked : target.value;

        setFormData((prev) => ({
            ...prev,
            [field]: value,
        }));

        if (errors[field]) {
            setErrors((prev) => ({
                ...prev,
                [field]: '',
            }));
        }
    };

    const handleSubmit = async () => {
        if (!validate()) {
            return;
        }

        await onSubmit(formData);
    };

    const handleImageFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) {
            return;
        }

        const isImage = file.type.startsWith('image/');
        if (!isImage) {
            setErrors((prev) => ({ ...prev, primaryImageUrl: 'Please select a valid image file' }));
            return;
        }

        const reader = new FileReader();
        reader.onload = () => {
            const result = typeof reader.result === 'string' ? reader.result : '';
            setFormData((prev) => ({ ...prev, primaryImageUrl: result, imageFile: file }));
            setPreviewImageUrl(result);
            setSelectedImageName(file.name);
            setErrors((prev) => ({ ...prev, primaryImageUrl: '' }));
        };
        reader.readAsDataURL(file);
    };

    const clearSelectedImage = () => {
        setFormData((prev) => ({ ...prev, primaryImageUrl: '', imageFile: null }));
        setPreviewImageUrl('');
        setSelectedImageName('');
        setErrors((prev) => ({ ...prev, primaryImageUrl: '' }));
    };

    const handleClose = () => {
        setErrors({});
        setFormData(emptyForm);
        setPreviewImageUrl('');
        setSelectedImageName('');
        onClose();
    };

    return (
        <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
            <DialogTitle
                sx={{
                    fontWeight: 700,
                    fontSize: '1.2rem',
                    bgcolor: `linear-gradient(90deg, ${alpha('#1F4D3A', 0.03)} 0%, rgba(255,255,255,0) 100%)`,
                }}
            >
                {uiText.title}
            </DialogTitle>

            <DialogContent sx={{ pt: 2.5 }}>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <TextField
                        label="Gear Name"
                        placeholder="e.g., Tent Pro X3"
                        fullWidth
                        value={formData.name}
                        onChange={handleChange('name')}
                        error={!!errors.name}
                        helperText={errors.name}
                        inputProps={{ maxLength: 140 }}
                        disabled={loading}
                    />

                    <TextField
                        label="Description"
                        placeholder="Brief description of the gear"
                        fullWidth
                        multiline
                        rows={3}
                        value={formData.description}
                        onChange={handleChange('description')}
                        disabled={loading}
                    />

                    <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 1.5 }}>
                        <TextField
                            select
                            label="Category"
                            value={formData.categoryId}
                            onChange={handleChange('categoryId')}
                            error={!!errors.categoryId}
                            helperText={errors.categoryId}
                            fullWidth
                            disabled={loading}
                        >
                            <MenuItem value="">Select a category</MenuItem>
                            {categories.map((cat) => (
                                <MenuItem key={cat.id} value={cat.id}>
                                    {cat.name}
                                </MenuItem>
                            ))}
                        </TextField>

                        <TextField
                            select
                            label="Hub"
                            value={formData.hub}
                            onChange={handleChange('hub')}
                            error={!!errors.hub}
                            helperText={errors.hub}
                            fullWidth
                            disabled={loading}
                        >
                            <MenuItem value="">Select a hub</MenuItem>
                            {hubs.map((hub) => (
                                <MenuItem key={hub.id} value={hub.id}>
                                    {hub.name}
                                </MenuItem>
                            ))}
                        </TextField>
                    </Box>

                    <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 1.5 }}>
                        <TextField
                            label="Available Count"
                            type="number"
                            fullWidth
                            value={formData.availableCount}
                            onChange={handleChange('availableCount')}
                            error={!!errors.availableCount}
                            helperText={errors.availableCount}
                            inputProps={{ min: 0, step: 1 }}
                            disabled={loading}
                        />

                        <TextField
                            select
                            label="Active Status"
                            fullWidth
                            value={formData.isActive ? 'ACTIVE' : 'INACTIVE'}
                            onChange={(e) =>
                                setFormData((prev) => ({
                                    ...prev,
                                    isActive: e.target.value === 'ACTIVE',
                                }))
                            }
                            disabled={loading}
                        >
                            <MenuItem value="ACTIVE">Active</MenuItem>
                            <MenuItem value="INACTIVE">Inactive</MenuItem>
                        </TextField>
                    </Box>

                    <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 1.5 }}>
                        <Box>
                            <TextField
                                label="Daily Rental Price"
                                type="number"
                                fullWidth
                                value={formData.dailyRentPrice}
                                onChange={handleChange('dailyRentPrice')}
                                error={!!errors.dailyRentPrice}
                                helperText={errors.dailyRentPrice}
                                InputProps={{
                                    startAdornment: <InputAdornment position="start">LKR</InputAdornment>,
                                }}
                                inputProps={{ min: 0, step: 0.01 }}
                                disabled={loading}
                            />
                        </Box>
                        <Box>
                            <TextField
                                label="Deposit Amount"
                                type="number"
                                fullWidth
                                value={formData.depositAmount}
                                onChange={handleChange('depositAmount')}
                                error={!!errors.depositAmount}
                                helperText={errors.depositAmount}
                                InputProps={{
                                    startAdornment: <InputAdornment position="start">LKR</InputAdornment>,
                                }}
                                inputProps={{ min: 0, step: 0.01 }}
                                disabled={loading}
                            />
                        </Box>
                    </Box>

                    <Box>
                        <Typography variant="body2" fontWeight={600} sx={{ mb: 1 }}>
                            Gear Image
                        </Typography>
                        <Box
                            component="label"
                            sx={{
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: 'center',
                                justifyContent: 'center',
                                width: '100%',
                                minHeight: 170,
                                border: `2px dashed ${alpha('#1F4D3A', 0.24)}`,
                                borderRadius: 2,
                                bgcolor: alpha('#1F4D3A', 0.02),
                                cursor: loading ? 'not-allowed' : 'pointer',
                                overflow: 'hidden',
                                position: 'relative',
                                '&:hover': {
                                    borderColor: alpha('#1F4D3A', 0.45),
                                },
                            }}
                        >
                            <input
                                type="file"
                                hidden
                                accept="image/*"
                                onChange={handleImageFileChange}
                                disabled={loading}
                            />

                            {previewImageUrl ? (
                                <Box
                                    component="img"
                                    src={previewImageUrl}
                                    alt="Gear preview"
                                    sx={{ width: '100%', height: 170, objectFit: 'cover' }}
                                />
                            ) : (
                                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 3 }}>
                                    <CloudUploadIcon sx={{ fontSize: 30, color: 'primary.main', mb: 1 }} />
                                    <Typography variant="body2" fontWeight={600}>Click to upload image</Typography>
                                    <Typography variant="caption" color="text.secondary">PNG, JPG, WEBP</Typography>
                                </Box>
                            )}
                        </Box>

                        {(selectedImageName || previewImageUrl) && (
                            <Box mt={1} display="flex" alignItems="center" justifyContent="space-between" gap={1}>
                                <Box display="flex" alignItems="center" gap={0.75} minWidth={0}>
                                    <ImageIcon fontSize="small" color="action" />
                                    <Typography variant="caption" color="text.secondary" noWrap>
                                        {selectedImageName || 'Selected image'}
                                    </Typography>
                                </Box>
                                <IconButton size="small" onClick={clearSelectedImage} disabled={loading}>
                                    <CloseIcon fontSize="small" />
                                </IconButton>
                            </Box>
                        )}

                        {errors.primaryImageUrl && (
                            <Typography variant="caption" color="error" sx={{ mt: 0.5, display: 'block' }}>
                                {errors.primaryImageUrl}
                            </Typography>
                        )}
                    </Box>
                </Box>
            </DialogContent>

            <DialogActions sx={{ p: 2, borderTop: `1px solid ${alpha('#1F4D3A', 0.1)}` }}>
                <Button onClick={handleClose} disabled={loading}>
                    Cancel
                </Button>
                <Button onClick={handleSubmit} variant="contained" disabled={loading} sx={{ minWidth: 110 }}>
                    {uiText.submit}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default GearFormModal;
