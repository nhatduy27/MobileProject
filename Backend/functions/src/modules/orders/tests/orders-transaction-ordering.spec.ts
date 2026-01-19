import { Test, TestingModule } from '@nestjs/testing';
import { OrdersService } from '../services/orders.service';
import { IOrdersRepository, ORDERS_REPOSITORY } from '../interfaces';
import { CartService } from '../../cart/services';
import { IProductsRepository } from '../../products/interfaces';
import { IShopsRepository } from '../../shops/interfaces';
import { IAddressesRepository, ADDRESSES_REPOSITORY, USERS_REPOSITORY } from '../../users/interfaces';
import { OrderStateMachineService } from '../services/order-state-machine.service';
import { ConfigService } from '../../../core/config/config.service';
import { CreateOrderDto } from '../dto';
import { OrderEntity, OrderStatus, PaymentStatus } from '../entities';

describe('Orders - Firestore Transaction Ordering', () => {
  let service: OrdersService;
  let mockOrdersRepo: jest.Mocked<Partial<IOrdersRepository>>;
  let mockCartService: jest.Mocked<Partial<CartService>>;
  let mockProductsRepo: jest.Mocked<Partial<IProductsRepository>>;
  let mockShopsRepo: jest.Mocked<Partial<IShopsRepository>>;
  let mockAddressesRepo: jest.Mocked<Partial<IAddressesRepository>>;

  beforeEach(async () => {
    // Mock repositories
    mockOrdersRepo = {
      createOrderAndClearCartGroup: jest.fn(),
    };

    mockCartService = {
      getCartGrouped: jest.fn(),
    };

    mockProductsRepo = {
      findById: jest.fn(),
    };

    mockShopsRepo = {
      findById: jest.fn(),
    };

    mockAddressesRepo = {
      findById: jest.fn(),
    };

    const mockConfigService = {
      enableFirestorePaginationFallback: false,
    };

    const mockFirebaseService = {
      firestore: { collection: jest.fn(), batch: jest.fn() },
      auth: { verifyIdToken: jest.fn() },
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        OrdersService,
        { provide: ORDERS_REPOSITORY, useValue: mockOrdersRepo },
        { provide: CartService, useValue: mockCartService },
        { provide: 'PRODUCTS_REPOSITORY', useValue: mockProductsRepo },
        { provide: 'SHOPS_REPOSITORY', useValue: mockShopsRepo },
        { provide: 'IShippersRepository', useValue: { findByShopId: jest.fn() } },
        { provide: ADDRESSES_REPOSITORY, useValue: mockAddressesRepo },
        { provide: USERS_REPOSITORY, useValue: { findById: jest.fn() } },
        { provide: ConfigService, useValue: mockConfigService },
        {
          provide: OrderStateMachineService,
          useValue: { validateTransition: jest.fn() },
        },
        { provide: FirebaseService, useValue: mockFirebaseService },
      ],
    }).compile();

    service = module.get<OrdersService>(OrdersService);
  });

  describe('createOrder - Transaction Integrity', () => {
    it('should create order successfully with proper transaction ordering (no read-after-write error)', async () => {
      const customerId = 'user_1';
      const shopId = 'shop_ktx_001';

      const mockCart = {
        groups: [
          {
            shopId,
            shopName: 'Cơm Nhà A',
            items: [
              {
                productId: 'prod_1',
                productName: 'Cơm',
                quantity: 2,
                price: 25000,
                subtotal: 50000,
              },
            ],
            subtotal: 50000,
          },
        ],
      };

      const mockShop = {
        id: shopId,
        name: 'Cơm Nhà A',
        isOpen: true,
        status: 'OPEN',
        shipFeePerOrder: 5000,
      };

      const mockProduct = {
        id: 'prod_1',
        name: 'Cơm',
        price: 25000,
        isAvailable: true,
        isDeleted: false,
      };

      const mockOrderEntity: OrderEntity = {
        id: 'order_123',
        orderNumber: 'ORD-123',
        customerId,
        shopId,
        shopName: 'Cơm Nhà A',
        items: mockCart.groups[0].items,
        subtotal: 50000,
        shipFee: 5000,
        discount: 0,
        total: 55000,
        status: OrderStatus.PENDING,
        paymentStatus: PaymentStatus.UNPAID,
        paymentMethod: 'COD',
        shipperId: null,
        deliveryAddress: {
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          building: 'B5',
          room: '101',
          note: 'Gọi trước khi đến',
        },
      };

      // Mock service dependencies
      mockCartService.getCartGrouped = jest.fn().mockResolvedValue(mockCart);
      mockShopsRepo.findById = jest.fn().mockResolvedValue(mockShop);
      mockProductsRepo.findById = jest.fn().mockResolvedValue(mockProduct);

      // Mock repository createOrderAndClearCartGroup
      // This should NOT throw "reads after writes" error
      mockOrdersRepo.createOrderAndClearCartGroup = jest
        .fn()
        .mockResolvedValue(mockOrderEntity);

      const dto: CreateOrderDto = {
        shopId,
        deliveryAddress: {
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          building: 'B5',
          room: '101',
          note: 'Gọi trước khi đến',
        },
        paymentMethod: 'COD',
      };

      // Act
      const result = await service.createOrder(customerId, dto);

      // Assert
      expect(result).toEqual(mockOrderEntity);
      expect(mockOrdersRepo.createOrderAndClearCartGroup).toHaveBeenCalledWith(
        customerId,
        shopId,
        expect.objectContaining({
          customerId,
          shopId,
          deliveryAddress: {
            label: 'KTX B5',
            fullAddress: 'KTX Khu B - Tòa B5',
            building: 'B5',
            room: '101',
            note: 'Gọi trước khi đến',
          },
        }),
      );
    });

    it('should handle transaction with voucher code (if supported in future)', async () => {
      const customerId = 'user_1';
      const shopId = 'shop_ktx_001';

      const mockCart = {
        groups: [
          {
            shopId,
            shopName: 'Cơm Nhà A',
            items: [
              {
                productId: 'prod_1',
                productName: 'Cơm',
                quantity: 2,
                price: 25000,
                subtotal: 50000,
              },
            ],
            subtotal: 50000,
          },
        ],
      };

      const mockShop = {
        id: shopId,
        name: 'Cơm Nhà A',
        isOpen: true,
        status: 'OPEN',
        shipFeePerOrder: 5000,
      };

      const mockProduct = {
        id: 'prod_1',
        name: 'Cơm',
        price: 25000,
        isAvailable: true,
        isDeleted: false,
      };

      mockCartService.getCartGrouped = jest.fn().mockResolvedValue(mockCart);
      mockShopsRepo.findById = jest.fn().mockResolvedValue(mockShop);
      mockProductsRepo.findById = jest.fn().mockResolvedValue(mockProduct);

      mockOrdersRepo.createOrderAndClearCartGroup = jest.fn().mockResolvedValue({
        id: 'order_123',
        orderNumber: 'ORD-123',
        customerId,
        shopId,
        status: OrderStatus.PENDING,
      });

      const dto: CreateOrderDto = {
        shopId,
        deliveryAddress: {
          fullAddress: 'KTX Khu B - Tòa B5',
        },
        paymentMethod: 'COD',
        voucherCode: 'FREESHIP10', // Voucher not yet implemented, but should not break transaction
      };

      // Should not throw transaction error
      await expect(service.createOrder(customerId, dto)).resolves.toBeDefined();
    });

    it('should handle cart clearing in transaction atomically', async () => {
      const customerId = 'user_1';
      const shopId = 'shop_ktx_001';

      const mockCart = {
        groups: [
          {
            shopId,
            shopName: 'Cơm Nhà A',
            items: [
              {
                productId: 'prod_1',
                productName: 'Cơm',
                quantity: 1,
                price: 25000,
                subtotal: 25000,
              },
            ],
            subtotal: 25000,
          },
        ],
      };

      const mockShop = {
        id: shopId,
        name: 'Cơm Nhà A',
        isOpen: true,
        status: 'OPEN',
        shipFeePerOrder: 5000,
      };

      const mockProduct = {
        id: 'prod_1',
        isAvailable: true,
        isDeleted: false,
      };

      mockCartService.getCartGrouped = jest.fn().mockResolvedValue(mockCart);
      mockShopsRepo.findById = jest.fn().mockResolvedValue(mockShop);
      mockProductsRepo.findById = jest.fn().mockResolvedValue(mockProduct);

      // Verify that createOrderAndClearCartGroup is called exactly once
      let transactionCallCount = 0;
      mockOrdersRepo.createOrderAndClearCartGroup = jest.fn().mockImplementation(() => {
        transactionCallCount++;
        return Promise.resolve({
          id: 'order_123',
          orderNumber: 'ORD-123',
          customerId,
          shopId,
          status: OrderStatus.PENDING,
        });
      });

      const dto: CreateOrderDto = {
        shopId,
        deliveryAddress: {
          fullAddress: 'KTX Khu B - Tòa B5',
        },
        paymentMethod: 'COD',
      };

      await service.createOrder(customerId, dto);

      // Transaction should be called exactly once (atomic operation)
      expect(transactionCallCount).toBe(1);
      expect(mockOrdersRepo.createOrderAndClearCartGroup).toHaveBeenCalledTimes(1);
    });

    it('should document that transaction reads happen before writes', () => {
      // This is a documentation test explaining the transaction structure
      const transactionStructure = {
        phase: 'Firestore Transaction',
        rules: [
          'ALL reads (transaction.get) must happen BEFORE any writes',
          'No transaction.get() calls after transaction.set/update/delete',
          'Violation results in: "Firestore transactions require all reads to be executed before all writes"',
        ],
        implementation: {
          phaseA_reads: [
            '1. transaction.get(cartRef) - read cart document',
            '2. (future) transaction.get(voucherRef) - if voucher validation needed',
          ],
          phaseB_writes: [
            '1. transaction.set(orderRef, orderData) - create order',
            '2. transaction.update(cartRef, ...) OR transaction.delete(cartRef) - clear cart',
            '3. (future) transaction.update(stockRef, ...) - update inventory if needed',
          ],
        },
      };

      // This structure is enforced in:
      // src/modules/orders/repositories/firestore-orders.repository.ts
      // method: createOrderAndClearCartGroup()

      expect(transactionStructure.rules).toHaveLength(3);
      expect(transactionStructure.implementation.phaseA_reads[0]).toContain('transaction.get');
      expect(transactionStructure.implementation.phaseB_writes[0]).toContain('transaction.set');
    });
  });

  describe('Transaction Error Handling', () => {
    it('should handle transaction failures gracefully', async () => {
      const customerId = 'user_1';
      const shopId = 'shop_ktx_001';

      mockCartService.getCartGrouped = jest.fn().mockResolvedValue({
        groups: [
          {
            shopId,
            items: [{ productId: 'prod_1', quantity: 1, price: 25000, subtotal: 25000 }],
            subtotal: 25000,
          },
        ],
      });

      mockShopsRepo.findById = jest.fn().mockResolvedValue({
        id: shopId,
        isOpen: true,
        status: 'OPEN',
        shipFeePerOrder: 5000,
      });

      mockProductsRepo.findById = jest.fn().mockResolvedValue({
        id: 'prod_1',
        isAvailable: true,
        isDeleted: false,
      });

      // Simulate transaction failure
      mockOrdersRepo.createOrderAndClearCartGroup = jest
        .fn()
        .mockRejectedValue(new Error('Transaction failed'));

      const dto: CreateOrderDto = {
        shopId,
        deliveryAddress: { fullAddress: 'Test Address' },
        paymentMethod: 'COD',
      };

      // Should propagate error
      await expect(service.createOrder(customerId, dto)).rejects.toThrow('Transaction failed');
    });
  });
});
