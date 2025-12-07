/**
 * Promotion Repository
 *
 * Data access layer cho Promotions collection
 */

import {getFirestore} from "firebase-admin/firestore";
import {Promotion} from "../models/promotion.model";

/**
 * Firestore data access for Promotions collection.
 */
export class PromotionRepository {
  private db = getFirestore();
  private collection = "promotions";

  /**
   * Lấy promotion theo code
   * @param {string} code Promotion code
   */
  async getPromotionByCode(code: string): Promise<Promotion | null> {
    // TODO: Add caching for frequently used promotion codes

    const snapshot = await this.db
      .collection(this.collection)
      .where("code", "==", code.toUpperCase())
      .limit(1)
      .get();

    if (snapshot.empty) {
      return null;
    }

    const doc = snapshot.docs[0];
    return {
      id: doc.id,
      ...doc.data(),
    } as Promotion;
  }

  /**
   * Lấy promotion theo ID
   * @param {string} id Promotion ID
   */
  async getPromotionById(id: string): Promise<Promotion | null> {
    const doc = await this.db.collection(this.collection).doc(id).get();

    if (!doc.exists) {
      return null;
    }

    return {
      id: doc.id,
      ...doc.data(),
    } as Promotion;
  }

  /**
   * Increment usage count của promotion
   * TODO: Use transaction để đảm bảo không vượt quá limit
   * @param {string} promotionId Promotion ID
   */
  async incrementUsageCount(promotionId: string): Promise<void> {
    void promotionId;
    // TODO: Implement with transaction
    // TODO: Check usageLimit before incrementing

    throw new Error("incrementUsageCount: Not implemented");
  }

  /**
   * Get active promotions
   */
  async getActivePromotions(): Promise<Promotion[]> {
    const now = new Date().toISOString();

    const snapshot = await this.db
      .collection(this.collection)
      .where("isActive", "==", true)
      .where("validUntil", ">", now)
      .get();

    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    } as Promotion));
  }
}

// Singleton instance
export const promotionRepository = new PromotionRepository();
