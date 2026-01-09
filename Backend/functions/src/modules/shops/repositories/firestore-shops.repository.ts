import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue, Timestamp } from 'firebase-admin/firestore';
import { IShopsRepository } from '../interfaces';
import { ShopEntity, ShopStatus, SubscriptionStatus } from '../entities/shop.entity';
import { CreateShopDto, UpdateShopDto } from '../dto';

@Injectable()
export class FirestoreShopsRepository implements IShopsRepository {
  private readonly collection = 'shops';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async create(
    ownerId: string,
    ownerName: string,
    data: CreateShopDto,
  ): Promise<ShopEntity> {
    const shopRef = this.firestore.collection(this.collection).doc();
    const now = Timestamp.now();
    const trialEndDate = new Date();
    trialEndDate.setDate(trialEndDate.getDate() + 7); // 7 days trial

    const shopData = {
      id: shopRef.id,
      ownerId,
      ownerName,
      ...data,
      isOpen: false,
      status: ShopStatus.OPEN,
      rating: 0,
      totalRatings: 0,
      totalOrders: 0,
      totalRevenue: 0,
      subscription: {
        status: SubscriptionStatus.TRIAL,
        startDate: now,
        trialEndDate: Timestamp.fromDate(trialEndDate),
        currentPeriodEnd: Timestamp.fromDate(trialEndDate),
        nextBillingDate: null,
        autoRenew: true,
      },
      createdAt: now,
      updatedAt: now,
    };

    await shopRef.set(shopData);
    return this.mapToEntity(shopData);
  }

  async findById(shopId: string): Promise<ShopEntity | null> {
    const doc = await this.firestore.collection(this.collection).doc(shopId).get();
    if (!doc.exists) return null;
    return this.mapToEntity(doc.data());
  }

  async findByOwnerId(ownerId: string): Promise<ShopEntity | null> {
    const snapshot = await this.firestore
      .collection(this.collection)
      .where('ownerId', '==', ownerId)
      .limit(1)
      .get();

    if (snapshot.empty) return null;
    return this.mapToEntity(snapshot.docs[0].data());
  }

  async update(shopId: string, data: Partial<UpdateShopDto>): Promise<ShopEntity> {
    const updateData: Record<string, any> = {
      ...data,
      updatedAt: FieldValue.serverTimestamp(),
    };

    // Remove undefined values
    Object.keys(updateData).forEach((key) => {
      if (updateData[key] === undefined) {
        delete updateData[key];
      }
    });

    await this.firestore.collection(this.collection).doc(shopId).update(updateData);

    const updated = await this.findById(shopId);
    if (!updated) throw new Error('Shop not found after update');
    return updated;
  }

  async toggleStatus(shopId: string, isOpen: boolean): Promise<void> {
    await this.firestore.collection(this.collection).doc(shopId).update({
      isOpen,
      updatedAt: FieldValue.serverTimestamp(),
    });
  }

  async findAll(params: {
    page: number;
    limit: number;
    status?: string;
    search?: string;
  }): Promise<{ shops: ShopEntity[]; total: number }> {
    let query: any = this.firestore.collection(this.collection);

    // Filter by status
    if (params.status) {
      query = query.where('status', '==', params.status);
    }

    // Get total count
    const countSnapshot = await query.get();
    const total = countSnapshot.size;

    // Apply pagination
    const offset = (params.page - 1) * params.limit;
    query = query.orderBy('createdAt', 'desc').offset(offset).limit(params.limit);

    const snapshot = await query.get();
    const shops = snapshot.docs.map((doc: any) => this.mapToEntity(doc.data()));

    // Client-side search if needed (Firestore doesn't support full-text search)
    let filteredShops = shops;
    if (params.search) {
      const searchLower = params.search.toLowerCase();
      filteredShops = shops.filter(
        (shop: ShopEntity) =>
          shop.name.toLowerCase().includes(searchLower) ||
          shop.description.toLowerCase().includes(searchLower),
      );
    }

    return { shops: filteredShops, total };
  }

  async updateStats(
    shopId: string,
    stats: {
      totalOrders?: number;
      totalRevenue?: number;
      rating?: number;
      totalRatings?: number;
    },
  ): Promise<void> {
    const updateData: Record<string, any> = {
      updatedAt: FieldValue.serverTimestamp(),
    };

    if (stats.totalOrders !== undefined) updateData.totalOrders = stats.totalOrders;
    if (stats.totalRevenue !== undefined) updateData.totalRevenue = stats.totalRevenue;
    if (stats.rating !== undefined) updateData.rating = stats.rating;
    if (stats.totalRatings !== undefined) updateData.totalRatings = stats.totalRatings;

    await this.firestore.collection(this.collection).doc(shopId).update(updateData);
  }

  private mapToEntity(data: any): ShopEntity {
    return {
      id: data.id,
      ownerId: data.ownerId,
      ownerName: data.ownerName,
      name: data.name,
      description: data.description,
      address: data.address,
      phone: data.phone,
      coverImageUrl: data.coverImageUrl,
      logoUrl: data.logoUrl,
      openTime: data.openTime,
      closeTime: data.closeTime,
      shipFeePerOrder: data.shipFeePerOrder,
      minOrderAmount: data.minOrderAmount,
      isOpen: data.isOpen,
      status: data.status,
      rating: data.rating,
      totalRatings: data.totalRatings,
      totalOrders: data.totalOrders,
      totalRevenue: data.totalRevenue,
      subscription: data.subscription,
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
    };
  }
}
