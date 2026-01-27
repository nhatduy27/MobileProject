import { useEffect, useState } from 'react';
import {
  Table,
  Button,
  Space,
  Tag,
  Select,
  Typography,
  Modal,
  Form,
  Input,
  message,
  Card,
  Descriptions,
} from 'antd';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  DollarOutlined,
  ReloadOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import api from '../api/client';
import type { Payout, PaginatedResponse, ListPayoutsQuery } from '../types';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { TextArea } = Input;

export default function Payouts() {
  const [payouts, setPayouts] = useState<Payout[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0,
  });
  const [filters, setFilters] = useState<ListPayoutsQuery>({
    status: 'PENDING',
  });
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [actionModalVisible, setActionModalVisible] = useState(false);
  const [selectedPayout, setSelectedPayout] = useState<Payout | null>(null);
  const [actionType, setActionType] = useState<'approve' | 'reject' | 'transferred'>('approve');
  const [form] = Form.useForm();

  useEffect(() => {
    loadPayouts();
  }, [pagination.current, pagination.pageSize, filters.status]);

  const loadPayouts = async () => {
    try {
      setLoading(true);
      const params: ListPayoutsQuery = {
        page: pagination.current,
        limit: pagination.pageSize,
        ...filters,
      };

      const response = await api.get<PaginatedResponse<Payout>>('/admin/payouts', { params });
      setPayouts(response.data.data);
      setPagination((prev) => ({
        ...prev,
        total: response.data.pagination?.total || 0,
      }));
    } catch (error: any) {
      console.error('Failed to load payouts:', error);
      message.error('Failed to load payouts');
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async (payout: Payout, type: 'approve' | 'reject' | 'transferred') => {
    setSelectedPayout(payout);
    setActionType(type);
    form.resetFields();
    setActionModalVisible(true);
  };

  const handleSubmitAction = async (values: any) => {
    if (!selectedPayout) return;

    try {
      let endpoint = '';
      let payload = {};

      switch (actionType) {
        case 'approve':
          endpoint = `/admin/payouts/${selectedPayout.id}/approve`;
          break;
        case 'reject':
          endpoint = `/admin/payouts/${selectedPayout.id}/reject`;
          payload = { reason: values.reason };
          break;
        case 'transferred':
          endpoint = `/admin/payouts/${selectedPayout.id}/transferred`;
          payload = { transferNote: values.transferNote };
          break;
      }

      await api.post(endpoint, payload);
      message.success(`Payout ${actionType}d successfully`);
      setActionModalVisible(false);
      loadPayouts();
    } catch (error: any) {
      console.error(`Failed to ${actionType} payout:`, error);
      message.error(`Failed to ${actionType} payout`);
    }
  };

  const handleTableChange = (newPagination: TablePaginationConfig) => {
    setPagination({
      current: newPagination.current || 1,
      pageSize: newPagination.pageSize || 20,
      total: pagination.total,
    });
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount);
  };

  const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
      PENDING: 'processing',
      APPROVED: 'success',
      REJECTED: 'error',
      TRANSFERRED: 'default',
    };
    return colors[status] || 'default';
  };

  const columns: ColumnsType<Payout> = [
    {
      title: 'User',
      key: 'user',
      render: (_, record) => (
        <div>
          <div style={{ fontWeight: 500 }}>{record.userName || '-'}</div>
          <div style={{ fontSize: 12, color: '#999' }}>{record.userEmail}</div>
        </div>
      ),
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount: number) => (
        <Text strong style={{ color: '#cf1322' }}>
          {formatCurrency(amount)}
        </Text>
      ),
    },
    {
      title: 'Bank Info',
      key: 'bankInfo',
      render: (_, record) => (
        <div>
          <div>{record.bankName || '-'}</div>
          <div style={{ fontSize: 12, color: '#999' }}>
            {record.bankAccountNumber || '-'}
          </div>
          <div style={{ fontSize: 12, color: '#999' }}>
            {record.bankAccountName || '-'}
          </div>
        </div>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => <Tag color={getStatusColor(status)}>{status}</Tag>,
    },
    {
      title: 'Requested At',
      dataIndex: 'requestedAt',
      key: 'requestedAt',
      render: (date: string) => dayjs(date).format('DD/MM/YYYY HH:mm'),
    },
    {
      title: 'Action',
      key: 'action',
      fixed: 'right',
      width: 200,
      render: (_, record) => (
        <Space direction="vertical" size="small" style={{ width: '100%' }}>
          <Button
            size="small"
            icon={<EyeOutlined />}
            onClick={() => {
              setSelectedPayout(record);
              setDetailModalVisible(true);
            }}
          >
            View Details
          </Button>
          {record.status === 'PENDING' && (
            <Space size="small">
              <Button
                type="primary"
                size="small"
                icon={<CheckCircleOutlined />}
                onClick={() => handleAction(record, 'approve')}
              >
                Approve
              </Button>
              <Button
                danger
                size="small"
                icon={<CloseCircleOutlined />}
                onClick={() => handleAction(record, 'reject')}
              >
                Reject
              </Button>
            </Space>
          )}
          {record.status === 'APPROVED' && (
            <Button
              type="primary"
              size="small"
              icon={<DollarOutlined />}
              onClick={() => handleAction(record, 'transferred')}
            >
              Mark Transferred
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={2} style={{ margin: 0 }}>Payouts Management</Title>
        <Button icon={<ReloadOutlined />} onClick={loadPayouts}>
          Refresh
        </Button>
      </div>

      <Card style={{ marginBottom: 16 }}>
        <Space size="middle" wrap>
          <Select
            placeholder="Filter by status"
            style={{ width: 180 }}
            value={filters.status}
            onChange={(value) => {
              setFilters((prev) => ({ ...prev, status: value }));
              setPagination((prev) => ({ ...prev, current: 1 }));
            }}
          >
            <Select.Option value="PENDING">Pending</Select.Option>
            <Select.Option value="APPROVED">Approved</Select.Option>
            <Select.Option value="REJECTED">Rejected</Select.Option>
            <Select.Option value="TRANSFERRED">Transferred</Select.Option>
          </Select>
        </Space>
      </Card>

      <Table
        columns={columns}
        dataSource={payouts}
        rowKey="id"
        loading={loading}
        pagination={pagination}
        onChange={handleTableChange}
        scroll={{ x: 1200 }}
      />

      {/* Detail Modal */}
      <Modal
        title="Payout Details"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setDetailModalVisible(false)}>
            Close
          </Button>,
        ]}
        width={600}
      >
        {selectedPayout && (
          <Descriptions column={1} bordered>
            <Descriptions.Item label="User">{selectedPayout.userName}</Descriptions.Item>
            <Descriptions.Item label="Email">{selectedPayout.userEmail}</Descriptions.Item>
            <Descriptions.Item label="Amount">
              <Text strong style={{ color: '#cf1322' }}>
                {formatCurrency(selectedPayout.amount)}
              </Text>
            </Descriptions.Item>
            <Descriptions.Item label="Bank Name">{selectedPayout.bankName}</Descriptions.Item>
            <Descriptions.Item label="Account Number">
              {selectedPayout.bankAccountNumber}
            </Descriptions.Item>
            <Descriptions.Item label="Account Name">
              {selectedPayout.bankAccountName}
            </Descriptions.Item>
            <Descriptions.Item label="Status">
              <Tag color={getStatusColor(selectedPayout.status)}>{selectedPayout.status}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Requested At">
              {dayjs(selectedPayout.requestedAt).format('DD/MM/YYYY HH:mm')}
            </Descriptions.Item>
            {selectedPayout.processedAt && (
              <Descriptions.Item label="Processed At">
                {dayjs(selectedPayout.processedAt).format('DD/MM/YYYY HH:mm')}
              </Descriptions.Item>
            )}
            {selectedPayout.reason && (
              <Descriptions.Item label="Reason">{selectedPayout.reason}</Descriptions.Item>
            )}
            {selectedPayout.transferNote && (
              <Descriptions.Item label="Transfer Note">
                {selectedPayout.transferNote}
              </Descriptions.Item>
            )}
          </Descriptions>
        )}
      </Modal>

      {/* Action Modal */}
      <Modal
        title={`${actionType.charAt(0).toUpperCase() + actionType.slice(1)} Payout`}
        open={actionModalVisible}
        onCancel={() => setActionModalVisible(false)}
        onOk={() => form.submit()}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmitAction}>
          {actionType === 'reject' && (
            <Form.Item
              label="Reason"
              name="reason"
              rules={[{ required: true, message: 'Please enter rejection reason' }]}
            >
              <TextArea rows={4} placeholder="Enter reason for rejection" />
            </Form.Item>
          )}
          {actionType === 'transferred' && (
            <Form.Item
              label="Transfer Note"
              name="transferNote"
              rules={[{ required: true, message: 'Please enter transfer note' }]}
            >
              <Input placeholder="e.g., KTX_PAYOUT_20260127_001" />
            </Form.Item>
          )}
          {actionType === 'approve' && (
            <div>Are you sure you want to approve this payout request?</div>
          )}
        </Form>
      </Modal>
    </div>
  );
}
