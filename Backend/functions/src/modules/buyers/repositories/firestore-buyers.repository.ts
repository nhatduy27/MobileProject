import { Injectable, Inject } from '@nestjs/common';
import { Firestore, Timestamp } from '@google-cloud/firestore';
import { IBuyersRepository, BuyerListFilters } from './buyers-repository.interface';
import { ShopBuyerEntity, BuyerTier, calculateBuyerTier } from '../entities/shop-buyer.entity';
import { BuyerSortBy } from '../dto';

@Injectable()
export class FirestoreBuyersRepository implements IBuyersRepository {
  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  /**
   * List buyers for a shop with filters and pagination
   */
  async listByShop(
    shopId: string,
    filters: BuyerListFilters,
  ): Promise<{ buyers: ShopBuyerEntity[]; total: number }> {
    let query = this.firestore
      .collection('shops')
      .doc(shopId)
      .collection('shopBuyers') as FirebaseFirestore.Query;

    // Filter by tier (if not ALL)
    if (filters.tier && filters.tier !== 'ALL') {
      query = query.where('tier', '==', filters.tier);
    }

    // Search: Prefix match on displayName or phone
    if (filters.search) {
      // Firestore limitation: Can't OR conditions, so we fetch all and filter in-memory
      // For production with 10k+ buyers, consider Algolia/Elasticsearch
      // MVP: Fetch all matching tier, filter client-side
    }

    // Sort
    if (filters.sort === BuyerSortBy.TOTAL_SPENT) {
      query = query.orderBy('totalSpent', 'desc');
    } else {
      // Default: createdAt DESC (newest first)
      query = query.orderBy('createdAt', 'desc');
    }

    // Count total (before pagination)
    const countSnapshot = await query.count().get();
    let total = countSnapshot.data().count;

    // Apply search filter in-memory if needed
    let allBuyers: ShopBuyerEntity[] = [];
    if (filters.search) {
      const allSnapshot = await query.get();
      const searchLower = filters.search.toLowerCase();
      
      allBuyers = allSnapshot.docs
        .map((doc) => this.mapToEntity(doc.id, doc.data()))
        .filter((buyer) => {
          const nameMatch = buyer.displayName?.toLowerCase().startsWith(searchLower);
          const phoneMatch = buyer.phone?.startsWith(filters.search || '');
          return nameMatch || phoneMatch;
        });

      total = allBuyers.length;

      // Manual pagination after filter
      const startIndex = (filters.page - 1) * filters.limit;
      const endIndex = startIndex + filters.limit;
      allBuyers = allBuyers.slice(startIndex, endIndex);

      return { buyers: allBuyers, total };
    }

    // No search: normal pagination
    const offset = (filters.page - 1) * filters.limit;
    query = query.offset(offset).limit(filters.limit);

    const snapshot = await query.get();
    const buyers = snapshot.docs.map((doc) => this.mapToEntity(doc.id, doc.data()));

    return { buyers, total };
  }

  /**
   * Find a buyer by customerId in shop
   */
  async findById(shopId: string, customerId: string): Promise<ShopBuyerEntity | null> {
    const doc = await this.firestore
      .collection('shops')
      .doc(shopId)
      .collection('shopBuyers')
      .doc(customerId)
      .get();

    if (!doc.exists) {
      return null;
    }

    return this.mapToEntity(doc.id, doc.data()!);
  }

  /**
   * Create or update buyer stats (for Cloud Function)
   */
  async createOrUpdate(shopId: string, data: Partial<ShopBuyerEntity>): Promise<ShopBuyerEntity> {
    const buyerRef = this.firestore
      .collection('shops')
      .doc(shopId)
      .collection('shopBuyers')
      .doc(data.customerId!);

    const now = Timestamp.now();
    const existingDoc = await buyerRef.get();

    // Calculate tier based on totalSpent
    const tier =
      data.totalSpent !== undefined ? calculateBuyerTier(data.totalSpent) : BuyerTier.NEW;

    if (existingDoc.exists) {
      // Update existing
      const updateData = {
        ...data,
        tier,
        tierLastUpdated: now,
        updatedAt: now,
      };

      await buyerRef.update(updateData);

      const updated = await buyerRef.get();
      return this.mapToEntity(updated.id, updated.data()!);
    } else {
      // Create new
      const newData = {
        customerId: data.customerId!,
        shopId,
        displayName: data.displayName || '',
        phone: data.phone || null,
        avatar: data.avatar || null,
        email: data.email || null,
        tier,
        tierLastUpdated: now,
        totalOrders: data.totalOrders || 0,
        totalSpent: data.totalSpent || 0,
        avgOrderValue: data.avgOrderValue || 0,
        firstOrderDate: data.firstOrderDate || null,
        lastOrderDate: data.lastOrderDate || null,
        joinedDate: data.joinedDate || now,
        createdAt: now,
        updatedAt: now,
      };

      await buyerRef.set(newData);
      return this.mapToEntity(buyerRef.id, newData);
    }
  }

  /**
   * Map Firestore doc to Entity
   */
  private mapToEntity(id: string, data: FirebaseFirestore.DocumentData): ShopBuyerEntity {
    return new ShopBuyerEntity({
      customerId: id,
      shopId: data.shopId,
      displayName: data.displayName,
      phone: data.phone || undefined,
      avatar: data.avatar || undefined,
      email: data.email || undefined,
      tier: data.tier as BuyerTier,
      tierLastUpdated: data.tierLastUpdated?.toDate(),
      totalOrders: data.totalOrders || 0,
      totalSpent: data.totalSpent || 0,
      avgOrderValue: data.avgOrderValue || 0,
      firstOrderDate: data.firstOrderDate?.toDate(),
      lastOrderDate: data.lastOrderDate?.toDate(),
      joinedDate: data.joinedDate?.toDate(),
      createdAt: data.createdAt?.toDate(),
      updatedAt: data.updatedAt?.toDate(),
    });
  }
}
