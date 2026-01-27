import { useEffect, useState } from 'react';
import {
  Table,
  Button,
  Space,
  Tag,
  Typography,
  Modal,
  Form,
  Input,
  InputNumber,
  Switch,
  Select,
  DatePicker,
  message,
  Popconfirm,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import api from '../api/client';
import type { ApiResponse } from '../types';

const { Title } = Typography;
const { RangePicker } = DatePicker;

interface Voucher {
  id: string;
  code: string;
  name: string;
  description?: string;
  discountType: 'PERCENTAGE' | 'FIXED';
  discountValue: number;
  maxDiscount?: number;
  minOrderAmount: number;
  usageLimit: number;
  currentUsage: number;
  usageLimitPerUser: number;
  validFrom: string;
  validTo: string;
  isActive: boolean;
  ownerType: 'PLATFORM' | 'SHOP';
  shopId?: string;
  shopName?: string;
  createdAt: string;
  updatedAt: string;
}

interface VoucherFormData {
  code: string;
  name: string;
  description?: string;
  discountType: 'PERCENTAGE' | 'FIXED';
  discountValue: number;
  maxDiscount?: number;
  minOrderAmount: number;
  usageLimit: number;
  usageLimitPerUser: number;
  validFrom: string;
  validTo: string;
  isActive: boolean;
  ownerType: 'PLATFORM' | 'SHOP';
  shopId?: string;
}

export default function Vouchers() {
  const [vouchers, setVouchers] = useState<Voucher[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingVoucher, setEditingVoucher] = useState<Voucher | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadVouchers();
  }, []);

  const loadVouchers = async () => {
    try {
      setLoading(true);
      // TODO: Implement admin voucher list API
      // For now, this will fail - need backend implementation
      const response = await api.get<ApiResponse<Voucher[]>>('/admin/vouchers');
      setVouchers(response.data.data || []);
    } catch (error: any) {
      console.error('Failed to load vouchers:', error);
      message.error('Failed to load vouchers - API not yet implemented');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingVoucher(null);
    form.resetFields();
    form.setFieldsValue({
      isActive: true,
      discountType: 'PERCENTAGE',
      ownerType: 'PLATFORM',
      usageLimit: 100,
      usageLimitPerUser: 1,
      minOrderAmount: 0,
    });
    setModalVisible(true);
  };

  const handleEdit = (voucher: Voucher) => {
    setEditingVoucher(voucher);
    form.setFieldsValue({
      ...voucher,
      validFrom: dayjs(voucher.validFrom),
      validTo: dayjs(voucher.validTo),
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      await api.delete(`/admin/vouchers/${id}`);
      message.success('Voucher deleted successfully');
      loadVouchers();
    } catch (error: any) {
      console.error('Failed to delete voucher:', error);
      message.error(error.message || 'Failed to delete voucher');
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      const payload: VoucherFormData = {
        ...values,
        validFrom: values.validFrom.toISOString(),
        validTo: values.validTo.toISOString(),
      };

      if (editingVoucher) {
        await api.put(`/admin/vouchers/${editingVoucher.id}`, payload);
        message.success('Voucher updated successfully');
      } else {
        await api.post('/admin/vouchers', payload);
        message.success('Voucher created successfully');
      }
      setModalVisible(false);
      loadVouchers();
    } catch (error: any) {
      console.error('Failed to save voucher:', error);
      message.error(error.message || 'Failed to save voucher');
    }
  };

  const handleToggleStatus = async (id: string, isActive: boolean) => {
    try {
      await api.put(`/admin/vouchers/${id}/status`, { isActive });
      message.success(`Voucher ${isActive ? 'activated' : 'deactivated'} successfully`);
      loadVouchers();
    } catch (error: any) {
      console.error('Failed to update voucher status:', error);
      message.error(error.message || 'Failed to update voucher status');
    }
  };

  const columns: ColumnsType<Voucher> = [
    {
      title: 'Code',
      dataIndex: 'code',
      key: 'code',
      render: (text) => <strong>{text}</strong>,
    },
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Type',
      dataIndex: 'ownerType',
      key: 'ownerType',
      render: (type: string) => (
        <Tag color={type === 'PLATFORM' ? 'blue' : 'green'}>
          {type}
        </Tag>
      ),
    },
    {
      title: 'Discount',
      key: 'discount',
      render: (_, record) => (
        <div>
          {record.discountType === 'PERCENTAGE' 
            ? `${record.discountValue}%` 
            : `${record.discountValue.toLocaleString()}đ`}
          {record.maxDiscount && (
            <div style={{ fontSize: 12, color: '#999' }}>
              Max: {record.maxDiscount.toLocaleString()}đ
            </div>
          )}
        </div>
      ),
    },
    {
      title: 'Min Order',
      dataIndex: 'minOrderAmount',
      key: 'minOrderAmount',
      render: (amount) => `${amount.toLocaleString()}đ`,
    },
    {
      title: 'Usage',
      key: 'usage',
      render: (_, record) => (
        <div>
          <div>{record.currentUsage} / {record.usageLimit}</div>
          <div style={{ fontSize: 12, color: '#999' }}>
            Per user: {record.usageLimitPerUser}
          </div>
        </div>
      ),
    },
    {
      title: 'Valid Period',
      key: 'validPeriod',
      render: (_, record) => (
        <div>
          <div>{dayjs(record.validFrom).format('DD/MM/YYYY')}</div>
          <div>{dayjs(record.validTo).format('DD/MM/YYYY')}</div>
        </div>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (isActive: boolean, record) => (
        <Switch
          checked={isActive}
          onChange={(checked) => handleToggleStatus(record.id, checked)}
        />
      ),
    },
    {
      title: 'Action',
      key: 'action',
      fixed: 'right',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            Edit
          </Button>
          <Popconfirm
            title="Delete voucher?"
            description="This action cannot be undone."
            onConfirm={() => handleDelete(record.id)}
            okText="Yes"
            cancelText="No"
            okButtonProps={{ danger: true }}
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              Delete
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={2} style={{ margin: 0 }}>Vouchers Management</Title>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadVouchers}>
            Refresh
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            Add Voucher
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={vouchers}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 20 }}
        scroll={{ x: 1200 }}
      />

      <Modal
        title={editingVoucher ? 'Edit Voucher' : 'Create Voucher'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={700}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            label="Voucher Code"
            name="code"
            rules={[
              { required: true, message: 'Please enter voucher code' },
              { pattern: /^[A-Z0-9]+$/, message: 'Code must be uppercase letters and numbers only' },
            ]}
          >
            <Input placeholder="e.g., FREESHIP10" maxLength={20} />
          </Form.Item>

          <Form.Item
            label="Name"
            name="name"
            rules={[{ required: true, message: 'Please enter voucher name' }]}
          >
            <Input placeholder="e.g., Free Shipping 10k" />
          </Form.Item>

          <Form.Item label="Description" name="description">
            <Input.TextArea rows={2} placeholder="e.g., Miễn phí ship cho đơn từ 50k" />
          </Form.Item>

          <Form.Item
            label="Owner Type"
            name="ownerType"
            rules={[{ required: true }]}
          >
            <Select>
              <Select.Option value="PLATFORM">Platform (Admin)</Select.Option>
              <Select.Option value="SHOP">Shop Owner</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            label="Discount Type"
            name="discountType"
            rules={[{ required: true }]}
          >
            <Select>
              <Select.Option value="PERCENTAGE">Percentage (%)</Select.Option>
              <Select.Option value="FIXED">Fixed Amount (đ)</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            label="Discount Value"
            name="discountValue"
            rules={[
              { required: true, message: 'Please enter discount value' },
              { type: 'number', min: 0, message: 'Must be >= 0' },
            ]}
          >
            <InputNumber style={{ width: '100%' }} placeholder="e.g., 10 (for 10% or 10,000đ)" />
          </Form.Item>

          <Form.Item
            label="Max Discount (đ) - for percentage only"
            name="maxDiscount"
          >
            <InputNumber style={{ width: '100%' }} min={0} placeholder="e.g., 50000" />
          </Form.Item>

          <Form.Item
            label="Min Order Amount (đ)"
            name="minOrderAmount"
            rules={[{ required: true }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} placeholder="e.g., 50000" />
          </Form.Item>

          <Form.Item
            label="Total Usage Limit"
            name="usageLimit"
            rules={[{ required: true }]}
          >
            <InputNumber style={{ width: '100%' }} min={1} placeholder="e.g., 100" />
          </Form.Item>

          <Form.Item
            label="Usage Limit Per User"
            name="usageLimitPerUser"
            rules={[{ required: true }]}
          >
            <InputNumber style={{ width: '100%' }} min={1} placeholder="e.g., 1" />
          </Form.Item>

          <Form.Item
            label="Valid From"
            name="validFrom"
            rules={[{ required: true, message: 'Please select start date' }]}
          >
            <DatePicker style={{ width: '100%' }} showTime />
          </Form.Item>

          <Form.Item
            label="Valid To"
            name="validTo"
            rules={[{ required: true, message: 'Please select end date' }]}
          >
            <DatePicker style={{ width: '100%' }} showTime />
          </Form.Item>

          <Form.Item label="Active" name="isActive" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
