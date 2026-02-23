import { Chip } from '@mui/material';
import type { ChipProps } from '@mui/material';

type StatusVariant =
    | 'ACTIVE' | 'INACTIVE'
    | 'PENDING' | 'CONFIRMED' | 'PICKED_UP' | 'RETURNED' | 'CANCELLED'
    | string;

const statusConfig: Record<string, { label: string; color: ChipProps['color']; sx?: object }> = {
    ACTIVE: { label: 'Active', color: 'success' },
    INACTIVE: { label: 'Inactive', color: 'default' },
    PENDING: { label: 'Pending', color: 'warning' },
    CONFIRMED: { label: 'Confirmed', color: 'info' },
    PICKED_UP: { label: 'Picked Up', color: 'primary' },
    RETURNED: { label: 'Returned', color: 'success' },
    CANCELLED: { label: 'Cancelled', color: 'error' },
};

interface StatusBadgeProps {
    status: StatusVariant;
    size?: 'small' | 'medium';
}

const StatusBadge = ({ status, size = 'small' }: StatusBadgeProps) => {
    const config = statusConfig[status] || { label: status, color: 'default' as const };
    return (
        <Chip
            label={config.label}
            color={config.color}
            size={size}
            variant="filled"
            sx={{ fontWeight: 600, borderRadius: '6px', ...(config.sx || {}) }}
        />
    );
};

export default StatusBadge;
