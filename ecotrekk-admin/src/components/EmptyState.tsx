import { Box, Typography, Button } from '@mui/material';
import { SvgIconComponent } from '@mui/icons-material';
import InboxIcon from '@mui/icons-material/Inbox';

interface EmptyStateProps {
    icon?: React.ReactNode;
    title: string;
    description?: string;
    action?: { label: string; onClick: () => void };
}

const EmptyState = ({ icon, title, description, action }: EmptyStateProps) => (
    <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        justifyContent="center"
        py={8}
        px={4}
        textAlign="center"
    >
        <Box
            sx={{
                width: 72,
                height: 72,
                borderRadius: '50%',
                bgcolor: 'grey.100',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                mb: 2,
            }}
        >
            {icon || <InboxIcon sx={{ fontSize: 36, color: 'grey.400' }} />}
        </Box>
        <Typography variant="h6" fontWeight={700} color="text.primary" mb={0.5}>
            {title}
        </Typography>
        {description && (
            <Typography variant="body2" color="text.secondary" maxWidth={340}>
                {description}
            </Typography>
        )}
        {action && (
            <Button variant="contained" onClick={action.onClick} sx={{ mt: 2.5, borderRadius: 2 }}>
                {action.label}
            </Button>
        )}
    </Box>
);

export default EmptyState;
