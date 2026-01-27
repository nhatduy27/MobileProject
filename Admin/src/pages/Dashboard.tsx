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

export default function Dashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get<{ data: DashboardStats }>('/admin/dashboard');
      setStats(response.data.data);
    } catch (err: any) {
      console.error('Failed to load dashboard:', err);
      setError(err.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
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

  return (
    <div>
      <Title level={2}>Dashboard Overview</Title>

      {/* User Statistics */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Users"
              value={stats.users.total}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
            <div style={{ marginTop: 8, fontSize: 12, color: '#666' }}>
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
          <Card>
            <Statistic
              title="Active Shops"
              value={stats.shops.active}
              prefix={<ShopOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Pending Approval"
              value={stats.shops.pendingApproval}
              prefix={<ShopOutlined />}
              valueStyle={{ color: '#faad14' }}
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
    </div>
  );
}
