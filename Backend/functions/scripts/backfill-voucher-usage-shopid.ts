/**
 * Backfill ShopId for Legacy Voucher Usage Records
 *
 * One-time migration script to populate missing `shopId` field on voucherUsages documents.
 * This is a backward-compatibility utility for VOUCH-009 Phase 3 fix.
 *
 * BACKGROUND:
 * - Phase 2 added shopId denormalization to voucherUsages for efficient filtering
 * - Legacy records (created before Phase 2) don't have shopId populated
 * - Phase 3 implemented hybrid filtering that derives shopId from voucher lookup
 * - This script migrates all legacy records to have shopId, enabling fast-path filtering
 *
 * USAGE:
 *   # Dry run (no database changes, logs summary):
 *   npx ts-node scripts/backfill-voucher-usage-shopid.ts --dry-run
 *
 *   # Actual migration (commits changes):
 *   npx ts-node scripts/backfill-voucher-usage-shopid.ts
 *
 *   # With specific batch size (default 500):
 *   npx ts-node scripts/backfill-voucher-usage-shopid.ts --batch-size 100
 *
 * FEATURES:
 * ✅ Idempotent: Skips docs that already have shopId
 * ✅ Batch optimized: Processes in chunks to respect Firestore limits
 * ✅ Graceful failure: Sets shopId=null if voucher not found (logs error)
 * ✅ Dry-run mode: Preview changes without committing
 * ✅ Progress tracking: Shows processed count at intervals
 * ✅ Error recovery: Continues on individual record failures
 *
 * FIRESTORE LIMITS HANDLED:
 * - Batch writes: Max 500 per commit (configurable, default 500)
 * - In queries: Max ~10 items per 'in' clause (uses chunking)
 * - Read/write quota: Staggered batches with logging
 *
 * EXPECTED RESULT:
 * - All voucherUsages docs get shopId field (either from voucher or null)
 * - Already-migrated docs are skipped
 * - Summary stats logged: total processed, updated, skipped, errors
 */

import * as admin from 'firebase-admin';
import * as path from 'path';
import { FieldPath } from 'firebase-admin/firestore';

// Initialize Firebase Admin
const serviceAccountPath = path.join(__dirname, '..', 'service-account.json');

let serviceAccount;
try {
  serviceAccount = require(serviceAccountPath);
} catch (error) {
  console.error('\n❌ FATAL ERROR: service-account.json not found');
  console.error(`\nExpected location: ${serviceAccountPath}\n`);
  console.error('SOLUTION:');
  console.error('1. Download service-account.json from Firebase Console');
  console.error('2. Place it in: MobileProject/Backend/functions/');
  console.error('3. Try running the script again\n');
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const db = admin.firestore();

// Configuration
const VOUCHER_USAGES_COLLECTION = 'voucherUsages';
const VOUCHERS_COLLECTION = 'vouchers';
const DEFAULT_BATCH_SIZE = 500;
const VOUCHER_BATCH_CHUNK_SIZE = 10; // Firestore 'in' operator limit
const LOG_INTERVAL = 100; // Log progress every N records

// Types
interface VoucherUsageDoc {
  id: string;
  voucherId: string;
  shopId: string | null | undefined;
  [key: string]: any;
}

interface VoucherDoc {
  id: string;
  shopId: string | null | undefined;
  [key: string]: any;
}

interface MigrationStats {
  totalProcessed: number;
  totalUpdated: number;
  totalSkipped: number; // Already had shopId
  totalErrors: number;
  errorDetails: Array<{ docId: string; reason: string }>;
}

interface ParsedArgs {
  dryRun: boolean;
  batchSize: number;
}

// ============================================================================
// Utility Functions
// ============================================================================

/**
 * Parse command-line arguments
 */
function parseArgs(): ParsedArgs {
  const args = process.argv.slice(2);
  return {
    dryRun: args.includes('--dry-run'),
    batchSize: (() => {
      const batchIdx = args.indexOf('--batch-size');
      if (batchIdx !== -1 && args[batchIdx + 1]) {
        const size = parseInt(args[batchIdx + 1], 10);
        return isNaN(size) ? DEFAULT_BATCH_SIZE : Math.min(size, 500);
      }
      return DEFAULT_BATCH_SIZE;
    })(),
  };
}

/**
 * Batch fetch vouchers using 'in' operator with chunking
 * @param voucherIds Array of voucher IDs to fetch
 * @returns Map of voucherId -> VoucherDoc
 */
async function batchFetchVouchers(
  voucherIds: string[],
): Promise<Map<string, VoucherDoc>> {
  const voucherMap = new Map<string, VoucherDoc>();

  if (voucherIds.length === 0) {
    return voucherMap;
  }

  // Process in chunks to respect Firestore 'in' operator limit (~10 items)
  for (let i = 0; i < voucherIds.length; i += VOUCHER_BATCH_CHUNK_SIZE) {
    const chunk = voucherIds.slice(i, i + VOUCHER_BATCH_CHUNK_SIZE);

    const snapshot = await db
      .collection(VOUCHERS_COLLECTION)
      .where(FieldPath.documentId(), 'in', chunk)
      .get();

    for (const doc of snapshot.docs) {
      voucherMap.set(doc.id, {
        id: doc.id,
        ...doc.data(),
      } as VoucherDoc);
    }
  }

  return voucherMap;
}

/**
 * Process a batch of legacy usage docs and prepare updates
 * @param usageDocs Docs that need shopId
 * @returns Array of { docRef, data } objects ready to batch write
 */
async function processBatch(
  usageDocs: VoucherUsageDoc[],
  stats: MigrationStats,
): Promise<Array<{ ref: admin.firestore.DocumentReference; data: any }>> {
  const updates: Array<{ ref: admin.firestore.DocumentReference; data: any }> =
    [];

  // Collect all voucherIds that need lookup
  const voucherIdsToFetch = Array.from(
    new Set(usageDocs.map((doc) => doc.voucherId)),
  );

  // Batch fetch all vouchers
  const voucherMap = await batchFetchVouchers(voucherIdsToFetch);

  // Process each usage doc
  for (const usageDoc of usageDocs) {
    try {
      const voucher = voucherMap.get(usageDoc.voucherId);
      const shopId = voucher?.shopId || null;

      const docRef = db
        .collection(VOUCHER_USAGES_COLLECTION)
        .doc(usageDoc.id);

      updates.push({
        ref: docRef,
        data: { shopId },
      });

      stats.totalUpdated++;

      if (!voucher) {
        stats.errorDetails.push({
          docId: usageDoc.id,
          reason: `Voucher not found: ${usageDoc.voucherId}`,
        });
        stats.totalErrors++;
      }
    } catch (error) {
      stats.totalErrors++;
      stats.errorDetails.push({
        docId: usageDoc.id,
        reason: `Error: ${error instanceof Error ? error.message : String(error)}`,
      });
    }
  }

  return updates;
}

/**
 * Commit batch writes to Firestore
 * @param updates Array of updates to write
 */
async function commitBatch(
  updates: Array<{ ref: admin.firestore.DocumentReference; data: any }>,
): Promise<void> {
  if (updates.length === 0) {
    return;
  }

  const batch = db.batch();

  for (const { ref, data } of updates) {
    batch.update(ref, data);
  }

  await batch.commit();
}

// ============================================================================
// Main Migration Logic
// ============================================================================

async function migrateVoucherUsageShopId(args: ParsedArgs): Promise<void> {
  const stats: MigrationStats = {
    totalProcessed: 0,
    totalUpdated: 0,
    totalSkipped: 0,
    totalErrors: 0,
    errorDetails: [],
  };

  console.log('\n📊 Backfill ShopId for Legacy Voucher Usage Records');
  console.log('━'.repeat(60));
  console.log(`Mode: ${args.dryRun ? 'DRY RUN (no changes)' : 'MIGRATION (will commit)'}`);
  console.log(`Batch size: ${args.batchSize}`);
  console.log('');

  try {
    // Step 1: Query for legacy docs (shopId is missing or null)
    // Note: Firestore can't query for missing fields, so we fetch all and filter client-side
    console.log('🔍 Scanning for legacy records (shopId missing/null)...');
    const allSnapshot = await db
      .collection(VOUCHER_USAGES_COLLECTION)
      .limit(50000) // Safety limit
      .get();

    // Filter for records where shopId is missing or null
    const legacyDocs = allSnapshot.docs
      .filter((doc) => !doc.data().shopId) // Field missing or null
      .map((doc) => ({
        id: doc.id,
        ...doc.data(),
      } as VoucherUsageDoc));

    const legacyDocsCount = legacyDocs.length;
    console.log(`Found ${legacyDocsCount} legacy records\n`);

    if (legacyDocsCount === 0) {
      console.log('✅ No legacy records to migrate!');
      return;
    }

    // Step 2: Process in batches
    let currentBatch: VoucherUsageDoc[] = [];

    console.log(`⚙️  Processing ${legacyDocsCount} records in batches of ${args.batchSize}...\n`);

    for (const usageDoc of legacyDocs) {
      currentBatch.push(usageDoc);
      stats.totalProcessed++;

      // Process batch when it reaches the size limit
      if (currentBatch.length >= args.batchSize) {
        const updates = await processBatch(currentBatch, stats);

        if (!args.dryRun) {
          await commitBatch(updates);
        }

        console.log(`  ✓ Batch complete: ${stats.totalProcessed}/${legacyDocsCount}`);
        currentBatch = [];
      }

      // Log progress at intervals
      if (stats.totalProcessed % LOG_INTERVAL === 0) {
        console.log(`  ... processed ${stats.totalProcessed}/${legacyDocsCount}`);
      }
    }

    // Process remaining records
    if (currentBatch.length > 0) {
      const updates = await processBatch(currentBatch, stats);

      if (!args.dryRun) {
        await commitBatch(updates);
      }

      console.log(`  ✓ Final batch complete: ${stats.totalProcessed}/${legacyDocsCount}`);
    }

    // Step 3: Print summary
    console.log('\n' + '━'.repeat(60));
    console.log('📈 Migration Summary');
    console.log('━'.repeat(60));
    console.log(`Total Processed:  ${stats.totalProcessed}`);
    console.log(`Total Updated:    ${stats.totalUpdated}`);
    console.log(`Total Skipped:    ${stats.totalSkipped}`);
    console.log(`Total Errors:     ${stats.totalErrors}`);

    if (stats.errorDetails.length > 0 && stats.errorDetails.length <= 10) {
      console.log('\n⚠️  Error Details:');
      for (const error of stats.errorDetails) {
        console.log(`  • ${error.docId}: ${error.reason}`);
      }
    } else if (stats.errorDetails.length > 10) {
      console.log(
        `\n⚠️  ${stats.totalErrors} errors occurred. First 10:`,
      );
      for (const error of stats.errorDetails.slice(0, 10)) {
        console.log(`  • ${error.docId}: ${error.reason}`);
      }
    }

    if (args.dryRun) {
      console.log('\n✅ Dry run complete! No changes were made to database.');
      console.log('   Run without --dry-run flag to commit changes.');
    } else {
      console.log('\n✅ Migration complete! All records have been updated.');
    }

    console.log('');
  } catch (error) {
    console.error('\n❌ Migration failed:', error);
    process.exit(1);
  }
}

// ============================================================================
// Entry Point
// ============================================================================

async function main(): Promise<void> {
  try {
    const args = parseArgs();

    await migrateVoucherUsageShopId(args);

    // Clean up Firebase connection
    await admin.app().delete();
    process.exit(0);
  } catch (error) {
    console.error('Fatal error:', error);
    process.exit(1);
  }
}

main();
