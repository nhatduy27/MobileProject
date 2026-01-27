import { useEffect, useState } from 'react';
import { Card, Row, Col, Statistic, Typography, Spin, Alert, Tag } from 'antd';
import {
  UserOutlined,
  ShopOutlined,
  ShoppingOutlined,
  DollarOutlined,
  WalletOutlined,
} from '@ant-design/icons';
import api from '../api/client';
import type { DashboardStats } from '../types';

const { Title } = Typography;

interface WalletStats {
  totalBalance: number;
  ownerBalance: number;
  shipperBalance: number;
  walletCount: number;
}

export default function Dashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [walletStats, setWalletStats] = useState<WalletStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadDashboard();
    loadWalletStats();
  }, []);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get<any>('/admin/dashboard');
      
      // Handle different response formats
      const responseData = response.data;
      console.log('Dashboard API Response:', responseData);
      
      let statsData = responseData.data || responseData;
      console.log('Parsed statsData:', statsData);
      console.log('Users object:', JSON.stringify(statsData.users, null, 2));
      
      // If there's nested stats structure, extract it
      if (statsData.stats) {
        statsData = statsData.stats;
      }
      
      // Ensure users object exists with proper structure
      if (statsData.users && typeof statsData.users.total !== 'undefined') {
        // Check if role counts might be using different field names
        if (!statsData.users.customers && statsData.users.customer !== undefined) {
          statsData.users.customers = statsData.users.customer;
        }
        if (!statsData.users.owners && statsData.users.owner !== undefined) {
          statsData.users.owners = statsData.users.owner;
        }
        if (!statsData.users.shippers && statsData.users.shipper !== undefined) {
          statsData.users.shippers = statsData.users.shipper;
        }
        // Check for CUSTOMER/OWNER/SHIPPER as keys (from backend)
        if (!statsData.users.customers && statsData.users.CUSTOMER !== undefined) {
          statsData.users.customers = statsData.users.CUSTOMER;
        }
        if (!statsData.users.owners && statsData.users.OWNER !== undefined) {
          statsData.users.owners = statsData.users.OWNER;
        }
        if (!statsData.users.shippers && statsData.users.SHIPPER !== undefined) {
          statsData.users.shippers = statsData.users.SHIPPER;
        }
      }
      
      console.log('Final statsData:', statsData);
      setStats(statsData);
    } catch (err: any) {
      console.error('Failed to load dashboard:', err);
      setError(err.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const loadWalletStats = async () => {
    try {
      const response = await api.get<any>('/admin/dashboard/wallets');
      const data = response.data?.data || response.data;
      setWalletStats(data);
    } catch (err) {
      console.error('Failed to load wallet stats:', err);
      // Non-blocking - wallet stats are optional
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount);
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 0' }}>
        <Spin size="large" tip="Loading dashboard..." />
      </div>
    );
  }

  if (error) {
    return (
      <Alert
        message="Error Loading Dashboard"
        description={error}
        type="error"
        showIcon
        action={
          <a onClick={loadDashboard} style={{ cursor: 'pointer' }}>
            Retry
          </a>
        }
      />
    );
  }

  if (!stats) {
    return <Alert message="No data available" type="info" showIcon />;
  }

  // Common card style for dashboard
  const cardStyle = {
    cursor: 'pointer',
    transition: 'all 0.2s ease',
    border: '1px solid #334155',
  };

  const cardHoverStyle = {
    boxShadow: '0 10px 15px rgba(0, 0, 0, 0.3)',
    transform: 'translateY(-2px)',
  };

  return (
    <div>
      <Title level={2} style={{ fontFamily: "'Fira Code', monospace", marginBottom: 24 }}>
        Dashboard Overview
      </Title>

      {/* User Statistics */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card
            style={cardStyle}
            hoverable
          >
            <Statistic
              title="Total Users"
              value={stats.users.total}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#22C55E' }}
            />
            <div style={{ marginTop: 8, fontSize: 12, color: '#94A3B8' }}>
              <Tag color="green">+{stats.users.newToday} today</Tag>
            </div>
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Customers"
              value={stats.users.customers}
              prefix={<UserOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Shop Owners"
              value={stats.users.owners}
              prefix={<ShopOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Shippers"
              value={stats.users.shippers}
              prefix={<UserOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* Shop Statistics */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Total Shops"
              value={stats.shops.total}
              prefix={<ShopOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card style={cardStyle} hoverable>
            <Statistic
              title="Active Shops"
              value={stats.shops.active}
              prefix={<ShopOutlined />}
              valueStyle={{ color: '#22C55E' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card style={cardStyle} hoverable>
            <Statistic
              title="Pending Approval"
              value={stats.shops.pendingApproval}
              prefix={<ShopOutlined />}
              valueStyle={{ color: '#F59E0B' }}
            />
          </Card>
        </Col>
      </Row>

      {/* Order Statistics */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Orders Today"
              value={stats.orders.today}
              prefix={<ShoppingOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Orders This Week"
              value={stats.orders.thisWeek}
              prefix={<ShoppingOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Orders This Month"
              value={stats.orders.thisMonth}
              prefix={<ShoppingOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* Revenue Statistics */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Revenue Today"
              value={stats.revenue.today}
              prefix={<DollarOutlined />}
              formatter={(value) => formatCurrency(value as number)}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Revenue This Week"
              value={stats.revenue.thisWeek}
              prefix={<DollarOutlined />}
              formatter={(value) => formatCurrency(value as number)}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Revenue This Month"
              value={stats.revenue.thisMonth}
              prefix={<DollarOutlined />}
              formatter={(value) => formatCurrency(value as number)}
            />
          </Card>
        </Col>
      </Row>

      {/* Payout Statistics */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12}>
          <Card>
            <Statistic
              title="Pending Payouts"
              value={stats.payouts.pending}
              prefix={<WalletOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12}>
          <Card>
            <Statistic
              title="Pending Amount"
              value={stats.payouts.totalPendingAmount}
              prefix={<WalletOutlined />}
              formatter={(value) => formatCurrency(value as number)}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
      </Row>

      {/* Wallet Balance Statistics */}
      {walletStats && (
        <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
          <Col xs={24}>
            <Title level={4} style={{ color: '#E2E8F0' }}>Total Wallet Balances</Title>
          </Col>
          <Col xs={24} sm={8}>
            <Card style={{ backgroundColor: '#1E3A5F', border: '1px solid #334155' }}>
              <Statistic
                title="Total Balance (All Wallets)"
                value={walletStats.totalBalance}
                prefix={<WalletOutlined />}
                formatter={(value) => formatCurrency(value as number)}
                valueStyle={{ color: '#22C55E' }}
              />
              <div style={{ marginTop: 8, fontSize: 12, color: '#94A3B8' }}>
                <Tag color="blue">{walletStats.walletCount} wallets</Tag>
              </div>
            </Card>
          </Col>
          <Col xs={24} sm={8}>
            <Card style={{ backgroundColor: '#1E3A5F', border: '1px solid #334155' }}>
              <Statistic
                title="Owner Wallets Balance"
                value={walletStats.ownerBalance}
                prefix={<ShopOutlined />}
                formatter={(value) => formatCurrency(value as number)}
                valueStyle={{ color: '#3B82F6' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={8}>
            <Card style={{ backgroundColor: '#1E3A5F', border: '1px solid #334155' }}>
              <Statistic
                title="Shipper Wallets Balance"
                value={walletStats.shipperBalance}
                prefix={<UserOutlined />}
                formatter={(value) => formatCurrency(value as number)}
                valueStyle={{ color: '#8B5CF6' }}
              />
            </Card>
          </Col>
        </Row>
      )}
    </div>
  );
}
