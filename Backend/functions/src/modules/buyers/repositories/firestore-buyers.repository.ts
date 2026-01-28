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

    // 1. Filter by Tier
    if (filters.tier && filters.tier !== 'ALL') {
      query = query.where('tier', '==', filters.tier);
    }

    // 2. Sort Setup (Luôn cần sort để data trả về có thứ tự nhất quán)
    if (filters.sort === BuyerSortBy.TOTAL_SPENT) {
      query = query.orderBy('totalSpent', 'desc');
    } else {
      query = query.orderBy('createdAt', 'desc');
    }

    // 3. CASE A: SEARCH (In-Memory Filtering)
    if (filters.search) {
      // Fetch all docs matching the Tier (Sorted)
      const allSnapshot = await query.get();
      const searchLower = filters.search.toLowerCase().trim();

      // Filter in memory (Prefix match)
      let allBuyers = allSnapshot.docs
        .map((doc) => this.mapToEntity(doc.id, doc.data()))
        .filter((buyer) => {
          const nameMatch = buyer.displayName?.toLowerCase().startsWith(searchLower);
          const phoneMatch = buyer.phone?.startsWith(searchLower); // searchLower đã trim
          return nameMatch || phoneMatch;
        });

      const total = allBuyers.length;

      // Manual pagination
      const startIndex = (filters.page - 1) * filters.limit;
      const endIndex = startIndex + filters.limit;
      allBuyers = allBuyers.slice(startIndex, endIndex);

      return { buyers: allBuyers, total };
    }

    // 4. CASE B: NO SEARCH (Direct Firestore Pagination)
    const countSnapshot = await query.count().get();
    const total = countSnapshot.data().count;

    const offset = (filters.page - 1) * filters.limit;
    query = query.offset(offset).limit(filters.limit);

    const snapshot = await query.get();
    const buyers = snapshot.docs.map((doc) => this.mapToEntity(doc.id, doc.data()));

    return { buyers, total };
  }

  /**
   * Find a buyer by customerId
   */
  async findById(shopId: string, customerId: string): Promise<ShopBuyerEntity | null> {
    const doc = await this.firestore
      .collection('shops')
      .doc(shopId)
      .collection('shopBuyers')
      .doc(customerId)
      .get();

    if (!doc.exists) return null;
    return this.mapToEntity(doc.id, doc.data()!);
  }

  /**
   * Create or update buyer
   */
  async createOrUpdate(shopId: string, data: Partial<ShopBuyerEntity>): Promise<ShopBuyerEntity> {
    const buyerRef = this.firestore
      .collection('shops')
      .doc(shopId)
      .collection('shopBuyers')
      .doc(data.customerId!);

    const now = Timestamp.now();
    const existingDoc = await buyerRef.get();

    if (existingDoc.exists) {
      // === UPDATE ===
      const currentData = existingDoc.data()!;

      // Chỉ tính lại Tier nếu có totalSpent mới
      let tier = currentData.tier;
      let tierLastUpdated = currentData.tierLastUpdated;

      if (data.totalSpent !== undefined) {
        tier = calculateBuyerTier(data.totalSpent);
        tierLastUpdated = now;
      }

      // Merge data
      const updateData: any = {
        ...data,
        tier,
        tierLastUpdated,
        updatedAt: now,
      };

      // Clean undefined
      Object.keys(updateData).forEach(
        (key) => updateData[key] === undefined && delete updateData[key],
      );

      await buyerRef.update(updateData);

      const updated = await buyerRef.get();
      return this.mapToEntity(updated.id, updated.data()!);
    } else {
      // === CREATE NEW ===
      const totalSpent = data.totalSpent || 0;
      const tier = calculateBuyerTier(totalSpent);

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
        totalSpent: totalSpent,
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

  private mapToEntity(id: string, data: FirebaseFirestore.DocumentData): ShopBuyerEntity {
    // Dùng Constructor của Class Entity
    return new ShopBuyerEntity({
      customerId: id,
      shopId: data.shopId,
      displayName: data.displayName,
      phone: data.phone,
      avatar: data.avatar,
      email: data.email,
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
