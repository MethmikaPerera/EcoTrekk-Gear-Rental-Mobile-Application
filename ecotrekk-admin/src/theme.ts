import { createTheme } from '@mui/material/styles';

const theme = createTheme({
    palette: {
        mode: 'light',
        primary: {
            main: '#1F4D3A',      // Forest Green
            light: '#2E7D5B',     // Emerald Green
            dark: '#163829',
            contrastText: '#ffffff',
        },
        secondary: {
            main: '#A8D5BA',      // Soft Mint
            light: '#C8E8D4',
            dark: '#2E7D5B',
            contrastText: '#1F4D3A',
        },
        background: {
            default: '#F4F6F5',
            paper: '#FFFFFF',
        },
        error: {
            main: '#DC2626',     // Danger Red
        },
        warning: {
            main: '#F59E0B',     // Pending Amber
        },
        success: {
            main: '#16A34A',     // Confirmed Green
        },
        info: {
            main: '#2563EB',     // Picked Up Blue
        },
        text: {
            primary: '#1E1E1E',
            secondary: '#6B7280',
            disabled: '#9CA3AF',
        },
        divider: '#E0E6E4',
    },
    typography: {
        fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
        h1: { fontWeight: 700 },
        h2: { fontWeight: 700 },
        h3: { fontWeight: 600 },
        h4: { fontWeight: 600 },
        h5: { fontWeight: 600 },
        h6: { fontWeight: 600 },
        button: { textTransform: 'none', fontWeight: 600 },
    },
    shape: {
        borderRadius: 10,
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: 8,
                    boxShadow: 'none',
                    '&:hover': { boxShadow: '0 2px 8px rgba(0,0,0,0.15)' },
                },
                containedPrimary: {
                    backgroundColor: '#1F4D3A',
                    '&:hover': { backgroundColor: '#2E7D5B' },
                },
                outlinedSecondary: {
                    backgroundColor: '#E6F2EC',
                    borderColor: '#A8D5BA',
                    color: '#1F4D3A',
                    '&:hover': { backgroundColor: '#D2E8DF' },
                },
            },
        },
        MuiCard: {
            styleOverrides: {
                root: {
                    borderRadius: 12,
                    boxShadow: '0 1px 4px rgba(0,0,0,0.08)',
                    border: '1px solid #E0E6E4',
                },
            },
        },
        MuiPaper: {
            styleOverrides: {
                root: {
                    borderRadius: 12,
                },
            },
        },
        MuiDrawer: {
            styleOverrides: {
                paper: {
                    borderRadius: 0,
                },
            },
        },
        MuiTableCell: {
            styleOverrides: {
                head: {
                    fontWeight: 600,
                    backgroundColor: '#F4F6F5',
                    color: '#6B7280',
                    fontSize: '0.78rem',
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                },
            },
        },
        MuiChip: {
            styleOverrides: {
                root: {
                    fontWeight: 600,
                    borderRadius: 6,
                },
            },
        },
        MuiTextField: {
            defaultProps: {
                size: 'small',
            },
        },
        MuiSelect: {
            defaultProps: {
                size: 'small',
            },
        },
        MuiOutlinedInput: {
            styleOverrides: {
                root: {
                    borderRadius: 8,
                },
            },
        },
        MuiDivider: {
            styleOverrides: {
                root: {
                    borderColor: '#E0E6E4',
                },
            },
        },
        MuiAlert: {
            styleOverrides: {
                standardWarning: {
                    backgroundColor: '#FEF3C7',
                    border: '1px solid #F59E0B',
                    borderLeft: '4px solid #D97706',
                    color: '#78350F',
                    '& .MuiAlert-icon': { color: '#D97706' },
                },
                standardError: {
                    backgroundColor: '#FEE2E2',
                    border: '1px solid #DC2626',
                    borderLeft: '4px solid #B91C1C',
                    color: '#7F1D1D',
                    '& .MuiAlert-icon': { color: '#B91C1C' },
                },
                standardSuccess: {
                    backgroundColor: '#DCFCE7',
                    border: '1px solid #16A34A',
                    borderLeft: '4px solid #15803D',
                    color: '#14532D',
                    '& .MuiAlert-icon': { color: '#15803D' },
                },
                standardInfo: {
                    backgroundColor: '#DBEAFE',
                    border: '1px solid #2563EB',
                    borderLeft: '4px solid #1D4ED8',
                    color: '#1E3A8A',
                    '& .MuiAlert-icon': { color: '#1D4ED8' },
                },
            },
        },
    },
});

export default theme;
