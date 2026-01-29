import { Test, TestingModule } from '@nestjs/testing';
import { NotFoundException, ForbiddenException, ConflictException } from '@nestjs/common';
import { Timestamp } from 'firebase-admin/firestore';
import { OrdersService } from './orders.service';
import { OrderStateMachineService } from './order-state-machine.service';
import { ProductsService } from '../../products/services';
import { CartService } from '../../cart/services';
import { VouchersService } from '../../vouchers/vouchers.service';
import { WalletsService } from '../../wallets/wallets.service';
import { PaymentsService } from '../../payments/payments.service';
import { ConfigService } from '../../../core/config/config.service';
import { FirebaseService } from '../../../core/firebase/firebase.service';
import { NotificationsService } from '../../notifications/services/notifications.service';
import { USERS_REPOSITORY } from '../../users/interfaces';
import { BuyersStatsService } from '../../buyers/services/buyers-stats.service';
import { ShopsService } from '../../shops/services/shops.service';
import { OrderEntity, OrderStatus, PaymentStatus } from '../entities';

describe('OrdersService - Owner Flow', () => {
  let service: OrdersService;
  let ordersRepo: any;
  let shopsRepo: any;
  let cartService: any;
  let productsRepo: any;

  const mockOrder: OrderEntity = {
    id: 'order_123',
    orderNumber: 'ORD-001',
    customerId: 'customer_1',
    shopId: 'shop_1',
    shopName: 'Test Shop',
    shipperId: null,
    items: [
      {
        productId: 'prod_1',
        productName: 'Test Product',
        quantity: 1,
        price: 10000,
        subtotal: 10000,
      },
    ],
    subtotal: 10000,
    shipFee: 5000,
    discount: 0,
    total: 15000,
    status: OrderStatus.PENDING,
    paymentStatus: PaymentStatus.UNPAID,
    paymentMethod: 'COD',
    deliveryAddress: {
      street: '123 Test St',
      ward: 'Test Ward',
      district: 'Test District',
      city: 'Test City',
    },
  };

  const mockShop = {
    id: 'shop_1',
    name: 'Test Shop',
    ownerId: 'owner_1',
    isOpen: true,
    status: 'OPEN',
    shipFeePerOrder: 5000,
  };

  beforeEach(async () => {
    ordersRepo = {
      findById: jest.fn(),
      update: jest.fn(),
      createOrderAndClearCartGroup: jest.fn(),
      query: jest.fn(),
      findMany: jest.fn(),
    };

    shopsRepo = {
      findById: jest.fn(),
      findByOwnerId: jest.fn(),
    };

    cartService = {
      getCartGrouped: jest.fn(),
      clearCartGroup: jest.fn(),
    };

    productsRepo = {
      findById: jest.fn(),
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
          provide: 'ORDERS_REPOSITORY',
          useValue: ordersRepo,
        },
        {
          provide: 'SHOPS_REPOSITORY',
          useValue: shopsRepo,
        },
        {
          provide: CartService,
          useValue: cartService,
        },
        {
          provide: 'PRODUCTS_REPOSITORY',
          useValue: productsRepo,
        },
        {
          provide: ProductsService,
          useValue: {
            incrementSoldCount: jest.fn().mockResolvedValue(undefined),
            decrementSoldCount: jest.fn().mockResolvedValue(undefined),
          },
        },
        {
          provide: 'IShippersRepository',
          useValue: {},
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
          provide: BuyersStatsService,
          useValue: {
            incrementOrderCount: jest.fn().mockResolvedValue(undefined),
            updateTotalSpent: jest.fn().mockResolvedValue(undefined),
            updateBuyerStatsOnDelivery: jest.fn().mockResolvedValue(undefined),
          },
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
        {
          provide: NotificationsService,
          useValue: {
            sendOrderNotification: jest.fn(),
            send: jest.fn(),
          },
        },
        {
          provide: PaymentsService,
          useValue: {
            initiateRefund: jest.fn().mockResolvedValue(null),
            createPayment: jest.fn(),
          },
        },
        {
          provide: WalletsService,
          useValue: {
            processOrderPayout: jest.fn().mockResolvedValue(undefined),
            updateBalance: jest.fn().mockResolvedValue(undefined),
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

  describe('ownerCancelOrder (ORDER-009)', () => {
    it('should cancel order when in PENDING status', async () => {
      const ownerId = 'owner_1';
      const orderId = 'order_123';
      const reason = 'Customer requested cancellation';

      const pendingOrder = { ...mockOrder, status: OrderStatus.PENDING };

      ordersRepo.findById.mockResolvedValueOnce(pendingOrder);
      shopsRepo.findById.mockResolvedValueOnce(mockShop);
      ordersRepo.update.mockResolvedValueOnce(undefined);
      ordersRepo.findById.mockResolvedValueOnce({
        ...pendingOrder,
        status: OrderStatus.CANCELLED,
        cancelledAt: Timestamp.now(),
        cancelledBy: 'OWNER',
        cancelReason: reason,
      });

      const result = await service.ownerCancelOrder(ownerId, orderId, reason);

      expect(result).toBeDefined();
      expect(result!.status).toBe(OrderStatus.CANCELLED);
      expect(result!.cancelledBy).toBe('OWNER');
      expect(ordersRepo.update).toHaveBeenCalledWith(orderId, {
        status: OrderStatus.CANCELLED,
        cancelledAt: expect.any(Timestamp),
        cancelReason: reason,
        cancelledBy: 'OWNER',
      });
    });

    it('should cancel order when in CONFIRMED status', async () => {
      const ownerId = 'owner_1';
      const orderId = 'order_123';
      const reason = 'Out of ingredients';

      const confirmedOrder = { ...mockOrder, status: OrderStatus.CONFIRMED };

      ordersRepo.findById.mockResolvedValueOnce(confirmedOrder);
      shopsRepo.findById.mockResolvedValueOnce(mockShop);
      ordersRepo.update.mockResolvedValueOnce(undefined);
      ordersRepo.findById.mockResolvedValueOnce({
        ...confirmedOrder,
        status: OrderStatus.CANCELLED,
        cancelledAt: Timestamp.now(),
        cancelledBy: 'OWNER',
        cancelReason: reason,
      });

      const result = await service.ownerCancelOrder(ownerId, orderId, reason);

      expect(result).toBeDefined();
      expect(result!.status).toBe(OrderStatus.CANCELLED);
      expect(result!.cancelledBy).toBe('OWNER');
      expect(ordersRepo.update).toHaveBeenCalledWith(orderId, {
        status: OrderStatus.CANCELLED,
        cancelledAt: expect.any(Timestamp),
        cancelReason: reason,
        cancelledBy: 'OWNER',
      });
    });

    it('should cancel order when in PREPARING status', async () => {
      const ownerId = 'owner_1';
      const orderId = 'order_123';
      const reason = 'Customer requested';

      const preparingOrder = { ...mockOrder, status: OrderStatus.PREPARING };

      ordersRepo.findById.mockResolvedValueOnce(preparingOrder);
      shopsRepo.findById.mockResolvedValueOnce(mockShop);
      ordersRepo.update.mockResolvedValueOnce(undefined);
      ordersRepo.findById.mockResolvedValueOnce({
        ...preparingOrder,
        status: OrderStatus.CANCELLED,
        cancelledAt: Timestamp.now(),
        cancelledBy: 'OWNER',
        cancelReason: reason,
      });

      const result = await service.ownerCancelOrder(ownerId, orderId, reason);

      expect(result).toBeDefined();
      expect(result!.status).toBe(OrderStatus.CANCELLED);
      expect(ordersRepo.update).toHaveBeenCalled();
    });

    it('should throw NotFoundException when order does not exist', async () => {
      const ownerId = 'owner_1';
      const orderId = 'nonexistent_order';

      ordersRepo.findById.mockResolvedValueOnce(null);

      await expect(service.ownerCancelOrder(ownerId, orderId)).rejects.toThrow(NotFoundException);
    });

    it('should throw ForbiddenException when owner does not own the shop', async () => {
      const ownerId = 'different_owner';
      const orderId = 'order_123';

      const confirmedOrder = { ...mockOrder, status: OrderStatus.CONFIRMED };

      ordersRepo.findById.mockResolvedValueOnce(confirmedOrder);
      shopsRepo.findById.mockResolvedValueOnce(mockShop);

      await expect(service.ownerCancelOrder(ownerId, orderId)).rejects.toThrow(ForbiddenException);
    });

    it('should throw ConflictException when order is in READY status', async () => {
      const ownerId = 'owner_1';
      const orderId = 'order_123';

      const readyOrder = { ...mockOrder, status: OrderStatus.READY };

      ordersRepo.findById.mockResolvedValueOnce(readyOrder);
      shopsRepo.findById.mockResolvedValueOnce(mockShop);

      await expect(service.ownerCancelOrder(ownerId, orderId)).rejects.toThrow(ConflictException);
    });

    it('should throw ConflictException when order is in SHIPPING status', async () => {
      const ownerId = 'owner_1';
      const orderId = 'order_123';

      const shippingOrder = { ...mockOrder, status: OrderStatus.SHIPPING };

      ordersRepo.findById.mockResolvedValueOnce(shippingOrder);
      shopsRepo.findById.mockResolvedValueOnce(mockShop);

      await expect(service.ownerCancelOrder(ownerId, orderId)).rejects.toThrow(ConflictException);
    });

    it('should throw ConflictException when order is DELIVERED', async () => {
      const ownerId = 'owner_1';
      const orderId = 'order_123';

      const deliveredOrder = { ...mockOrder, status: OrderStatus.DELIVERED };

      ordersRepo.findById.mockResolvedValueOnce(deliveredOrder);
      shopsRepo.findById.mockResolvedValueOnce(mockShop);

      await expect(service.ownerCancelOrder(ownerId, orderId)).rejects.toThrow(ConflictException);
    });

    it('should throw ConflictException when order is already CANCELLED', async () => {
      const ownerId = 'owner_1';
      const orderId = 'order_123';

      const cancelledOrder = { ...mockOrder, status: OrderStatus.CANCELLED };

      ordersRepo.findById.mockResolvedValueOnce(cancelledOrder);
      shopsRepo.findById.mockResolvedValueOnce(mockShop);

      await expect(service.ownerCancelOrder(ownerId, orderId)).rejects.toThrow(ConflictException);
    });

    it('should set default reason when not provided', async () => {
      const ownerId = 'owner_1';
      const orderId = 'order_123';

      const confirmedOrder = { ...mockOrder, status: OrderStatus.CONFIRMED };

      ordersRepo.findById.mockResolvedValueOnce(confirmedOrder);
      shopsRepo.findById.mockResolvedValueOnce(mockShop);
      ordersRepo.update.mockResolvedValueOnce(undefined);
      ordersRepo.findById.mockResolvedValueOnce({
        ...confirmedOrder,
        status: OrderStatus.CANCELLED,
        cancelledAt: Timestamp.now(),
        cancelledBy: 'OWNER',
        cancelReason: 'Cancelled by owner',
      });

      await service.ownerCancelOrder(ownerId, orderId);

      expect(ordersRepo.update).toHaveBeenCalledWith(orderId, {
        status: OrderStatus.CANCELLED,
        cancelledAt: expect.any(Timestamp),
        cancelReason: 'Cancelled by owner',
        cancelledBy: 'OWNER',
      });
    });

    it('should handle second cancel call idempotently (should fail)', async () => {
      const ownerId = 'owner_1';
      const orderId = 'order_123';

      const cancelledOrder = { ...mockOrder, status: OrderStatus.CANCELLED };

      ordersRepo.findById.mockResolvedValueOnce(cancelledOrder);
      shopsRepo.findById.mockResolvedValueOnce(mockShop);

      // Second call should fail because order is already cancelled
      await expect(service.ownerCancelOrder(ownerId, orderId)).rejects.toThrow(ConflictException);
    });

    it('should trigger refund if order is already paid', async () => {
      const ownerId = 'owner_1';
      const orderId = 'order_123';

      const paidOrder = {
        ...mockOrder,
        status: OrderStatus.CONFIRMED,
        paymentStatus: PaymentStatus.PAID,
        paymentMethod: 'ZALOPAY',
      };

      ordersRepo.findById.mockResolvedValueOnce(paidOrder);
      shopsRepo.findById.mockResolvedValueOnce(mockShop);
      ordersRepo.update.mockResolvedValueOnce(undefined);
      ordersRepo.findById.mockResolvedValueOnce({
        ...paidOrder,
        status: OrderStatus.CANCELLED,
        cancelledAt: Timestamp.now(),
        cancelledBy: 'OWNER',
      });

      // TODO: When payment service available, verify refund is called
      await service.ownerCancelOrder(ownerId, orderId);

      expect(ordersRepo.update).toHaveBeenCalledWith(orderId, {
        status: OrderStatus.CANCELLED,
        cancelledAt: expect.any(Timestamp),
        cancelReason: expect.any(String),
        cancelledBy: 'OWNER',
      });
      // Refund triggering is stubbed for MVP, should verify after payment module ready
    });

    it('should throw ForbiddenException when shop not found', async () => {
      const ownerId = 'owner_1';
      const orderId = 'order_123';

      const confirmedOrder = { ...mockOrder, status: OrderStatus.CONFIRMED };

      ordersRepo.findById.mockResolvedValueOnce(confirmedOrder);
      shopsRepo.findById.mockResolvedValueOnce(null);

      await expect(service.ownerCancelOrder(ownerId, orderId)).rejects.toThrow(ForbiddenException);
    });
  });
});
