import { Chip, Box, keyframes } from '@mui/material';
import type { ChipProps } from '@mui/material';

type StatusVariant =
    | 'ACTIVE' | 'INACTIVE'
    | 'PENDING' | 'CONFIRMED' | 'PICKED_UP' | 'RETURNED' | 'CANCELLED'
    | string;

const pulseAnimation = keyframes`
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(46, 125, 91, 0.7); }
  70% { transform: scale(1); box-shadow: 0 0 0 6px rgba(46, 125, 91, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(46, 125, 91, 0); }
`;

const statusConfig: Record<string, { label: string; color: ChipProps['color']; sx?: object, dotColor?: string }> = {
    ACTIVE: { label: 'Active', color: 'success', dotColor: '#2e7d5b', sx: { bgcolor: 'rgba(46, 125, 91, 0.1)', color: '#2e7d5b' } },
    INACTIVE: { label: 'Inactive', color: 'default', dotColor: '#9e9e9e', sx: { bgcolor: 'rgba(158, 158, 158, 0.15)', color: '#757575' } },
    PENDING: { label: 'Pending', color: 'warning', dotColor: '#ed6c02', sx: { bgcolor: 'rgba(237, 108, 2, 0.1)', color: '#ed6c02' } },
    CONFIRMED: { label: 'Confirmed', color: 'info', dotColor: '#0288d1', sx: { bgcolor: 'rgba(2, 136, 209, 0.1)', color: '#0288d1' } },
    PICKED_UP: { label: 'Picked Up', color: 'primary', dotColor: '#1976d2', sx: { bgcolor: 'rgba(25, 118, 210, 0.1)', color: '#1976d2' } },
    RETURNED: { label: 'Returned', color: 'success', dotColor: '#2e7d5b', sx: { bgcolor: 'rgba(46, 125, 91, 0.1)', color: '#2e7d5b' } },
    CANCELLED: { label: 'Cancelled', color: 'error', dotColor: '#d32f2f', sx: { bgcolor: 'rgba(211, 47, 47, 0.1)', color: '#d32f2f' } },
};

interface StatusBadgeProps {
    status: StatusVariant;
    size?: 'small' | 'medium';
}

const StatusBadge = ({ status, size = 'small' }: StatusBadgeProps) => {
    const config = statusConfig[status] || { label: status, color: 'default' as const };
    
    // Creating the pulsing dot if the status warrants it (has a dotColor)
    const renderDot = () => {
        if (!config.dotColor) return null;
        const isPulse = status === 'ACTIVE' || status === 'PENDING';
        return (
            <Box
                component="span"
                sx={{
                    width: 8, height: 8, borderRadius: '50%',
                    bgcolor: config.dotColor,
                    marginRight: 0.8,
                    display: 'inline-block',
                    animation: isPulse ? `${pulseAnimation} 2s infinite` : 'none'
                }}
            />
        );
    };

    return (
        <Chip
            icon={renderDot() as any}
            label={config.label}
            size={size}
            variant="filled"
            sx={{ 
                fontWeight: 700, 
                borderRadius: '8px', 
                fontSize: size === 'small' ? '0.75rem' : '0.875rem',
                border: 'none',
                '& .MuiChip-icon': { ml: size === 'small' ? '8px' : '10px' },
                ...(config.sx || {}) 
            }}
        />
    );
};

export default StatusBadge;
