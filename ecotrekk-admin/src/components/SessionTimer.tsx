import { useEffect, useState, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Chip, Dialog, DialogTitle, DialogContent, DialogActions,
    Button, Typography, Box, LinearProgress,
} from '@mui/material';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { jwtDecode } from 'jwt-decode';
import { useAuth } from '../context/AuthContext';
import { useSnackbar } from 'notistack';

const WARN_SECONDS = 120; // warn 2 minutes before expiry

interface DecodedToken {
    exp: number;
    iat?: number;
}

const formatElapsed = (secs: number): string => {
    const h = Math.floor(secs / 3600);
    const m = Math.floor((secs % 3600) / 60);
    const s = secs % 60;
    if (h > 0) return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
};

const SessionTimer = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const { enqueueSnackbar } = useSnackbar();

    const [elapsed, setElapsed] = useState(0);     // seconds since login (counts up)
    const [secsLeft, setSecsLeft] = useState(9999); // seconds until JWT expiry
    const [warnOpen, setWarnOpen] = useState(false);
    const warnShown = useRef(false);

    const handleLogout = useCallback((reason: 'expired' | 'manual') => {
        logout();
        navigate('/login');
        if (reason === 'expired') {
            enqueueSnackbar('Your session has expired. Please sign in again.', { variant: 'warning' });
        }
    }, [logout, navigate, enqueueSnackbar]);

    useEffect(() => {
        if (!user?.token) return;

        let sessionStart: number;
        let totalDuration: number;

        try {
            const decoded = jwtDecode<DecodedToken>(user.token);
            const now = Math.floor(Date.now() / 1000);
            sessionStart = decoded.iat ?? now;
            totalDuration = decoded.exp - sessionStart;
        } catch {
            return;
        }

        const tick = () => {
            const now = Math.floor(Date.now() / 1000);
            const elapsedSecs = now - sessionStart;
            const remaining = totalDuration - elapsedSecs;

            setElapsed(elapsedSecs);
            setSecsLeft(Math.max(0, remaining));

            if (remaining <= 0) {
                handleLogout('expired');
                return;
            }
            if (remaining <= WARN_SECONDS && !warnShown.current) {
                setWarnOpen(true);
                warnShown.current = true;
            }
        };

        tick();
        const interval = setInterval(tick, 1000);
        return () => clearInterval(interval);
    }, [user?.token, handleLogout]);

    if (!user) return null;

    const isWarning = secsLeft <= WARN_SECONDS;
    const warnProgress = isWarning ? (secsLeft / WARN_SECONDS) * 100 : 100;

    return (
        <>
            <Chip
                icon={<AccessTimeIcon sx={{ fontSize: '0.9rem !important' }} />}
                label={formatElapsed(elapsed)}
                size="small"
                sx={{
                    fontFamily: 'monospace',
                    fontWeight: 700,
                    fontSize: '0.75rem',
                    display: { xs: 'none', sm: 'flex' },
                    bgcolor: isWarning ? '#FEF3C7' : 'rgba(31,77,58,0.08)',
                    color: isWarning ? '#92400E' : '#1F4D3A',
                    border: isWarning
                        ? '1px solid #F59E0B'
                        : '1px solid rgba(31,77,58,0.2)',
                    animation: isWarning && secsLeft <= 30
                        ? 'pulse 1s infinite'
                        : 'none',
                    '@keyframes pulse': {
                        '0%, 100%': { opacity: 1 },
                        '50%': { opacity: 0.5 },
                    },
                }}
            />

            {/* Warning Dialog */}
            <Dialog open={warnOpen} onClose={() => setWarnOpen(false)} maxWidth="xs" fullWidth>
                <DialogTitle>
                    <Box display="flex" alignItems="center" gap={1}>
                        <WarningAmberIcon sx={{ color: '#D97706' }} />
                        <Typography fontWeight={700}>Session Expiring Soon</Typography>
                    </Box>
                </DialogTitle>
                <DialogContent>
                    <Typography color="text.secondary" mb={2}>
                        Your session will expire in{' '}
                        <strong style={{ color: '#92400E' }}>{formatElapsed(secsLeft)}</strong>.
                        Any unsaved work will be lost.
                    </Typography>
                    <LinearProgress
                        variant="determinate"
                        value={warnProgress}
                        sx={{
                            height: 6, borderRadius: 3,
                            bgcolor: '#FEF3C7',
                            '& .MuiLinearProgress-bar': { bgcolor: '#D97706', borderRadius: 3 },
                        }}
                    />
                    <Typography variant="caption" color="text.secondary" mt={1} display="block">
                        Contact your administrator to extend your session if needed.
                    </Typography>
                </DialogContent>
                <DialogActions sx={{ px: 3, pb: 2, gap: 1 }}>
                    <Button
                        variant="outlined"
                        color="error"
                        onClick={() => { setWarnOpen(false); handleLogout('manual'); }}
                    >
                        Sign Out Now
                    </Button>
                    <Button variant="contained" onClick={() => setWarnOpen(false)} autoFocus>
                        Stay Logged In
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
};

export default SessionTimer;
