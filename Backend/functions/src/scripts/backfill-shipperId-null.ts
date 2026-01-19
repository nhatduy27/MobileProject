/**
 * Migration Script: Backfill shipperId: null for existing orders
 *
 * Purpose:
 * - Scans orders collection for documents missing the shipperId field
 * - Updates each doc to explicitly set shipperId: null
 * - Handles batch writes to respect Firestore limits
 *
 * Why this is needed:
 * Orders created before the fix (shipperId: null enforcement) were saved without
 * the shipperId field. Firestore query .where('shipperId', '==', null) does NOT
 * match documents where the field is missing.
 *
 * This script makes existing unassigned orders visible to shippers.
 *
 * Run with:
 * ts-node src/scripts/backfill-shipperId-null.ts
 * OR
 * node -r ts-node/register src/scripts/backfill-shipperId-null.ts
 */

import * as admin from 'firebase-admin';
import * as path from 'path';

// Initialize Firebase Admin SDK
const serviceAccountPath = path.join(__dirname, '../../service-account.json');
const serviceAccount = require(serviceAccountPath);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount as admin.ServiceAccount),
  projectId: serviceAccount.project_id,
});

const firestore = admin.firestore();
const ORDERS_COLLECTION = 'orders';
const BATCH_SIZE = 400; // Firestore batch limit

interface OrderDoc {
  id: string;
  status: string;
  shipperId?: string;
  [key: string]: any;
}

/**
 * Check if a document has the shipperId field
 */
function hasShipperId(doc: admin.firestore.DocumentData): boolean {
  return 'shipperId' in doc;
}

/**
 * Main migration logic
 */
async function backfillShipperId(): Promise<void> {
  console.log('🔍 Starting migration: backfill shipperId=null for unassigned orders...\n');

  let scannedCount = 0;
  let updatedCount = 0;
  let batchCount = 0;

  try {
    // Get all orders (paginated to avoid memory issues)
    const snapshot = await firestore.collection(ORDERS_COLLECTION).get();

    console.log(`📊 Total orders found: ${snapshot.size}`);
    console.log(`⏳ Processing in batches of ${BATCH_SIZE}...\n`);

    const orders: OrderDoc[] = [];

    // Collect orders missing shipperId field
    snapshot.forEach((doc) => {
      scannedCount++;
      const data = doc.data();

      if (!hasShipperId(data)) {
        // Order is missing shipperId field - needs backfill
        orders.push({
          id: doc.id,
          status: data.status,
          shipperId: undefined,
          ...data,
        });
      }
    });

    console.log(`📋 Orders scanned: ${scannedCount}`);
    console.log(`❌ Orders missing shipperId field: ${orders.length}`);
    console.log(`✅ Orders already with shipperId: ${scannedCount - orders.length}\n`);

    if (orders.length === 0) {
      console.log('✨ No orders need backfilling. Migration complete!');
      return;
    }

    // Process in batches
    for (let i = 0; i < orders.length; i += BATCH_SIZE) {
      const batch = orders.slice(i, i + BATCH_SIZE);
      const writeBatch = firestore.batch();

      batch.forEach((order) => {
        const docRef = firestore.collection(ORDERS_COLLECTION).doc(order.id);
        writeBatch.update(docRef, {
          shipperId: null,
        });
        updatedCount++;
      });

      await writeBatch.commit();
      batchCount++;
      const progress = Math.min((i + BATCH_SIZE) * 100 / orders.length, 100);
      console.log(`  ✓ Batch ${batchCount}: ${batch.length} orders updated (${progress.toFixed(0)}%)`);
    }

    console.log('\n✅ Migration completed successfully!\n');
    console.log('📊 Final Statistics:');
    console.log(`  • Scanned: ${scannedCount}`);
    console.log(`  • Updated: ${updatedCount}`);
    console.log(`  • Already correct: ${scannedCount - updatedCount}`);
    console.log(`  • Batches: ${batchCount}\n`);

    console.log('🔄 Next step: Verify shipper available orders endpoint');
    console.log('   GET /api/orders/shipper/available should now return previously unassigned orders\n');
  } catch (error) {
    console.error('❌ Migration failed:', error);
    process.exit(1);
  } finally {
    await admin.app().delete();
  }
}

// Run migration
backfillShipperId().then(() => {
  console.log('✨ Done!');
  process.exit(0);
}).catch((error) => {
  console.error('Fatal error:', error);
  process.exit(1);
});
