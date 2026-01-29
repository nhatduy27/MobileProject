import { useEffect, useState, useRef, useCallback } from 'react';
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
  Alert,
  Progress,
  Image,
  Spin,
} from 'antd';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  DollarOutlined,
  ReloadOutlined,
  EyeOutlined,
  QrcodeOutlined,
  LoadingOutlined,
  SyncOutlined,
} from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { collection, query, where, onSnapshot, orderBy, limit } from 'firebase/firestore';
import { db } from '../config/firebase';
import api from '../api/client';
import type { Payout, ListPayoutsQuery } from '../types';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { TextArea } = Input;

// Extended Payout type with qrUrl
interface PayoutWithQr extends Payout {
  qrUrl?: string;
}

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
  
  // Detail modal
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedPayout, setSelectedPayout] = useState<Payout | null>(null);
  
  // Reject/Transfer action modal
  const [actionModalVisible, setActionModalVisible] = useState(false);
  const [actionType, setActionType] = useState<'reject' | 'transferred'>('reject');
  const [form] = Form.useForm();
  
  // QR Transfer modal state
  const [qrModalVisible, setQrModalVisible] = useState(false);
  const [qrPayout, setQrPayout] = useState<PayoutWithQr | null>(null);
  const [isPolling, setIsPolling] = useState(false);
  const [pollingAttempt, setPollingAttempt] = useState(0);
  const [pollingMessage, setPollingMessage] = useState('');
  // Polling ref to track and cancel polling
  const pollingRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const pollingAttemptRef = useRef(0);
  const maxPollingAttempts = 20;

  // Memoize loadPayouts to use in realtime listener
  const loadPayouts = useCallback(async () => {
    try {
      setLoading(true);
      const params: ListPayoutsQuery = {
        page: pagination.current,
        limit: pagination.pageSize,
        ...filters,
      };

      const response = await api.get<any>('/admin/payouts', { params });
      
      // Handle different response formats from backend
      const responseData = response.data;
      let payoutsArray: Payout[] = [];
      let totalCount = 0;

      console.log('Payouts API Response:', responseData);

      if (Array.isArray(responseData)) {
        payoutsArray = responseData;
        totalCount = responseData.length;
      } else if (Array.isArray(responseData.data)) {
        payoutsArray = responseData.data;
        totalCount = responseData.pagination?.total || responseData.data.length;
      } else if (responseData.data && Array.isArray(responseData.data.payouts)) {
        payoutsArray = responseData.data.payouts;
        totalCount = responseData.data.total || responseData.data.payouts.length;
      } else if (responseData.data && Array.isArray(responseData.data.data)) {
        payoutsArray = responseData.data.data;
        totalCount = responseData.data.pagination?.total || responseData.data.data.length;
      } else if (responseData.payouts && Array.isArray(responseData.payouts)) {
        payoutsArray = responseData.payouts;
        totalCount = responseData.total || responseData.payouts.length;
      }

      console.log('Parsed payoutsArray:', payoutsArray, 'Total:', totalCount);

      setPayouts(payoutsArray);
      setPagination((prev) => ({
        ...prev,
        total: totalCount,
      }));
    } catch (error: any) {
      console.error('Failed to load payouts:', error);
      message.error('Failed to load payouts');
    } finally {
      setLoading(false);
    }
  }, [pagination.current, pagination.pageSize, filters]);

  // Load payouts when dependencies change
  useEffect(() => {
    loadPayouts();
  }, [loadPayouts]);

  // ========================================
  // REALTIME: Listen to Firestore for auto-refresh
  // ========================================
  useEffect(() => {
    // Build query based on current filter
    const statusFilter = filters.status || 'PENDING';
    const payoutsQuery = query(
      collection(db, 'payoutRequests'),
      where('status', '==', statusFilter),
      orderBy('requestedAt', 'desc'),
      limit(100)
    );

    // Listen for changes
    const unsubscribe = onSnapshot(
      payoutsQuery,
      (snapshot) => {
        // When data changes, reload from API (to get full data with joins)
        // Use a small delay to batch rapid changes
        const timeoutId = setTimeout(() => {
          loadPayouts();
        }, 500);
        return () => clearTimeout(timeoutId);
      },
      (error) => {
        console.error('Firestore listener error:', error);
      }
    );

    return () => unsubscribe();
  }, [filters.status, loadPayouts]);

  // Cleanup polling on unmount
  useEffect(() => {
    return () => {
      if (pollingRef.current) {
        clearTimeout(pollingRef.current);
      }
    };
  }, []);

  // Auto-start polling when QR modal opens (after 3 seconds delay)
  useEffect(() => {
    if (qrModalVisible && qrPayout && !isPolling) {
      const timeout = setTimeout(() => {
        startPolling();
      }, 3000);
      return () => clearTimeout(timeout);
    }
  }, [qrModalVisible, qrPayout]);

  // Generate QR URL for payout (SePay format) - defined early for use in handleProcess
  const generateQrUrl = (payout: Payout) => {
    const content = `PAYOUT${payout.id?.substring(0, 8).toUpperCase() || ''}`;
    // Use backend field names first, fallback to legacy names
    const bank = payout.bankCode || payout.bankName || '';
    const acc = payout.accountNumber || payout.bankAccountNumber || '';
    const amount = payout.amount || 0;
    // Use correct SePay QR template URL format
    return `https://qr.sepay.vn/img?acc=${acc}&bank=${bank}&amount=${amount}&des=${encodeURIComponent(content)}&template=compact`;
  };

  // Process payout - shows QR modal WITHOUT calling approve API yet
  // Status only changes when transfer is verified
  const handleProcess = (payout: Payout) => {
    console.log('Processing payout:', payout); // Debug log to see all fields
    // Generate QR URL locally for PENDING payout
    const qrUrl = generateQrUrl(payout);
    console.log('Generated QR URL:', qrUrl); // Debug log
    setQrPayout({ ...payout, qrUrl });
    setQrModalVisible(true);
    message.info('Vui lòng quét mã QR để chuyển tiền. Trạng thái sẽ chỉ thay đổi khi xác nhận được giao dịch.');
  };

  // Start polling for transfer verification
  // Only call approve API when transfer is actually detected
  const startPolling = async () => {
    if (!qrPayout) return;
    
    setIsPolling(true);
    setPollingAttempt(0);
    setPollingMessage('Đang kiểm tra giao dịch...');
    
    const poll = async (attempt: number) => {
      if (attempt >= maxPollingAttempts) {
        setIsPolling(false);
        setPollingMessage('Không phát hiện giao dịch. Vui lòng thử lại.');
        return;
      }
      
      try {
        setPollingAttempt(attempt + 1);
        setPollingMessage(`Đang kiểm tra... (Lần ${attempt + 1}/${maxPollingAttempts})`);
        
        // Only call verify - backend will auto-approve when transfer is detected
        // This prevents premature APPROVED status when admin cancels
        const verifyResponse = await api.post<any>(`/admin/payouts/${qrPayout.id}/verify`);
        const result = verifyResponse.data?.data || verifyResponse.data;
        
        console.log('Verify response:', result);
        
        if (result.matched) {
          // Success! Transfer detected and auto-approved
          setIsPolling(false);
          setPollingMessage('');
          message.success('Chuyển tiền thành công! Giao dịch đã được xác nhận.');
          setQrModalVisible(false);
          setQrPayout(null);
          loadPayouts();
          return;
        }
        
        // Not matched yet - continue polling
        pollingRef.current = setTimeout(() => poll(attempt + 1), 5000);
      } catch (error: any) {
        console.error('Poll error:', error);
        // Continue polling on error
        pollingRef.current = setTimeout(() => poll(attempt + 1), 5000);
      }
    };
    
    poll(0);
  };

  // Stop polling
  const stopPolling = () => {
    if (pollingRef.current) {
      clearTimeout(pollingRef.current);
    }
    setIsPolling(false);
    setPollingMessage('');
    setPollingAttempt(0);
  };

  // Manual mark as transferred
  const handleManualTransfer = async () => {
    if (!qrPayout) return;
    
    setSelectedPayout(qrPayout);
    setActionType('transferred');
    form.resetFields();
    setQrModalVisible(false);
    stopPolling();
    setActionModalVisible(true);
  };

  // Close QR modal - just close, no status change (payout stays PENDING)
  const closeQrModal = () => {
    stopPolling();
    setQrModalVisible(false);
    setQrPayout(null);
    // Don't reload - nothing changed since we didn't call approve API
  };

  // Handle reject action
  const handleReject = (payout: Payout) => {
    setSelectedPayout(payout);
    setActionType('reject');
    form.resetFields();
    setActionModalVisible(true);
  };

  // Handle mark transferred action (for APPROVED status from table)
  const handleMarkTransferred = (payout: Payout) => {
    setSelectedPayout(payout);
    setActionType('transferred');
    form.resetFields();
    setActionModalVisible(true);
  };

  const handleSubmitAction = async (values: any) => {
    if (!selectedPayout) return;

    try {
      let endpoint = '';
      let payload = {};

      switch (actionType) {
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
      message.success(`Payout ${actionType === 'reject' ? 'rejected' : 'marked as transferred'} successfully`);
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
      APPROVED: 'warning',
      REJECTED: 'error',
      TRANSFERRED: 'success',
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
          <div>{record.bankCode || record.bankName || '-'}</div>
          <div style={{ fontSize: 12, color: '#999' }}>
            {record.accountNumber || record.bankAccountNumber || '-'}
          </div>
          <div style={{ fontSize: 12, color: '#999' }}>
            {record.accountName || record.bankAccountName || '-'}
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
      width: 220,
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
                icon={<QrcodeOutlined />}
                onClick={() => handleProcess(record)}
              >
                Process
              </Button>
              <Button
                danger
                size="small"
                icon={<CloseCircleOutlined />}
                onClick={() => handleReject(record)}
              >
                Reject
              </Button>
            </Space>
          )}
        </Space>
      ),
    },
  ];

  // Generate expected transfer content
  const getTransferContent = (payoutId: string) => {
    return `PAYOUT${payoutId.substring(0, 8).toUpperCase()}`;
  };

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
            <Descriptions.Item label="Bank Name">{selectedPayout.bankCode || selectedPayout.bankName || '-'}</Descriptions.Item>
            <Descriptions.Item label="Account Number">
              {selectedPayout.accountNumber || selectedPayout.bankAccountNumber || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Account Name">
              {selectedPayout.accountName || selectedPayout.bankAccountName || '-'}
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

      {/* QR Transfer Modal */}
      <Modal
        title={
          <Space>
            <QrcodeOutlined />
            <span>Chuyển tiền cho {qrPayout?.userName || 'Owner/Shipper'}</span>
          </Space>
        }
        open={qrModalVisible}
        onCancel={closeQrModal}
        footer={null}
        width={500}
        maskClosable={!isPolling}
        closable={!isPolling}
      >
        {qrPayout && (
          <div style={{ textAlign: 'center' }}>
            {/* QR Code Image */}
            {qrPayout.qrUrl ? (
              <Image
                src={qrPayout.qrUrl}
                alt="Payout QR Code"
                style={{ maxWidth: 280, marginBottom: 16 }}
                fallback="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
              />
            ) : (
              <Alert
                message="QR không khả dụng"
                description="Vui lòng chuyển khoản thủ công theo thông tin bên dưới"
                type="warning"
                style={{ marginBottom: 16 }}
              />
            )}

            {/* Transfer Info */}
            <Descriptions column={1} bordered size="small" style={{ textAlign: 'left', marginBottom: 16 }}>
              <Descriptions.Item label="Số tiền">
                <Text strong style={{ color: '#cf1322', fontSize: 16 }}>
                  {formatCurrency(qrPayout.amount)}
                </Text>
              </Descriptions.Item>
              <Descriptions.Item label="Ngân hàng">{qrPayout.bankCode || qrPayout.bankName || '-'}</Descriptions.Item>
              <Descriptions.Item label="Số tài khoản">
                <Text copyable>{qrPayout.accountNumber || qrPayout.bankAccountNumber || '-'}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="Tên tài khoản">{qrPayout.accountName || qrPayout.bankAccountName || '-'}</Descriptions.Item>
              <Descriptions.Item label="Nội dung CK">
                <Text copyable strong style={{ color: '#1890ff' }}>
                  {getTransferContent(qrPayout.id)}
                </Text>
              </Descriptions.Item>
            </Descriptions>

            <Alert
              message="Quan trọng"
              description="Vui lòng nhập chính xác nội dung chuyển khoản để hệ thống tự động xác nhận."
              type="warning"
              showIcon
              style={{ marginBottom: 16, textAlign: 'left' }}
            />

            {/* Polling Progress */}
            {isPolling && (
              <div style={{ marginBottom: 16 }}>
                <Spin indicator={<LoadingOutlined style={{ fontSize: 24 }} spin />} />
                <div style={{ marginTop: 8 }}>{pollingMessage}</div>
                <Progress
                  percent={Math.round((pollingAttempt / maxPollingAttempts) * 100)}
                  status="active"
                  style={{ marginTop: 8 }}
                />
              </div>
            )}

            {/* Polling result message */}
            {!isPolling && pollingMessage && (
              <Alert
                message={pollingMessage}
                type="info"
                style={{ marginBottom: 16 }}
              />
            )}

            {/* Action Buttons */}
            <Space direction="vertical" style={{ width: '100%' }}>
              {isPolling ? (
                <Button
                  danger
                  block
                  onClick={() => {
                    stopPolling();
                  }}
                >
                  Hủy bỏ
                </Button>
              ) : (
                <>
                  <Button
                    type="primary"
                    size="large"
                    block
                    loading
                    icon={<SyncOutlined spin />}
                  >
                    Đang chờ xác nhận...
                  </Button>
                  <Button
                    block
                    onClick={closeQrModal}
                  >
                    Hủy bỏ
                  </Button>
                </>
              )}
            </Space>
          </div>
        )}
      </Modal>

      {/* Reject/Manual Transfer Action Modal */}
      <Modal
        title={actionType === 'reject' ? 'Reject Payout' : 'Mark as Transferred'}
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
              <Input placeholder="e.g., Đã CK lúc 22:30, mã GD: ABC123" />
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  );
}
