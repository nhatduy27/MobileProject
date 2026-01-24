/**
 * Orders Detail Endpoints Test
 * 
 * Unit tests for OWNER and SHIPPER order detail endpoints:
 * - GET /api/orders/shop/:id (OWNER)
 * - GET /api/orders/shipper/:id (SHIPPER)
 * 
 * Tests cover:
 * - Authorization (403 for wrong role, 404 for missing order)
 * - Response structure (no double wrap)
 * - Ownership validation
 */

import { Test, TestingModule } from '@nestjs/testing';
import { NotFoundException, ForbiddenException } from '@nestjs/common';
import { OrdersService } from '../services/orders.service';
import { OrdersOwnerController } from '../controllers/orders-owner.controller';
import { OrdersShipperController } from '../controllers/orders-shipper.controller';
import { OrderEntity, OrderStatus, PaymentStatus } from '../entities';
import { Timestamp } from 'firebase-admin/firestore';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';

describe('Orders Detail Endpoints', () => {
  let ownerController: OrdersOwnerController;
  let shipperController: OrdersShipperController;
  let ordersService: jest.Mocked<OrdersService>;

  const mockOrderEntity = {
    id: 'order_abc123def456',
    orderNumber: 'ORD-1705591320000-A2B3C4',
    customerId: 'user_cust_001',
    customer: {
      id: 'user_cust_001',
      displayName: 'Nguyễn Văn A',
      phone: '0901234567',
    },
    shopId: 'shop_123',
    shopName: 'Cơm Tấm Sườn',
    shipperId: 'shipper_456',
    shipper: {
      id: 'shipper_456',
      displayName: 'Trần Minh Đạt',
      phone: '0987654321',
    },
    items: [
      {
        productId: 'prod_123',
        productName: 'Cơm sườn bì chả',
        quantity: 2,
        price: 35000,
        subtotal: 70000,
      },
      {
        productId: 'prod_456',
        productName: 'Trà đá',
        quantity: 1,
        price: 5000,
        subtotal: 5000,
      },
    ],
    subtotal: 75000,
    shipFee: 0,
    shipperPayout: 15000,
    discount: 0,
    total: 75000,
    status: OrderStatus.SHIPPING,
    paymentStatus: PaymentStatus.UNPAID,
    paymentMethod: 'COD',
    deliveryAddress: {
      label: 'home',
      fullAddress: '268 Lý Thường Kiệt, Phường 14, Quận 10, TP.HCM',
      building: 'A1',
      room: '101',
      note: 'Gọi trước khi đến',
    },
    deliveryNote: 'Giao nhanh giúp em',
    createdAt: Timestamp.fromDate(new Date('2026-01-19T10:00:00.000Z')),
    updatedAt: Timestamp.fromDate(new Date('2026-01-19T10:30:00.000Z')),
    confirmedAt: Timestamp.fromDate(new Date('2026-01-19T10:05:00.000Z')),
    preparingAt: Timestamp.fromDate(new Date('2026-01-19T10:15:00.000Z')),
    readyAt: Timestamp.fromDate(new Date('2026-01-19T10:25:00.000Z')),
    shippingAt: Timestamp.fromDate(new Date('2026-01-19T10:30:00.000Z')),
  } as OrderEntity;

  beforeEach(async () => {
    const mockOrdersService = {
      getShopOrderDetail: jest.fn(),
      getShipperOrderDetail: jest.fn(),
    };

    const module: TestingModule = await Test.createTestingModule({
      controllers: [OrdersOwnerController, OrdersShipperController],
      providers: [
        {
          provide: OrdersService,
          useValue: mockOrdersService,
        },
      ],
    })
      .overrideGuard(AuthGuard)
      .useValue({ canActivate: () => true })
      .overrideGuard(RolesGuard)
      .useValue({ canActivate: () => true })
      .compile();

    ownerController = module.get<OrdersOwnerController>(OrdersOwnerController);
    shipperController = module.get<OrdersShipperController>(OrdersShipperController);
    ordersService = module.get(OrdersService);
  });

  describe('GET /api/orders/shop/:id (OWNER Detail)', () => {
    const mockRequest = { user: { uid: 'owner_123', role: 'OWNER' } };

    it('should return full order detail for owner', async () => {
      ordersService.getShopOrderDetail.mockResolvedValue(mockOrderEntity as any);

      const result = await ownerController.getShopOrderDetail(
        mockRequest,
        'order_abc123def456',
      );

      expect(result).toEqual(mockOrderEntity);
      expect(ordersService.getShopOrderDetail).toHaveBeenCalledWith(
        'owner_123',
        'order_abc123def456',
      );
    });

    it('should return 404 if order not found', async () => {
      ordersService.getShopOrderDetail.mockRejectedValue(
        new NotFoundException({
          code: 'ORDER_NOT_FOUND',
          message: 'Order not found',
          statusCode: 404,
        }),
      );

      await expect(
        ownerController.getShopOrderDetail(mockRequest, 'nonexistent_order'),
      ).rejects.toThrow(NotFoundException);
    });

    it('should return 403 if order does not belong to owner shop', async () => {
      ordersService.getShopOrderDetail.mockRejectedValue(
        new ForbiddenException({
          code: 'ORDER_ACCESS_DENIED',
          message: 'You do not have permission to view this order',
          statusCode: 403,
        }),
      );

      await expect(
        ownerController.getShopOrderDetail(mockRequest, 'order_other_shop'),
      ).rejects.toThrow(ForbiddenException);
    });

    it('should include all order fields (full detail)', async () => {
      ordersService.getShopOrderDetail.mockResolvedValue(mockOrderEntity as any);

      const result = await ownerController.getShopOrderDetail(
        mockRequest,
        'order_abc123def456',
      );

      // Verify all required fields are present in new OwnerOrderDetailDto format
      expect(result.id).toBeDefined();
      expect(result.orderNumber).toBeDefined();
      expect(result.customer).toBeDefined();
      expect(result.customer.id).toBe('user_cust_001');
      expect(result.customer.displayName).toBe('Nguyễn Văn A');
      expect(result.customer.phone).toBe('0901234567'); // Phone always present
      expect(result.shopId).toBeDefined();
      expect(result.shopName).toBeDefined();
      expect(result.items).toHaveLength(2);
      expect(result.items[0].productName).toBe('Cơm sườn bì chả');
      expect(result.subtotal).toBe(75000);
      expect(result.shipFee).toBe(0);
      expect(result.shipperPayout).toBe(15000);
      expect(result.total).toBe(75000);
      expect(result.deliveryAddress).toBeDefined();
      expect(result.deliveryAddress.label).toBe('home');
      expect(result.deliveryAddress.fullAddress).toContain('268 Lý Thường Kiệt');
      expect(result.createdAt).toBeDefined();
      expect(result.confirmedAt).toBeDefined();
      expect(result.shipperId).toBe('shipper_456');
      expect(result.shipper).toBeDefined();
      expect(result.shipper?.id).toBe('shipper_456');
      expect(result.shipper?.phone).toBeDefined();
    });

    it('should NOT double wrap response', async () => {
      ordersService.getShopOrderDetail.mockResolvedValue(mockOrderEntity as any);

      const result = await ownerController.getShopOrderDetail(
        mockRequest,
        'order_abc123def456',
      );

      // Verify result is OwnerOrderDetailDto (not wrapped in additional layer)
      expect(result).toHaveProperty('id');
      expect(result).toHaveProperty('orderNumber');
      expect(result).toHaveProperty('customer');
      expect(result).not.toHaveProperty('success');
      expect(result).not.toHaveProperty('data');
      expect(result).not.toHaveProperty('timestamp');
    });
  });

  describe('GET /api/orders/shipper/:id (SHIPPER Detail)', () => {
    const mockRequest = { user: { uid: 'shipper_456', role: 'SHIPPER' } };

    it('should return full order detail for assigned shipper', async () => {
      ordersService.getShipperOrderDetail.mockResolvedValue(mockOrderEntity as any);

      const result = await shipperController.getShipperOrderDetail(
        mockRequest,
        'order_abc123def456',
      );

      expect(result).toEqual(mockOrderEntity);
      expect(ordersService.getShipperOrderDetail).toHaveBeenCalledWith(
        'shipper_456',
        'order_abc123def456',
      );
    });

    it('should return 404 if order not found', async () => {
      ordersService.getShipperOrderDetail.mockRejectedValue(
        new NotFoundException({
          code: 'ORDER_NOT_FOUND',
          message: 'Order not found',
          statusCode: 404,
        }),
      );

      await expect(
        shipperController.getShipperOrderDetail(mockRequest, 'nonexistent_order'),
      ).rejects.toThrow(NotFoundException);
    });

    it('should return 403 if order not assigned to shipper and not READY/unassigned', async () => {
      ordersService.getShipperOrderDetail.mockRejectedValue(
        new ForbiddenException({
          code: 'ORDER_ACCESS_DENIED',
          message: 'You do not have permission to view this order',
          statusCode: 403,
        }),
      );

      await expect(
        shipperController.getShipperOrderDetail(mockRequest, 'order_other_shipper'),
      ).rejects.toThrow(ForbiddenException);
    });

    it('should allow access to READY unassigned orders (preview before accept)', async () => {
      const readyOrder = {
        ...mockOrderEntity,
        status: OrderStatus.READY,
        shipperId: undefined, // Unassigned
      };

      ordersService.getShipperOrderDetail.mockResolvedValue(readyOrder as any);

      const result = await shipperController.getShipperOrderDetail(
        mockRequest,
        'order_ready_unassigned',
      );

      expect(result.status).toBe(OrderStatus.READY);
      expect(result.shipperId).toBeUndefined();
    });

    it('should include customer contact info for delivery', async () => {
      ordersService.getShipperOrderDetail.mockResolvedValue(mockOrderEntity as any);

      const result = await shipperController.getShipperOrderDetail(
        mockRequest,
        'order_abc123def456',
      );

      // Shipper needs customer contact for delivery - now via customer field
      expect(result.customer).toBeDefined();
      expect(result.customer?.phone).toBe('0901234567');
      expect(result.deliveryAddress).toBeDefined();
      expect(result.deliveryAddress.fullAddress).toContain('268 Lý Thường Kiệt');
    });

    it('should NOT double wrap response', async () => {
      ordersService.getShipperOrderDetail.mockResolvedValue(mockOrderEntity as any);

      const result = await shipperController.getShipperOrderDetail(
        mockRequest,
        'order_abc123def456',
      );

      // Verify result is OrderEntity (not wrapped)
      expect(result).toHaveProperty('id');
      expect(result).toHaveProperty('orderNumber');
      expect(result).not.toHaveProperty('success');
      expect(result).not.toHaveProperty('data');
      expect(result).not.toHaveProperty('timestamp');
    });
  });

  describe('Response Structure Validation', () => {
    it('owner detail response should match OwnerOrderDetailDto shape', async () => {
      ordersService.getShopOrderDetail.mockResolvedValue(mockOrderEntity as any);

      const result = await ownerController.getShopOrderDetail(
        { user: { uid: 'owner_123' } },
        'order_abc123def456',
      );

      // Required fields
      expect(result).toHaveProperty('id');
      expect(result).toHaveProperty('orderNumber');
      expect(result).toHaveProperty('customer'); // DTO field, not customerSnapshot
      expect(result.customer).toHaveProperty('id');
      expect(result.customer).toHaveProperty('phone'); // phone always present
      expect(result).toHaveProperty('shopId');
      expect(result).toHaveProperty('shopName');
      expect(result).toHaveProperty('items');
      expect(result).toHaveProperty('subtotal');
      expect(result).toHaveProperty('shipFee');
      expect(result).toHaveProperty('discount');
      expect(result).toHaveProperty('total');
      expect(result).toHaveProperty('status');
      expect(result).toHaveProperty('paymentStatus');
      expect(result).toHaveProperty('paymentMethod');
      expect(result).toHaveProperty('deliveryAddress');
      expect(result).toHaveProperty('createdAt');
      expect(result).toHaveProperty('updatedAt');

      // Shipper field (optional, only when shipperId != null)
      expect(result).toHaveProperty('shipperId');
      if (result.shipperId) {
        expect(result).toHaveProperty('shipper');
      }

      // Optional fields
      expect(result).toHaveProperty('deliveryNote');
    });

    it('shipper detail response should include customer contact field', async () => {
      ordersService.getShipperOrderDetail.mockResolvedValue(mockOrderEntity as any);

      const result = await shipperController.getShipperOrderDetail(
        { user: { uid: 'shipper_456' } },
        'order_abc123def456',
      );

      // Required fields for shipper
      expect(result).toHaveProperty('id');
      expect(result).toHaveProperty('orderNumber');
      expect(result).toHaveProperty('deliveryAddress');
      expect(result).toHaveProperty('customer'); // For contact info
      expect(result.customer).toHaveProperty('phone');
      expect(result).toHaveProperty('items'); // For verification
      expect(result).toHaveProperty('total'); // For COD collection
      expect(result).toHaveProperty('paymentMethod');
      expect(result).toHaveProperty('status');
    });
  });

  describe('Global Interceptor Behavior', () => {
    it('controller returns OwnerOrderDetailDto for global interceptor to wrap', async () => {
      ordersService.getShopOrderDetail.mockResolvedValue(mockOrderEntity as any);

      const result = await ownerController.getShopOrderDetail(
        { user: { uid: 'owner_123' } },
        'order_abc123def456',
      );

      // Controller returns OwnerOrderDetailDto
      expect(result).toHaveProperty('customer');
      expect(result).toHaveProperty('shipperId');

      // Global TransformInterceptor will wrap this:
      // { success: true, data: result, timestamp: "..." }
    });
  });
});
