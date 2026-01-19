import { Test, TestingModule } from '@nestjs/testing';
import {
  NotFoundException,
  ConflictException,
  ForbiddenException,
} from '@nestjs/common';
import { OrdersService } from './orders.service';
import { OrderStateMachineService } from './order-state-machine.service';
import { IOrdersRepository, ORDERS_REPOSITORY } from '../interfaces';
import { CartService } from '../../cart/services';
import { ConfigService } from '../../../core/config/config.service';
import { USERS_REPOSITORY } from '../../users/interfaces';
import { OrderStatus, PaymentStatus, OrderEntity } from '../entities';
import { Timestamp } from 'firebase-admin/firestore';

describe('OrdersService - Shipper Flow (Phase 2)', () => {
  let service: OrdersService;
  let ordersRepo: jest.Mocked<IOrdersRepository>;
  let shippersRepo: any;

  const mockOrder: OrderEntity = {
    id: 'order_123',
    orderNumber: 'ORD-1737200400000-ABC123',
    customerId: 'customer_1',
    shopId: 'shop_1',
    shopName: 'Test Shop',
    items: [
      {
        productId: 'prod_1',
        productName: 'Test Product',
        quantity: 2,
        price: 25000,
        subtotal: 50000,
      },
    ],
    subtotal: 50000,
    shipFee: 5000,
    discount: 0,
    total: 55000,
    status: OrderStatus.READY,
    paymentStatus: PaymentStatus.UNPAID,
    paymentMethod: 'COD',
    deliveryAddress: {
      street: '123 Test St',
      ward: 'Ward 1',
      district: 'District 1',
      city: 'HCMC',
    },
    createdAt: Timestamp.now(),
    updatedAt: Timestamp.now(),
  };

  beforeEach(async () => {
    const mockOrdersRepository = {
      findById: jest.fn(),
      update: jest.fn(),
      query: jest.fn(),
      findMany: jest.fn(),
      count: jest.fn(),
    };

    const mockShippersRepository = {
      findById: jest.fn(),
      update: jest.fn(),
    };

    const mockAddressesRepository = {
      findById: jest.fn(),
    };

    const mockConfigService = {
      enableFirestorePaginationFallback: false,
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        OrdersService,
        OrderStateMachineService,
        {
          provide: ORDERS_REPOSITORY,
          useValue: mockOrdersRepository,
        },
        {
          provide: CartService,
          useValue: {},
        },
        {
          provide: 'PRODUCTS_REPOSITORY',
          useValue: {},
        },
        {
          provide: 'SHOPS_REPOSITORY',
          useValue: {},
        },
        {
          provide: 'IShippersRepository',
          useValue: mockShippersRepository,
        },
        {
          provide: 'IAddressesRepository',
          useValue: mockAddressesRepository,
        },
        {
          provide: USERS_REPOSITORY,
          useValue: { findById: jest.fn() },
        },
        {
          provide: ConfigService,
          useValue: mockConfigService,
        },
      ],
    }).compile();

    service = module.get<OrdersService>(OrdersService);
    ordersRepo = module.get(ORDERS_REPOSITORY);
    shippersRepo = module.get('IShippersRepository');
  });

  describe('acceptOrder (ORDER-013)', () => {
    it('should accept order from READY and assign shipper', async () => {
      const shipperId = 'shipper_1';
      ordersRepo.findById.mockResolvedValueOnce(mockOrder);
      shippersRepo.findById.mockResolvedValueOnce({
        id: shipperId,
        shipperInfo: { status: 'AVAILABLE' },
      });
      ordersRepo.update.mockResolvedValueOnce(undefined);
      shippersRepo.update.mockResolvedValueOnce(undefined);
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId,
        status: OrderStatus.SHIPPING,
      });

      const result = await service.acceptOrder(shipperId, 'order_123');

      expect(ordersRepo.findById).toHaveBeenCalledWith('order_123');
      expect(ordersRepo.update).toHaveBeenCalledWith(
        'order_123',
        expect.objectContaining({
          shipperId,
          status: OrderStatus.SHIPPING,
        }),
      );
      expect(result.shipperId).toBe(shipperId);
      expect(result.status).toBe(OrderStatus.SHIPPING);
    });

    it('should throw NotFoundException if order not found', async () => {
      ordersRepo.findById.mockResolvedValueOnce(null);

      await expect(
        service.acceptOrder('shipper_1', 'order_999'),
      ).rejects.toThrow(NotFoundException);
    });

    it('should throw ConflictException if order not READY', async () => {
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        status: OrderStatus.PENDING,
      });

      await expect(
        service.acceptOrder('shipper_1', 'order_123'),
      ).rejects.toThrow(ConflictException);
    });

    it('should throw ConflictException if shipper already assigned', async () => {
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId: 'shipper_other',
      });

      await expect(
        service.acceptOrder('shipper_1', 'order_123'),
      ).rejects.toThrow(ConflictException);
    });
  });

  describe('markShipping (ORDER-014)', () => {
    it('should allow updating shippingAt timestamp', async () => {
      const shipperId = 'shipper_1';
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId,
        status: OrderStatus.SHIPPING,
      });
      ordersRepo.update.mockResolvedValueOnce(undefined);
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId,
        status: OrderStatus.SHIPPING,
        shippingAt: Timestamp.now(),
      });

      const result = await service.markShipping(shipperId, 'order_123');

      expect(result.status).toBe(OrderStatus.SHIPPING);
    });

    it('should throw ForbiddenException if shipper mismatch', async () => {
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId: 'shipper_other',
        status: OrderStatus.SHIPPING,
      });

      await expect(
        service.markShipping('shipper_1', 'order_123'),
      ).rejects.toThrow(ForbiddenException);
    });

    it('should throw ConflictException if not in SHIPPING status', async () => {
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId: 'shipper_1',
        status: OrderStatus.READY,
      });

      await expect(
        service.markShipping('shipper_1', 'order_123'),
      ).rejects.toThrow(ConflictException);
    });
  });

  describe('markDelivered (ORDER-015)', () => {
    it('should mark order as delivered from SHIPPING', async () => {
      const shipperId = 'shipper_1';
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId,
        status: OrderStatus.SHIPPING,
      });
      ordersRepo.update.mockResolvedValueOnce(undefined);
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId,
        status: OrderStatus.DELIVERED,
        deliveredAt: Timestamp.now(),
      });

      const result = await service.markDelivered(shipperId, 'order_123');

      expect(ordersRepo.update).toHaveBeenCalledWith(
        'order_123',
        expect.objectContaining({
          status: OrderStatus.DELIVERED,
        }),
      );
      expect(result.status).toBe(OrderStatus.DELIVERED);
    });

    it('should mark COD order as PAID upon delivery', async () => {
      const shipperId = 'shipper_1';
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId,
        status: OrderStatus.SHIPPING,
        paymentMethod: 'COD',
        paymentStatus: PaymentStatus.UNPAID,
      });
      ordersRepo.update.mockResolvedValueOnce(undefined);
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId,
        status: OrderStatus.DELIVERED,
        paymentStatus: PaymentStatus.PAID,
      });

      await service.markDelivered(shipperId, 'order_123');

      expect(ordersRepo.update).toHaveBeenCalledWith(
        'order_123',
        expect.objectContaining({
          paymentStatus: PaymentStatus.PAID,
        }),
      );
    });

    it('should throw ForbiddenException if shipper mismatch', async () => {
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId: 'shipper_other',
        status: OrderStatus.SHIPPING,
      });

      await expect(
        service.markDelivered('shipper_1', 'order_123'),
      ).rejects.toThrow(ForbiddenException);
    });

    it('should throw ConflictException if not in SHIPPING status', async () => {
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId: 'shipper_1',
        status: OrderStatus.READY,
      });

      await expect(
        service.markDelivered('shipper_1', 'order_123'),
      ).rejects.toThrow(ConflictException);
    });
  });

  describe('getShipperOrders (ORDER-016)', () => {
    it('should return orders assigned to shipper', async () => {
      const shipperId = 'shipper_1';
      const mockOrders = [
        { ...mockOrder, shipperId, id: 'order_1' },
        { ...mockOrder, shipperId, id: 'order_2' },
      ];

      const mockQuery = {
        where: jest.fn().mockReturnThis(),
        orderBy: jest.fn().mockReturnThis(),
        limit: jest.fn().mockReturnThis(),
        offset: jest.fn().mockReturnThis(),
      };

      ordersRepo.count.mockResolvedValueOnce(2);
      ordersRepo.query.mockReturnValue(mockQuery as any);
      ordersRepo.findMany.mockResolvedValueOnce(mockOrders);

      const result = await service.getShipperOrders(shipperId, {
        limit: 10,
      });

      expect(result.orders).toHaveLength(2);
      expect(result.total).toBe(2);
      expect(mockQuery.where).toHaveBeenCalledWith('shipperId', '==', shipperId);
    });

    it('should support status filtering', async () => {
      const shipperId = 'shipper_1';
      const mockQuery = {
        where: jest.fn().mockReturnThis(),
        orderBy: jest.fn().mockReturnThis(),
        limit: jest.fn().mockReturnThis(),
        offset: jest.fn().mockReturnThis(),
      };

      ordersRepo.count.mockResolvedValueOnce(0);
      ordersRepo.query.mockReturnValue(mockQuery as any);
      ordersRepo.findMany.mockResolvedValueOnce([]);

      await service.getShipperOrders(shipperId, {
        status: 'SHIPPING',
        limit: 10,
      });

      expect(mockQuery.where).toHaveBeenCalledWith('shipperId', '==', shipperId);
      expect(mockQuery.where).toHaveBeenCalledWith('status', '==', 'SHIPPING');
    });

    it('should support page-based pagination', async () => {
      const shipperId = 'shipper_1';
      const mockQuery = {
        where: jest.fn().mockReturnThis(),
        orderBy: jest.fn().mockReturnThis(),
        limit: jest.fn().mockReturnThis(),
        offset: jest.fn().mockReturnThis(),
      };

      ordersRepo.count.mockResolvedValueOnce(0);
      ordersRepo.query.mockReturnValue(mockQuery as any);
      ordersRepo.findMany.mockResolvedValueOnce([]);

      await service.getShipperOrders(shipperId, {
        page: 2,
        limit: 10,
      });

      expect(mockQuery.where).toHaveBeenCalledWith('shipperId', '==', shipperId);
    });
  });

  describe('Integration: First-to-accept wins', () => {
    it('should prevent multiple shippers accepting same order', async () => {
      const shipper1 = 'shipper_1';
      const shipper2 = 'shipper_2';

      // First shipper accepts successfully
      ordersRepo.findById.mockResolvedValueOnce(mockOrder);
      shippersRepo.findById.mockResolvedValueOnce({
        id: shipper1,
        shipperInfo: { status: 'AVAILABLE' },
      });
      ordersRepo.update.mockResolvedValueOnce(undefined);
      shippersRepo.update.mockResolvedValueOnce(undefined);
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId: shipper1,
        status: OrderStatus.SHIPPING,
      });

      await service.acceptOrder(shipper1, 'order_123');

      // Second shipper tries to accept - should fail
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId: shipper1,
        status: OrderStatus.SHIPPING,
      });

      await expect(service.acceptOrder(shipper2, 'order_123')).rejects.toThrow(
        ConflictException,
      );
    });
  });

  describe('Error Handling - Firestore Indexes', () => {
    it('should propagate Firestore index errors to caller', async () => {
      const shipperId = 'shipper_1';
      
      // Simulate Firestore error for missing index
      const firestoreError = {
        code: 'FAILED_PRECONDITION',
        message:
          'The query requires an index. You can create it here: https://console.firebase.google.com/firestore/indexes?create_composite=...',
      };

      // Mock count to reject with Firestore error
      ordersRepo.count.mockRejectedValueOnce(firestoreError);

      // The error should propagate up from the repository
      await expect(
        service.getShipperOrders(shipperId, { limit: 10 })
      ).rejects.toBeDefined();
    });
  });
});
