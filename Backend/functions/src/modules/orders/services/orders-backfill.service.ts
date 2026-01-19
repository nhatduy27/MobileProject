/**
 * Backfill Service: Fix missing shipperId field in existing orders
 *
 * Why this is needed:
 * Firestore query .where('shipperId', '==', null) only matches documents where
 * the field explicitly exists with value null. It does NOT match documents
 * where the field is missing/undefined.
 *
 * Problem: Orders created before the fix may lack shipperId field entirely.
 * Solution: This service scans and backfills those orders with shipperId: null.
 *
 * Safe to run multiple times (idempotent).
 */

import { Injectable } from '@nestjs/common';
import { FirebaseService } from '../../../core/firebase/firebase.service';
import { OrderStatus } from '../entities/order.entity';

interface BackfillResult {
  scanned: number;
  updated: number;
  skipped: number;
  errors: Array<{ docId: string; error: string }>;
}

@Injectable()
export class OrdersBackfillService {
  constructor(private firebase: FirebaseService) {}

  /**
   * Backfill shipperId: null for orders missing this field
   *
   * Scans:
   * - All READY orders (and other statuses if needed)
   * - Identifies those without shipperId field
   * - Updates them to include shipperId: null
   *
   * Safe: Batch writes in groups of 100
   */
  async backfillShipperIdNull(
    statuses: OrderStatus[] = [
      OrderStatus.READY,
      OrderStatus.PENDING,
      OrderStatus.CONFIRMED,
      OrderStatus.PREPARING,
    ],
  ): Promise<BackfillResult> {
    const db = this.firebase.firestore;
    const result: BackfillResult = {
      scanned: 0,
      updated: 0,
      skipped: 0,
      errors: [],
    };

    try {
      // Get all orders (paginate if needed)
      const snapshot = await db.collection('orders').get();

      console.log(`[Backfill] Starting scan of ${snapshot.size} orders...`);

      const docsToUpdate: Array<{ id: string; data: any }> = [];

      // Identify orders missing shipperId
      snapshot.forEach((doc: any) => {
        result.scanned++;
        const data = doc.data();

        // Check if order should be backfilled
        if (statuses.includes(data.status) && !('shipperId' in data)) {
          // Missing shipperId field - needs update
          docsToUpdate.push({
            id: doc.id,
            data,
          });
        }
      });

      console.log(
        `[Backfill] Found ${docsToUpdate.length} orders missing shipperId field`,
      );

      // Batch update in groups of 100 (safe Firestore limit)
      const batchSize = 100;
      for (let i = 0; i < docsToUpdate.length; i += batchSize) {
        const batch = docsToUpdate.slice(i, i + batchSize);
        const writeBatch = db.batch();

        batch.forEach(({ id }) => {
          const docRef = db.collection('orders').doc(id);
          writeBatch.update(docRef, {
            shipperId: null,
          });
          result.updated++;
        });

        await writeBatch.commit();
        console.log(
          `[Backfill] Batch ${Math.floor(i / batchSize) + 1}: Updated ${batch.length} orders`,
        );
      }

      result.skipped = result.scanned - result.updated;

      console.log(
        `[Backfill] Complete! Scanned: ${result.scanned}, Updated: ${result.updated}, Skipped: ${result.skipped}`,
      );

      return result;
    } catch (error) {
      console.error('[Backfill] Error during backfill:', error);
      result.errors.push({
        docId: 'batch_operation',
        error: error instanceof Error ? error.message : String(error),
      });
      throw error;
    }
  }

  /**
   * Defensive check: Validate that shipper can see available orders
   * Logs warning if mismatch between expected and actual results
   */
  async validateShipperAvailableOrders(
    shopId: string,
    _shipperId: string,
  ): Promise<{ valid: boolean; issue: string | null }> {
    const db = this.firebase.firestore;

    try {
      // Count READY orders for shop (no shipperId filter)
      const allReadySnapshot = await db
        .collection('orders')
        .where('shopId', '==', shopId)
        .where('status', '==', OrderStatus.READY)
        .count()
        .get();
      const totalReady = allReadySnapshot.data().count;

      // Count READY + unassigned orders (with shipperId filter)
      const availableSnapshot = await db
        .collection('orders')
        .where('shopId', '==', shopId)
        .where('status', '==', OrderStatus.READY)
        .where('shipperId', '==', null)
        .count()
        .get();
      const availableCount = availableSnapshot.data().count;

      if (totalReady > 0 && availableCount === 0) {
        // Mismatch: READY orders exist but none visible to shipper
        const issue = `Mismatch detected: ${totalReady} READY orders exist but shipper sees 0. ` +
          `This usually means some orders lack shipperId field. ` +
          `Run backfill to fix.`;
        console.warn('[Validation]', issue);
        return {
          valid: false,
          issue,
        };
      }

      return {
        valid: true,
        issue: null,
      };
    } catch (error) {
      console.error('[Validation] Error checking order visibility:', error);
      return {
        valid: false,
        issue: error instanceof Error ? error.message : String(error),
      };
    }
  }
}
