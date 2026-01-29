#!/usr/bin/env node

/**
 * Backfill Script: Fix Product soldCount
 *
 * Purpose: Recompute soldCount for all products based on DELIVERED orders
 * and mark orders as soldCountApplied to prevent double-counting
 *
 * Definition:
 * soldCount = total quantity sold of a product across DELIVERED orders only
 *
 * Usage:
 *   npx ts-node scripts/backfill-soldcount.ts                    # All products (dry run)
 *   npx ts-node scripts/backfill-soldcount.ts --execute           # Execute updates
 *   npx ts-node scripts/backfill-soldcount.ts --product prod_123  # Specific product (dry run)
 *   npx ts-node scripts/backfill-soldcount.ts --product prod_123 --execute  # Specific product (execute)
 *
 * Safe to run multiple times (idempotent):
 * - Recalculates from scratch each time
 * - No double-counting risk
 * - Can be run on live data
 * - Dry run mode by default
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

interface ProductSoldCount {
  productId: string;
  productName: string;
  oldSoldCount: number;
  newSoldCount: number;
  ordersCount: number;
}

interface BackfillStats {
  totalProducts: number;
  totalOrders: number;
  productsUpdated: number;
  ordersMarked: number;
  errors: Array<{ productId: string; error: string }>;
}

/**
 * Calculate soldCount for a single product from DELIVERED orders
 */
async function calculateProductSoldCount(productId: string): Promise<ProductSoldCount> {
  // Get product
  const productDoc = await db.collection('products').doc(productId).get();
  if (!productDoc.exists) {
    throw new Error(`Product ${productId} not found`);
  }

  const productData = productDoc.data()!;
  const oldSoldCount = productData.soldCount || 0;

  // Find all DELIVERED orders containing this product
  const ordersSnapshot = await db
    .collection('orders')
    .where('status', '==', 'DELIVERED')
    .get();

  let newSoldCount = 0;
  let ordersCount = 0;

  for (const orderDoc of ordersSnapshot.docs) {
    const orderData = orderDoc.data();
    const items = orderData.items || [];

    for (const item of items) {
      if (item.productId === productId) {
        newSoldCount += item.quantity;
        ordersCount++;
        break; // Count each order only once even if product appears multiple times
      }
    }
  }

  return {
    productId,
    productName: productData.name,
    oldSoldCount,
    newSoldCount,
    ordersCount,
  };
}

/**
 * Update product soldCount and mark orders as soldCountApplied
 */
async function updateProductSoldCount(
  productId: string,
  newSoldCount: number,
  execute: boolean,
): Promise<void> {
  if (!execute) {
    console.log(`     [DRY RUN] Would update soldCount to ${newSoldCount}`);
    return;
  }

  // Update product soldCount
  await db.collection('products').doc(productId).update({
    soldCount: newSoldCount,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  console.log(`     ✅ Updated soldCount to ${newSoldCount}`);
}

/**
 * Mark all DELIVERED orders as soldCountApplied
 */
async function markOrdersAsSoldCountApplied(execute: boolean): Promise<number> {
  if (!execute) {
    console.log(`\n📋 [DRY RUN] Would mark DELIVERED orders as soldCountApplied`);
    return 0;
  }

  console.log(`\n📋 Marking DELIVERED orders as soldCountApplied...`);

  const ordersSnapshot = await db
    .collection('orders')
    .where('status', '==', 'DELIVERED')
    .get();

  let markedCount = 0;
  const batchSize = 500;
  let batch = db.batch();
  let operationCount = 0;

  for (const orderDoc of ordersSnapshot.docs) {
    const orderData = orderDoc.data();
    
    // Only mark if not already applied
    if (!orderData.soldCountApplied) {
      batch.update(orderDoc.ref, { soldCountApplied: true });
      operationCount++;
      markedCount++;

      // Commit batch when it reaches 500 operations
      if (operationCount >= batchSize) {
        await batch.commit();
        batch = db.batch();
        operationCount = 0;
        console.log(`   ✓ Marked ${markedCount} orders so far...`);
      }
    }
  }

  // Commit remaining operations
  if (operationCount > 0) {
    await batch.commit();
  }

  console.log(`   ✅ Marked ${markedCount} orders as soldCountApplied`);
  return markedCount;
}

/**
 * Backfill soldCount for a single product
 */
async function backfillProduct(productId: string, execute: boolean): Promise<ProductSoldCount> {
  console.log(`\n📦 Processing product ${productId}...`);

  const result = await calculateProductSoldCount(productId);

  console.log(`   Product: ${result.productName}`);
  console.log(`   Old soldCount: ${result.oldSoldCount}`);
  console.log(`   New soldCount: ${result.newSoldCount} (from ${result.ordersCount} orders)`);
  console.log(`   Difference: ${result.newSoldCount - result.oldSoldCount > 0 ? '+' : ''}${result.newSoldCount - result.oldSoldCount}`);

  if (result.oldSoldCount !== result.newSoldCount) {
    await updateProductSoldCount(productId, result.newSoldCount, execute);
  } else {
    console.log(`   ✓ Already correct (no update needed)`);
  }

  return result;
}

/**
 * Backfill soldCount for all products
 */
async function backfillAllProducts(execute: boolean): Promise<BackfillStats> {
  console.log(`\n📦 Fetching all products...`);

  const productsSnapshot = await db.collection('products').get();
  const totalProducts = productsSnapshot.size;

  console.log(`   Found ${totalProducts} products\n`);

  const stats: BackfillStats = {
    totalProducts,
    totalOrders: 0,
    productsUpdated: 0,
    ordersMarked: 0,
    errors: [],
  };

  const results: ProductSoldCount[] = [];

  for (let i = 0; i < productsSnapshot.docs.length; i++) {
    const productDoc = productsSnapshot.docs[i];
    const productId = productDoc.id;

    try {
      console.log(`\n[${i + 1}/${totalProducts}] Processing ${productId}...`);
      const result = await backfillProduct(productId, execute);
      results.push(result);

      if (result.oldSoldCount !== result.newSoldCount) {
        stats.productsUpdated++;
      }

      stats.totalOrders += result.ordersCount;
    } catch (error) {
      console.error(`   ❌ Error: ${error}`);
      stats.errors.push({ productId, error: String(error) });
    }
  }

  // Mark orders as soldCountApplied
  stats.ordersMarked = await markOrdersAsSoldCountApplied(execute);

  // Print summary
  console.log('\n\n' + '='.repeat(60));
  console.log('📊 BACKFILL SUMMARY');
  console.log('='.repeat(60));
  console.log(`\nMode: ${execute ? '✅ EXECUTE' : '🔍 DRY RUN'}`);
  console.log(`\nProducts:`);
  console.log(`  Total scanned: ${stats.totalProducts}`);
  console.log(`  Need updates: ${stats.productsUpdated}`);
  console.log(`  Already correct: ${stats.totalProducts - stats.productsUpdated - stats.errors.length}`);
  console.log(`  Errors: ${stats.errors.length}`);

  console.log(`\nOrders:`);
  console.log(`  Total DELIVERED orders: ${stats.totalOrders}`);
  console.log(`  Marked as soldCountApplied: ${stats.ordersMarked}`);

  if (stats.productsUpdated > 0) {
    console.log(`\n📋 Products with changes:`);
    console.log('─'.repeat(60));
    results
      .filter((r) => r.oldSoldCount !== r.newSoldCount)
      .forEach((r) => {
        const diff = r.newSoldCount - r.oldSoldCount;
        const sign = diff > 0 ? '+' : '';
        console.log(
          `  ${r.productId.substring(0, 12)}... | ${r.productName.substring(0, 30).padEnd(30)} | ${r.oldSoldCount.toString().padStart(4)} → ${r.newSoldCount.toString().padStart(4)} (${sign}${diff})`,
        );
      });
  }

  if (stats.errors.length > 0) {
    console.log(`\n❌ Errors:`);
    stats.errors.forEach((e) => {
      console.log(`  ${e.productId}: ${e.error}`);
    });
  }

  if (!execute) {
    console.log(`\n⚠️  DRY RUN MODE - No changes were made`);
    console.log(`   Run with --execute flag to apply changes`);
  } else {
    console.log(`\n✅ Backfill completed successfully!`);
  }

  console.log('='.repeat(60) + '\n');

  return stats;
}

/**
 * Main function
 */
async function main() {
  const args = process.argv.slice(2);
  const executeFlag = args.includes('--execute');
  const productFlag = args.find((arg) => arg.startsWith('--product'));
  const specificProductId = productFlag ? productFlag.split('=')[1] : null;

  console.log('\n🔄 Product soldCount Backfill Script');
  console.log('=====================================\n');
  console.log(`Mode: ${executeFlag ? '✅ EXECUTE (will make changes)' : '🔍 DRY RUN (no changes)'}`);

  try {
    if (specificProductId) {
      // Backfill specific product
      console.log(`📍 Target: Single product (${specificProductId})`);
      await backfillProduct(specificProductId, executeFlag);
      
      // Also mark orders if executing
      if (executeFlag) {
        await markOrdersAsSoldCountApplied(true);
      }
    } else {
      // Backfill all products
      console.log(`📍 Target: All products`);
      await backfillAllProducts(executeFlag);
    }

    process.exit(0);
  } catch (error) {
    console.error('\n❌ Fatal error:', error);
    process.exit(1);
  }
}

// Run main function
main();
