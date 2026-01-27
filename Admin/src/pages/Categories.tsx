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
import api from '../api/client';
import type { Category, ApiResponse } from '../types';
import dayjs from 'dayjs';

const { Title } = Typography;

interface CategoryFormData {
  name: string;
  nameEn?: string;
  slug: string;
  icon?: string;
  description?: string;
  sortOrder: number;
  isActive: boolean;
}

export default function Categories() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      setLoading(true);
      const response = await api.get<any>('/admin/categories');
      
      // Handle different response formats from backend
      const responseData = response.data;
      let categoriesArray: Category[] = [];

      console.log('Categories API Response:', responseData);

      if (Array.isArray(responseData)) {
        // Direct array response
        categoriesArray = responseData;
      } else if (Array.isArray(responseData.data)) {
        // Wrapped in { data: [...] }
        categoriesArray = responseData.data;
      } else if (responseData.data && Array.isArray(responseData.data.categories)) {
        // Wrapped in { data: { categories: [...] } }
        categoriesArray = responseData.data.categories;
      } else if (responseData.data && Array.isArray(responseData.data.data)) {
        // Wrapped in { data: { data: [...] } }
        categoriesArray = responseData.data.data;
      } else if (responseData.categories && Array.isArray(responseData.categories)) {
        // Wrapped in { categories: [...] }
        categoriesArray = responseData.categories;
      }

      console.log('Parsed categoriesArray:', categoriesArray);
      setCategories(categoriesArray);
    } catch (error: any) {
      console.error('Failed to load categories:', error);
      message.error('Failed to load categories');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingCategory(null);
    form.resetFields();
    form.setFieldsValue({
      isActive: true,
      sortOrder: 0,
    });
    setModalVisible(true);
  };

  const handleEdit = (category: Category) => {
    setEditingCategory(category);
    form.setFieldsValue(category);
    setModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      await api.delete(`/admin/categories/${id}`);
      message.success('Category deleted successfully');
      loadCategories();
    } catch (error: any) {
      console.error('Failed to delete category:', error);
      message.error(error.message || 'Failed to delete category');
    }
  };

  const handleSubmit = async (values: CategoryFormData) => {
    try {
      if (editingCategory) {
        await api.put(`/admin/categories/${editingCategory.id}`, values);
        message.success('Category updated successfully');
      } else {
        await api.post('/admin/categories', values);
        message.success('Category created successfully');
      }
      setModalVisible(false);
      loadCategories();
    } catch (error: any) {
      console.error('Failed to save category:', error);
      message.error(error.message || 'Failed to save category');
    }
  };

  const columns: ColumnsType<Category> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      render: (text, record) => (
        <div>
          <div style={{ fontWeight: 500 }}>{text}</div>
          <div style={{ fontSize: 12, color: '#999' }}>{record.nameEn}</div>
        </div>
      ),
    },
    {
      title: 'Slug',
      dataIndex: 'slug',
      key: 'slug',
    },
    {
      title: 'Icon',
      dataIndex: 'icon',
      key: 'icon',
      render: (text) => text || '-',
    },
    {
      title: 'Sort Order',
      dataIndex: 'sortOrder',
      key: 'sortOrder',
      sorter: (a, b) => a.sortOrder - b.sortOrder,
    },
    {
      title: 'Products',
      dataIndex: 'productCount',
      key: 'productCount',
      render: (count) => count || 0,
    },
    {
      title: 'Status',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (isActive: boolean) => (
        <Tag color={isActive ? 'success' : 'default'}>
          {isActive ? 'Active' : 'Inactive'}
        </Tag>
      ),
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
            title="Are you sure you want to delete this category?"
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
        <Title level={2} style={{ margin: 0 }}>Categories Management</Title>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadCategories}>
            Refresh
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            Add Category
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={categories}
        rowKey="id"
        loading={loading}
        pagination={false}
        scroll={{ x: 1000 }}
      />

      <Modal
        title={editingCategory ? 'Edit Category' : 'Create Category'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            isActive: true,
            sortOrder: 0,
          }}
        >
          <Form.Item
            label="Name (Vietnamese)"
            name="name"
            rules={[{ required: true, message: 'Please enter category name' }]}
          >
            <Input placeholder="e.g., Đồ ăn nhanh" />
          </Form.Item>

          <Form.Item
            label="Name (English)"
            name="nameEn"
          >
            <Input placeholder="e.g., Fast Food" />
          </Form.Item>

          <Form.Item
            label="Slug"
            name="slug"
            rules={[
              { required: true, message: 'Please enter slug' },
              {
                pattern: /^[a-z0-9-]+$/,
                message: 'Slug must contain only lowercase letters, numbers, and hyphens',
              },
            ]}
          >
            <Input placeholder="e.g., do-an-nhanh" />
          </Form.Item>

          <Form.Item label="Icon" name="icon">
            <Input placeholder="e.g., fastfood" />
          </Form.Item>

          <Form.Item label="Description" name="description">
            <Input.TextArea rows={3} placeholder="e.g., Burger, Pizza, Gà rán" />
          </Form.Item>

          <Form.Item
            label="Sort Order"
            name="sortOrder"
            rules={[{ required: true, message: 'Please enter sort order' }]}
          >
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item label="Active" name="isActive" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
