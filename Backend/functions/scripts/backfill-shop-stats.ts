#!/usr/bin/env node

/**
 * Backfill Script: Recompute Shop Stats
 *
 * Purpose: Recompute totalOrders, totalRevenue, rating, and totalRatings for all shops
 * (or a specific shop if shopId provided)
 *
 * Usage:
 *   npx ts-node scripts/backfill-shop-stats.ts               # All shops
 *   npx ts-node scripts/backfill-shop-stats.ts shop_abc123   # Specific shop
 *
 * Safe to run multiple times (idempotent):
 * - Recalculates from scratch each time
 * - No double-counting risk
 * - Can be run on live data
 */

import * as admin from 'firebase-admin';
import * as path from 'path';

// Initialize Firebase Admin SDK
if (!admin.apps.length) {
  const serviceAccountPath = path.join(__dirname, '../service-account.json');
  admin.initializeApp({
    credential: admin.credential.cert(require(serviceAccountPath)),
  });
}

const db = admin.firestore();

interface BackfillResult {
  shopId: string;
  totalOrders: number;
  totalRevenue: number;
  rating: number;
  totalRatings: number;
  error?: string;
}

/**
 * Recompute stats for a single shop
 */
async function recomputeShopStats(shopId: string): Promise<BackfillResult> {
  try {
    console.log(`  📊 Processing shop ${shopId}...`);

    // 1. Count DELIVERED orders and sum revenue
    const ordersSnapshot = await db
      .collection('orders')
      .where('shopId', '==', shopId)
      .where('status', '==', 'DELIVERED')
      .get();

    const totalOrders = ordersSnapshot.size;
    const totalRevenue = ordersSnapshot.docs.reduce(
      (sum: number, doc: any) => sum + (doc.data().total || 0),
      0,
    );

    console.log(`     ✓ Orders: ${totalOrders}, Revenue: ${totalRevenue}`);

    // 2. Count reviews and calculate average rating
    const reviewsSnapshot = await db.collection('reviews').where('shopId', '==', shopId).get();

    const totalRatings = reviewsSnapshot.size;
    const ratings = reviewsSnapshot.docs.map((doc: any) => doc.data().rating || 0);
    const rating =
      ratings.length > 0 ? Math.round((ratings.reduce((a: number, b: number) => a + b, 0) / ratings.length) * 10) / 10 : 0;

    console.log(`     ✓ Reviews: ${totalRatings}, Avg Rating: ${rating}`);

    // 3. Update shop document
    await db.collection('shops').doc(shopId).update({
      totalOrders,
      totalRevenue,
      rating,
      totalRatings,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    console.log(`     ✅ Updated shop ${shopId}`);

    return { shopId, totalOrders, totalRevenue, rating, totalRatings };
  } catch (error) {
    console.error(`     ❌ Error processing shop ${shopId}:`, error);
    return {
      shopId,
      totalOrders: 0,
      totalRevenue: 0,
      rating: 0,
      totalRatings: 0,
      error: String(error),
    };
  }
}

/**
 * Main backfill function
 */
async function main() {
  const args = process.argv.slice(2);
  const specificShopId = args[0];

  console.log('\n🔄 Shop Stats Backfill Script');
  console.log('================================\n');

  try {
    const results: BackfillResult[] = [];

    if (specificShopId) {
      // Backfill specific shop
      console.log(`📍 Target: Single shop (${specificShopId})\n`);
      const result = await recomputeShopStats(specificShopId);
      results.push(result);
    } else {
      // Backfill all shops
      console.log(`📍 Target: All shops\n`);

      const shopsSnapshot = await db.collection('shops').get();
      const totalShops = shopsSnapshot.size;

      console.log(`📊 Found ${totalShops} shops\n`);

      for (const shopDoc of shopsSnapshot.docs) {
        const result = await recomputeShopStats(shopDoc.id);
        results.push(result);
      }
    }

    // Print summary
    console.log('\n📋 Summary:');
    console.log('===========\n');

    const successful = results.filter((r) => !r.error);
    const failed = results.filter((r) => r.error);

    console.log(`✅ Successful: ${successful.length}`);
    if (successful.length > 0) {
      successful.forEach((r) => {
        console.log(
          `   ${r.shopId}: ${r.totalOrders} orders, ${r.totalRevenue} revenue, ${r.rating} rating (${r.totalRatings} reviews)`,
        );
      });
    }

    if (failed.length > 0) {
      console.log(`\n❌ Failed: ${failed.length}`);
      failed.forEach((r) => {
        console.log(`   ${r.shopId}: ${r.error}`);
      });
    }

    console.log(`\n✨ Backfill complete!\n`);

    if (failed.length === 0) {
      process.exit(0);
    } else {
      process.exit(1);
    }
  } catch (error) {
    console.error('\n❌ Fatal error:', error);
    process.exit(1);
  }
}

main();
