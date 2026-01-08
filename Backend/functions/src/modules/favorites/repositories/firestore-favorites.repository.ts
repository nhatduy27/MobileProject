import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue } from 'firebase-admin/firestore';
import { IFavoritesRepository, PaginatedResult } from '../interfaces';
import { FavoriteEntity } from '../entities';

@Injectable()
export class FirestoreFavoritesRepository implements IFavoritesRepository {
  private readonly collection = 'userFavorites';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async add(userId: string, favorite: Partial<FavoriteEntity>): Promise<FavoriteEntity> {
    const id = `${userId}_${favorite.productId}`;
    const docRef = this.firestore.collection(this.collection).doc(id);

    const data: Record<string, any> = {
      id,
      userId,
      productId: favorite.productId,
      productName: favorite.productName,
      productPrice: favorite.productPrice,
      productImage: favorite.productImage || null,
      shopId: favorite.shopId,
      shopName: favorite.shopName,
      createdAt: FieldValue.serverTimestamp(),
    };

    await docRef.set(data);

    return new FavoriteEntity({
      id: data.id,
      userId: data.userId,
      productId: data.productId,
      productName: data.productName,
      productPrice: data.productPrice,
      productImage: data.productImage || undefined,
      shopId: data.shopId,
      shopName: data.shopName,
      createdAt: new Date(),
    });
  }

  async remove(userId: string, productId: string): Promise<void> {
    const id = `${userId}_${productId}`;
    await this.firestore.collection(this.collection).doc(id).delete();
  }

  async isFavorited(userId: string, productId: string): Promise<boolean> {
    const id = `${userId}_${productId}`;
    const doc = await this.firestore.collection(this.collection).doc(id).get();
    return doc.exists;
  }

  async findByUserAndProduct(userId: string, productId: string): Promise<FavoriteEntity | null> {
    const id = `${userId}_${productId}`;
    const doc = await this.firestore.collection(this.collection).doc(id).get();
    if (!doc.exists) return null;
    return this.mapToEntity(doc.data());
  }

  async findByUserId(
    userId: string,
    page: number,
    limit: number,
  ): Promise<PaginatedResult<FavoriteEntity>> {
    const offset = (page - 1) * limit;

    // Get total count
    const countSnapshot = await this.firestore
      .collection(this.collection)
      .where('userId', '==', userId)
      .count()
      .get();
    const total = countSnapshot.data().count;

    // Get paginated data
    const snapshot = await this.firestore
      .collection(this.collection)
      .where('userId', '==', userId)
      .orderBy('createdAt', 'desc')
      .offset(offset)
      .limit(limit)
      .get();

    const data = snapshot.docs.map((doc) => this.mapToEntity(doc.data()));

    return {
      data,
      pagination: {
        total,
        page,
        limit,
        hasMore: offset + data.length < total,
      },
    };
  }

  async countByUserId(userId: string): Promise<number> {
    const snapshot = await this.firestore
      .collection(this.collection)
      .where('userId', '==', userId)
      .count()
      .get();
    return snapshot.data().count;
  }

  private mapToEntity(data: any): FavoriteEntity {
    return new FavoriteEntity({
      id: data.id,
      userId: data.userId,
      productId: data.productId,
      productName: data.productName,
      productPrice: data.productPrice,
      productImage: data.productImage,
      shopId: data.shopId,
      shopName: data.shopName,
      createdAt: data.createdAt?.toDate?.() || new Date(),
    });
  }
}
