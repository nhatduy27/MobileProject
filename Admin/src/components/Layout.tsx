import { useState } from 'react';
import { Layout as AntLayout, Menu, Avatar, Dropdown, Typography, Space, theme, Badge } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  DashboardOutlined,
  UserOutlined,
  ShopOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  AppstoreOutlined,
  TagOutlined,
  WalletOutlined,
} from '@ant-design/icons';
import type { MenuProps } from 'antd';
import { useAuth } from '../contexts/AuthContext';
import { useRealtime } from '../contexts/RealtimeContext';

const { Header, Sider, Content } = AntLayout;
const { Text } = Typography;

export default function Layout() {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const { pendingPayoutsCount, pendingShopsCount } = useRealtime();
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  const menuItems: MenuProps['items'] = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: 'Dashboard',
    },
    {
      key: '/users',
      icon: <UserOutlined />,
      label: 'Users',
    },
    {
      key: '/shops',
      icon: <ShopOutlined />,
      label: (
        <span>
          Shops
          {pendingShopsCount > 0 && (
            <Badge
              count={pendingShopsCount}
              size="small"
              style={{ marginLeft: 8, backgroundColor: '#F59E0B' }}
            />
          )}
        </span>
      ),
    },
    {
      key: '/categories',
      icon: <AppstoreOutlined />,
      label: 'Categories',
    },
    {
      key: '/vouchers',
      icon: <TagOutlined />,
      label: 'Vouchers',
    },
    {
      key: '/payouts',
      icon: <WalletOutlined />,
      label: (
        <span>
          Payouts
          {pendingPayoutsCount > 0 && (
            <Badge
              count={pendingPayoutsCount}
              size="small"
              style={{ marginLeft: 8, backgroundColor: '#EF4444' }}
            />
          )}
        </span>
      ),
    },
  ];

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key);
  };

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/login');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: (
        <div>
          <div style={{ fontWeight: 500 }}>{user?.displayName || user?.email}</div>
          <div style={{ fontSize: 12, color: '#999' }}>{user?.email}</div>
        </div>
      ),
      disabled: true,
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Logout',
      onClick: handleLogout,
      danger: true,
    },
  ];

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed} theme="dark">
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white',
            fontSize: collapsed ? 16 : 14,
            fontWeight: 600,
            fontFamily: "'Fira Code', monospace",
            letterSpacing: collapsed ? 0 : '0.5px',
            transition: 'all 0.2s ease',
            background: 'linear-gradient(180deg, #1E293B 0%, #0F172A 100%)',
            borderBottom: '1px solid #334155',
          }}
        >
          <span style={{ 
            color: '#22C55E', 
            marginRight: collapsed ? 0 : 6,
            fontSize: collapsed ? 20 : 16,
          }}>
            ⬢
          </span>
          {!collapsed && 'KTX Admin'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <AntLayout>
        <Header
          style={{
            padding: '0 24px',
            background: colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 1px 4px rgba(0,21,41,.08)',
          }}
        >
          <div>
            {collapsed ? (
              <MenuUnfoldOutlined
                onClick={() => setCollapsed(!collapsed)}
                style={{ fontSize: 18, cursor: 'pointer' }}
              />
            ) : (
              <MenuFoldOutlined
                onClick={() => setCollapsed(!collapsed)}
                style={{ fontSize: 18, cursor: 'pointer' }}
              />
            )}
          </div>
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <Space style={{ cursor: 'pointer' }}>
              <Avatar icon={<UserOutlined />} src={user?.avatarUrl} />
              <Text>{user?.displayName || 'Admin'}</Text>
            </Space>
          </Dropdown>
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 280,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
          }}
        >
          <Outlet />
        </Content>
      </AntLayout>
    </AntLayout>
  );
}
