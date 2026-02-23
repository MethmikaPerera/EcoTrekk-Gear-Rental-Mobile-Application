import { Box, Typography, Breadcrumbs, Link } from '@mui/material';
import NavigateNextIcon from '@mui/icons-material/NavigateNext';
import { Link as RouterLink } from 'react-router-dom';
import { ReactNode } from 'react';

interface Breadcrumb {
    label: string;
    to?: string;
}

interface PageHeaderProps {
    title: string;
    subtitle?: string;
    breadcrumbs?: Breadcrumb[];
    action?: ReactNode;
}

const PageHeader = ({ title, subtitle, breadcrumbs, action }: PageHeaderProps) => {
    return (
        <Box
            sx={{
                display: 'flex',
                alignItems: { xs: 'flex-start', sm: 'center' },
                justifyContent: 'space-between',
                flexDirection: { xs: 'column', sm: 'row' },
                gap: 2,
                mb: 3,
            }}
        >
            <Box>
                {breadcrumbs && breadcrumbs.length > 0 && (
                    <Breadcrumbs separator={<NavigateNextIcon fontSize="small" />} sx={{ mb: 0.5 }}>
                        {breadcrumbs.map((crumb, i) =>
                            crumb.to ? (
                                <Link
                                    key={i}
                                    component={RouterLink}
                                    to={crumb.to}
                                    underline="hover"
                                    color="text.secondary"
                                    fontSize="0.8rem"
                                >
                                    {crumb.label}
                                </Link>
                            ) : (
                                <Typography key={i} fontSize="0.8rem" color="text.primary" fontWeight={600}>
                                    {crumb.label}
                                </Typography>
                            )
                        )}
                    </Breadcrumbs>
                )}
                <Typography variant="h5" fontWeight={700} color="text.primary" lineHeight={1.2}>
                    {title}
                </Typography>
                {subtitle && (
                    <Typography variant="body2" color="text.secondary" mt={0.5}>
                        {subtitle}
                    </Typography>
                )}
            </Box>
            {action && <Box>{action}</Box>}
        </Box>
    );
};

export default PageHeader;
