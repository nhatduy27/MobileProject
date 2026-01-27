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
  Image,
  Card,
  Rate,
} from 'antd';
import {
  SearchOutlined,
  StopOutlined,
  CheckCircleOutlined,
  ReloadOutlined,
  ShopOutlined,
} from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import api from '../api/client';
import type { Shop, PaginatedResponse, ListShopsQuery } from '../types';
import dayjs from 'dayjs';

const { Title } = Typography;
const { confirm } = Modal;

export default function Shops() {
  const [shops, setShops] = useState<Shop[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0,
  });
  const [filters, setFilters] = useState<ListShopsQuery>({});

  useEffect(() => {
    loadShops();
  }, [pagination.current, pagination.pageSize, filters.status, filters.search]);

  const loadShops = async () => {
    try {
      setLoading(true);
      const params: ListShopsQuery = {
        page: pagination.current,
        limit: pagination.pageSize,
        ...filters,
      };

      const response = await api.get<any>('/admin/shops', { params });
      
      const responseData = response.data;
      let shopsArray: Shop[] = [];
      let totalCount = 0;

      // Handle different response formats from backend
      if (Array.isArray(responseData)) {
        shopsArray = responseData;
        totalCount = responseData.length;
      } else if (Array.isArray(responseData.data)) {
        shopsArray = responseData.data;
        totalCount = responseData.pagination?.total || responseData.data.length;
      } else if (responseData.data && Array.isArray(responseData.data.shops)) {
        // Wrapped in { data: { shops: [...], total: N } }
        shopsArray = responseData.data.shops;
        totalCount = responseData.data.total || responseData.data.shops.length;
      } else if (responseData.data && Array.isArray(responseData.data.data)) {
        // Wrapped in { data: { data: [...] } }
        shopsArray = responseData.data.data;
        totalCount = responseData.data.pagination?.total || responseData.data.data.length;
      } else if (responseData.shops && Array.isArray(responseData.shops)) {
        shopsArray = responseData.shops;
        totalCount = responseData.total || responseData.shops.length;
      }

      setShops(shopsArray);
      setPagination((prev) => ({
        ...prev,
        total: totalCount,
      }));
    } catch (error: any) {
      console.error('Failed to load shops:', error);
      message.error('Failed to load shops');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (
    shopId: string,
    status: 'OPEN' | 'SUSPENDED' | 'BANNED'
  ) => {
    const shop = shops.find((s) => s.id === shopId);
    const statusText = status.toLowerCase();

    confirm({
      title: `Confirm change shop status`,
      content: `Are you sure you want to change ${shop?.name} status to ${statusText}?`,
      okText: 'Yes',
      okType: status === 'BANNED' ? 'danger' : 'primary',
      cancelText: 'No',
      onOk: async () => {
        try {
          await api.put(`/admin/shops/${shopId}/status`, {
            status,
            reason: `Status changed to ${status} by admin`,
          });
          message.success(`Shop status changed successfully`);
          loadShops();
        } catch (error: any) {
          console.error('Failed to change shop status:', error);
          message.error('Failed to change shop status');
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

  const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
      OPEN: 'success',
      CLOSED: 'default',
      SUSPENDED: 'warning',
      BANNED: 'error',
      PENDING_APPROVAL: 'processing',
    };
    return colors[status] || 'default';
  };

  const columns: ColumnsType<Shop> = [
    {
      title: 'Logo',
      dataIndex: 'logoUrl',
      key: 'logoUrl',
      width: 80,
      render: (url: string, record) => (
        url ? (
          <Image
            src={url}
            alt={record.name}
            width={50}
            height={50}
            style={{ objectFit: 'cover', borderRadius: 4 }}
            fallback="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
          />
        ) : (
          <div
            style={{
              width: 50,
              height: 50,
              background: '#f0f0f0',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              borderRadius: 4,
            }}
          >
            <ShopOutlined style={{ fontSize: 24, color: '#999' }} />
          </div>
        )
      ),
    },
    {
      title: 'Shop Name',
      dataIndex: 'name',
      key: 'name',
      width: 200,
    },
    {
      title: 'Owner',
      key: 'owner',
      render: (_, record) => (
        <div>
          <div>{record.ownerName || '-'}</div>
          <div style={{ fontSize: 12, color: '#999' }}>{record.ownerEmail}</div>
        </div>
      ),
    },
    {
      title: 'Category',
      dataIndex: 'categoryName',
      key: 'categoryName',
      render: (text) => text || '-',
    },
    {
      title: 'Rating',
      dataIndex: 'rating',
      key: 'rating',
      render: (rating: number) => <Rate disabled value={rating} allowHalf />,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => <Tag color={getStatusColor(status)}>{status}</Tag>,
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => dayjs(date).format('DD/MM/YYYY'),
    },
    {
      title: 'Action',
      key: 'action',
      fixed: 'right',
      width: 200,
      render: (_, record) => (
        <Space>
          {record.status === 'PENDING_APPROVAL' && (
            <Button
              type="primary"
              size="small"
              icon={<CheckCircleOutlined />}
              onClick={() => handleStatusChange(record.id, 'OPEN')}
            >
              Approve
            </Button>
          )}
          {(record.status === 'OPEN' || record.status === 'CLOSED') && (
            <Button
              size="small"
              danger
              icon={<StopOutlined />}
              onClick={() => handleStatusChange(record.id, 'SUSPENDED')}
            >
              Suspend
            </Button>
          )}
          {record.status === 'SUSPENDED' && (
            <>
              <Button
                type="primary"
                size="small"
                icon={<CheckCircleOutlined />}
                onClick={() => handleStatusChange(record.id, 'OPEN')}
              >
                Activate
              </Button>
              <Button
                size="small"
                danger
                onClick={() => handleStatusChange(record.id, 'BANNED')}
              >
                Ban
              </Button>
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={2} style={{ margin: 0 }}>Shops Management</Title>
        <Button icon={<ReloadOutlined />} onClick={loadShops}>
          Refresh
        </Button>
      </div>

      <Card style={{ marginBottom: 16 }}>
        <Space size="middle" wrap>
          <Input
            placeholder="Search by shop name"
            prefix={<SearchOutlined />}
            style={{ width: 250 }}
            onChange={(e) => {
              setFilters((prev) => ({ ...prev, search: e.target.value || undefined }));
              setPagination((prev) => ({ ...prev, current: 1 }));
            }}
            allowClear
          />
          <Select
            placeholder="Filter by status"
            style={{ width: 180 }}
            onChange={(value) => {
              setFilters((prev) => ({ ...prev, status: value }));
              setPagination((prev) => ({ ...prev, current: 1 }));
            }}
            allowClear
          >
            <Select.Option value="OPEN">Open</Select.Option>
            <Select.Option value="CLOSED">Closed</Select.Option>
            <Select.Option value="SUSPENDED">Suspended</Select.Option>
            <Select.Option value="BANNED">Banned</Select.Option>
            <Select.Option value="PENDING_APPROVAL">Pending Approval</Select.Option>
          </Select>
        </Space>
      </Card>

      <Table
        columns={columns}
        dataSource={shops}
        rowKey="id"
        loading={loading}
        pagination={pagination}
        onChange={handleTableChange}
        scroll={{ x: 1200 }}
      />
    </div>
  );
}
