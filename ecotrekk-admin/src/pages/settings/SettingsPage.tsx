import React from 'react';
import { useState } from 'react';
import {
    Box, Card, CardContent, Grid, Typography, TextField, Button,
    Divider, Alert, Switch, FormControlLabel, CircularProgress,
} from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import SettingsIcon from '@mui/icons-material/Settings';
import NotificationsIcon from '@mui/icons-material/Notifications';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import SupportIcon from '@mui/icons-material/Support';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PageHeader from '../../components/PageHeader';

const SettingsPage = () => {
    const [saving, setSaving] = useState<string | null>(null);
    const [saved, setSaved] = useState<string | null>(null);

    const [inventory, setInventory] = useState({
        lowStockThreshold: 5,
        outOfStockAlert: true,
        lowStockAlert: true,
    });

    const [hours, setHours] = useState({
        weekdayOpen: '08:00',
        weekdayClose: '18:00',
        weekendOpen: '09:00',
        weekendClose: '16:00',
        closedOnHolidays: true,
    });

    const [support, setSupport] = useState({
        email: 'support@ecotrekk.com',
        phone: '+94112345678',
        whatsapp: '+94712345678',
        address: '45 Galle Rd, Colombo 03',
    });

    const [notifications, setNotifications] = useState({
        orderConfirmTemplate: 'Hello {name},\nYour order {orderId} has been confirmed. Pick up from {hub} on {date}.',
        cancellationTemplate: 'Hello {name},\nWe are sorry. Your order {orderId} has been cancelled.',
    });

    const handleSave = async (section: string) => {
        setSaving(section);
        await new Promise((r) => setTimeout(r, 800));
        setSaving(null);
        setSaved(section);
        setTimeout(() => setSaved(null), 3000);
    };

    const SectionCard = ({ id, title, icon, children }: { id: string; title: string; icon: React.ReactNode; children: React.ReactNode }) => (
        <Card sx={{ mb: 2.5 }}>
            <CardContent sx={{ p: 3 }}>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2.5}>
                    <Box display="flex" alignItems="center" gap={1}>
                        <Box sx={{ color: 'primary.main' }}>{icon}</Box>
                        <Typography variant="h6" fontWeight={700}>{title}</Typography>
                    </Box>
                    {saved === id && (
                        <Alert severity="success" icon={<CheckCircleIcon fontSize="small" />} sx={{ py: 0, borderRadius: 2 }}>
                            Saved!
                        </Alert>
                    )}
                </Box>
                <Divider sx={{ mb: 2.5 }} />
                {children}
                <Box mt={2.5}>
                    <Button
                        variant="contained"
                        startIcon={saving === id ? <CircularProgress size={16} color="inherit" /> : <SaveIcon />}
                        onClick={() => handleSave(id)}
                        disabled={!!saving}
                    >
                        {saving === id ? 'Saving...' : 'Save Changes'}
                    </Button>
                </Box>
            </CardContent>
        </Card>
    );

    return (
        <Box>
            <PageHeader
                title="Settings"
                subtitle="System configuration and operational settings"
                breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'Settings' }]}
            />

            <Grid container spacing={0}>
                <Grid size={{ xs: 12, lg: 9 }}>
                    {/* Inventory Settings */}
                    <SectionCard id="inventory" title="Inventory Settings" icon={<SettingsIcon />}>
                        <Grid container spacing={2} alignItems="center">
                            <Grid size={{ xs: 12, sm: 5 }}>
                                <TextField
                                    label="Low Stock Threshold"
                                    type="number"
                                    fullWidth
                                    value={inventory.lowStockThreshold}
                                    onChange={(e) => setInventory((p) => ({ ...p, lowStockThreshold: +e.target.value }))}
                                    inputProps={{ min: 1 }}
                                    helperText="Items below this quantity will be flagged as low stock"
                                />
                            </Grid>
                            <Grid size={{ xs: 12, sm: 7 }}>
                                <Box display="flex" flexDirection="column" gap={0.5}>
                                    <FormControlLabel
                                        control={<Switch checked={inventory.lowStockAlert} onChange={(e) => setInventory((p) => ({ ...p, lowStockAlert: e.target.checked }))} color="primary" />}
                                        label="Enable low stock alerts"
                                    />
                                    <FormControlLabel
                                        control={<Switch checked={inventory.outOfStockAlert} onChange={(e) => setInventory((p) => ({ ...p, outOfStockAlert: e.target.checked }))} color="primary" />}
                                        label="Enable out-of-stock alerts"
                                    />
                                </Box>
                            </Grid>
                        </Grid>
                    </SectionCard>

                    {/* Business Hours */}
                    <SectionCard id="hours" title="Business Hours" icon={<AccessTimeIcon />}>
                        <Grid container spacing={2}>
                            <Grid size={{ xs: 12 }}>
                                <Typography variant="subtitle2" color="text.secondary" mb={1} fontWeight={600}>Weekdays (Mon–Fri)</Typography>
                            </Grid>
                            <Grid size={{ xs: 6, sm: 3 }}>
                                <TextField
                                    label="Opening Time"
                                    type="time"
                                    fullWidth
                                    value={hours.weekdayOpen}
                                    onChange={(e) => setHours((p) => ({ ...p, weekdayOpen: e.target.value }))}
                                    InputLabelProps={{ shrink: true }}
                                />
                            </Grid>
                            <Grid size={{ xs: 6, sm: 3 }}>
                                <TextField
                                    label="Closing Time"
                                    type="time"
                                    fullWidth
                                    value={hours.weekdayClose}
                                    onChange={(e) => setHours((p) => ({ ...p, weekdayClose: e.target.value }))}
                                    InputLabelProps={{ shrink: true }}
                                />
                            </Grid>
                            <Grid size={{ xs: 12 }}>
                                <Typography variant="subtitle2" color="text.secondary" mb={1} fontWeight={600}>Weekends (Sat–Sun)</Typography>
                            </Grid>
                            <Grid size={{ xs: 6, sm: 3 }}>
                                <TextField
                                    label="Opening Time"
                                    type="time"
                                    fullWidth
                                    value={hours.weekendOpen}
                                    onChange={(e) => setHours((p) => ({ ...p, weekendOpen: e.target.value }))}
                                    InputLabelProps={{ shrink: true }}
                                />
                            </Grid>
                            <Grid size={{ xs: 6, sm: 3 }}>
                                <TextField
                                    label="Closing Time"
                                    type="time"
                                    fullWidth
                                    value={hours.weekendClose}
                                    onChange={(e) => setHours((p) => ({ ...p, weekendClose: e.target.value }))}
                                    InputLabelProps={{ shrink: true }}
                                />
                            </Grid>
                            <Grid size={{ xs: 12 }}>
                                <FormControlLabel
                                    control={<Switch checked={hours.closedOnHolidays} onChange={(e) => setHours((p) => ({ ...p, closedOnHolidays: e.target.checked }))} color="primary" />}
                                    label="Closed on public holidays"
                                />
                            </Grid>
                        </Grid>
                    </SectionCard>

                    {/* Support Contact */}
                    <SectionCard id="support" title="Support Contact" icon={<SupportIcon />}>
                        <Grid container spacing={2}>
                            <Grid size={{ xs: 12, sm: 6 }}>
                                <TextField label="Support Email" fullWidth value={support.email} onChange={(e) => setSupport((p) => ({ ...p, email: e.target.value }))} type="email" />
                            </Grid>
                            <Grid size={{ xs: 12, sm: 6 }}>
                                <TextField label="Phone Number" fullWidth value={support.phone} onChange={(e) => setSupport((p) => ({ ...p, phone: e.target.value }))} />
                            </Grid>
                            <Grid size={{ xs: 12, sm: 6 }}>
                                <TextField label="WhatsApp Number" fullWidth value={support.whatsapp} onChange={(e) => setSupport((p) => ({ ...p, whatsapp: e.target.value }))} />
                            </Grid>
                            <Grid size={{ xs: 12 }}>
                                <TextField label="Office Address" fullWidth value={support.address} onChange={(e) => setSupport((p) => ({ ...p, address: e.target.value }))} multiline rows={2} />
                            </Grid>
                        </Grid>
                    </SectionCard>

                    {/* Notification Templates */}
                    <SectionCard id="notifications" title="Notification Templates" icon={<NotificationsIcon />}>
                        <Alert severity="info" sx={{ mb: 2, borderRadius: 2 }}>
                            Use variables: <code>{'{name}'}</code>, <code>{'{orderId}'}</code>, <code>{'{hub}'}</code>, <code>{'{date}'}</code>
                        </Alert>
                        <Grid container spacing={2}>
                            <Grid size={{ xs: 12 }}>
                                <TextField
                                    label="Order Confirmation Template"
                                    fullWidth
                                    multiline
                                    rows={4}
                                    value={notifications.orderConfirmTemplate}
                                    onChange={(e) => setNotifications((p) => ({ ...p, orderConfirmTemplate: e.target.value }))}
                                />
                            </Grid>
                            <Grid size={{ xs: 12 }}>
                                <TextField
                                    label="Cancellation Template"
                                    fullWidth
                                    multiline
                                    rows={3}
                                    value={notifications.cancellationTemplate}
                                    onChange={(e) => setNotifications((p) => ({ ...p, cancellationTemplate: e.target.value }))}
                                />
                            </Grid>
                        </Grid>
                    </SectionCard>
                </Grid>
            </Grid>
        </Box>
    );
};

export default SettingsPage;
