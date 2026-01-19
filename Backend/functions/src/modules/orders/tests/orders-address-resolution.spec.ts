import { Test, TestingModule } from '@nestjs/testing';
import { NotFoundException, ForbiddenException, BadRequestException } from '@nestjs/common';
import { OrdersService } from '../services/orders.service';
import { ORDERS_REPOSITORY } from '../interfaces';
import { CartService } from '../../cart/services';
import { ADDRESSES_REPOSITORY, USERS_REPOSITORY } from '../../users/interfaces';
import { OrderStateMachineService } from '../services/order-state-machine.service';
import { ConfigService } from '../../../core/config/config.service';

describe('OrdersService - Address Resolution', () => {
  let service: OrdersService;
  let addressesRepo: any;
  let cartService: any;
  let ordersRepo: any;
  let productsRepo: any;
  let shopsRepo: any;

  beforeEach(async () => {
    const mockAddressesRepo = {
      findById: jest.fn(),
    };

    const mockCartService = {
      getCartGrouped: jest.fn(),
    };

    const mockOrdersRepo = {
      createOrderAndClearCartGroup: jest.fn(),
    };

    const mockProductsRepo = {
      findById: jest.fn(),
    };

    const mockShopsRepo = {
      findById: jest.fn(),
    };

    const mockShippersRepo = {};
    const mockStateMachine = {};

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
        { provide: 'IShippersRepository', useValue: mockShippersRepo },
        { provide: ADDRESSES_REPOSITORY, useValue: mockAddressesRepo },
        { provide: USERS_REPOSITORY, useValue: { findById: jest.fn() } },
        { provide: OrderStateMachineService, useValue: mockStateMachine },
        { provide: ConfigService, useValue: mockConfigService },
        { provide: FirebaseService, useValue: mockFirebaseService },
      ],
    }).compile();

    service = module.get<OrdersService>(OrdersService);
    addressesRepo = module.get(ADDRESSES_REPOSITORY);
    cartService = module.get(CartService);
    ordersRepo = module.get(ORDERS_REPOSITORY);
    productsRepo = module.get('PRODUCTS_REPOSITORY');
    shopsRepo = module.get('SHOPS_REPOSITORY');
  });

  describe('Create Order with deliveryAddressId', () => {
    it('should resolve address from addressId successfully', async () => {
      const customerId = 'customer_1';
      const addressId = 'addr_abc123';

      const mockAddress = {
        id: addressId,
        userId: customerId,
        label: 'KTX B5',
        fullAddress: 'KTX Khu B - Tòa B5',
        building: 'B5',
        room: '101',
        note: 'Gọi trước khi đến',
        isDefault: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      const mockCart = {
        groups: [
          {
            shopId: 'shop_1',
            items: [
              {
                productId: 'prod_1',
                productName: 'Test Product',
                quantity: 2,
                price: 50000,
                subtotal: 100000,
              },
            ],
            subtotal: 100000,
          },
        ],
      };

      const mockShop = {
        id: 'shop_1',
        name: 'Test Shop',
        isOpen: true,
        status: 'OPEN',
        shipFeePerOrder: 5000,
      };

      const mockProduct = {
        id: 'prod_1',
        isAvailable: true,
        isDeleted: false,
      };

      const mockCreatedOrder = {
        id: 'order_1',
        orderNumber: 'ORD-123',
        customerId,
        deliveryAddress: {
          id: addressId,
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          building: 'B5',
          room: '101',
          note: 'Gọi trước khi đến',
        },
      };

      addressesRepo.findById.mockResolvedValue(mockAddress);
      cartService.getCartGrouped.mockResolvedValue(mockCart);
      shopsRepo.findById.mockResolvedValue(mockShop);
      productsRepo.findById.mockResolvedValue(mockProduct);
      ordersRepo.createOrderAndClearCartGroup.mockResolvedValue(mockCreatedOrder);

      const dto = {
        shopId: 'shop_1',
        deliveryAddressId: addressId,
        paymentMethod: 'COD' as const,
      };

      const result = await service.createOrder(customerId, dto);

      expect(addressesRepo.findById).toHaveBeenCalledWith(addressId);
      expect(result.deliveryAddress.id).toBe(addressId);
      expect(result.deliveryAddress.fullAddress).toBe('KTX Khu B - Tòa B5');
    });

    it('should throw NotFoundException if address not found', async () => {
      const customerId = 'customer_1';
      const addressId = 'addr_notfound';

      addressesRepo.findById.mockResolvedValue(null);

      const mockCart = {
        groups: [
          {
            shopId: 'shop_1',
            items: [{ productId: 'prod_1', quantity: 1, price: 50000, subtotal: 50000 }],
            subtotal: 50000,
          },
        ],
      };

      const mockShop = {
        id: 'shop_1',
        isOpen: true,
        status: 'OPEN',
        shipFeePerOrder: 5000,
      };

      const mockProduct = { id: 'prod_1', isAvailable: true, isDeleted: false };

      cartService.getCartGrouped.mockResolvedValue(mockCart);
      shopsRepo.findById.mockResolvedValue(mockShop);
      productsRepo.findById.mockResolvedValue(mockProduct);

      const dto = {
        shopId: 'shop_1',
        deliveryAddressId: addressId,
        paymentMethod: 'COD' as const,
      };

      await expect(service.createOrder(customerId, dto)).rejects.toThrow(NotFoundException);
      await expect(service.createOrder(customerId, dto)).rejects.toMatchObject({
        response: expect.objectContaining({
          code: 'ADDRESS_NOT_FOUND',
        }),
      });
    });

    it('should throw ForbiddenException if address belongs to another user', async () => {
      const customerId = 'customer_1';
      const addressId = 'addr_other';

      const mockAddress = {
        id: addressId,
        userId: 'customer_2', // Different user
        label: 'KTX',
        fullAddress: 'KTX Khu B',
      };

      addressesRepo.findById.mockResolvedValue(mockAddress);

      const mockCart = {
        groups: [
          {
            shopId: 'shop_1',
            items: [{ productId: 'prod_1', quantity: 1, price: 50000, subtotal: 50000 }],
            subtotal: 50000,
          },
        ],
      };

      const mockShop = {
        id: 'shop_1',
        isOpen: true,
        status: 'OPEN',
        shipFeePerOrder: 5000,
      };

      const mockProduct = { id: 'prod_1', isAvailable: true, isDeleted: false };

      cartService.getCartGrouped.mockResolvedValue(mockCart);
      shopsRepo.findById.mockResolvedValue(mockShop);
      productsRepo.findById.mockResolvedValue(mockProduct);

      const dto = {
        shopId: 'shop_1',
        deliveryAddressId: addressId,
        paymentMethod: 'COD' as const,
      };

      await expect(service.createOrder(customerId, dto)).rejects.toThrow(ForbiddenException);
      await expect(service.createOrder(customerId, dto)).rejects.toMatchObject({
        response: expect.objectContaining({
          code: 'ADDRESS_ACCESS_DENIED',
        }),
      });
    });

    it('should allow deliveryNote to override address note', async () => {
      const customerId = 'customer_1';
      const addressId = 'addr_abc';

      const mockAddress = {
        id: addressId,
        userId: customerId,
        label: 'KTX',
        fullAddress: 'KTX Khu B - Tòa B5',
        note: 'Original note',
      };

      const mockCart = {
        groups: [
          {
            shopId: 'shop_1',
            items: [{ productId: 'prod_1', quantity: 1, price: 50000, subtotal: 50000 }],
            subtotal: 50000,
          },
        ],
      };

      const mockShop = {
        id: 'shop_1',
        name: 'Shop',
        isOpen: true,
        status: 'OPEN',
        shipFeePerOrder: 5000,
      };

      const mockProduct = { id: 'prod_1', isAvailable: true, isDeleted: false };

      const mockCreatedOrder = {
        id: 'order_1',
        deliveryAddress: {
          id: addressId,
          fullAddress: 'KTX Khu B - Tòa B5',
          note: 'Override note',
        },
      };

      addressesRepo.findById.mockResolvedValue(mockAddress);
      cartService.getCartGrouped.mockResolvedValue(mockCart);
      shopsRepo.findById.mockResolvedValue(mockShop);
      productsRepo.findById.mockResolvedValue(mockProduct);
      ordersRepo.createOrderAndClearCartGroup.mockResolvedValue(mockCreatedOrder);

      const dto = {
        shopId: 'shop_1',
        deliveryAddressId: addressId,
        deliveryNote: 'Override note',
        paymentMethod: 'COD' as const,
      };

      const result = await service.createOrder(customerId, dto);

      expect(result.deliveryAddress.note).toBe('Override note');
    });
  });

  describe('Create Order with direct deliveryAddress snapshot', () => {
    it('should accept direct address snapshot', async () => {
      const customerId = 'customer_1';

      const mockCart = {
        groups: [
          {
            shopId: 'shop_1',
            items: [{ productId: 'prod_1', quantity: 1, price: 50000, subtotal: 50000 }],
            subtotal: 50000,
          },
        ],
      };

      const mockShop = {
        id: 'shop_1',
        name: 'Shop',
        isOpen: true,
        status: 'OPEN',
        shipFeePerOrder: 5000,
      };

      const mockProduct = { id: 'prod_1', isAvailable: true, isDeleted: false };

      const mockCreatedOrder = {
        id: 'order_1',
        deliveryAddress: {
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          building: 'B5',
          room: '101',
          note: 'Direct note',
        },
      };

      cartService.getCartGrouped.mockResolvedValue(mockCart);
      shopsRepo.findById.mockResolvedValue(mockShop);
      productsRepo.findById.mockResolvedValue(mockProduct);
      ordersRepo.createOrderAndClearCartGroup.mockResolvedValue(mockCreatedOrder);

      const dto = {
        shopId: 'shop_1',
        deliveryAddress: {
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          building: 'B5',
          room: '101',
          note: 'Direct note',
        },
        paymentMethod: 'COD' as const,
      };

      const result = await service.createOrder(customerId, dto);

      expect(result.deliveryAddress.fullAddress).toBe('KTX Khu B - Tòa B5');
      expect(result.deliveryAddress.building).toBe('B5');
      expect(addressesRepo.findById).not.toHaveBeenCalled();
    });

    it('should throw BadRequestException if both addressId and address are missing', async () => {
      const customerId = 'customer_1';

      const mockCart = {
        groups: [
          {
            shopId: 'shop_1',
            items: [{ productId: 'prod_1', quantity: 1, price: 50000, subtotal: 50000 }],
            subtotal: 50000,
          },
        ],
      };

      const mockShop = {
        id: 'shop_1',
        isOpen: true,
        status: 'OPEN',
        shipFeePerOrder: 5000,
      };

      const mockProduct = { id: 'prod_1', isAvailable: true, isDeleted: false };

      cartService.getCartGrouped.mockResolvedValue(mockCart);
      shopsRepo.findById.mockResolvedValue(mockShop);
      productsRepo.findById.mockResolvedValue(mockProduct);

      const dto = {
        shopId: 'shop_1',
        paymentMethod: 'COD' as const,
        // Missing both deliveryAddressId and deliveryAddress
      };

      await expect(service.createOrder(customerId, dto)).rejects.toThrow(BadRequestException);
      await expect(service.createOrder(customerId, dto)).rejects.toMatchObject({
        response: expect.objectContaining({
          code: 'ORDER_INVALID_ADDRESS',
        }),
      });
    });
  });

  describe('Address Resolution Priority', () => {
    it('should prefer deliveryAddressId when both are provided', async () => {
      const customerId = 'customer_1';
      const addressId = 'addr_saved';

      const mockSavedAddress = {
        id: addressId,
        userId: customerId,
        fullAddress: 'Saved Address from DB',
      };

      const mockCart = {
        groups: [
          {
            shopId: 'shop_1',
            items: [{ productId: 'prod_1', quantity: 1, price: 50000, subtotal: 50000 }],
            subtotal: 50000,
          },
        ],
      };

      const mockShop = {
        id: 'shop_1',
        isOpen: true,
        status: 'OPEN',
        shipFeePerOrder: 5000,
      };

      const mockProduct = { id: 'prod_1', isAvailable: true, isDeleted: false };

      const mockCreatedOrder = {
        id: 'order_1',
        deliveryAddress: { fullAddress: 'Saved Address from DB' },
      };

      addressesRepo.findById.mockResolvedValue(mockSavedAddress);
      cartService.getCartGrouped.mockResolvedValue(mockCart);
      shopsRepo.findById.mockResolvedValue(mockShop);
      productsRepo.findById.mockResolvedValue(mockProduct);
      ordersRepo.createOrderAndClearCartGroup.mockResolvedValue(mockCreatedOrder);

      const dto = {
        shopId: 'shop_1',
        deliveryAddressId: addressId,
        deliveryAddress: {
          fullAddress: 'Direct Address (should be ignored)',
        },
        paymentMethod: 'COD' as const,
      };

      const result = await service.createOrder(customerId, dto);

      expect(addressesRepo.findById).toHaveBeenCalledWith(addressId);
      expect(result.deliveryAddress.fullAddress).toBe('Saved Address from DB');
    });
  });
});
