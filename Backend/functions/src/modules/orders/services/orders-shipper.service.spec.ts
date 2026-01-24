import { Test, TestingModule } from '@nestjs/testing';
import {
  NotFoundException,
  ConflictException,
  ForbiddenException,
  BadRequestException,
} from '@nestjs/common';
import { OrdersService } from './orders.service';
import { OrderStateMachineService } from './order-state-machine.service';
import { IOrdersRepository, ORDERS_REPOSITORY } from '../interfaces';
import { CartService } from '../../cart/services';
import { VouchersService } from '../../vouchers/vouchers.service';
import { ConfigService } from '../../../core/config/config.service';
import { FirebaseService } from '../../../core/firebase/firebase.service';
import { USERS_REPOSITORY } from '../../users/interfaces';
import { OrderStatus, PaymentStatus, OrderEntity } from '../entities';
import { Timestamp } from 'firebase-admin/firestore';

describe('OrdersService - Shipper Flow (Phase 2)', () => {
  let service: OrdersService;
  let ordersRepo: jest.Mocked<IOrdersRepository>;
  let shippersRepo: any;
  let usersRepo: any;

  const mockOrder: OrderEntity = {
    id: 'order_123',
    orderNumber: 'ORD-1737200400000-ABC123',
    customerId: 'customer_1',
    shopId: 'shop_1',
    shopName: 'Test Shop',
    shipperId: null,
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
      acceptOrderAtomically: jest.fn(),
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
          provide: VouchersService,
          useValue: {
            validateVoucher: jest.fn(),
            applyVoucherAtomic: jest.fn(),
          },
        },
        {
          provide: ConfigService,
          useValue: mockConfigService,
        },
        {
          provide: FirebaseService,
          useValue: {
            firestore: { collection: jest.fn(), batch: jest.fn() },
            auth: { verifyIdToken: jest.fn() },
          },
        },
      ],
    }).compile();

    service = module.get<OrdersService>(OrdersService);
    ordersRepo = module.get(ORDERS_REPOSITORY);
    shippersRepo = module.get('IShippersRepository');
    usersRepo = module.get(USERS_REPOSITORY);
  });

  describe('acceptOrder (ORDER-013)', () => {
    it('should accept order from READY and assign shipper using atomic transaction', async () => {
      const shipperId = 'shipper_1';
      const mockShipper = {
        id: shipperId,
        shipperInfo: { status: 'AVAILABLE' },
      };
      const updatedOrder = {
        ...mockOrder,
        shipperId,
        status: OrderStatus.SHIPPING,
      };

      ordersRepo.findById.mockResolvedValueOnce(mockOrder);
      shippersRepo.findById.mockResolvedValueOnce(mockShipper);
      ordersRepo.acceptOrderAtomically.mockResolvedValueOnce(updatedOrder);

      const result = await service.acceptOrder(shipperId, 'order_123');

      // Verify validation checks were performed
      expect(ordersRepo.findById).toHaveBeenCalledWith('order_123');
      expect(shippersRepo.findById).toHaveBeenCalledWith(shipperId);
      
      // Verify atomic transaction was called (not separate update calls)
      expect(ordersRepo.acceptOrderAtomically).toHaveBeenCalledWith(
        'order_123',
        shipperId,
      );
      
      // Verify result
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
        status: OrderStatus.READY,
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
        status: OrderStatus.READY,
      });

      await expect(
        service.markShipping('shipper_1', 'order_123'),
      ).rejects.toThrow(ForbiddenException);
    });

    it('should throw ConflictException if not in SHIPPING status', async () => {
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        shipperId: 'shipper_1',
        status: OrderStatus.SHIPPING,
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
      const shopId = 'shop_123';
      const mockUser = {
        id: shipperId,
        displayName: 'Shipper A',
        shipperInfo: { shopId },
      };
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

      usersRepo.findById.mockResolvedValueOnce(mockUser);
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
      const shopId = 'shop_123';
      const mockUser = {
        id: shipperId,
        displayName: 'Shipper A',
        shipperInfo: { shopId },
      };
      const mockQuery = {
        where: jest.fn().mockReturnThis(),
        orderBy: jest.fn().mockReturnThis(),
        limit: jest.fn().mockReturnThis(),
        offset: jest.fn().mockReturnThis(),
      };

      usersRepo.findById.mockResolvedValueOnce(mockUser);
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
      const shopId = 'shop_123';
      const mockUser = {
        id: shipperId,
        displayName: 'Shipper A',
        shipperInfo: { shopId },
      };
      const mockQuery = {
        where: jest.fn().mockReturnThis(),
        orderBy: jest.fn().mockReturnThis(),
        limit: jest.fn().mockReturnThis(),
        offset: jest.fn().mockReturnThis(),
      };

      usersRepo.findById.mockResolvedValueOnce(mockUser);
      ordersRepo.count.mockResolvedValueOnce(50);
      ordersRepo.query.mockReturnValue(mockQuery as any);
      ordersRepo.findMany.mockResolvedValueOnce([]);

      const result = await service.getShipperOrders(shipperId, {
        page: 2,
        limit: 10,
      });

      expect(result.page).toBe(2);
      expect(result.limit).toBe(10);
      expect(result.total).toBe(50);
      expect(result.totalPages).toBe(5);
      // (2-1)*10 = 10
      expect(mockQuery.offset).toHaveBeenCalledWith(10);
      expect(mockQuery.limit).toHaveBeenCalledWith(10);
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

  describe('getShipperOrdersAvailable (ORDER-016 EXTENSION - Available Orders)', () => {
    it('should return READY unassigned orders for shipper shop (reads from users collection)', async () => {
      const shipperId = 'shipper_1';
      const shopId = 'shop_123';
      
      // Mock user/shipper with shipperInfo.shopId (from users collection)
      const mockUser = {
        id: shipperId,
        displayName: 'Shipper A',
        phone: '0901234567',
        shipperInfo: {
          shopId,
          status: 'AVAILABLE',
        },
      };

      const mockOrders = [
        {
          ...mockOrder,
          status: 'READY' as OrderStatus,
          shipperId: null,
          shopId,
          id: 'order_ready_1',
        },
        {
          ...mockOrder,
          status: 'READY' as OrderStatus,
          shipperId: null,
          shopId,
          id: 'order_ready_2',
        },
      ];

      const mockQuery = {
        where: jest.fn().mockReturnThis(),
        orderBy: jest.fn().mockReturnThis(),
        limit: jest.fn().mockReturnThis(),
        offset: jest.fn().mockReturnThis(),
      };

      // Mock: Read from users collection (same as owner list endpoint)
      usersRepo.findById.mockResolvedValueOnce(mockUser);
      ordersRepo.count.mockResolvedValueOnce(2);
      ordersRepo.query.mockReturnValue(mockQuery as any);
      ordersRepo.findMany.mockResolvedValueOnce(mockOrders);

      const result = await service.getShipperOrdersAvailable(shipperId, {
        limit: 10,
      });

      expect(result.orders).toHaveLength(2);
      expect(result.total).toBe(2);
      // Verify the query filters
      expect(mockQuery.where).toHaveBeenCalledWith('status', '==', 'READY');
      expect(mockQuery.where).toHaveBeenCalledWith('shipperId', '==', null);
      expect(mockQuery.where).toHaveBeenCalledWith('shopId', '==', shopId);
      expect(mockQuery.orderBy).toHaveBeenCalledWith('createdAt', 'desc');
    });

    it('should throw BadRequestException if shipper missing shopId (not assigned to shop)', async () => {
      const shipperId = 'shipper_1';
      
      // Mock user without shipperInfo.shopId
      const mockUserNoShop = {
        id: shipperId,
        displayName: 'Shipper A',
        phone: '0901234567',
        shipperInfo: {
          // shopId is missing/null
          status: 'AVAILABLE',
        },
      };

      usersRepo.findById.mockResolvedValueOnce(mockUserNoShop);

      await expect(
        service.getShipperOrdersAvailable(shipperId, { limit: 10 })
      ).rejects.toThrow(BadRequestException);
    });

    it('should throw NotFoundException if shipper not found', async () => {
      const shipperId = 'shipper_invalid';

      usersRepo.findById.mockResolvedValueOnce(null);

      await expect(
        service.getShipperOrdersAvailable(shipperId, { limit: 10 })
      ).rejects.toThrow(NotFoundException);
    });

    it('should support pagination correctly', async () => {
      const shipperId = 'shipper_1';
      const shopId = 'shop_123';
      
      const mockUser = {
        id: shipperId,
        displayName: 'Shipper A',
        phone: '0901234567',
        shipperInfo: {
          shopId,
          status: 'AVAILABLE',
        },
      };

      const mockOrders = [
        {
          ...mockOrder,
          status: 'READY' as OrderStatus,
          shipperId: null,
          shopId,
          id: 'order_ready_1',
        },
      ];

      const mockQuery = {
        where: jest.fn().mockReturnThis(),
        orderBy: jest.fn().mockReturnThis(),
        limit: jest.fn().mockReturnThis(),
        offset: jest.fn().mockReturnThis(),
      };

      usersRepo.findById.mockResolvedValueOnce(mockUser);
      ordersRepo.count.mockResolvedValueOnce(5); // Total 5 available orders
      ordersRepo.query.mockReturnValue(mockQuery as any);
      ordersRepo.findMany.mockResolvedValueOnce(mockOrders);

      const result = await service.getShipperOrdersAvailable(shipperId, {
        page: 2,
        limit: 1,
      });

      expect(result.total).toBe(5);
      expect(result.totalPages).toBe(5);
      expect(result.page).toBe(2);
      expect(result.limit).toBe(1);
      // Verify offset calculation: (2-1) * 1 = 1
      expect(mockQuery.offset).toHaveBeenCalledWith(1);
      expect(mockQuery.limit).toHaveBeenCalledWith(1);
    });

    it('REGRESSION: should return matching total and orders count (null filter bug fix)', async () => {
      const shipperId = 'shipper_1';
      const shopId = 'shop_123';
      
      const mockUser = {
        id: shipperId,
        displayName: 'Shipper A',
        phone: '0901234567',
        shipperInfo: {
          shopId,
          status: 'AVAILABLE',
        },
      };

      const mockOrders = [
        {
          ...mockOrder,
          status: 'READY' as OrderStatus,
          shipperId: null,
          shopId,
          id: 'order_ready_1',
        },
        {
          ...mockOrder,
          status: 'READY' as OrderStatus,
          shipperId: null,
          shopId,
          id: 'order_ready_2',
        },
      ];

      const mockQuery = {
        where: jest.fn().mockReturnThis(),
        orderBy: jest.fn().mockReturnThis(),
        limit: jest.fn().mockReturnThis(),
        offset: jest.fn().mockReturnThis(),
      };

      usersRepo.findById.mockResolvedValueOnce(mockUser);
      // CRITICAL: count() must include the shipperId:null filter
      // Before fix: count() would skip null values, returning higher count than fetch
      // After fix: count() includes null values, matching the fetch query filters
      ordersRepo.count.mockResolvedValueOnce(2);
      ordersRepo.query.mockReturnValue(mockQuery as any);
      ordersRepo.findMany.mockResolvedValueOnce(mockOrders);

      const result = await service.getShipperOrdersAvailable(shipperId, {
        limit: 10,
      });

      // VERIFY FIX: total and orders.length must match
      expect(result.total).toBe(2);
      expect(result.orders).toHaveLength(2);  // ← Bug was: total=2 but orders=0
      expect(result.totalPages).toBe(1);

      // VERIFY: ordersRepo.count was called with null value for shipperId filter
      expect(ordersRepo.count).toHaveBeenCalledWith({
        status: OrderStatus.READY,
        shipperId: null,  // ← Must include null (not skip it)
        shopId,
      });

      // VERIFY: query filters include shipperId:null
      expect(mockQuery.where).toHaveBeenCalledWith('shipperId', '==', null);
    });
  });
});
