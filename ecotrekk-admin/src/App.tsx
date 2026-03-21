import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { SnackbarProvider } from 'notistack';
import theme from './theme';
import { AuthProvider } from './context/AuthContext';
import { WebSocketProvider } from './context/WebSocketProvider';
import ProtectedRoute from './routes/ProtectedRoute';
import AdminLayout from './layouts/AdminLayout';

// Pages
import LoginPage from './pages/auth/LoginPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import GearListPage from './pages/inventory/GearListPage';
import HubListPage from './pages/hubs/HubListPage';
import MyHubPage from './pages/hubs/MyHubPage';
import OrderListPage from './pages/orders/OrderListPage.tsx';
import OrderDetailPage from './pages/orders/OrderDetailPage.tsx';
import UserListPage from './pages/users/UserListPage';
import ReportsPage from './pages/reports/ReportsPage';
import AdminListPage from './pages/admins/AdminListPage';
import AdminProfilePage from './pages/profile/AdminProfilePage';


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
            <WebSocketProvider>
              <BrowserRouter>
                <Routes>
                  {/* Public */}
                  <Route path="/login" element={<LoginPage />} />

                  {/* Protected - any authenticated user */}
                  <Route element={<ProtectedRoute />}>
                    <Route element={<AdminLayout />}>
                      <Route path="/dashboard" element={<DashboardPage />} />
                      <Route path="/inventory/gears" element={<GearListPage />} />

                      {/* Hub pages by role */}
                      <Route element={<ProtectedRoute requiredRoles={['SUPER_ADMIN']} />}>
                        <Route path="/inventory/hubs" element={<HubListPage />} />
                      </Route>
                      <Route element={<ProtectedRoute requiredRoles={['ADMIN']} />}>
                        <Route path="/inventory/my-hub" element={<MyHubPage />} />
                      </Route>

                      <Route path="/orders" element={<OrderListPage />} />
                      <Route path="/orders/:id" element={<OrderDetailPage />} />
                      <Route path="/users" element={<UserListPage />} />
                      <Route path="/reports" element={<ReportsPage />} />

                      {/* Highly Protected - SUPER_ADMIN ONLY */}
                      <Route element={<ProtectedRoute requiredRoles={['SUPER_ADMIN']} />}>
                          <Route path="/admins" element={<AdminListPage />} />
                      </Route>

                      {/* Common authenticated routes */}
                      <Route path="/profile" element={<AdminProfilePage />} />
                    </Route>
                  </Route>

                  {/* Fallback */}
                  <Route path="*" element={<Navigate to="/dashboard" replace />} />
                  <Route path="/" element={<Navigate to="/dashboard" replace />} />
                </Routes>
              </BrowserRouter>
            </WebSocketProvider>
          </AuthProvider>
        </SnackbarProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}

export default App;
