import { Test, TestingModule } from '@nestjs/testing';
import { OrdersService } from '../services/orders.service';
import { ORDERS_REPOSITORY } from '../interfaces';
import { CartService } from '../../cart/services';
import { VouchersService } from '../../vouchers/vouchers.service';
import { NotificationsService } from '../../notifications/services/notifications.service';
import { ADDRESSES_REPOSITORY, USERS_REPOSITORY } from '../../users/interfaces';
import { OrderStateMachineService } from '../services/order-state-machine.service';
import { ConfigService } from '../../../core/config/config.service';
import { FirebaseService } from '../../../core/firebase/firebase.service';
import { PaymentsService } from '../../payments/payments.service';
import { OrderEntity, OrderStatus, PaymentStatus } from '../entities';
import { Timestamp } from 'firebase-admin/firestore';
import { WalletsService } from '../../wallets/wallets.service';
import { BuyersStatsService } from '../../buyers/services/buyers-stats.service';
import { ShopsService } from '../../shops/services/shops.service';

describe('OrdersService - Owner List DTO Mapping', () => {
  let service: OrdersService;

  beforeEach(async () => {
    const mockOrdersRepo = {
      findMany: jest.fn(),
      query: jest.fn().mockReturnThis(),
      where: jest.fn().mockReturnThis(),
      orderBy: jest.fn().mockReturnThis(),
      limit: jest.fn().mockReturnThis(),
      offset: jest.fn().mockReturnThis(),
      countWhere: jest.fn(),
    };

    const mockCartService = {};
    const mockProductsRepo = {};
    const mockShopsRepo = {};
    const mockShippersRepo = {};
    const mockAddressesRepo = {};
    const mockUsersRepo = {};
    const mockVouchersService = {
      validateVoucher: jest.fn(),
      applyVoucherAtomic: jest.fn(),
    };
    const mockNotificationsService = {
      send: jest.fn().mockResolvedValue(undefined),
      sendToTopic: jest.fn().mockResolvedValue(undefined),
    };
    const mockStateMachine = {};
    const mockConfigService = {
      enableFirestorePaginationFallback: false,
    };
    const mockFirebaseService = {
      firestore: { collection: jest.fn(), batch: jest.fn() },
      auth: { verifyIdToken: jest.fn() },
    };
    const mockWalletsService = {
      processOrderPayout: jest.fn().mockResolvedValue(undefined),
      updateBalance: jest.fn().mockResolvedValue(undefined),
    };
    const mockPaymentsService = {
      initiateRefund: jest.fn().mockResolvedValue(null),
      createPayment: jest.fn(),
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        OrdersService,
        { provide: ORDERS_REPOSITORY, useValue: mockOrdersRepo },
        { provide: CartService, useValue: mockCartService },
        { provide: 'PRODUCTS_REPOSITORY', useValue: mockProductsRepo },
        { provide: 'SHOPS_REPOSITORY', useValue: mockShopsRepo },
        { provide: 'IShippersRepository', useValue: mockShippersRepo },
        { provide: ADDRESSES_REPOSITORY, useValue: mockAddressesRepo },
        { provide: USERS_REPOSITORY, useValue: mockUsersRepo },
        { provide: VouchersService, useValue: mockVouchersService },
        { provide: NotificationsService, useValue: mockNotificationsService },
        { provide: PaymentsService, useValue: mockPaymentsService },
        { provide: WalletsService, useValue: mockWalletsService },
        { provide: OrderStateMachineService, useValue: mockStateMachine },
        { provide: ConfigService, useValue: mockConfigService },
        { provide: FirebaseService, useValue: mockFirebaseService },
        {
          provide: BuyersStatsService,
          useValue: {
            incrementOrderCount: jest.fn().mockResolvedValue(undefined),
            updateTotalSpent: jest.fn().mockResolvedValue(undefined),
            updateBuyerStatsOnDelivery: jest.fn().mockResolvedValue(undefined),
          },
        },
        {
          provide: ShopsService,
          useValue: {
            updateShopStats: jest.fn().mockResolvedValue(undefined),
            getMyShop: jest.fn(),
          },
        },
      ],
    }).compile();

    service = module.get<OrdersService>(OrdersService);
  });

  describe('mapToListDto', () => {
    it('should map order with 1 item - preview size 1', () => {
      const order: OrderEntity = {
        id: 'order_123',
        orderNumber: 'ORD-001',
        customerId: 'user_cust_001',
        customerSnapshot: {
          id: 'user_cust_001',
          displayName: 'Nguyễn Văn A',
          phone: '0901234567',
        },
        shopId: 'shop_123',
        shopName: 'Cơm Tấm Sườn',
        shipperId: null,
        items: [
          {
            productId: 'prod_001',
            productName: 'Cơm Tấm Sườn',
            quantity: 1,
            price: 35000,
            subtotal: 35000,
          },
        ],
        subtotal: 35000,
        shipFee: 0,
        shipperPayout: 15000,
        discount: 0,
        total: 35000,
        status: OrderStatus.PENDING,
        paymentStatus: PaymentStatus.UNPAID,
        paymentMethod: 'COD',
        deliveryAddress: {
          fullAddress: '268 Lý Thường Kiệt, P14, Q10, TP.HCM',
          label: 'home',
        },
        createdAt: Timestamp.fromDate(new Date('2026-01-19T10:00:00Z')),
      };

      const dto = service['mapToListDto'](order);

      expect(dto.id).toBe('order_123');
      expect(dto.orderNumber).toBe('ORD-001');
      expect(dto.itemCount).toBe(1);
      expect(dto.itemsPreview).toHaveLength(1);
      expect(dto.itemsPreviewCount).toBe(1);
      expect(dto.itemsPreview![0]).toEqual({
        productId: 'prod_001',
        productName: 'Cơm Tấm Sườn',
        quantity: 1,
        price: 35000,
        subtotal: 35000,
      });
      expect(dto.customer).toEqual({
        id: 'user_cust_001',
        displayName: 'Nguyễn Văn A',
        phone: '0901234567',
      });
      expect(dto.createdAt).toBe('2026-01-19T10:00:00.000Z');
    });

    it('should map order with 5 items - preview size 3, itemCount 5', () => {
      const order: OrderEntity = {
        id: 'order_456',
        orderNumber: 'ORD-002',
        customerId: 'user_cust_002',
        customerSnapshot: {
          id: 'user_cust_002',
          displayName: 'Trần Thị B',
        },
        shopId: 'shop_123',
        shopName: 'Cơm Tấm Sườn',
        shipperId: null,
        items: [
          {
            productId: 'prod_001',
            productName: 'Cơm Tấm Sườn',
            quantity: 2,
            price: 35000,
            subtotal: 70000,
          },
          {
            productId: 'prod_002',
            productName: 'Cơm Tấm Bì',
            quantity: 1,
            price: 30000,
            subtotal: 30000,
          },
          {
            productId: 'prod_003',
            productName: 'Cơm Tấm Chả',
            quantity: 1,
            price: 28000,
            subtotal: 28000,
          },
          {
            productId: 'prod_004',
            productName: 'Trà Đá',
            quantity: 3,
            price: 5000,
            subtotal: 15000,
          },
          {
            productId: 'prod_005',
            productName: 'Nước Suối',
            quantity: 2,
            price: 10000,
            subtotal: 20000,
          },
        ],
        subtotal: 163000,
        shipFee: 0,
        shipperPayout: 15000,
        discount: 0,
        total: 163000,
        status: OrderStatus.CONFIRMED,
        paymentStatus: PaymentStatus.PAID,
        paymentMethod: 'ZALOPAY',
        deliveryAddress: {
          fullAddress: '268 Lý Thường Kiệt, P14, Q10, TP.HCM',
          label: 'home',
        },
        createdAt: Timestamp.fromDate(new Date('2026-01-19T11:30:00Z')),
      };

      const dto = service['mapToListDto'](order);

      expect(dto.id).toBe('order_456');
      expect(dto.orderNumber).toBe('ORD-002');
      expect(dto.itemCount).toBe(5); // Total items
      expect(dto.itemsPreview).toHaveLength(3); // Max preview
      expect(dto.itemsPreviewCount).toBe(3);

      // Verify first 3 items are in preview
      expect(dto.itemsPreview![0].productName).toBe('Cơm Tấm Sườn');
      expect(dto.itemsPreview![1].productName).toBe('Cơm Tấm Bì');
      expect(dto.itemsPreview![2].productName).toBe('Cơm Tấm Chả');

      // Customer without phone
      expect(dto.customer).toEqual({
        id: 'user_cust_002',
        displayName: 'Trần Thị B',
        phone: undefined,
      });
    });

    it('should map order without customer snapshot', () => {
      const order: OrderEntity = {
        id: 'order_789',
        orderNumber: 'ORD-003',
        customerId: 'user_cust_003',
        // No customerSnapshot (legacy order)
        shopId: 'shop_456',
        shopName: 'Phở Bò',
        shipperId: null,
        items: [
          {
            productId: 'prod_101',
            productName: 'Phở Bò Tái',
            quantity: 1,
            price: 45000,
            subtotal: 45000,
          },
        ],
        subtotal: 45000,
        shipFee: 0,
        shipperPayout: 20000,
        discount: 0,
        total: 45000,
        status: OrderStatus.DELIVERED,
        paymentStatus: PaymentStatus.PAID,
        paymentMethod: 'COD',
        deliveryAddress: {
          fullAddress: '123 Lê Lợi, Q1, TP.HCM',
          label: 'office',
        },
        createdAt: Timestamp.fromDate(new Date('2026-01-18T15:00:00Z')),
      };

      const dto = service['mapToListDto'](order);

      expect(dto.id).toBe('order_789');
      expect(dto.customer).toBeUndefined(); // No snapshot available
      expect(dto.itemsPreview).toHaveLength(1);
      expect(dto.itemCount).toBe(1);
    });

    it('should preserve ISO string format for createdAt', () => {
      const testDate = new Date('2026-01-19T12:34:56.789Z');
      const order: OrderEntity = {
        id: 'order_999',
        orderNumber: 'ORD-004',
        customerId: 'user_cust_004',
        shopId: 'shop_789',
        shopName: 'Test Shop',
        shipperId: null,
        items: [
          {
            productId: 'prod_999',
            productName: 'Test Product',
            quantity: 1,
            price: 10000,
            subtotal: 10000,
          },
        ],
        subtotal: 10000,
        shipFee: 0,
        discount: 0,
        total: 10000,
        status: OrderStatus.PENDING,
        paymentStatus: PaymentStatus.UNPAID,
        paymentMethod: 'COD',
        deliveryAddress: {
          fullAddress: 'Test Address',
          label: 'home',
        },
        createdAt: Timestamp.fromDate(testDate),
      };

      const dto = service['mapToListDto'](order);

      expect(dto.createdAt).toBe('2026-01-19T12:34:56.789Z');
      expect(typeof dto.createdAt).toBe('string');
    });

    it('should include paymentMethod, deliveryAddress, shipperId, and updatedAt (OWNER list overview)', () => {
      const order: OrderEntity = {
        id: 'order_555',
        orderNumber: 'ORD-005',
        customerId: 'user_cust_005',
        customerSnapshot: {
          id: 'user_cust_005',
          displayName: 'Lê Văn C',
          phone: '0987654321',
        },
        shopId: 'shop_123',
        shopName: 'Cơm Tấm Sườn',
        shipperId: 'shipper_456',
        items: [
          {
            productId: 'prod_123',
            productName: 'Cơm sườn bì chả',
            quantity: 2,
            price: 35000,
            subtotal: 70000,
          },
        ],
        subtotal: 70000,
        shipFee: 0,
        shipperPayout: 15000,
        discount: 0,
        total: 70000,
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
        createdAt: Timestamp.fromDate(new Date('2026-01-19T10:00:00Z')),
        updatedAt: Timestamp.fromDate(new Date('2026-01-19T10:30:00Z')),
      };

      const dto = service['mapToListDto'](order);

      // Verify new overview fields
      expect(dto.paymentMethod).toBe('COD');
      expect(dto.deliveryAddress).toEqual({
        label: 'home',
        fullAddress: '268 Lý Thường Kiệt, Phường 14, Quận 10, TP.HCM',
        building: 'A1',
        room: '101',
      });
      expect(dto.shipperId).toBe('shipper_456');
      expect(dto.updatedAt).toBe('2026-01-19T10:30:00.000Z');

      // Verify existing fields still work
      expect(dto.itemsPreview).toHaveLength(1);
      expect(dto.customer).toEqual({
        id: 'user_cust_005',
        displayName: 'Lê Văn C',
        phone: '0987654321',
      });
    });

    it('should handle order without deliveryAddress or shipperId (edge case)', () => {
      const order: OrderEntity = {
        id: 'order_666',
        orderNumber: 'ORD-006',
        customerId: 'user_cust_006',
        shopId: 'shop_123',
        shopName: 'Test Shop',
        shipperId: null,
        items: [
          {
            productId: 'prod_001',
            productName: 'Test Product',
            quantity: 1,
            price: 10000,
            subtotal: 10000,
          },
        ],
        subtotal: 10000,
        shipFee: 0,
        discount: 0,
        total: 10000,
        status: OrderStatus.PENDING,
        paymentStatus: PaymentStatus.UNPAID,
        paymentMethod: 'ZALOPAY',
        deliveryAddress: {
          fullAddress: 'Minimal Address',
        },
        createdAt: Timestamp.fromDate(new Date('2026-01-19T10:00:00Z')),
      };

      const dto = service['mapToListDto'](order);

      expect(dto.paymentMethod).toBe('ZALOPAY');
      expect(dto.deliveryAddress).toEqual({
        label: undefined,
        fullAddress: 'Minimal Address',
        building: undefined,
        room: undefined,
      });
      expect(dto.shipperId).toBeNull(); // Now returns null instead of undefined
      expect(dto.updatedAt).toBeUndefined(); // No updatedAt timestamp
    });

    it('should include shipper snapshot when shipperSnapshot is present', () => {
      const order: OrderEntity = {
        id: 'order_777',
        orderNumber: 'ORD-007',
        customerId: 'user_cust_007',
        customerSnapshot: {
          id: 'user_cust_007',
          displayName: 'Nguyễn Thị D',
          phone: '0912345678',
        },
        shopId: 'shop_123',
        shopName: 'Cơm Tấm Sườn',
        shipperId: 'shipper_789',
        shipperSnapshot: {
          id: 'shipper_789',
          displayName: 'Nguyễn Văn Ship',
          phone: '0923456789',
        },
        items: [
          {
            productId: 'prod_123',
            productName: 'Cơm sườn',
            quantity: 1,
            price: 35000,
            subtotal: 35000,
          },
        ],
        subtotal: 35000,
        shipFee: 0,
        shipperPayout: 15000,
        discount: 0,
        total: 35000,
        status: OrderStatus.SHIPPING,
        paymentStatus: PaymentStatus.PAID,
        paymentMethod: 'ZALOPAY',
        deliveryAddress: {
          label: 'home',
          fullAddress: '123 Test Street',
        },
        createdAt: Timestamp.fromDate(new Date('2026-01-19T11:00:00Z')),
        updatedAt: Timestamp.fromDate(new Date('2026-01-19T11:30:00Z')),
      };

      const dto = service['mapToListDto'](order);

      // Verify shipper snapshot is included
      expect(dto.shipperId).toBe('shipper_789');
      expect(dto.shipper).toEqual({
        id: 'shipper_789',
        displayName: 'Nguyễn Văn Ship',
        phone: '0923456789',
      });

      // Verify customer snapshot is also included
      expect(dto.customer).toEqual({
        id: 'user_cust_007',
        displayName: 'Nguyễn Thị D',
        phone: '0912345678',
      });
    });

    it('should handle order with shipperId but no shipperSnapshot (legacy case)', () => {
      const order: OrderEntity = {
        id: 'order_888',
        orderNumber: 'ORD-008',
        customerId: 'user_cust_008',
        shopId: 'shop_123',
        shopName: 'Test Shop',
        shipperId: 'shipper_999', // Has shipperId but no snapshot
        items: [
          {
            productId: 'prod_001',
            productName: 'Test Product',
            quantity: 1,
            price: 10000,
            subtotal: 10000,
          },
        ],
        subtotal: 10000,
        shipFee: 0,
        shipperPayout: 15000,
        discount: 0,
        total: 10000,
        status: OrderStatus.SHIPPING,
        paymentStatus: PaymentStatus.UNPAID,
        paymentMethod: 'COD',
        deliveryAddress: {
          fullAddress: 'Test Address',
        },
        createdAt: Timestamp.fromDate(new Date('2026-01-19T12:00:00Z')),
      };

      const dto = service['mapToListDto'](order);

      expect(dto.shipperId).toBe('shipper_999');
      expect(dto.shipper).toBeUndefined(); // No snapshot available (legacy order)
    });

    it('should resolve shipper from shipperMap when snapshot is missing', () => {
      const order: OrderEntity = {
        id: 'order_999',
        orderNumber: 'ORD-009',
        customerId: 'user_cust_009',
        shopId: 'shop_123',
        shopName: 'Test Shop',
        shipperId: 'shipper_resolved_123',
        // No shipperSnapshot - should use map
        items: [
          {
            productId: 'prod_001',
            productName: 'Test Product',
            quantity: 1,
            price: 10000,
            subtotal: 10000,
          },
        ],
        subtotal: 10000,
        shipFee: 0,
        shipperPayout: 15000,
        discount: 0,
        total: 10000,
        status: OrderStatus.SHIPPING,
        paymentStatus: PaymentStatus.UNPAID,
        paymentMethod: 'COD',
        deliveryAddress: {
          fullAddress: 'Test Address',
        },
        createdAt: Timestamp.fromDate(new Date('2026-01-19T13:00:00Z')),
      };

      const shipperMap = new Map([
        [
          'shipper_resolved_123',
          {
            id: 'shipper_resolved_123',
            displayName: 'Resolved Shipper',
            phone: '0999888777',
          },
        ],
      ]);

      const dto = service['mapToListDto'](order, shipperMap);

      expect(dto.shipperId).toBe('shipper_resolved_123');
      expect(dto.shipper).toEqual({
        id: 'shipper_resolved_123',
        displayName: 'Resolved Shipper',
        phone: '0999888777',
      });
    });

    it('should use shipperSnapshot over shipperMap when both present', () => {
      const order: OrderEntity = {
        id: 'order_1000',
        orderNumber: 'ORD-010',
        customerId: 'user_cust_010',
        shopId: 'shop_123',
        shopName: 'Test Shop',
        shipperId: 'shipper_123',
        shipperSnapshot: {
          id: 'shipper_123',
          displayName: 'Snapshot Shipper',
          phone: '0911222333',
        },
        items: [
          {
            productId: 'prod_001',
            productName: 'Test Product',
            quantity: 1,
            price: 10000,
            subtotal: 10000,
          },
        ],
        subtotal: 10000,
        shipFee: 0,
        shipperPayout: 15000,
        discount: 0,
        total: 10000,
        status: OrderStatus.SHIPPING,
        paymentStatus: PaymentStatus.PAID,
        paymentMethod: 'ZALOPAY',
        deliveryAddress: {
          fullAddress: 'Test Address',
        },
        createdAt: Timestamp.fromDate(new Date('2026-01-19T14:00:00Z')),
      };

      const shipperMap = new Map([
        [
          'shipper_123',
          {
            id: 'shipper_123',
            displayName: 'Map Shipper (should be ignored)',
            phone: '0988999000',
          },
        ],
      ]);

      const dto = service['mapToListDto'](order, shipperMap);

      expect(dto.shipperId).toBe('shipper_123');
      // Should use snapshot, not map
      expect(dto.shipper).toEqual({
        id: 'shipper_123',
        displayName: 'Snapshot Shipper',
        phone: '0911222333',
      });
    });

    it('should handle null shipperId correctly', () => {
      const order: OrderEntity = {
        id: 'order_1001',
        orderNumber: 'ORD-011',
        customerId: 'user_cust_011',
        shopId: 'shop_123',
        shopName: 'Test Shop',
        shipperId: null, // Not assigned
        items: [
          {
            productId: 'prod_001',
            productName: 'Test Product',
            quantity: 1,
            price: 10000,
            subtotal: 10000,
          },
        ],
        subtotal: 10000,
        shipFee: 0,
        shipperPayout: 15000,
        discount: 0,
        total: 10000,
        status: OrderStatus.PENDING,
        paymentStatus: PaymentStatus.UNPAID,
        paymentMethod: 'COD',
        deliveryAddress: {
          fullAddress: 'Test Address',
        },
        createdAt: Timestamp.fromDate(new Date('2026-01-19T15:00:00Z')),
      };

      const dto = service['mapToListDto'](order);

      expect(dto.shipperId).toBeNull();
      expect(dto.shipper).toBeUndefined();
    });

    it('should resolve customer from customerMap when snapshot is missing (legacy order)', () => {
      const order: OrderEntity = {
        id: 'order_legacy_001',
        orderNumber: 'ORD-LEGACY-001',
        customerId: 'user_cust_legacy_123',
        // NO customerSnapshot - should use customerMap
        shopId: 'shop_123',
        shopName: 'Cơm Tấm Sườn',
        shipperId: null,
        items: [
          {
            productId: 'prod_001',
            productName: 'Cơm Tấm Sườn',
            quantity: 1,
            price: 35000,
            subtotal: 35000,
          },
        ],
        subtotal: 35000,
        shipFee: 0,
        shipperPayout: 15000,
        discount: 0,
        total: 35000,
        status: OrderStatus.DELIVERED,
        paymentStatus: PaymentStatus.PAID,
        paymentMethod: 'COD',
        deliveryAddress: {
          fullAddress: '123 Test Street',
        },
        createdAt: Timestamp.fromDate(new Date('2026-01-19T16:00:00Z')),
      };

      const customerMap = new Map([
        [
          'user_cust_legacy_123',
          {
            id: 'user_cust_legacy_123',
            displayName: 'Resolved Customer',
            phone: '0901111111',
          },
        ],
      ]);

      const shipperMap = new Map();

      const dto = service['mapToListDto'](order, shipperMap, customerMap);

      expect(dto.customer).toEqual({
        id: 'user_cust_legacy_123',
        displayName: 'Resolved Customer',
        phone: '0901111111',
      });
    });

    it('should omit phone field if customer has no phone in customerMap', () => {
      const order: OrderEntity = {
        id: 'order_no_phone_001',
        orderNumber: 'ORD-NO-PHONE-001',
        customerId: 'user_cust_no_phone',
        // NO customerSnapshot
        shopId: 'shop_123',
        shopName: 'Test Shop',
        shipperId: null,
        items: [
          {
            productId: 'prod_001',
            productName: 'Test Product',
            quantity: 1,
            price: 10000,
            subtotal: 10000,
          },
        ],
        subtotal: 10000,
        shipFee: 0,
        shipperPayout: 5000,
        discount: 0,
        total: 15000,
        status: OrderStatus.PENDING,
        paymentStatus: PaymentStatus.UNPAID,
        paymentMethod: 'COD',
        deliveryAddress: {
          fullAddress: 'Test Address',
        },
        createdAt: Timestamp.fromDate(new Date('2026-01-19T17:00:00Z')),
      };

      const customerMap = new Map([
        [
          'user_cust_no_phone',
          {
            id: 'user_cust_no_phone',
            displayName: 'Customer No Phone',
            // phone field is undefined
          },
        ],
      ]);

      const shipperMap = new Map();

      const dto = service['mapToListDto'](order, shipperMap, customerMap);

      // Should not include phone field at all when undefined
      expect(dto.customer).toEqual({
        id: 'user_cust_no_phone',
        displayName: 'Customer No Phone',
      });
      expect(dto.customer?.phone).toBeUndefined();
    });

    it('should use customerSnapshot over customerMap when both present', () => {
      const order: OrderEntity = {
        id: 'order_snapshot_priority',
        orderNumber: 'ORD-SNAPSHOT-PRIORITY',
        customerId: 'user_cust_priority_123',
        customerSnapshot: {
          id: 'user_cust_priority_123',
          displayName: 'Snapshot Customer',
          phone: '0922222222',
        },
        shopId: 'shop_123',
        shopName: 'Test Shop',
        shipperId: null,
        items: [
          {
            productId: 'prod_001',
            productName: 'Test Product',
            quantity: 1,
            price: 10000,
            subtotal: 10000,
          },
        ],
        subtotal: 10000,
        shipFee: 0,
        shipperPayout: 5000,
        discount: 0,
        total: 15000,
        status: OrderStatus.PENDING,
        paymentStatus: PaymentStatus.UNPAID,
        paymentMethod: 'COD',
        deliveryAddress: {
          fullAddress: 'Test Address',
        },
        createdAt: Timestamp.fromDate(new Date('2026-01-19T18:00:00Z')),
      };

      const customerMap = new Map([
        [
          'user_cust_priority_123',
          {
            id: 'user_cust_priority_123',
            displayName: 'Map Customer (should be ignored)',
            phone: '0933333333',
          },
        ],
      ]);

      const shipperMap = new Map();

      const dto = service['mapToListDto'](order, shipperMap, customerMap);

      // Should use snapshot, not map
      expect(dto.customer).toEqual({
        id: 'user_cust_priority_123',
        displayName: 'Snapshot Customer',
        phone: '0922222222',
      });
    });

    it('should resolve customer from customerMap while also resolving shipper from shipperMap', () => {
      const order: OrderEntity = {
        id: 'order_both_resolved',
        orderNumber: 'ORD-BOTH-RESOLVED',
        customerId: 'user_cust_both_001',
        // NO customerSnapshot
        shopId: 'shop_123',
        shopName: 'Cơm Tấm Sườn',
        shipperId: 'shipper_resolved_456',
        // NO shipperSnapshot
        items: [
          {
            productId: 'prod_001',
            productName: 'Cơm Tấm Sườn',
            quantity: 1,
            price: 35000,
            subtotal: 35000,
          },
        ],
        subtotal: 35000,
        shipFee: 0,
        shipperPayout: 15000,
        discount: 0,
        total: 35000,
        status: OrderStatus.SHIPPING,
        paymentStatus: PaymentStatus.PAID,
        paymentMethod: 'COD',
        deliveryAddress: {
          fullAddress: '268 Lý Thường Kiệt',
        },
        createdAt: Timestamp.fromDate(new Date('2026-01-19T19:00:00Z')),
      };

      const customerMap = new Map([
        [
          'user_cust_both_001',
          {
            id: 'user_cust_both_001',
            displayName: 'Customer From Map',
            phone: '0944444444',
          },
        ],
      ]);

      const shipperMap = new Map([
        [
          'shipper_resolved_456',
          {
            id: 'shipper_resolved_456',
            displayName: 'Shipper From Map',
            phone: '0955555555',
          },
        ],
      ]);

      const dto = service['mapToListDto'](order, shipperMap, customerMap);

      // Both should be resolved
      expect(dto.customer).toEqual({
        id: 'user_cust_both_001',
        displayName: 'Customer From Map',
        phone: '0944444444',
      });

      expect(dto.shipperId).toBe('shipper_resolved_456');
      expect(dto.shipper).toEqual({
        id: 'shipper_resolved_456',
        displayName: 'Shipper From Map',
        phone: '0955555555',
      });
    });
  });
});
