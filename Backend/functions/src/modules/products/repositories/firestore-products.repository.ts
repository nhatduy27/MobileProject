import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue, Timestamp } from 'firebase-admin/firestore';
import { IProductsRepository } from '../interfaces';
import { ProductEntity } from '../entities';
import { ProductFilterDto, ProductSortOption } from '../dto';
import { ShopStatus } from '../../shops/entities/shop.entity';
import { globalCache, CACHE_TTL } from '../../../shared/utils';

@Injectable()
export class FirestoreProductsRepository implements IProductsRepository {
  private readonly collection = 'products';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async create(
    shopId: string,
    shopName: string,
    categoryName: string,
    data: {
      name: string;
      description: string;
      price: number;
      categoryId: string;
      imageUrl: string;
      preparationTime: number;
    },
  ): Promise<ProductEntity> {
    const productRef = this.firestore.collection(this.collection).doc();
    const now = Timestamp.now();

    const productData = {
      shopId,
      shopName, // Denormalized
      categoryName, // Denormalized
      name: data.name,
      description: data.description,
      price: data.price,
      categoryId: data.categoryId,
      imageUrl: data.imageUrl || null,
      isAvailable: true,
      preparationTime: data.preparationTime,
      rating: 0,
      totalRatings: 0,
      soldCount: 0,
      sortOrder: 0,
      isDeleted: false,
      createdAt: now,
      updatedAt: now,
    };

    await productRef.set(productData);

    // Invalidate cache for this shop
    this.invalidateShopCache(shopId);

    return this.mapToEntity({ id: productRef.id, ...productData });
  }

  async findById(id: string): Promise<ProductEntity | null> {
    const doc = await this.firestore.collection(this.collection).doc(id).get();

    if (!doc.exists) {
      return null;
    }

    return this.mapToEntity({ id: doc.id, ...doc.data() });
  }

  async findByShopId(
    shopId: string,
    filters: ProductFilterDto,
  ): Promise<{ products: ProductEntity[]; total: number }> {
    // Build cache key based on shopId and filters
    const cacheKey = this.buildMenuCacheKey(shopId, filters);

    // Check cache first
    const cached = globalCache.get<{ products: ProductEntity[]; total: number }>(cacheKey);
    if (cached) {
      return cached;
    }

    // Cache miss - fetch from Firestore
    console.log(`📦 Cache MISS: ${cacheKey}`);

    let query: FirebaseFirestore.Query = this.firestore
      .collection(this.collection)
      .where('shopId', '==', shopId)
      .where('isDeleted', '==', false);

    // Filter by category
    if (filters.categoryId) {
      query = query.where('categoryId', '==', filters.categoryId);
    }

    // Filter by availability (owner only)
    let isAvailableBoolean: boolean | undefined = undefined;
    if (typeof filters.isAvailable === 'string') {
      isAvailableBoolean =
        filters.isAvailable === 'true' ? true : filters.isAvailable === 'false' ? false : undefined;
    } else if (typeof filters.isAvailable === 'boolean') {
      isAvailableBoolean = filters.isAvailable;
    }

    if (isAvailableBoolean !== undefined && isAvailableBoolean !== null) {
      query = query.where('isAvailable', '==', isAvailableBoolean);
    }

    const snapshot = await query.get();
    let products = snapshot.docs.map((doc) => this.mapToEntity({ id: doc.id, ...doc.data() }));

    // Client-side search
    if (filters.q) {
      const searchLower = filters.q.toLowerCase();
      products = products.filter(
        (p) =>
          p.name.toLowerCase().includes(searchLower) ||
          p.description.toLowerCase().includes(searchLower),
      );
    }

    // Total after filters, before pagination
    const total = products.length;

    this.sortProducts(products, filters.sort || ProductSortOption.NEWEST);

    // Pagination
    const page = filters.page || 1;
    const limit = filters.limit || 20;
    const offset = (page - 1) * limit;
    const paginatedProducts = products.slice(offset, offset + limit);

    const result = { products: paginatedProducts, total };

    // Cache the result (only for basic queries without pagination to maximize cache hits)
    if (!filters.q && page === 1 && limit >= 20) {
      globalCache.set(cacheKey, result, CACHE_TTL.MENU);
    }

    return result;
  }

  async searchGlobal(
    filters: ProductFilterDto,
  ): Promise<{ products: ProductEntity[]; total: number }> {
    let query: FirebaseFirestore.Query = this.firestore
      .collection(this.collection)
      .where('isDeleted', '==', false)
      .where('isAvailable', '==', true);

    // Filter by category
    if (filters.categoryId) {
      query = query.where('categoryId', '==', filters.categoryId);
    }

    // Filter by shop
    if (filters.shopId) {
      query = query.where('shopId', '==', filters.shopId);
    }

    const snapshot = await query.get();
    let products = snapshot.docs.map((doc) => this.mapToEntity({ id: doc.id, ...doc.data() }));

    // Client-side search
    if (filters.q) {
      const searchLower = filters.q.toLowerCase();
      products = products.filter(
        (p) =>
          p.name.toLowerCase().includes(searchLower) ||
          p.description.toLowerCase().includes(searchLower),
      );
    }

    // Client-side price filter
    if (filters.minPrice !== undefined) {
      products = products.filter((p) => p.price >= filters.minPrice!);
    }
    if (filters.maxPrice !== undefined) {
      products = products.filter((p) => p.price <= filters.maxPrice!);
    }

    // Filter out products from closed shops
    const shopIds = Array.from(new Set(products.map((p) => p.shopId)));
    const shopDocs = await Promise.all(
      shopIds.map((id) => this.firestore.collection('shops').doc(id).get()),
    );

    const shopStatusMap = new Map<string, { isOpen: boolean; status?: string }>();
    shopDocs.forEach((doc) => {
      if (!doc.exists) return;
      const data = doc.data();
      if (!data) return;
      shopStatusMap.set(doc.id, { isOpen: !!data.isOpen, status: data.status });
    });

    products = products.filter((p) => {
      const shop = shopStatusMap.get(p.shopId);
      return shop?.isOpen && shop.status === ShopStatus.OPEN;
    });

    const total = products.length;

    // Sort
    this.sortProducts(products, filters.sort || ProductSortOption.NEWEST);

    // Pagination
    const page = filters.page || 1;
    const limit = filters.limit || 20;
    const offset = (page - 1) * limit;
    const paginatedProducts = products.slice(offset, offset + limit);

    return { products: paginatedProducts, total };
  }

  async update(id: string, data: Partial<ProductEntity>): Promise<void> {
    // Fetch product to get shopId for cache invalidation
    const productDoc = await this.firestore.collection(this.collection).doc(id).get();
    const shopId = productDoc.data()?.shopId;

    const updateData: Record<string, unknown> = {
      ...data,
      updatedAt: FieldValue.serverTimestamp(),
    };

    await this.firestore.collection(this.collection).doc(id).update(updateData);

    // Invalidate cache for this shop
    if (shopId) {
      this.invalidateShopCache(shopId);
    }
  }

  async toggleAvailability(id: string, isAvailable: boolean): Promise<void> {
    // Fetch product to get shopId for cache invalidation
    const productDoc = await this.firestore.collection(this.collection).doc(id).get();
    const shopId = productDoc.data()?.shopId;

    await this.firestore.collection(this.collection).doc(id).update({
      isAvailable,
      updatedAt: FieldValue.serverTimestamp(),
    });

    // Invalidate cache for this shop
    if (shopId) {
      this.invalidateShopCache(shopId);
    }
  }

  async softDelete(id: string): Promise<void> {
    // Fetch product to get shopId for cache invalidation
    const productDoc = await this.firestore.collection(this.collection).doc(id).get();
    const shopId = productDoc.data()?.shopId;

    await this.firestore.collection(this.collection).doc(id).update({
      isDeleted: true,
      updatedAt: FieldValue.serverTimestamp(),
    });

    // Invalidate cache for this shop
    if (shopId) {
      this.invalidateShopCache(shopId);
    }
  }

  async updateStats(
    id: string,
    stats: {
      rating?: number;
      totalRatings?: number;
      soldCount?: number;
    },
  ): Promise<void> {
    const updateData: Record<string, unknown> = {
      updatedAt: FieldValue.serverTimestamp(),
    };

    if (stats.rating !== undefined) updateData.rating = stats.rating;
    if (stats.totalRatings !== undefined) updateData.totalRatings = stats.totalRatings;
    if (stats.soldCount !== undefined) updateData.soldCount = stats.soldCount;

    await this.firestore.collection(this.collection).doc(id).update(updateData);
  }

  /**
   * Increment soldCount atomically for multiple products
   * Uses FieldValue.increment for atomic operation
   */
  async incrementSoldCount(items: Array<{ productId: string; quantity: number }>): Promise<void> {
    const batch = this.firestore.batch();

    for (const item of items) {
      const productRef = this.firestore.collection(this.collection).doc(item.productId);
      batch.update(productRef, {
        soldCount: FieldValue.increment(item.quantity),
        updatedAt: FieldValue.serverTimestamp(),
      });
    }

    await batch.commit();

    // Invalidate cache for affected shops
    const productIds = items.map((i) => i.productId);
    const products = await Promise.all(productIds.map((id) => this.findById(id)));
    const shopIds = new Set(products.filter((p) => p).map((p) => p!.shopId));
    shopIds.forEach((shopId) => this.invalidateShopCache(shopId));
  }

  /**
   * Decrement soldCount atomically for multiple products
   * Uses FieldValue.increment with negative values for atomic operation
   */
  async decrementSoldCount(items: Array<{ productId: string; quantity: number }>): Promise<void> {
    const batch = this.firestore.batch();

    for (const item of items) {
      const productRef = this.firestore.collection(this.collection).doc(item.productId);
      batch.update(productRef, {
        soldCount: FieldValue.increment(-item.quantity),
        updatedAt: FieldValue.serverTimestamp(),
      });
    }

    await batch.commit();

    // Invalidate cache for affected shops
    const productIds = items.map((i) => i.productId);
    const products = await Promise.all(productIds.map((id) => this.findById(id)));
    const shopIds = new Set(products.filter((p) => p).map((p) => p!.shopId));
    shopIds.forEach((shopId) => this.invalidateShopCache(shopId));
  }

  /**
   * Map Firestore document to ProductEntity
   */
  private mapToEntity(data: FirebaseFirestore.DocumentData): ProductEntity {
    return {
      id: data.id,
      shopId: data.shopId,
      shopName: data.shopName,
      name: data.name,
      description: data.description,
      price: data.price,
      categoryId: data.categoryId,
      categoryName: data.categoryName,
      imageUrl: data.imageUrl,
      isAvailable: data.isAvailable,
      preparationTime: data.preparationTime,
      rating: data.rating,
      totalRatings: data.totalRatings,
      soldCount: data.soldCount,
      sortOrder: data.sortOrder,
      isDeleted: data.isDeleted,
      createdAt: data.createdAt?.toDate?.().toISOString?.() || data.createdAt,
      updatedAt: data.updatedAt?.toDate?.().toISOString?.() || data.updatedAt,
    };
  }

  /**
   * Sort products array by option
   */
  private sortProducts(products: ProductEntity[], sort: ProductSortOption): void {
    switch (sort) {
      case ProductSortOption.POPULAR:
        products.sort((a, b) => b.soldCount - a.soldCount);
        break;
      case ProductSortOption.RATING:
        products.sort((a, b) => b.rating - a.rating);
        break;
      case ProductSortOption.PRICE:
        products.sort((a, b) => a.price - b.price);
        break;
      case ProductSortOption.NEWEST:
      default:
        products.sort((a, b) => {
          const dateA = new Date(a.createdAt).getTime();
          const dateB = new Date(b.createdAt).getTime();
          return dateB - dateA;
        });
    }
  }

  // ==================== Cache Helper Methods ====================

  /**
   * Build a cache key for shop menu queries
   */
  private buildMenuCacheKey(shopId: string, filters: ProductFilterDto): string {
    const parts = [`shop:${shopId}:products`];

    if (filters.categoryId) {
      parts.push(`cat:${filters.categoryId}`);
    }

    if (filters.isAvailable !== undefined) {
      parts.push(`avail:${filters.isAvailable}`);
    }

    return parts.join(':');
  }

  /**
   * Invalidate all cache entries for a specific shop
   */
  invalidateShopCache(shopId: string): void {
    globalCache.invalidateByPrefix(`shop:${shopId}:`);
    // Also invalidate global search index
    globalCache.invalidate('products:search:index');
  }
}
