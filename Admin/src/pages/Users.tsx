import { useEffect, useState } from 'react';
import {
  Table,
  Button,
  Space,
  Tag,
  Input,
  Select,
  Typography,
  Modal,
  message,
  Tooltip,
  Card,
} from 'antd';
import {
  SearchOutlined,
  StopOutlined,
  CheckCircleOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import api from '../api/client';
import type { User, PaginatedResponse, ListUsersQuery } from '../types';
import dayjs from 'dayjs';

const { Title } = Typography;
const { confirm } = Modal;

export default function Users() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0,
  });
  const [filters, setFilters] = useState<ListUsersQuery>({});

  useEffect(() => {
    loadUsers();
  }, [pagination.current, pagination.pageSize, filters.role, filters.status, filters.search]);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const params: ListUsersQuery = {
        page: pagination.current,
        limit: pagination.pageSize,
        ...filters,
      };

      const response = await api.get<PaginatedResponse<User>>('/admin/users', { params });
      setUsers(response.data.data);
      setPagination((prev) => ({
        ...prev,
        total: response.data.pagination.total,
      }));
    } catch (error: any) {
      console.error('Failed to load users:', error);
      message.error('Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (userId: string, status: 'ACTIVE' | 'BANNED') => {
    const user = users.find((u) => u.id === userId);
    const actionText = status === 'BANNED' ? 'ban' : 'unban';

    confirm({
      title: `Confirm ${actionText} user`,
      content: `Are you sure you want to ${actionText} ${user?.email}?`,
      okText: 'Yes',
      okType: status === 'BANNED' ? 'danger' : 'primary',
      cancelText: 'No',
      onOk: async () => {
        try {
          await api.put(`/admin/users/${userId}/status`, {
            status,
            reason: status === 'BANNED' ? 'Banned by admin' : 'Unbanned by admin',
          });
          message.success(`User ${actionText}ned successfully`);
          loadUsers();
        } catch (error: any) {
          console.error(`Failed to ${actionText} user:`, error);
          message.error(`Failed to ${actionText} user`);
        }
      },
    });
  };

  const handleTableChange = (newPagination: TablePaginationConfig) => {
    setPagination({
      current: newPagination.current || 1,
      pageSize: newPagination.pageSize || 20,
      total: pagination.total,
    });
  };

  const columns: ColumnsType<User> = [
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
      width: 250,
    },
    {
      title: 'Display Name',
      dataIndex: 'displayName',
      key: 'displayName',
      render: (text) => text || '-',
    },
    {
      title: 'Phone',
      dataIndex: 'phoneNumber',
      key: 'phoneNumber',
      render: (text) => text || '-',
    },
    {
      title: 'Role',
      dataIndex: 'role',
      key: 'role',
      render: (role: string) => {
        const colors: Record<string, string> = {
          ADMIN: 'red',
          OWNER: 'blue',
          SHIPPER: 'green',
          CUSTOMER: 'default',
        };
        return <Tag color={colors[role]}>{role}</Tag>;
      },
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'ACTIVE' ? 'success' : 'error'}>{status}</Tag>
      ),
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => dayjs(date).format('DD/MM/YYYY HH:mm'),
    },
    {
      title: 'Action',
      key: 'action',
      render: (_, record) => (
        <Space>
          {record.status === 'ACTIVE' ? (
            <Tooltip title="Ban user">
              <Button
                type="text"
                danger
                icon={<StopOutlined />}
                onClick={() => handleStatusChange(record.id, 'BANNED')}
                disabled={record.role === 'ADMIN'}
              >
                Ban
              </Button>
            </Tooltip>
          ) : (
            <Tooltip title="Unban user">
              <Button
                type="text"
                icon={<CheckCircleOutlined />}
                onClick={() => handleStatusChange(record.id, 'ACTIVE')}
              >
                Unban
              </Button>
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={2} style={{ margin: 0 }}>Users Management</Title>
        <Button icon={<ReloadOutlined />} onClick={loadUsers}>
          Refresh
        </Button>
      </div>

      <Card style={{ marginBottom: 16 }}>
        <Space size="middle" wrap>
          <Input
            placeholder="Search by name or email"
            prefix={<SearchOutlined />}
            style={{ width: 250 }}
            onChange={(e) => {
              setFilters((prev) => ({ ...prev, search: e.target.value || undefined }));
              setPagination((prev) => ({ ...prev, current: 1 }));
            }}
            allowClear
          />
          <Select
            placeholder="Filter by role"
            style={{ width: 150 }}
            onChange={(value) => {
              setFilters((prev) => ({ ...prev, role: value }));
              setPagination((prev) => ({ ...prev, current: 1 }));
            }}
            allowClear
          >
            <Select.Option value="CUSTOMER">Customer</Select.Option>
            <Select.Option value="OWNER">Owner</Select.Option>
            <Select.Option value="SHIPPER">Shipper</Select.Option>
          </Select>
          <Select
            placeholder="Filter by status"
            style={{ width: 150 }}
            onChange={(value) => {
              setFilters((prev) => ({ ...prev, status: value }));
              setPagination((prev) => ({ ...prev, current: 1 }));
            }}
            allowClear
          >
            <Select.Option value="ACTIVE">Active</Select.Option>
            <Select.Option value="BANNED">Banned</Select.Option>
          </Select>
        </Space>
      </Card>

      <Table
        columns={columns}
        dataSource={users}
        rowKey="id"
        loading={loading}
        pagination={pagination}
        onChange={handleTableChange}
        scroll={{ x: 1000 }}
      />
    </div>
  );
}
