import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider, theme } from 'antd';
import { AuthProvider } from './contexts/AuthContext';
import { RealtimeProvider } from './contexts/RealtimeContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Users from './pages/Users';
import Shops from './pages/Shops';
import Categories from './pages/Categories';
import Vouchers from './pages/Vouchers';
import Payouts from './pages/Payouts';

function App() {
  return (
    <ConfigProvider
      theme={{
        algorithm: theme.darkAlgorithm,
        token: {
          // Primary colors
          colorPrimary: '#22C55E',
          colorSuccess: '#22C55E',
          colorError: '#EF4444',
          colorWarning: '#F59E0B',
          colorInfo: '#3B82F6',

          // Background colors
          colorBgContainer: '#0F172A',
          colorBgElevated: '#1E293B',
          colorBgLayout: '#020617',
          colorBgSpotlight: '#1E293B',

          // Border colors
          colorBorder: '#334155',
          colorBorderSecondary: '#1E293B',

          // Text colors
          colorText: '#F8FAFC',
          colorTextSecondary: '#94A3B8',
          colorTextTertiary: '#64748B',
          colorTextQuaternary: '#475569',

          // Typography
          fontFamily: "'Fira Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
          fontFamilyCode: "'Fira Code', monospace",

          // Border radius
          borderRadius: 8,
          borderRadiusLG: 12,
          borderRadiusSM: 6,

          // Shadows
          boxShadow: '0 4px 6px rgba(0, 0, 0, 0.4)',
          boxShadowSecondary: '0 10px 15px rgba(0, 0, 0, 0.4)',
        },
        components: {
          Button: {
            primaryShadow: '0 2px 0 rgba(34, 197, 94, 0.2)',
            defaultBg: '#1E293B',
            defaultBorderColor: '#334155',
          },
          Card: {
            colorBgContainer: '#0F172A',
            colorBorderSecondary: '#1E293B',
          },
          Table: {
            colorBgContainer: '#0F172A',
            headerBg: '#1E293B',
            rowHoverBg: '#1E293B',
            borderColor: '#334155',
          },
          Modal: {
            contentBg: '#0F172A',
            headerBg: '#0F172A',
          },
          Menu: {
            darkItemBg: '#0F172A',
            darkSubMenuItemBg: '#020617',
            darkItemSelectedBg: '#1E293B',
          },
          Input: {
            colorBgContainer: '#1E293B',
            colorBorder: '#334155',
            activeBorderColor: '#22C55E',
            hoverBorderColor: '#22C55E',
          },
          Select: {
            colorBgContainer: '#1E293B',
            colorBorder: '#334155',
            optionSelectedBg: '#1E293B',
          },
        },
      }}
    >
      <AuthProvider>
        <RealtimeProvider>
          <BrowserRouter>
            <Routes>
            <Route path="/login" element={<Login />} />

            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <Layout />
                </ProtectedRoute>
              }
            >
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="users" element={<Users />} />
              <Route path="shops" element={<Shops />} />
              <Route path="categories" element={<Categories />} />
              <Route path="vouchers" element={<Vouchers />} />
              <Route path="payouts" element={<Payouts />} />
            </Route>

              <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Routes>
          </BrowserRouter>
        </RealtimeProvider>
      </AuthProvider>
    </ConfigProvider>
  );
}

export default App;
