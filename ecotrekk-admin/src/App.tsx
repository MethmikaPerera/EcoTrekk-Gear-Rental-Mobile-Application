import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { SnackbarProvider } from 'notistack';
import theme from './theme';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';
import AdminLayout from './layouts/AdminLayout';

// Pages
import LoginPage from './pages/auth/LoginPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import GearListPage from './pages/inventory/GearListPage';
import GearFormPage from './pages/inventory/GearFormPage';
import HubListPage from './pages/hubs/HubListPage';
import OrderListPage from './pages/orders/OrderListPage';
import OrderDetailPage from './pages/orders/OrderDetailPage';
import UserListPage from './pages/users/UserListPage';
import ReportsPage from './pages/reports/ReportsPage';


const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, staleTime: 30000 },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <SnackbarProvider maxSnack={3} anchorOrigin={{ vertical: 'top', horizontal: 'right' }}>
          <AuthProvider>
            <BrowserRouter>
              <Routes>
                {/* Public */}
                <Route path="/login" element={<LoginPage />} />

                {/* Protected - any authenticated user */}
                <Route element={<ProtectedRoute />}>
                  <Route element={<AdminLayout />}>
                    <Route path="/dashboard" element={<DashboardPage />} />
                    <Route path="/inventory/gears" element={<GearListPage />} />
                    <Route path="/inventory/gears/new" element={<GearFormPage />} />
                    <Route path="/inventory/gears/:id/edit" element={<GearFormPage />} />
                    <Route path="/inventory/hubs" element={<HubListPage />} />
                    <Route path="/orders" element={<OrderListPage />} />
                    <Route path="/orders/:id" element={<OrderDetailPage />} />
                    <Route path="/users" element={<UserListPage />} />
                    <Route path="/reports" element={<ReportsPage />} />


                  </Route>
                </Route>

                {/* Fallback */}
                <Route path="*" element={<Navigate to="/dashboard" replace />} />
                <Route path="/" element={<Navigate to="/dashboard" replace />} />
              </Routes>
            </BrowserRouter>
          </AuthProvider>
        </SnackbarProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}

export default App;
