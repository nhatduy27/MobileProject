import { Injectable, Inject, NotFoundException, ConflictException } from '@nestjs/common';
import { Timestamp } from 'firebase-admin/firestore';
import { ICartRepository, CART_REPOSITORY } from '../interfaces';
import { IProductsRepository } from '../../products/interfaces';
import { IShopsRepository } from '../../shops/interfaces';
import {
  AddToCartDto,
  UpdateCartItemDto,
  CartGroupDto,
  CartItemDto,
  CartGroupsQueryDto,
} from '../dto';
import { CartItem } from '../entities';

export interface CartGroupsOptions extends CartGroupsQueryDto {
  // No additional flags needed - defaults are handled in service
}

export interface CartGroupsResult {
  groups: CartGroupDto[];
  page: number;
  limit: number;
  totalGroups: number;
  totalPages: number;
}

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
    options?: CartGroupsOptions,
  ): Promise<CartGroupsResult> {
    const includeAll = options?.includeAll ?? false;
    const page = options?.page ?? 1;
    const limit = options?.limit ?? 10;

    // 1. Get cart
    const cart = await this.cartRepo.findByCustomerId(customerId);
    if (!cart || cart.items.length === 0) {
      // Always return metadata for consistency
      return {
        groups: [],
        page,
        limit,
        totalGroups: 0,
        totalPages: 0,
      };
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

    // 5. Build groups with timestamps for ordering
    const groupsWithMeta: Array<{ group: CartGroupDto; lastUpdated: number }> = [];

    const toMillis = (value: any): number => {
      if (!value) return 0;
      if (value instanceof Timestamp) return value.toMillis();
      if (typeof value === 'object' && typeof value.toDate === 'function') {
        return value.toDate().getTime();
      }
      if (value instanceof Date) return value.getTime();
      const parsed = new Date(value).getTime();
      return Number.isNaN(parsed) ? 0 : parsed;
    };

    for (const [shopId, items] of itemsByShop.entries()) {
      const shop = shopMap.get(shopId);
      if (!shop) continue;

      const cartItems: CartItemDto[] = items.map((item) => ({
        productId: item.productId,
        shopId: item.shopId,
        productName: item.productName,
        productImage: item.productImage,
        quantity: item.quantity,
        price: item.priceAtAdd,
        subtotal: item.priceAtAdd * item.quantity,
        addedAt: new Date(toMillis(item.addedAt)).toISOString(),
        updatedAt: new Date(toMillis(item.updatedAt)).toISOString(),
      }));

      const subtotal = cartItems.reduce((sum, item) => sum + item.subtotal, 0);
      const lastUpdated = Math.max(
        ...items.map((item) =>
          Math.max(toMillis(item.updatedAt), toMillis(item.addedAt)),
        ),
      );

      groupsWithMeta.push({
        group: {
          shopId,
          shopName: shop.name,
          isOpen: shop.isOpen && shop.status === 'OPEN',
          shipFee: 0,
          items: cartItems,
          subtotal,
          lastActivityAt: new Date(lastUpdated).toISOString(),
        },
        lastUpdated,
      });
    }

    // 6. Stable ordering: lastUpdated desc, fallback shopName asc
    groupsWithMeta.sort((a, b) => {
      if (a.lastUpdated !== b.lastUpdated) {
        return b.lastUpdated - a.lastUpdated;
      }
      return a.group.shopName.localeCompare(b.group.shopName);
    });

    const orderedGroups = groupsWithMeta.map((entry) => entry.group);
    const totalGroups = orderedGroups.length;

    // 7. Calculate pagination metadata
    const offset = (page - 1) * limit;
    const totalPages = totalGroups === 0 ? 0 : Math.ceil(totalGroups / limit);

    // If includeAll=true, return all groups but still include metadata
    if (includeAll) {
      return {
        groups: orderedGroups,
        page: 1,
        limit: totalGroups || 10,
        totalGroups,
        totalPages: totalGroups > 0 ? 1 : 0,
      };
    }

    // Normal pagination: slice groups
    const pagedGroups = page > totalPages && totalPages > 0 ? [] : orderedGroups.slice(offset, offset + limit);

    return {
      groups: pagedGroups,
      page,
      limit,
      totalGroups,
      totalPages,
    };
  }

  async getCartGroupByShop(
    customerId: string,
    shopId: string,
  ): Promise<{ group: CartGroupDto | null }> {
    const grouped = await this.getCartGrouped(customerId, { includeAll: true });
    const group = grouped.groups.find((g) => g.shopId === shopId) || null;
    return { group };
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
