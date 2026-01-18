import { Injectable, Inject, NotFoundException, ConflictException } from '@nestjs/common';
import { Timestamp } from 'firebase-admin/firestore';
import { ICartRepository, CART_REPOSITORY } from '../interfaces';
import { IProductsRepository } from '../../products/interfaces';
import { IShopsRepository } from '../../shops/interfaces';
import { AddToCartDto, UpdateCartItemDto, CartGroupDto, CartItemDto } from '../dto';
import { CartItem } from '../entities';

@Injectable()
export class CartService {
  constructor(
    @Inject(CART_REPOSITORY)
    private readonly cartRepo: ICartRepository,
    @Inject('PRODUCTS_REPOSITORY')
    private readonly productsRepo: IProductsRepository,
    @Inject('SHOPS_REPOSITORY')
    private readonly shopsRepo: IShopsRepository,
  ) {}

  async addToCart(
    customerId: string,
    dto: AddToCartDto,
  ): Promise<{ id: string; groups: CartGroupDto[] }> {
    // 1. Validate product exists and is active
    const product = await this.productsRepo.findById(dto.productId);
    if (!product || !product.isAvailable || product.isDeleted) {
      throw new NotFoundException({
        code: 'CART_001',
        message: 'Product not found or unavailable',
        statusCode: 404,
      });
    }

    // 2. Validate shop exists
    const shop = await this.shopsRepo.findById(product.shopId);
    if (!shop) {
      throw new NotFoundException({
        code: 'CART_001',
        message: 'Shop not found',
        statusCode: 404,
      });
    }

    // 3. Check if shop is open
    if (!shop.isOpen || shop.status !== 'OPEN') {
      throw new ConflictException({
        code: 'CART_002',
        message: 'Shop is currently closed',
        statusCode: 409,
      });
    }

    // 4. Get or create cart
    let cart = await this.cartRepo.findByCustomerId(customerId);
    const cartExisted = !!cart;

    if (!cart) {
      cart = {
        customerId,
        items: [],
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      };
    }

    // 5. Check if product already in cart
    const existingItemIndex = cart.items.findIndex(
      (item) => item.productId === dto.productId,
    );

    // 5a. Calculate quantity: INCREMENT if exists, otherwise use request quantity
    const existingQuantity = existingItemIndex >= 0 ? cart.items[existingItemIndex].quantity : 0;
    const newQuantity = existingQuantity + dto.quantity;

    // 5b. Validate total quantity doesn't exceed maximum
    if (newQuantity > 999) {
      throw new ConflictException({
        code: 'CART_006',
        message: `Tổng số lượng không được vượt quá 999 (hiện tại: ${existingQuantity}, thêm: ${dto.quantity})`,
        statusCode: 409,
      });
    }

    const newItem: CartItem = {
      productId: dto.productId,
      shopId: product.shopId,
      productName: product.name,
      productImage: product.imageUrl || '',
      quantity: newQuantity, // ✅ INCREMENT: adds to existing quantity if item already in cart
      priceAtAdd: product.price,
      addedAt: existingItemIndex >= 0 ? cart.items[existingItemIndex].addedAt : Timestamp.now(),
      updatedAt: Timestamp.now(),
    };

    // 6. Update or add item
    if (existingItemIndex >= 0) {
      cart.items[existingItemIndex] = newItem;
    } else {
      cart.items.push(newItem);
    }

    cart.updatedAt = Timestamp.now();

    // 7. Save cart
    if (cartExisted) {
      await this.cartRepo.update(cart);
    } else {
      await this.cartRepo.create(cart);
    }

    // 8. Return with grouped response
    const grouped = await this.getCartGrouped(customerId);
    return {
      id: customerId,
      groups: grouped.groups,
    };
  }

  async getCartGrouped(
    customerId: string,
  ): Promise<{ groups: CartGroupDto[] }> {
    // 1. Get cart
    const cart = await this.cartRepo.findByCustomerId(customerId);
    if (!cart || cart.items.length === 0) {
      return { groups: [] };
    }

    // 2. Group items by shopId
    const itemsByShop = new Map<string, CartItem[]>();
    for (const item of cart.items) {
      if (!itemsByShop.has(item.shopId)) {
        itemsByShop.set(item.shopId, []);
      }
      itemsByShop.get(item.shopId)!.push(item);
    }

    // 3. Batch fetch shops (optimization)
    const shopIds = Array.from(itemsByShop.keys());
    const shopPromises = shopIds.map((shopId) =>
      this.shopsRepo.findById(shopId),
    );
    const shops = await Promise.all(shopPromises);

    // 4. Build shop map
    const shopMap = new Map<string, typeof shops[0]>();
    shops.forEach((shop, index) => {
      if (shop) {
        shopMap.set(shopIds[index], shop);
      }
    });

    // 5. Build groups
    const groups: CartGroupDto[] = [];
    for (const [shopId, items] of itemsByShop.entries()) {
      const shop = shopMap.get(shopId);
      if (!shop) continue; // Skip if shop not found

      const cartItems: CartItemDto[] = items.map((item) => ({
        productId: item.productId,
        shopId: item.shopId,
        productName: item.productName,
        productImage: item.productImage,
        quantity: item.quantity,
        price: item.priceAtAdd, // Map priceAtAdd to price
        subtotal: item.priceAtAdd * item.quantity,
      }));

      const subtotal = cartItems.reduce((sum, item) => sum + item.subtotal, 0);

      groups.push({
        shopId,
        shopName: shop.name,
        isOpen: shop.isOpen && shop.status === 'OPEN',
        shipFee: 0, // Cart doesn't calculate shipFee, Order stage does
        items: cartItems,
        subtotal,
      });
    }

    return { groups };
  }

  async updateCartItem(
    customerId: string,
    productId: string,
    dto: UpdateCartItemDto,
  ): Promise<{ id: string; groups: CartGroupDto[] }> {
    // 1. Get cart
    const cart = await this.cartRepo.findByCustomerId(customerId);
    if (!cart) {
      throw new NotFoundException({
        code: 'CART_003',
        message: 'Cart not found',
        statusCode: 404,
      });
    }

    // 2. Find item
    const itemIndex = cart.items.findIndex(
      (item) => item.productId === productId,
    );
    if (itemIndex === -1) {
      throw new NotFoundException({
        code: 'CART_003',
        message: 'Product not found in cart',
        statusCode: 404,
      });
    }

    // 3. Update quantity
    cart.items[itemIndex].quantity = dto.quantity;
    cart.items[itemIndex].updatedAt = Timestamp.now();
    cart.updatedAt = Timestamp.now();

    // 4. Save
    await this.cartRepo.update(cart);

    // 5. Return with grouped response
    const grouped = await this.getCartGrouped(customerId);
    return {
      id: customerId,
      groups: grouped.groups,
    };
  }

  async removeCartItem(customerId: string, productId: string): Promise<void> {
    // 1. Get cart
    const cart = await this.cartRepo.findByCustomerId(customerId);
    if (!cart) {
      throw new NotFoundException({
        code: 'CART_004',
        message: 'Cart not found',
        statusCode: 404,
      });
    }

    // 2. Find item
    const itemIndex = cart.items.findIndex(
      (item) => item.productId === productId,
    );
    if (itemIndex === -1) {
      throw new NotFoundException({
        code: 'CART_004',
        message: 'Product not found in cart',
        statusCode: 404,
      });
    }

    // 3. Remove item
    cart.items.splice(itemIndex, 1);
    cart.updatedAt = Timestamp.now();

    // 4. Save (or delete if empty)
    if (cart.items.length === 0) {
      await this.cartRepo.delete(customerId);
    } else {
      await this.cartRepo.update(cart);
    }
  }

  async clearCart(customerId: string): Promise<void> {
    // Idempotent - no error if cart doesn't exist
    const cart = await this.cartRepo.findByCustomerId(customerId);
    if (cart) {
      await this.cartRepo.delete(customerId);
    }
  }

  async clearCartByShop(
    customerId: string,
    shopId: string,
  ): Promise<{ removedCount: number; groups: CartGroupDto[] }> {
    // 1. Get cart (return empty if no cart exists)
    const cart = await this.cartRepo.findByCustomerId(customerId);
    if (!cart || cart.items.length === 0) {
      return { removedCount: 0, groups: [] };
    }

    // 2. Count items to remove
    const itemsToRemove = cart.items.filter((item) => item.shopId === shopId);
    const removedCount = itemsToRemove.length;

    // 3. Remove items from this shop
    cart.items = cart.items.filter((item) => item.shopId !== shopId);
    cart.updatedAt = Timestamp.now();

    // 4. Save (or delete if empty)
    if (cart.items.length === 0) {
      await this.cartRepo.delete(customerId);
      return { removedCount, groups: [] };
    } else {
      await this.cartRepo.update(cart);
    }

    // 5. Return updated grouped cart
    const grouped = await this.getCartGrouped(customerId);
    return {
      removedCount,
      groups: grouped.groups,
    };
  }
}
