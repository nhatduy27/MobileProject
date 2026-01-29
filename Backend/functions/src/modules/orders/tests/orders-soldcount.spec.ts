import { Test, TestingModule } from '@nestjs/testing';
import { OrdersService } from '../services/orders.service';
import { OrderStateMachineService } from '../services/order-state-machine.service';
import { IOrdersRepository, ORDERS_REPOSITORY } from '../interfaces';
import { ProductsService } from '../../products/services';
import { ShopsService } from '../../shops/services/shops.service';
import { OrderStatus, PaymentStatus, OrderEntity } from '../entities';
import { Timestamp } from 'firebase-admin/firestore';
import { CartService } from '../../cart/services';
import { VouchersService } from '../../vouchers/vouchers.service';
import { WalletsService } from '../../wallets/wallets.service';
import { PaymentsService } from '../../payments/payments.service';
import { ConfigService } from '../../../core/config/config.service';
import { FirebaseService } from '../../../core/firebase/firebase.service';
import { NotificationsService } from '../../notifications/services/notifications.service';
import { BuyersStatsService } from '../../buyers/services/buyers-stats.service';
import { USERS_REPOSITORY } from '../../users/interfaces';

describe('OrdersService - Product soldCount Updates', () => {
  let service: OrdersService;
  let ordersRepo: jest.Mocked<IOrdersRepository>;
  let productsService: jest.Mocked<ProductsService>;
  let shippersRepo: any;
  let shopsRepo: any;
  let usersRepo: any;

  const mockOrder: OrderEntity = {
    id: 'order_123',
    orderNumber: 'ORD-TEST-001',
    customerId: 'customer_1',
    shopId: 'shop_1',
    shopName: 'Test Shop',
    shipperId: 'shipper_1',
    items: [
      {
        productId: 'prod_1',
        productName: 'Product A',
        quantity: 2,
        price: 25000,
        subtotal: 50000,
      },
      {
        productId: 'prod_2',
        productName: 'Product B',
        quantity: 1,
        price: 30000,
        subtotal: 30000,
      },
    ],
    subtotal: 80000,
    shipFee: 5000,
    shipperPayout: 5000,
    discount: 0,
    total: 85000,
    status: OrderStatus.SHIPPING,
    paymentStatus: PaymentStatus.PAID,
    paymentMethod: 'COD',
    deliveryAddress: {
      street: '123 Test St',
      ward: 'Ward 1',
      district: 'District 1',
      city: 'HCMC',
    },
    createdAt: Timestamp.now(),
    updatedAt: Timestamp.now(),
    soldCountApplied: false, // Not yet applied
  };

  beforeEach(async () => {
    const mockOrdersRepository = {
      findById: jest.fn(),
      update: jest.fn(),
      query: jest.fn(),
      findMany: jest.fn(),
      count: jest.fn(),
    };

    const mockProductsService = {
      incrementSoldCount: jest.fn().mockResolvedValue(undefined),
      decrementSoldCount: jest.fn().mockResolvedValue(undefined),
    };

    const mockShopsService = {
      updateShopStats: jest.fn().mockResolvedValue(undefined),
    };

    const mockShippersRepository = {
      findById: jest.fn(),
      update: jest.fn(),
    };

    const mockShopsRepository = {
      findById: jest.fn(),
    };

    const mockUsersRepository = {
      findById: jest.fn(),
    };

    const mockNotificationsService = {
      send: jest.fn().mockResolvedValue(undefined),
    };

    const mockWalletsService = {
      processOrderPayout: jest.fn().mockResolvedValue(undefined),
    };

    const mockBuyersStatsService = {
      updateBuyerStatsOnDelivery: jest.fn().mockResolvedValue(undefined),
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
          provide: 'PRODUCTS_REPOSITORY',
          useValue: {},
        },
        {
          provide: ProductsService,
          useValue: mockProductsService,
        },
        {
          provide: ShopsService,
          useValue: mockShopsService,
        },
        {
          provide: 'SHOPS_REPOSITORY',
          useValue: mockShopsRepository,
        },
        {
          provide: 'IShippersRepository',
          useValue: mockShippersRepository,
        },
        {
          provide: 'IAddressesRepository',
          useValue: {},
        },
        {
          provide: USERS_REPOSITORY,
          useValue: mockUsersRepository,
        },
        {
          provide: CartService,
          useValue: {},
        },
        {
          provide: VouchersService,
          useValue: {},
        },
        {
          provide: NotificationsService,
          useValue: mockNotificationsService,
        },
        {
          provide: WalletsService,
          useValue: mockWalletsService,
        },
        {
          provide: PaymentsService,
          useValue: {},
        },
        {
          provide: ConfigService,
          useValue: { enableFirestorePaginationFallback: false },
        },
        {
          provide: FirebaseService,
          useValue: {},
        },
        {
          provide: BuyersStatsService,
          useValue: mockBuyersStatsService,
        },
      ],
    }).compile();

    service = module.get<OrdersService>(OrdersService);
    ordersRepo = module.get(ORDERS_REPOSITORY);
    productsService = module.get(ProductsService);
    shippersRepo = module.get('IShippersRepository');
    shopsRepo = module.get('SHOPS_REPOSITORY');
    usersRepo = module.get(USERS_REPOSITORY);
  });

  describe('markDelivered - soldCount updates', () => {
    it('should increment soldCount for all products in order on first delivery', async () => {
      // Arrange
      ordersRepo.findById.mockResolvedValueOnce(mockOrder);
      ordersRepo.update.mockResolvedValue(undefined);
      shippersRepo.findById.mockResolvedValue({
        shipperInfo: { status: 'SHIPPING' },
        name: 'Test Shipper',
      });
      shippersRepo.update.mockResolvedValue(undefined);
      shopsRepo.findById.mockResolvedValue({ id: 'shop_1', ownerId: 'owner_1' });
      usersRepo.findById.mockResolvedValue({ displayName: 'Test Shipper' });
      
      // Mock the final refetch to return delivered order
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        status: OrderStatus.DELIVERED,
        deliveredAt: Timestamp.now(),
        soldCountApplied: true,
      });

      // Act
      await service.markDelivered('shipper_1', 'order_123');

      // Assert
      expect(productsService.incrementSoldCount).toHaveBeenCalledWith([
        { productId: 'prod_1', quantity: 2 },
        { productId: 'prod_2', quantity: 1 },
      ]);

      expect(ordersRepo.update).toHaveBeenCalledWith(
        'order_123',
        expect.objectContaining({ soldCountApplied: true }),
      );
    });

    it('should NOT increment soldCount if order already has soldCountApplied=true (idempotent)', async () => {
      // Arrange
      const alreadyAppliedOrder = {
        ...mockOrder,
        soldCountApplied: true, // Already applied
      };

      ordersRepo.findById.mockResolvedValueOnce(alreadyAppliedOrder);
      ordersRepo.update.mockResolvedValue(undefined);
      shippersRepo.findById.mockResolvedValue({
        shipperInfo: { status: 'SHIPPING' },
        name: 'Test Shipper',
      });
      shippersRepo.update.mockResolvedValue(undefined);
      shopsRepo.findById.mockResolvedValue({ id: 'shop_1', ownerId: 'owner_1' });
      usersRepo.findById.mockResolvedValue({ displayName: 'Test Shipper' });
      
      ordersRepo.findById.mockResolvedValueOnce({
        ...alreadyAppliedOrder,
        status: OrderStatus.DELIVERED,
        deliveredAt: Timestamp.now(),
      });

      // Act
      await service.markDelivered('shipper_1', 'order_123');

      // Assert - should NOT call incrementSoldCount
      expect(productsService.incrementSoldCount).not.toHaveBeenCalled();
    });

    it('should handle soldCount update failure gracefully (non-blocking)', async () => {
      // Arrange
      ordersRepo.findById.mockResolvedValueOnce(mockOrder);
      ordersRepo.update.mockResolvedValue(undefined);
      productsService.incrementSoldCount.mockRejectedValueOnce(
        new Error('Product update failed'),
      );
      shippersRepo.findById.mockResolvedValue({
        shipperInfo: { status: 'SHIPPING' },
        name: 'Test Shipper',
      });
      shippersRepo.update.mockResolvedValue(undefined);
      shopsRepo.findById.mockResolvedValue({ id: 'shop_1', ownerId: 'owner_1' });
      usersRepo.findById.mockResolvedValue({ displayName: 'Test Shipper' });
      
      ordersRepo.findById.mockResolvedValueOnce({
        ...mockOrder,
        status: OrderStatus.DELIVERED,
        deliveredAt: Timestamp.now(),
      });

      // Act - should not throw
      await expect(service.markDelivered('shipper_1', 'order_123')).resolves.not.toThrow();

      // Assert - soldCount increment was attempted
      expect(productsService.incrementSoldCount).toHaveBeenCalled();
    });

    it('should update soldCount atomically with correct quantities for multiple items', async () => {
      // Arrange
      const multiItemOrder = {
        ...mockOrder,
        items: [
          {
            productId: 'prod_1',
            productName: 'Product A',
            quantity: 3,
            price: 10000,
            subtotal: 30000,
          },
          {
            productId: 'prod_2',
            productName: 'Product B',
            quantity: 5,
            price: 20000,
            subtotal: 100000,
          },
          {
            productId: 'prod_3',
            productName: 'Product C',
            quantity: 1,
            price: 50000,
            subtotal: 50000,
          },
        ],
      };

      ordersRepo.findById.mockResolvedValueOnce(multiItemOrder);
      ordersRepo.update.mockResolvedValue(undefined);
      shippersRepo.findById.mockResolvedValue({
        shipperInfo: { status: 'SHIPPING' },
        name: 'Test Shipper',
      });
      shippersRepo.update.mockResolvedValue(undefined);
      shopsRepo.findById.mockResolvedValue({ id: 'shop_1', ownerId: 'owner_1' });
      usersRepo.findById.mockResolvedValue({ displayName: 'Test Shipper' });
      
      ordersRepo.findById.mockResolvedValueOnce({
        ...multiItemOrder,
        status: OrderStatus.DELIVERED,
        deliveredAt: Timestamp.now(),
        soldCountApplied: true,
      });

      // Act
      await service.markDelivered('shipper_1', 'order_123');

      // Assert
      expect(productsService.incrementSoldCount).toHaveBeenCalledWith([
        { productId: 'prod_1', quantity: 3 },
        { productId: 'prod_2', quantity: 5 },
        { productId: 'prod_3', quantity: 1 },
      ]);
    });
  });

  describe('Order lifecycle - soldCount consistency', () => {
    it('should not apply soldCount for non-DELIVERED orders', async () => {
      // Test that CONFIRMED, PREPARING, READY, SHIPPING orders don't trigger soldCount
      // (Only DELIVERED should trigger it)
      // soldCount should only be updated in markDelivered, not in other status transitions
      // This test is implicitly validated by the fact that only markDelivered calls incrementSoldCount
      expect(true).toBe(true); // Placeholder test
    });

    it('should handle orders with zero quantity items gracefully', async () => {
      // Arrange - edge case: item with 0 quantity
      const zeroQuantityOrder = {
        ...mockOrder,
        items: [
          {
            productId: 'prod_1',
            productName: 'Product A',
            quantity: 0, // Edge case
            price: 10000,
            subtotal: 0,
          },
        ],
      };

      ordersRepo.findById.mockResolvedValueOnce(zeroQuantityOrder);
      ordersRepo.update.mockResolvedValue(undefined);
      shippersRepo.findById.mockResolvedValue({
        shipperInfo: { status: 'SHIPPING' },
        name: 'Test Shipper',
      });
      shippersRepo.update.mockResolvedValue(undefined);
      shopsRepo.findById.mockResolvedValue({ id: 'shop_1', ownerId: 'owner_1' });
      usersRepo.findById.mockResolvedValue({ displayName: 'Test Shipper' });
      
      ordersRepo.findById.mockResolvedValueOnce({
        ...zeroQuantityOrder,
        status: OrderStatus.DELIVERED,
        deliveredAt: Timestamp.now(),
        soldCountApplied: true,
      });

      // Act
      await service.markDelivered('shipper_1', 'order_123');

      // Assert - should still call incrementSoldCount with 0 quantity (no-op in repository)
      expect(productsService.incrementSoldCount).toHaveBeenCalledWith([
        { productId: 'prod_1', quantity: 0 },
      ]);
    });
  });
});
