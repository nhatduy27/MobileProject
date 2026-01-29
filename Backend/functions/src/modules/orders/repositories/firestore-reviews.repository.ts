import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue } from 'firebase-admin/firestore';
import { IReviewsRepository } from '../interfaces/reviews.repository.interface';
import { ReviewEntity } from '../entities/review.entity';

@Injectable()
export class FirestoreReviewsRepository implements IReviewsRepository {
  private readonly collection = 'reviews';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async findById(id: string): Promise<ReviewEntity | null> {
    const doc = await this.firestore.collection(this.collection).doc(id).get();
    if (!doc.exists) {
      return null;
    }
    return this.mapToEntity({ id: doc.id, ...doc.data() });
  }

  async findByOrderId(orderId: string): Promise<ReviewEntity | null> {
    const snapshot = await this.firestore
      .collection(this.collection)
      .where('orderId', '==', orderId)
      .limit(1)
      .get();

    if (snapshot.empty) {
      return null;
    }

    const doc = snapshot.docs[0];
    return this.mapToEntity({ id: doc.id, ...doc.data() });
  }

  async findByShopId(
    shopId: string,
    options: { page?: number; limit?: number } = {},
  ): Promise<{ reviews: ReviewEntity[]; total: number }> {
    const { page = 1, limit = 20 } = options;

    // Get total count
    const countSnapshot = await this.firestore
      .collection(this.collection)
      .where('shopId', '==', shopId)
      .get();
    const total = countSnapshot.size;

    // Get paginated results
    const snapshot = await this.firestore
      .collection(this.collection)
      .where('shopId', '==', shopId)
      .orderBy('createdAt', 'desc')
      .limit(limit)
      .offset((page - 1) * limit)
      .get();

    const reviews = snapshot.docs.map((doc) => this.mapToEntity({ id: doc.id, ...doc.data() }));

    return { reviews, total };
  }

  async findByCustomerId(customerId: string): Promise<ReviewEntity[]> {
    const snapshot = await this.firestore
      .collection(this.collection)
      .where('customerId', '==', customerId)
      .orderBy('createdAt', 'desc')
      .get();

    return snapshot.docs.map((doc) => this.mapToEntity({ id: doc.id, ...doc.data() }));
  }

  async create(review: Omit<ReviewEntity, 'id'>): Promise<ReviewEntity> {
    const docRef = this.firestore.collection(this.collection).doc();
    const now = FieldValue.serverTimestamp();

    const data = {
      ...review,
      createdAt: now,
      updatedAt: now,
    };

    await docRef.set(data);

    return {
      id: docRef.id,
      ...review,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
  }

  async update(id: string, updates: Partial<ReviewEntity>): Promise<void> {
    const updateData = {
      ...updates,
      updatedAt: FieldValue.serverTimestamp(),
    };

    await this.firestore.collection(this.collection).doc(id).update(updateData);
  }

  async delete(id: string): Promise<void> {
    await this.firestore.collection(this.collection).doc(id).delete();
  }

  async getShopStats(shopId: string): Promise<{ avgRating: number; totalReviews: number }> {
    const snapshot = await this.firestore
      .collection(this.collection)
      .where('shopId', '==', shopId)
      .get();

    if (snapshot.empty) {
      return { avgRating: 0, totalReviews: 0 };
    }

    const ratings = snapshot.docs.map((doc) => doc.data().rating || 0);
    const sum = ratings.reduce((a, b) => a + b, 0);
    const avgRating = Math.round((sum / ratings.length) * 10) / 10; // 1 decimal place

    return { avgRating, totalReviews: ratings.length };
  }

  /**
   * Find reviews containing a specific product
   * Since productReviews is an array, we need to filter in-memory
   */
  async findByProductId(
    productId: string,
    options: { page?: number; limit?: number } = {},
  ): Promise<{ reviews: Array<{ review: ReviewEntity; productReview: any }>; total: number }> {
    const { page = 1, limit = 20 } = options;

    // Query all reviews that have productReviews array
    // Note: Firestore doesn't support querying array item properties directly,
    // so we need to filter in-memory
    const snapshot = await this.firestore
      .collection(this.collection)
      .orderBy('createdAt', 'desc')
      .get();

    // Filter reviews containing this productId
    const matchingReviews: Array<{ review: ReviewEntity; productReview: any }> = [];
    
    for (const doc of snapshot.docs) {
      const data = doc.data();
      const productReviews = data.productReviews || [];
      
      const productReview = productReviews.find(
        (pr: any) => pr.productId === productId,
      );
      
      if (productReview) {
        matchingReviews.push({
          review: this.mapToEntity({ id: doc.id, ...data }),
          productReview,
        });
      }
    }

    const total = matchingReviews.length;
    const start = (page - 1) * limit;
    const paginatedReviews = matchingReviews.slice(start, start + limit);

    return { reviews: paginatedReviews, total };
  }

  /**
   * Get product rating stats from all reviews
   */
  async getProductStats(productId: string): Promise<{ avgRating: number; totalReviews: number }> {
    const { reviews } = await this.findByProductId(productId, { limit: 1000 });

    if (reviews.length === 0) {
      return { avgRating: 0, totalReviews: 0 };
    }

    const ratings = reviews.map((r) => r.productReview.rating);
    const sum = ratings.reduce((a, b) => a + b, 0);
    const avgRating = Math.round((sum / ratings.length) * 10) / 10;

    return { avgRating, totalReviews: ratings.length };
  }

  private mapToEntity(data: FirebaseFirestore.DocumentData): ReviewEntity {
    return {
      id: data.id,
      orderId: data.orderId,
      customerId: data.customerId,
      customerName: data.customerName,
      shopId: data.shopId,
      rating: data.rating,
      comment: data.comment,
      productReviews: data.productReviews,
      ownerReply: data.ownerReply,
      ownerRepliedAt: data.ownerRepliedAt?.toDate?.().toISOString?.() || data.ownerRepliedAt,
      createdAt: data.createdAt?.toDate?.().toISOString?.() || data.createdAt,
      updatedAt: data.updatedAt?.toDate?.().toISOString?.() || data.updatedAt,
    };
  }
}
