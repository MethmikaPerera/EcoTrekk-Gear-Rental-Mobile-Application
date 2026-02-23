import { Table, TableBody, TableCell, TableHead, TableRow, Skeleton, Box } from '@mui/material';

interface SkeletonTableProps {
    columns?: number;
    rows?: number;
}

const SkeletonTable = ({ columns = 6, rows = 8 }: SkeletonTableProps) => {
    return (
        <Box>
            <Table>
                <TableHead>
                    <TableRow>
                        {Array.from({ length: columns }).map((_, i) => (
                            <TableCell key={i}>
                                <Skeleton variant="text" width="80%" height={20} />
                            </TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {Array.from({ length: rows }).map((_, rowIdx) => (
                        <TableRow key={rowIdx}>
                            {Array.from({ length: columns }).map((_, colIdx) => (
                                <TableCell key={colIdx}>
                                    {colIdx === 0 ? (
                                        <Box display="flex" alignItems="center" gap={1.5}>
                                            <Skeleton variant="rounded" width={40} height={40} />
                                            <Skeleton variant="text" width={100} />
                                        </Box>
                                    ) : (
                                        <Skeleton variant="text" width={`${60 + Math.random() * 30}%`} />
                                    )}
                                </TableCell>
                            ))}
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </Box>
    );
};

export default SkeletonTable;
