import { Test, TestingModule } from '@nestjs/testing';
import { ConflictException, NotFoundException } from '@nestjs/common';
import { CartService } from './cart.service';
import { ICartRepository, CART_REPOSITORY } from '../interfaces';
import { IProductsRepository } from '../../products/interfaces';
import { IShopsRepository } from '../../shops/interfaces';
import { AddToCartDto } from '../dto';
import { CartEntity } from '../entities';
import { Timestamp } from 'firebase-admin/firestore';
import { ShopStatus, SubscriptionStatus } from '../../shops/entities/shop.entity';

describe('CartService - Add to Cart Increment Logic', () => {
  let service: CartService;
  let cartRepo: jest.Mocked<ICartRepository>;
  let productsRepo: jest.Mocked<IProductsRepository>;
  let shopsRepo: jest.Mocked<IShopsRepository>;

  const mockProduct = {
    id: 'prod_123',
    shopId: 'shop_456',
    name: 'Test Product',
    imageUrl: 'https://example.com/image.jpg',
    price: 50000,
    isAvailable: true,
    isDeleted: false,
    categoryId: 'cat_1',
    categoryName: 'Test Category',
    description: 'Test product',
    preparationTime: 15,
    rating: 4.5,
    totalRatings: 10,
    soldCount: 100,
    sortOrder: 0,
    shopName: 'Test Shop',
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-01T00:00:00.000Z',
  };

  const mockShop = {
    id: 'shop_456',
    ownerId: 'owner_789',
    ownerName: 'Test Owner',
    name: 'Test Shop',
    description: 'Test shop',
    address: '123 Test St',
    phone: '+84901234567',
    isOpen: true,
    status: ShopStatus.OPEN,
    openTime: '08:00',
    closeTime: '22:00',
    shipFeePerOrder: 15000,
    minOrderAmount: 50000,
    rating: 4.5,
    totalRatings: 50,
    totalOrders: 200,
    totalRevenue: 10000000,
    subscription: {
      status: SubscriptionStatus.ACTIVE,
      startDate: '2026-01-01T00:00:00.000Z',
      trialEndDate: null,
      currentPeriodEnd: '2026-02-01T00:00:00.000Z',
      nextBillingDate: '2026-02-01T00:00:00.000Z',
      autoRenew: true,
    },
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-01T00:00:00.000Z',
  };

  beforeEach(async () => {
    const mockCartRepo = {
      findByCustomerId: jest.fn(),
      create: jest.fn(),
      update: jest.fn(),
      delete: jest.fn(),
    };

    const mockProductsRepo = {
      findById: jest.fn(),
      create: jest.fn(),
      update: jest.fn(),
      findByShopId: jest.fn(),
      searchGlobal: jest.fn(),
      toggleAvailability: jest.fn(),
      softDelete: jest.fn(),
      updateStats: jest.fn(),
    };

    const mockShopsRepo = {
      findById: jest.fn(),
      findByOwnerId: jest.fn(),
      create: jest.fn(),
      update: jest.fn(),
      toggleStatus: jest.fn(),
      findAll: jest.fn(),
      updateStats: jest.fn(),
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        CartService,
        { provide: CART_REPOSITORY, useValue: mockCartRepo },
        { provide: 'PRODUCTS_REPOSITORY', useValue: mockProductsRepo },
        { provide: 'SHOPS_REPOSITORY', useValue: mockShopsRepo },
      ],
    }).compile();

    service = module.get<CartService>(CartService);
    cartRepo = module.get(CART_REPOSITORY);
    productsRepo = module.get('PRODUCTS_REPOSITORY');
    shopsRepo = module.get('SHOPS_REPOSITORY');

    // Default mocks
    productsRepo.findById.mockResolvedValue(mockProduct);
    shopsRepo.findById.mockResolvedValue(mockShop);
  });

  describe('addToCart - Increment Behavior', () => {
    it('should create new cart item with specified quantity when cart is empty', async () => {
      // Arrange
      const customerId = 'customer_123';
      const dto: AddToCartDto = { productId: 'prod_123', quantity: 3 };
      
      cartRepo.findByCustomerId.mockResolvedValue(null);
      cartRepo.create.mockResolvedValue({
        customerId,
        items: [],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      });

      // Act
      await service.addToCart(customerId, dto);

      // Assert
      expect(cartRepo.create).toHaveBeenCalledTimes(1);
      const createdCart = cartRepo.create.mock.calls[0][0];
      expect(createdCart.items).toHaveLength(1);
      expect(createdCart.items[0].productId).toBe('prod_123');
      expect(createdCart.items[0].quantity).toBe(3);
    });

    it('should add new item to existing cart when product not already in cart', async () => {
      // Arrange
      const customerId = 'customer_123';
      const dto: AddToCartDto = { productId: 'prod_123', quantity: 2 };
      
      const existingCart: CartEntity = {
        customerId,
        items: [
          {
            productId: 'prod_999',
            shopId: 'shop_456',
            productName: 'Other Product',
            productImage: '',
            quantity: 5,
            priceAtAdd: 30000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
        ],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(existingCart);

      // Act
      await service.addToCart(customerId, dto);

      // Assert
      expect(cartRepo.update).toHaveBeenCalledTimes(1);
      const updatedCart = cartRepo.update.mock.calls[0][0];
      expect(updatedCart.items).toHaveLength(2);
      expect(updatedCart.items[0].productId).toBe('prod_999');
      expect(updatedCart.items[0].quantity).toBe(5); // Unchanged
      expect(updatedCart.items[1].productId).toBe('prod_123');
      expect(updatedCart.items[1].quantity).toBe(2); // New item
    });

    it('should INCREMENT quantity when adding same product again (2 + 3 = 5)', async () => {
      // Arrange
      const customerId = 'customer_123';
      const dto: AddToCartDto = { productId: 'prod_123', quantity: 3 };
      
      const existingCart: CartEntity = {
        customerId,
        items: [
          {
            productId: 'prod_123',
            shopId: 'shop_456',
            productName: 'Test Product',
            productImage: 'https://example.com/image.jpg',
            quantity: 2, // Existing quantity
            priceAtAdd: 50000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
        ],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(existingCart);

      // Act
      await service.addToCart(customerId, dto);

      // Assert
      expect(cartRepo.update).toHaveBeenCalledTimes(1);
      const updatedCart = cartRepo.update.mock.calls[0][0];
      expect(updatedCart.items).toHaveLength(1); // Still one item
      expect(updatedCart.items[0].productId).toBe('prod_123');
      expect(updatedCart.items[0].quantity).toBe(5); // 2 + 3 = 5 ✅ INCREMENTED
    });

    it('should INCREMENT correctly with multiple adds (1 + 2 + 3 = 6)', async () => {
      // Arrange
      const customerId = 'customer_123';
      
      let currentCart: CartEntity = {
        customerId,
        items: [],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockImplementation(() => Promise.resolve(currentCart));
      cartRepo.create.mockImplementation((cart) => {
        currentCart = cart;
        return Promise.resolve(cart);
      });
      cartRepo.update.mockImplementation((cart) => {
        currentCart = cart;
        return Promise.resolve();
      });

      // Act - Add 1
      await service.addToCart(customerId, { productId: 'prod_123', quantity: 1 });
      expect(currentCart.items[0].quantity).toBe(1);

      // Act - Add 2 more
      await service.addToCart(customerId, { productId: 'prod_123', quantity: 2 });
      expect(currentCart.items[0].quantity).toBe(3); // 1 + 2

      // Act - Add 3 more
      await service.addToCart(customerId, { productId: 'prod_123', quantity: 3 });
      expect(currentCart.items[0].quantity).toBe(6); // 1 + 2 + 3 ✅
    });

    it('should throw ConflictException when total quantity exceeds 999', async () => {
      // Arrange
      const customerId = 'customer_123';
      const dto: AddToCartDto = { productId: 'prod_123', quantity: 100 };
      
      const existingCart: CartEntity = {
        customerId,
        items: [
          {
            productId: 'prod_123',
            shopId: 'shop_456',
            productName: 'Test Product',
            productImage: '',
            quantity: 950, // Existing: 950, adding 100 => 1050 > 999
            priceAtAdd: 50000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
        ],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(existingCart);

      // Act & Assert
      await expect(service.addToCart(customerId, dto)).rejects.toThrow(ConflictException);
      await expect(service.addToCart(customerId, dto)).rejects.toThrow(
        /Tổng số lượng không được vượt quá 999/
      );

      // Should not update cart
      expect(cartRepo.update).not.toHaveBeenCalled();
    });

    it('should allow adding up to exactly 999 quantity', async () => {
      // Arrange
      const customerId = 'customer_123';
      const dto: AddToCartDto = { productId: 'prod_123', quantity: 99 };
      
      const existingCart: CartEntity = {
        customerId,
        items: [
          {
            productId: 'prod_123',
            shopId: 'shop_456',
            productName: 'Test Product',
            productImage: '',
            quantity: 900, // 900 + 99 = 999 ✅
            priceAtAdd: 50000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
        ],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(existingCart);

      // Act
      await service.addToCart(customerId, dto);

      // Assert
      expect(cartRepo.update).toHaveBeenCalledTimes(1);
      const updatedCart = cartRepo.update.mock.calls[0][0];
      expect(updatedCart.items[0].quantity).toBe(999); // Max allowed
    });

    it('should preserve addedAt timestamp when incrementing existing item', async () => {
      // Arrange
      const customerId = 'customer_123';
      const dto: AddToCartDto = { productId: 'prod_123', quantity: 1 };
      
      const originalAddedAt = Timestamp.fromDate(new Date('2026-01-01T00:00:00.000Z'));
      
      const existingCart: CartEntity = {
        customerId,
        items: [
          {
            productId: 'prod_123',
            shopId: 'shop_456',
            productName: 'Test Product',
            productImage: '',
            quantity: 5,
            priceAtAdd: 50000,
            addedAt: originalAddedAt, // Original timestamp
            updatedAt: Timestamp.now(),
          },
        ],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(existingCart);

      // Act
      await service.addToCart(customerId, dto);

      // Assert
      const updatedCart = cartRepo.update.mock.calls[0][0];
      expect(updatedCart.items[0].addedAt).toEqual(originalAddedAt); // Preserved ✅
      expect(updatedCart.items[0].quantity).toBe(6); // Incremented
    });

    it('should update product name and price snapshot when incrementing', async () => {
      // Arrange
      const customerId = 'customer_123';
      const dto: AddToCartDto = { productId: 'prod_123', quantity: 1 };
      
      const existingCart: CartEntity = {
        customerId,
        items: [
          {
            productId: 'prod_123',
            shopId: 'shop_456',
            productName: 'Old Product Name',
            productImage: 'old-image.jpg',
            quantity: 2,
            priceAtAdd: 40000, // Old price
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
        ],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(existingCart);

      // Act
      await service.addToCart(customerId, dto);

      // Assert
      const updatedCart = cartRepo.update.mock.calls[0][0];
      expect(updatedCart.items[0].productName).toBe('Test Product'); // Updated snapshot
      expect(updatedCart.items[0].priceAtAdd).toBe(50000); // Updated price snapshot
      expect(updatedCart.items[0].quantity).toBe(3); // Incremented
    });

    it('should throw NotFoundException when product does not exist', async () => {
      // Arrange
      const customerId = 'customer_123';
      const dto: AddToCartDto = { productId: 'nonexistent_prod', quantity: 1 };
      
      productsRepo.findById.mockResolvedValue(null);

      // Act & Assert
      await expect(service.addToCart(customerId, dto)).rejects.toThrow(NotFoundException);
      await expect(service.addToCart(customerId, dto)).rejects.toThrow(
        /Product not found or unavailable/
      );
    });

    it('should throw NotFoundException when product is unavailable', async () => {
      // Arrange
      const customerId = 'customer_123';
      const dto: AddToCartDto = { productId: 'prod_123', quantity: 1 };
      
      productsRepo.findById.mockResolvedValue({
        ...mockProduct,
        isAvailable: false, // Unavailable
      });

      // Act & Assert
      await expect(service.addToCart(customerId, dto)).rejects.toThrow(NotFoundException);
    });

    it('should throw ConflictException when shop is closed', async () => {
      // Arrange
      const customerId = 'customer_123';
      const dto: AddToCartDto = { productId: 'prod_123', quantity: 1 };
      
      shopsRepo.findById.mockResolvedValue({
        ...mockShop,
        isOpen: false, // Shop closed
      });

      // Act & Assert
      await expect(service.addToCart(customerId, dto)).rejects.toThrow(ConflictException);
      await expect(service.addToCart(customerId, dto)).rejects.toThrow(
        /Shop is currently closed/
      );
    });

    it('should handle items from different shops correctly', async () => {
      // Arrange
      const customerId = 'customer_123';
      const dto1: AddToCartDto = { productId: 'prod_123', quantity: 2 };
      const dto2: AddToCartDto = { productId: 'prod_456', quantity: 3 };
      
      const product2 = {
        ...mockProduct,
        id: 'prod_456',
        shopId: 'shop_789',
        name: 'Product from Different Shop',
      };

      const shop2 = {
        ...mockShop,
        id: 'shop_789',
        name: 'Different Shop',
      };

      let currentCart: CartEntity = {
        customerId,
        items: [],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockImplementation(() => Promise.resolve(currentCart));
      cartRepo.create.mockImplementation((cart) => {
        currentCart = cart;
        return Promise.resolve(cart);
      });
      cartRepo.update.mockImplementation((cart) => {
        currentCart = cart;
        return Promise.resolve();
      });

      productsRepo.findById.mockImplementation((id) => {
        if (id === 'prod_123') return Promise.resolve(mockProduct);
        if (id === 'prod_456') return Promise.resolve(product2);
        return Promise.resolve(null);
      });

      shopsRepo.findById.mockImplementation((id) => {
        if (id === 'shop_456') return Promise.resolve(mockShop);
        if (id === 'shop_789') return Promise.resolve(shop2);
        return Promise.resolve(null);
      });

      // Act
      await service.addToCart(customerId, dto1);
      await service.addToCart(customerId, dto2);

      // Assert
      expect(currentCart.items).toHaveLength(2);
      expect(currentCart.items[0].productId).toBe('prod_123');
      expect(currentCart.items[0].quantity).toBe(2);
      expect(currentCart.items[1].productId).toBe('prod_456');
      expect(currentCart.items[1].quantity).toBe(3);
    });
  });

  describe('clearCartByShop - Clear Shop Group', () => {
    it('should remove only items from specified shop, keep other shops unchanged', async () => {
      // Arrange
      const customerId = 'customer_123';
      const shopIdToRemove = 'shop_456';
      
      const existingCart: CartEntity = {
        customerId,
        items: [
          {
            productId: 'prod_123',
            shopId: 'shop_456', // Will be removed
            productName: 'Product A',
            productImage: '',
            quantity: 2,
            priceAtAdd: 50000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
          {
            productId: 'prod_456',
            shopId: 'shop_456', // Will be removed
            productName: 'Product B',
            productImage: '',
            quantity: 1,
            priceAtAdd: 30000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
          {
            productId: 'prod_789',
            shopId: 'shop_999', // Will be kept
            productName: 'Product C',
            productImage: '',
            quantity: 3,
            priceAtAdd: 40000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
        ],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(existingCart);

      // Act
      const result = await service.clearCartByShop(customerId, shopIdToRemove);

      // Assert
      expect(result.removedCount).toBe(2); // 2 items from shop_456
      expect(cartRepo.update).toHaveBeenCalledTimes(1);
      
      const updatedCart = cartRepo.update.mock.calls[0][0];
      expect(updatedCart.items).toHaveLength(1); // Only 1 item left
      expect(updatedCart.items[0].shopId).toBe('shop_999'); // From other shop
      expect(updatedCart.items[0].productId).toBe('prod_789');
      
      expect(result.groups).toHaveLength(1); // Only one shop group left
    });

    it('should return removedCount 0 and unchanged cart when shop not in cart', async () => {
      // Arrange
      const customerId = 'customer_123';
      const shopIdNotInCart = 'shop_nonexistent';
      
      const existingCart: CartEntity = {
        customerId,
        items: [
          {
            productId: 'prod_123',
            shopId: 'shop_456',
            productName: 'Product A',
            productImage: '',
            quantity: 2,
            priceAtAdd: 50000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
        ],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(existingCart);

      // Act
      const result = await service.clearCartByShop(customerId, shopIdNotInCart);

      // Assert
      expect(result.removedCount).toBe(0); // No items removed
      expect(cartRepo.update).toHaveBeenCalledTimes(1);
      
      const updatedCart = cartRepo.update.mock.calls[0][0];
      expect(updatedCart.items).toHaveLength(1); // Cart unchanged
      expect(updatedCart.items[0].shopId).toBe('shop_456');
    });

    it('should delete cart and return empty groups when cart becomes empty', async () => {
      // Arrange
      const customerId = 'customer_123';
      const shopId = 'shop_456';
      
      const existingCart: CartEntity = {
        customerId,
        items: [
          {
            productId: 'prod_123',
            shopId: 'shop_456', // Only shop in cart
            productName: 'Product A',
            productImage: '',
            quantity: 2,
            priceAtAdd: 50000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
          {
            productId: 'prod_456',
            shopId: 'shop_456', // Only shop in cart
            productName: 'Product B',
            productImage: '',
            quantity: 1,
            priceAtAdd: 30000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
        ],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(existingCart);

      // Act
      const result = await service.clearCartByShop(customerId, shopId);

      // Assert
      expect(result.removedCount).toBe(2); // All items removed
      expect(result.groups).toEqual([]); // Empty cart
      expect(cartRepo.delete).toHaveBeenCalledWith(customerId); // Cart deleted
      expect(cartRepo.update).not.toHaveBeenCalled(); // No update, deleted instead
    });

    it('should return empty result when no cart exists', async () => {
      // Arrange
      const customerId = 'customer_123';
      const shopId = 'shop_456';
      
      cartRepo.findByCustomerId.mockResolvedValue(null); // No cart

      // Act
      const result = await service.clearCartByShop(customerId, shopId);

      // Assert
      expect(result.removedCount).toBe(0);
      expect(result.groups).toEqual([]);
      expect(cartRepo.delete).not.toHaveBeenCalled();
      expect(cartRepo.update).not.toHaveBeenCalled();
    });

    it('should return empty result when cart exists but is empty', async () => {
      // Arrange
      const customerId = 'customer_123';
      const shopId = 'shop_456';
      
      const emptyCart: CartEntity = {
        customerId,
        items: [], // Empty cart
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(emptyCart);

      // Act
      const result = await service.clearCartByShop(customerId, shopId);

      // Assert
      expect(result.removedCount).toBe(0);
      expect(result.groups).toEqual([]);
      expect(cartRepo.delete).not.toHaveBeenCalled();
      expect(cartRepo.update).not.toHaveBeenCalled();
    });

    it('should handle removing items from one of multiple shops', async () => {
      // Arrange
      const customerId = 'customer_123';
      const shopIdToRemove = 'shop_456';
      
      const existingCart: CartEntity = {
        customerId,
        items: [
          {
            productId: 'prod_1',
            shopId: 'shop_456', // Remove
            productName: 'Product 1',
            productImage: '',
            quantity: 1,
            priceAtAdd: 10000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
          {
            productId: 'prod_2',
            shopId: 'shop_789', // Keep
            productName: 'Product 2',
            productImage: '',
            quantity: 2,
            priceAtAdd: 20000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
          {
            productId: 'prod_3',
            shopId: 'shop_999', // Keep
            productName: 'Product 3',
            productImage: '',
            quantity: 3,
            priceAtAdd: 30000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
        ],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(existingCart);

      // Act
      const result = await service.clearCartByShop(customerId, shopIdToRemove);

      // Assert
      expect(result.removedCount).toBe(1); // 1 item from shop_456
      expect(cartRepo.update).toHaveBeenCalledTimes(1);
      
      const updatedCart = cartRepo.update.mock.calls[0][0];
      expect(updatedCart.items).toHaveLength(2); // 2 items remaining
      expect(updatedCart.items.map(i => i.shopId)).toEqual(['shop_789', 'shop_999']);
      expect(updatedCart.items.map(i => i.productId)).toEqual(['prod_2', 'prod_3']);
    });
  });

  describe('getCartGroupByShop - Group Detail', () => {
    it('should return group for shop with items', async () => {
      const customerId = 'customer_123';

      const cart: CartEntity = {
        customerId,
        items: [
          {
            productId: 'prod_1',
            shopId: 'shop_456',
            productName: 'Product A',
            productImage: '',
            quantity: 2,
            priceAtAdd: 50000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
          {
            productId: 'prod_2',
            shopId: 'shop_789',
            productName: 'Product B',
            productImage: '',
            quantity: 1,
            priceAtAdd: 30000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
        ],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(cart);
      shopsRepo.findById.mockImplementation((id) => {
        if (id === 'shop_456') return Promise.resolve(mockShop);
        if (id === 'shop_789') return Promise.resolve({ ...mockShop, id: 'shop_789', name: 'Different Shop' });
        return Promise.resolve(null);
      });

      const result = await service.getCartGroupByShop(customerId, 'shop_456');
      expect(result.group).not.toBeNull();
      expect(result.group!.shopId).toBe('shop_456');
      expect(result.group!.items).toHaveLength(1);
      expect(result.group!.items[0].productId).toBe('prod_1');
      expect(result.group!.subtotal).toBe(100000); // 2 * 50000
    });

    it('should return group null when no items for that shop', async () => {
      const customerId = 'customer_123';

      const cart: CartEntity = {
        customerId,
        items: [
          {
            productId: 'prod_2',
            shopId: 'shop_789',
            productName: 'Product B',
            productImage: '',
            quantity: 1,
            priceAtAdd: 30000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
        ],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(cart);
      shopsRepo.findById.mockResolvedValue({ ...mockShop, id: 'shop_789', name: 'Different Shop' });

      const result = await service.getCartGroupByShop(customerId, 'shop_456');
      expect(result.group).toBeNull();
    });

    it('should not include items from other shops', async () => {
      const customerId = 'customer_123';

      const cart: CartEntity = {
        customerId,
        items: [
          {
            productId: 'prod_A',
            shopId: 'shop_X',
            productName: 'Product X1',
            productImage: '',
            quantity: 2,
            priceAtAdd: 10000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
          {
            productId: 'prod_B',
            shopId: 'shop_Y',
            productName: 'Product Y1',
            productImage: '',
            quantity: 3,
            priceAtAdd: 20000,
            addedAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          },
        ],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };

      cartRepo.findByCustomerId.mockResolvedValue(cart);
      shopsRepo.findById.mockImplementation((id) => {
        if (id === 'shop_X') return Promise.resolve({ ...mockShop, id: 'shop_X', name: 'Shop X' });
        if (id === 'shop_Y') return Promise.resolve({ ...mockShop, id: 'shop_Y', name: 'Shop Y' });
        return Promise.resolve(null);
      });

      const result = await service.getCartGroupByShop(customerId, 'shop_X');
      expect(result.group).not.toBeNull();
      expect(result.group!.items.map((i) => i.productId)).toEqual(['prod_A']);
    });
  });
});
