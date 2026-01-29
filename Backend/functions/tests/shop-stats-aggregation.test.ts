import * as admin from 'firebase-admin';
import * as test from 'firebase-functions-test';
import { expect } from 'chai';

/**
 * Shop Stats Aggregation Tests
 * 
 * Tests the consistency and correctness of shop statistics (totalOrders, 
 * totalRevenue, rating, totalRatings) across delivery workflow and manual triggers.
 */

// Initialize Firebase emulator for testing
const firestore = admin.firestore();

describe('Shop Stats Aggregation', () => {
  let shopId: string;
  let userId: string;

  before(async () => {
    // Use Firebase emulator
    if (process.env.FIRESTORE_EMULATOR_HOST) {
      console.log('Using Firestore Emulator');
    }

    // Create test shop and user
    shopId = `shop_${Date.now()}`;
    userId = `user_${Date.now()}`;

    const shopRef = firestore.collection('shops').doc(shopId);
    await shopRef.set({
      name: 'Test Shop',
      totalOrders: 0,
      totalRevenue: 0,
      rating: 0,
      totalRatings: 0,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    const userRef = firestore.collection('users').doc(userId);
    await userRef.set({
      name: 'Test User',
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  });

  after(async () => {
    // Cleanup test data
    await firestore.collection('shops').doc(shopId).delete();
    await firestore.collection('users').doc(userId).delete();

    // Cleanup test orders
    const ordersSnapshot = await firestore
      .collection('orders')
      .where('shopId', '==', shopId)
      .get();
    for (const doc of ordersSnapshot.docs) {
      await doc.ref.delete();
    }

    // Cleanup test reviews
    const reviewsSnapshot = await firestore.collection('reviews').where('shopId', '==', shopId).get();
    for (const doc of reviewsSnapshot.docs) {
      await doc.ref.delete();
    }
  });

  describe('Single Order Delivery', () => {
    it('should update shop stats when order is marked as DELIVERED', async () => {
      // 1. Create order
      const orderId = `order_${Date.now()}`;
      const orderRef = firestore.collection('orders').doc(orderId);
      const orderAmount = 1000;

      await orderRef.set({
        shopId,
        userId,
        status: 'PENDING',
        total: orderAmount,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // 2. Mark as delivered (this should trigger stats update)
      await orderRef.update({
        status: 'DELIVERED',
        deliveredAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // 3. Verify shop stats are updated
      const shopSnapshot = await firestore.collection('shops').doc(shopId).get();
      const shopData = shopSnapshot.data() || {};

      expect(shopData.totalOrders).to.equal(1);
      expect(shopData.totalRevenue).to.equal(orderAmount);
    });

    it('should not count non-DELIVERED orders in stats', async () => {
      const initialShop = (await firestore.collection('shops').doc(shopId).get()).data();
      const initialOrders = initialShop?.totalOrders || 0;

      // Create PENDING order (not delivered)
      const orderId = `order_pending_${Date.now()}`;
      await firestore.collection('orders').doc(orderId).set({
        shopId,
        userId,
        status: 'PENDING',
        total: 500,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // Stats should not change
      const updatedShop = (await firestore.collection('shops').doc(shopId).get()).data();
      expect(updatedShop?.totalOrders).to.equal(initialOrders);
    });
  });

  describe('Multiple Orders', () => {
    it('should correctly aggregate stats for multiple delivered orders', async () => {
      // Clear shop stats
      await firestore.collection('shops').doc(shopId).update({
        totalOrders: 0,
        totalRevenue: 0,
      });

      // Create and deliver multiple orders
      const orderAmounts = [1000, 2000, 1500];
      const orderIds: string[] = [];

      for (const amount of orderAmounts) {
        const orderId = `order_multi_${Date.now()}_${Math.random()}`;
        orderIds.push(orderId);

        await firestore.collection('orders').doc(orderId).set({
          shopId,
          userId,
          status: 'DELIVERED',
          total: amount,
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
        });
      }

      // Manually trigger backfill to recalculate
      // (In production, this would be called via endpoint or scheduled)
      await updateShopStatsAggregation(shopId);

      // Verify aggregation
      const shopSnapshot = await firestore.collection('shops').doc(shopId).get();
      const shopData = shopSnapshot.data() || {};

      expect(shopData.totalOrders).to.equal(3);
      expect(shopData.totalRevenue).to.equal(4500);

      // Cleanup
      for (const orderId of orderIds) {
        await firestore.collection('orders').doc(orderId).delete();
      }
    });
  });

  describe('Rating Aggregation', () => {
    it('should calculate average rating from reviews', async () => {
      // Create reviews
      const reviewIds: string[] = [];
      const ratings = [5, 4, 3];

      for (const rating of ratings) {
        const reviewId = `review_${Date.now()}_${Math.random()}`;
        reviewIds.push(reviewId);

        await firestore.collection('reviews').doc(reviewId).set({
          shopId,
          userId,
          rating,
          comment: `Test review with ${rating} stars`,
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
        });
      }

      // Trigger stats recalculation
      await updateShopStatsAggregation(shopId);

      // Verify rating
      const shopSnapshot = await firestore.collection('shops').doc(shopId).get();
      const shopData = shopSnapshot.data() || {};

      expect(shopData.totalRatings).to.equal(3);
      expect(shopData.rating).to.equal(4); // (5+4+3)/3 = 4

      // Cleanup
      for (const reviewId of reviewIds) {
        await firestore.collection('reviews').doc(reviewId).delete();
      }
    });

    it('should return 0 rating when shop has no reviews', async () => {
      // Clear reviews
      const reviewsSnapshot = await firestore.collection('reviews').where('shopId', '==', shopId).get();
      for (const doc of reviewsSnapshot.docs) {
        await doc.ref.delete();
      }

      // Recalculate
      await updateShopStatsAggregation(shopId);

      // Verify
      const shopSnapshot = await firestore.collection('shops').doc(shopId).get();
      const shopData = shopSnapshot.data() || {};

      expect(shopData.rating).to.equal(0);
      expect(shopData.totalRatings).to.equal(0);
    });

    it('should round rating to 1 decimal place', async () => {
      // Create reviews with specific ratings to test rounding
      const reviewIds: string[] = [];
      const ratings = [5, 4]; // Average = 4.5

      for (const rating of ratings) {
        const reviewId = `review_round_${Date.now()}_${Math.random()}`;
        reviewIds.push(reviewId);

        await firestore.collection('reviews').doc(reviewId).set({
          shopId,
          userId,
          rating,
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
        });
      }

      // Recalculate
      await updateShopStatsAggregation(shopId);

      // Verify rounding
      const shopSnapshot = await firestore.collection('shops').doc(shopId).get();
      const shopData = shopSnapshot.data() || {};

      expect(shopData.rating).to.equal(4.5);

      // Cleanup
      for (const reviewId of reviewIds) {
        await firestore.collection('reviews').doc(reviewId).delete();
      }
    });
  });

  describe('Data Consistency', () => {
    it('should maintain consistency across multiple recalculations', async () => {
      // Create known data state
      await firestore.collection('shops').doc(shopId).update({
        totalOrders: 0,
        totalRevenue: 0,
        rating: 0,
        totalRatings: 0,
      });

      const orderId = `order_consistency_${Date.now()}`;
      await firestore.collection('orders').doc(orderId).set({
        shopId,
        userId,
        status: 'DELIVERED',
        total: 3000,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // Recalculate multiple times
      await updateShopStatsAggregation(shopId);
      const firstCalc = (await firestore.collection('shops').doc(shopId).get()).data();

      await updateShopStatsAggregation(shopId);
      const secondCalc = (await firestore.collection('shops').doc(shopId).get()).data();

      await updateShopStatsAggregation(shopId);
      const thirdCalc = (await firestore.collection('shops').doc(shopId).get()).data();

      // All calculations should be identical (idempotent)
      expect(firstCalc?.totalOrders).to.equal(secondCalc?.totalOrders);
      expect(firstCalc?.totalOrders).to.equal(thirdCalc?.totalOrders);
      expect(firstCalc?.totalRevenue).to.equal(secondCalc?.totalRevenue);
      expect(firstCalc?.totalRevenue).to.equal(thirdCalc?.totalRevenue);

      // Cleanup
      await firestore.collection('orders').doc(orderId).delete();
    });

    it('should not double-count when backfilling', async () => {
      // Create order
      const orderId = `order_double_${Date.now()}`;
      await firestore.collection('orders').doc(orderId).set({
        shopId,
        userId,
        status: 'DELIVERED',
        total: 2500,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // Get initial count
      const beforeCount = (await firestore.collection('shops').doc(shopId).get()).data()?.totalOrders || 0;

      // Backfill
      await updateShopStatsAggregation(shopId);
      const afterBackfill = (await firestore.collection('shops').doc(shopId).get()).data()?.totalOrders || 0;

      // Backfill again
      await updateShopStatsAggregation(shopId);
      const afterSecondBackfill = (await firestore.collection('shops').doc(shopId).get()).data()?.totalOrders || 0;

      // Should be idempotent
      expect(afterBackfill).to.equal(afterSecondBackfill);

      // Cleanup
      await firestore.collection('orders').doc(orderId).delete();
    });
  });

  describe('Analytics Status Alignment', () => {
    it('should use DELIVERED status for analytics queries (not COMPLETED)', async () => {
      // Create orders with different statuses
      const deliveredId = `order_delivered_${Date.now()}`;
      const completedId = `order_completed_${Date.now()}`;

      await firestore.collection('orders').doc(deliveredId).set({
        shopId,
        userId,
        status: 'DELIVERED',
        total: 1000,
      });

      await firestore.collection('orders').doc(completedId).set({
        shopId,
        userId,
        status: 'COMPLETED',
        total: 1000,
      });

      // Recalculate (should only count DELIVERED)
      await updateShopStatsAggregation(shopId);

      const shopData = (await firestore.collection('shops').doc(shopId).get()).data() || {};

      // Should count only DELIVERED, not COMPLETED
      expect(shopData.totalOrders).to.equal(1);
      expect(shopData.totalRevenue).to.equal(1000);

      // Cleanup
      await firestore.collection('orders').doc(deliveredId).delete();
      await firestore.collection('orders').doc(completedId).delete();
    });
  });
});

/**
 * Helper function to simulate stats recalculation
 * (In production, this would be called via endpoint/scheduled function)
 */
async function updateShopStatsAggregation(shopId: string): Promise<void> {
  // Count DELIVERED orders and sum revenue
  const ordersSnapshot = await firestore
    .collection('orders')
    .where('shopId', '==', shopId)
    .where('status', '==', 'DELIVERED')
    .get();

  const totalOrders = ordersSnapshot.size;
  const totalRevenue = ordersSnapshot.docs.reduce(
    (sum: number, doc: any) => sum + (doc.data().total || 0),
    0,
  );

  // Count reviews and calculate rating
  const reviewsSnapshot = await firestore.collection('reviews').where('shopId', '==', shopId).get();
  const totalRatings = reviewsSnapshot.size;
  const ratings = reviewsSnapshot.docs.map((doc: any) => doc.data().rating || 0);
  const rating =
    ratings.length > 0 ? Math.round((ratings.reduce((a: number, b: number) => a + b, 0) / ratings.length) * 10) / 10 : 0;

  // Update shop
  await firestore.collection('shops').doc(shopId).update({
    totalOrders,
    totalRevenue,
    rating,
    totalRatings,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
}
