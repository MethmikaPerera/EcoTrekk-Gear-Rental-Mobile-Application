import { useMemo, useState } from 'react';
import {
    Alert,
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    IconButton,
    List,
    ListItem,
    ListItemText,
    Stack,
    Tab,
    Tabs,
    TextField,
    Tooltip,
    Typography,
} from '@mui/material';
import { alpha } from '@mui/material/styles';
import AddIcon from '@mui/icons-material/Add';
import CheckIcon from '@mui/icons-material/Check';
import CloseIcon from '@mui/icons-material/Close';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import ImageIcon from '@mui/icons-material/Image';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import {
    createGearCategory,
    deleteGearCategory,
    getGearCategories,
    updateGearCategory,
    type GearCategoryOption,
} from '../../../api/gearApi';
import { getCloudinarySignature, saveCloudinaryAsset, uploadToCloudinary } from '../../../api/uploadApi';

interface GearCategoryManagerModalProps {
    open: boolean;
    onClose: () => void;
}

type ApiErrorShape = {
    response?: {
        data?: {
            message?: string;
        };
    };
    message?: string;
};

const extractErrorMessage = (error: unknown, fallback: string) => {
    const apiError = error as ApiErrorShape;
    const dataMessage = apiError.response?.data?.message;
    if (typeof dataMessage === 'string' && dataMessage.trim()) {
        return dataMessage;
    }

    const rootMessage = apiError.message;
    if (typeof rootMessage === 'string' && rootMessage.trim()) {
        return rootMessage;
    }

    return fallback;
};

const GearCategoryManagerModal = ({ open, onClose }: GearCategoryManagerModalProps) => {
    const queryClient = useQueryClient();
    const { enqueueSnackbar } = useSnackbar();

    const [newCategoryName, setNewCategoryName] = useState('');
    const [newCategoryImageFile, setNewCategoryImageFile] = useState<File | null>(null);
    const [newCategoryImagePreviewUrl, setNewCategoryImagePreviewUrl] = useState('');
    const [newCategoryImageError, setNewCategoryImageError] = useState('');
    const [isUploadingImage, setIsUploadingImage] = useState(false);
    const [activeTab, setActiveTab] = useState(0);
    const [editingCategoryId, setEditingCategoryId] = useState<number | null>(null);
    const [editingCategoryName, setEditingCategoryName] = useState('');
    const [editingCategoryImageFile, setEditingCategoryImageFile] = useState<File | null>(null);
    const [editingCategoryImagePreviewUrl, setEditingCategoryImagePreviewUrl] = useState('');

    const { data: categories = [], isLoading, isError } = useQuery({
        queryKey: ['gear-category-options'],
        queryFn: async () => {
            const response = await getGearCategories();
            return response.data;
        },
        enabled: open,
    });

    const mutationOptions = {
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['gear-category-options'] });
        },
    };

    const createCategoryMutation = useMutation({
        mutationFn: (payload: { name: string }) => createGearCategory(payload),
        ...mutationOptions,
    });

    const updateCategoryMutation = useMutation({
        mutationFn: (payload: { id: number; name: string }) => updateGearCategory(payload.id, { name: payload.name }),
        ...mutationOptions,
    });

    const deleteCategoryMutation = useMutation({
        mutationFn: (categoryId: number) => deleteGearCategory(categoryId),
        ...mutationOptions,
    });

    const sortedCategories = useMemo(
        () => [...categories].sort((a: GearCategoryOption, b: GearCategoryOption) => a.name.localeCompare(b.name)),
        [categories]
    );

    const isBusy = createCategoryMutation.isPending || updateCategoryMutation.isPending || deleteCategoryMutation.isPending || isUploadingImage;

    const handleImagePick = (
        file: File | null,
        setFile: (next: File | null) => void,
        setPreviewUrl: (next: string) => void
    ) => {
        if (!file) {
            return;
        }

        if (!file.type.startsWith('image/')) {
            enqueueSnackbar('Please choose a valid image file.', { variant: 'error' });
            return;
        }

        const previewUrl = URL.createObjectURL(file);
        setFile(file);
        setPreviewUrl(previewUrl);
    };

    const clearPickedImage = (
        setFile: (next: File | null) => void,
        setPreviewUrl: (next: string) => void
    ) => {
        setFile(null);
        setPreviewUrl('');
    };

    const resetLocalState = () => {
        setNewCategoryName('');
        setNewCategoryImageFile(null);
        setNewCategoryImagePreviewUrl('');
        setNewCategoryImageError('');
        setIsUploadingImage(false);
        setActiveTab(0);
        setEditingCategoryId(null);
        setEditingCategoryName('');
        setEditingCategoryImageFile(null);
        setEditingCategoryImagePreviewUrl('');
    };

    const handleClose = () => {
        resetLocalState();
        onClose();
    };

    const handleCreate = async () => {
        const name = newCategoryName.trim();
        if (!name) {
            enqueueSnackbar('Category name is required.', { variant: 'error' });
            return;
        }
        if (name.length > 80) {
            enqueueSnackbar('Category name cannot exceed 80 characters.', { variant: 'error' });
            return;
        }
        if (!newCategoryImageFile) {
            setNewCategoryImageError('Category image is required.');
            enqueueSnackbar('Category image is required.', { variant: 'error' });
            return;
        }

        try {
            const createResponse = await createCategoryMutation.mutateAsync({ name });

            if (newCategoryImageFile) {
                setIsUploadingImage(true);
                try {
                    const categoryId = createResponse.data.id;
                    const signature = await getCloudinarySignature({
                        folderType: 'categories',
                        entityId: categoryId,
                    });
                    const uploadResult = await uploadToCloudinary(newCategoryImageFile, signature);
                    await saveCloudinaryAsset({
                        folderType: 'categories',
                        entityId: categoryId,
                        secureUrl: uploadResult.secure_url,
                        publicId: uploadResult.public_id,
                    });
                    await queryClient.invalidateQueries({ queryKey: ['gear-category-options'] });
                } finally {
                    setIsUploadingImage(false);
                }
            }

            setNewCategoryName('');
            setNewCategoryImageFile(null);
            setNewCategoryImagePreviewUrl('');
            setNewCategoryImageError('');
            enqueueSnackbar(`Category "${name}" added.`, { variant: 'success' });
        } catch (error) {
            enqueueSnackbar(extractErrorMessage(error, 'Failed to create category.'), { variant: 'error' });
        }
    };

    const handleStartEdit = (category: GearCategoryOption) => {
        setEditingCategoryId(category.id);
        setEditingCategoryName(category.name);
        setEditingCategoryImageFile(null);
        setEditingCategoryImagePreviewUrl('');
    };

    const handleCancelEdit = () => {
        setEditingCategoryId(null);
        setEditingCategoryName('');
        setEditingCategoryImageFile(null);
        setEditingCategoryImagePreviewUrl('');
    };

    const handleSaveEdit = async (categoryId: number) => {
        const name = editingCategoryName.trim();
        if (!name) {
            enqueueSnackbar('Category name is required.', { variant: 'error' });
            return;
        }
        if (name.length > 80) {
            enqueueSnackbar('Category name cannot exceed 80 characters.', { variant: 'error' });
            return;
        }

        try {
            await updateCategoryMutation.mutateAsync({ id: categoryId, name });

            if (editingCategoryImageFile) {
                const signature = await getCloudinarySignature({
                    folderType: 'categories',
                    entityId: categoryId,
                });
                const uploadResult = await uploadToCloudinary(editingCategoryImageFile, signature);
                await saveCloudinaryAsset({
                    folderType: 'categories',
                    entityId: categoryId,
                    secureUrl: uploadResult.secure_url,
                    publicId: uploadResult.public_id,
                });
            }

            await queryClient.invalidateQueries({ queryKey: ['gear-category-options'] });
            enqueueSnackbar('Category updated.', { variant: 'success' });
            handleCancelEdit();
        } catch (error) {
            enqueueSnackbar(extractErrorMessage(error, 'Failed to update category.'), { variant: 'error' });
        }
    };

    const handleDelete = async (category: GearCategoryOption) => {
        try {
            await deleteCategoryMutation.mutateAsync(category.id);
            enqueueSnackbar(`Category "${category.name}" deleted.`, { variant: 'success' });
            if (editingCategoryId === category.id) {
                handleCancelEdit();
            }
        } catch (error) {
            enqueueSnackbar(extractErrorMessage(error, 'Failed to delete category.'), { variant: 'error' });
        }
    };

    return (
        <Dialog open={open} onClose={isBusy ? undefined : handleClose} fullWidth maxWidth="sm">
            <DialogTitle sx={{ pb: 1 }}>
                <Typography fontWeight={700}>Manage Gear Categories</Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                    Create new categories with images or update existing category names and images.
                </Typography>
            </DialogTitle>
            <DialogContent>
                <Tabs
                    value={activeTab}
                    onChange={(_, value: number) => setActiveTab(value)}
                    sx={{ mb: 2, borderBottom: '1px solid', borderColor: 'divider' }}
                >
                    <Tab label="Add Category" />
                    <Tab label="Manage Existing" />
                </Tabs>

                {activeTab === 0 && (
                    <Box>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
                            Category name and image are required for creation.
                        </Typography>

                        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ mb: 2 }}>
                            <TextField
                                fullWidth
                                label="New Category"
                                value={newCategoryName}
                                onChange={(e) => {
                                    setNewCategoryName(e.target.value);
                                }}
                                disabled={isBusy}
                                inputProps={{ maxLength: 80 }}
                            />
                        </Stack>

                        <Box
                            component="label"
                            sx={{
                                mb: 1,
                                width: '100%',
                                minHeight: 155,
                                border: '2px dashed',
                                borderColor: newCategoryImageError ? 'error.main' : 'divider',
                                borderRadius: 2,
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                cursor: isBusy ? 'not-allowed' : 'pointer',
                                backgroundColor: newCategoryImageError ? alpha('#d32f2f', 0.03) : alpha('#1f4d3a', 0.02),
                                transition: 'all 0.2s ease',
                                '&:hover': {
                                    borderColor: newCategoryImageError ? 'error.main' : 'primary.main',
                                },
                            }}
                        >
                            <input
                                hidden
                                type="file"
                                accept="image/*"
                                onChange={(event) => {
                                    setNewCategoryImageError('');
                                    handleImagePick(
                                        event.target.files?.[0] ?? null,
                                        setNewCategoryImageFile,
                                        setNewCategoryImagePreviewUrl
                                    );
                                }}
                                disabled={isBusy}
                            />

                            {newCategoryImagePreviewUrl ? (
                                <Box
                                    component="img"
                                    src={newCategoryImagePreviewUrl}
                                    alt="New category preview"
                                    sx={{ width: '100%', minHeight: 155, maxHeight: 220, borderRadius: 1.5, objectFit: 'cover' }}
                                />
                            ) : (
                                <Stack alignItems="center" spacing={1} sx={{ py: 3 }}>
                                    <CloudUploadIcon color={newCategoryImageError ? 'error' : 'action'} />
                                    <Typography variant="body2" fontWeight={600} color={newCategoryImageError ? 'error.main' : 'text.primary'}>
                                        Upload category image (required)
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary">
                                        PNG, JPG, WEBP
                                    </Typography>
                                </Stack>
                            )}
                        </Box>

                        {newCategoryImageFile && (
                            <Stack direction="row" spacing={0.5} alignItems="center" sx={{ minWidth: 0, mb: 1.5 }}>
                                <ImageIcon fontSize="small" color="action" />
                                <Typography variant="caption" color="text.secondary" noWrap>
                                    {newCategoryImageFile.name}
                                </Typography>
                                <IconButton
                                    size="small"
                                    onClick={() => {
                                        clearPickedImage(setNewCategoryImageFile, setNewCategoryImagePreviewUrl);
                                        setNewCategoryImageError('Category image is required.');
                                    }}
                                    disabled={isBusy}
                                >
                                    <CloseIcon fontSize="small" />
                                </IconButton>
                            </Stack>
                        )}

                        {newCategoryImageError && (
                            <Typography variant="caption" color="error" sx={{ display: 'block', mb: 1.5 }}>
                                {newCategoryImageError}
                            </Typography>
                        )}

                        <Button
                            variant="contained"
                            startIcon={<AddIcon />}
                            onClick={handleCreate}
                            disabled={isBusy}
                            sx={{ minWidth: { sm: 150 } }}
                        >
                            {isBusy ? 'Adding...' : 'Add Category'}
                        </Button>
                    </Box>
                )}

                {isError && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        Failed to load categories.
                    </Alert>
                )}

                {activeTab === 1 && (
                    <List dense disablePadding sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1.5 }}>
                        {isLoading ? (
                            <ListItem>
                                <ListItemText primary="Loading categories..." />
                            </ListItem>
                        ) : sortedCategories.length === 0 ? (
                            <ListItem>
                                <ListItemText primary="No categories found." />
                            </ListItem>
                        ) : (
                            sortedCategories.map((category) => {
                                const isEditing = editingCategoryId === category.id;

                                return (
                                    <ListItem
                                        key={category.id}
                                        divider
                                        secondaryAction={
                                            <Stack direction="row" spacing={0.5}>
                                                {isEditing ? (
                                                    <>
                                                        <Tooltip title="Save" arrow>
                                                            <span>
                                                                <IconButton
                                                                    edge="end"
                                                                    color="success"
                                                                    onClick={() => handleSaveEdit(category.id)}
                                                                    disabled={isBusy}
                                                                >
                                                                    <CheckIcon fontSize="small" />
                                                                </IconButton>
                                                            </span>
                                                        </Tooltip>
                                                        <Tooltip title="Cancel" arrow>
                                                            <span>
                                                                <IconButton edge="end" onClick={handleCancelEdit} disabled={isBusy}>
                                                                    <CloseIcon fontSize="small" />
                                                                </IconButton>
                                                            </span>
                                                        </Tooltip>
                                                    </>
                                                ) : (
                                                    <>
                                                        <Tooltip title="Rename" arrow>
                                                            <span>
                                                                <IconButton
                                                                    edge="end"
                                                                    onClick={() => handleStartEdit(category)}
                                                                    disabled={isBusy}
                                                                >
                                                                    <EditIcon fontSize="small" />
                                                                </IconButton>
                                                            </span>
                                                        </Tooltip>
                                                        <Tooltip title="Delete" arrow>
                                                            <span>
                                                                <IconButton
                                                                    edge="end"
                                                                    color="error"
                                                                    onClick={() => handleDelete(category)}
                                                                    disabled={isBusy}
                                                                >
                                                                    <DeleteIcon fontSize="small" />
                                                                </IconButton>
                                                            </span>
                                                        </Tooltip>
                                                    </>
                                                )}
                                            </Stack>
                                        }
                                    >
                                        {isEditing ? (
                                            <Box sx={{ width: '100%', pr: 12 }}>
                                                <TextField
                                                    size="small"
                                                    fullWidth
                                                    value={editingCategoryName}
                                                    onChange={(e) => setEditingCategoryName(e.target.value)}
                                                    disabled={isBusy}
                                                    inputProps={{ maxLength: 80 }}
                                                />

                                                <Stack direction="row" spacing={1} sx={{ mt: 1 }} alignItems="center" flexWrap="wrap">
                                                    <Button component="label" size="small" variant="outlined" startIcon={<CloudUploadIcon />} disabled={isBusy}>
                                                        Upload Image
                                                        <input
                                                            hidden
                                                            type="file"
                                                            accept="image/*"
                                                            onChange={(event) =>
                                                                handleImagePick(
                                                                    event.target.files?.[0] ?? null,
                                                                    setEditingCategoryImageFile,
                                                                    setEditingCategoryImagePreviewUrl
                                                                )
                                                            }
                                                        />
                                                    </Button>

                                                    {editingCategoryImageFile && (
                                                        <Stack direction="row" spacing={0.5} alignItems="center" sx={{ minWidth: 0 }}>
                                                            <ImageIcon fontSize="small" color="action" />
                                                            <Typography variant="caption" color="text.secondary" noWrap>
                                                                {editingCategoryImageFile.name}
                                                            </Typography>
                                                            <IconButton
                                                                size="small"
                                                                onClick={() => clearPickedImage(setEditingCategoryImageFile, setEditingCategoryImagePreviewUrl)}
                                                                disabled={isBusy}
                                                            >
                                                                <CloseIcon fontSize="small" />
                                                            </IconButton>
                                                        </Stack>
                                                    )}
                                                </Stack>

                                                {editingCategoryImagePreviewUrl && (
                                                    <Box
                                                        component="img"
                                                        src={editingCategoryImagePreviewUrl}
                                                        alt="Edit category preview"
                                                        sx={{ width: 64, height: 64, borderRadius: 1.5, objectFit: 'cover', mt: 1 }}
                                                    />
                                                )}
                                            </Box>
                                        ) : (
                                            <Stack direction="row" spacing={1.2} alignItems="center" sx={{ pr: 12, width: '100%' }}>
                                                {category.imageUrl ? (
                                                    <Box
                                                        component="img"
                                                        src={category.imageUrl}
                                                        alt={`${category.name} image`}
                                                        sx={{ width: 40, height: 40, borderRadius: 1.2, objectFit: 'cover', flexShrink: 0 }}
                                                    />
                                                ) : (
                                                    <Box
                                                        sx={{
                                                            width: 40,
                                                            height: 40,
                                                            borderRadius: 1.2,
                                                            bgcolor: alpha('#1f4d3a', 0.08),
                                                            display: 'flex',
                                                            alignItems: 'center',
                                                            justifyContent: 'center',
                                                            flexShrink: 0,
                                                        }}
                                                    >
                                                        <ImageIcon fontSize="small" color="disabled" />
                                                    </Box>
                                                )}
                                                <ListItemText
                                                    primary={category.name}
                                                />
                                            </Stack>
                                        )}
                                    </ListItem>
                                );
                            })
                        )}
                    </List>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={handleClose} disabled={isBusy}>Close</Button>
            </DialogActions>
        </Dialog>
    );
};

export default GearCategoryManagerModal;
