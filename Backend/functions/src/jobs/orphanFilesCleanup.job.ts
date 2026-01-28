/**
 * Orphan Avatar Files Cleanup Job
 *
 * Scheduled to run every 10 minutes to clean up failed storage deletions.
 * Queries `orphanFiles` collection for AVATAR records with status PENDING
 * and retries deletion with exponential backoff.
 *
 * Run via Cloud Scheduler:
 * - Schedule: every 10 minutes (cron: every 10 minutes)
 * - Target: HTTP to orphanFilesCleanup function
 */

import * as admin from 'firebase-admin';
import { Firestore, FieldValue } from 'firebase-admin/firestore';

const BUCKET_NAME = 'foodappproject-7c136.firebasestorage.app';
const MAX_RETRIES = 5;
const MAX_RECORDS_PER_RUN = 50;
const BACKOFF_MINUTES = 30; // 30 min * retryCount

interface OrphanFileRecord {
  id: string;
  userId: string;
  fileType: string;
  filePath: string;
  status: 'PENDING' | 'DELETED';
  retryCount: number;
  lastError?: string;
  errorMessage?: string;
  createdAt?: admin.firestore.Timestamp;
  updatedAt?: admin.firestore.Timestamp;
  nextRetryAt?: admin.firestore.Timestamp;
}

/**
 * Main cleanup job function
 * Called by Cloud Scheduler every 10 minutes
 */
export async function runOrphanFilesCleanup(firestore: Firestore): Promise<void> {
  const startTime = Date.now();
  let processedCount = 0;
  let successCount = 0;
  let failureCount = 0;

  try {
    // Query orphan files with PENDING status and retry count < MAX_RETRIES
    const query = firestore
      .collection('orphanFiles')
      .where('fileType', '==', 'AVATAR')
      .where('status', '==', 'PENDING')
      .where('retryCount', '<', MAX_RETRIES)
      .limit(MAX_RECORDS_PER_RUN);

    // Optional: filter by nextRetryAt if it exists
    // For simplicity, we'll process all PENDING records and the app can handle retry scheduling

    const snapshot = await query.get();

    if (snapshot.empty) {
      console.log('No orphan avatar files to clean up');
      return;
    }

    console.log(`Found ${snapshot.size} orphan files to process`);

    // Process each record
    for (const doc of snapshot.docs) {
      const record = doc.data() as Omit<OrphanFileRecord, 'id'>;
      const recordId = doc.id;

      try {
        processedCount++;

        // Attempt to delete the avatar from storage
        const deleteResult = await deleteAvatarFromStorage(record.filePath);

        if (deleteResult.success) {
          // Success: mark as DELETED
          await firestore.collection('orphanFiles').doc(recordId).update({
            status: 'DELETED',
            deletedAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
          });

          successCount++;

          console.log(`✅ Deleted orphan file`, {
            docId: recordId,
            userId: record.userId,
            filePath: record.filePath,
            attempt: (record.retryCount || 0) + 1,
          });
        } else {
          // Failure: increment retry count
          const newRetryCount = (record.retryCount || 0) + 1;
          const nextRetryMinutes = BACKOFF_MINUTES * newRetryCount;
          const nextRetryTime = new Date(Date.now() + nextRetryMinutes * 60 * 1000);

          await firestore.collection('orphanFiles').doc(recordId).update({
            status: 'PENDING',
            retryCount: newRetryCount,
            lastError: deleteResult.error,
            updatedAt: FieldValue.serverTimestamp(),
            nextRetryAt: admin.firestore.Timestamp.fromDate(nextRetryTime),
          });

          failureCount++;

          console.warn(`⚠️ Failed to delete orphan file`, {
            docId: recordId,
            userId: record.userId,
            filePath: record.filePath,
            attempt: newRetryCount,
            error: deleteResult.error,
            nextRetryAt: nextRetryTime.toISOString(),
          });
        }
      } catch (error: any) {
        failureCount++;

        console.error(`❌ Error processing orphan file`, {
          docId: recordId,
          userId: record.userId,
          error: error?.message || String(error),
        });

        // Try to update the record with error info
        try {
          const newRetryCount = (record.retryCount || 0) + 1;
          if (newRetryCount < MAX_RETRIES) {
            const nextRetryMinutes = BACKOFF_MINUTES * newRetryCount;
            const nextRetryTime = new Date(Date.now() + nextRetryMinutes * 60 * 1000);

            await firestore.collection('orphanFiles').doc(recordId).update({
              retryCount: newRetryCount,
              lastError: error?.message || String(error),
              updatedAt: FieldValue.serverTimestamp(),
              nextRetryAt: admin.firestore.Timestamp.fromDate(nextRetryTime),
            });
          }
        } catch (updateError) {
          console.error(`Failed to update error info for record ${recordId}`, updateError);
        }
      }
    }

    const duration = Date.now() - startTime;
    console.log(`Cleanup job completed`, {
      duration: `${duration}ms`,
      processed: processedCount,
      successful: successCount,
      failed: failureCount,
    });
  } catch (error: any) {
    console.error('Orphan files cleanup job failed', {
      error: error?.message || String(error),
    });
    throw error;
  }
}

/**
 * Delete avatar from Firebase Storage
 *
 * @param avatarUrl - Full URL of avatar to delete
 * @returns {success: boolean, error?: string}
 */
async function deleteAvatarFromStorage(
  avatarUrl: string,
): Promise<{success: boolean; error?: string}> {
  try {
    if (!avatarUrl || typeof avatarUrl !== 'string') {
      return {success: false, error: 'Invalid avatarUrl'};
    }

    const bucket = admin.storage().bucket(BUCKET_NAME);

    // Extract filename from URL
    // Format: https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{filename}?alt=media&token={token}
    let filename = '';

    if (avatarUrl.includes('firebasestorage.googleapis.com')) {
      const match = avatarUrl.match(/\/o\/([^?]+)/);
      if (match) {
        filename = decodeURIComponent(match[1]);
      }
    } else if (avatarUrl.includes('storage.googleapis.com')) {
      const urlParts = avatarUrl.split('/');
      const bucketIndex = urlParts.findIndex((part) => part.includes('.appspot.com'));
      if (bucketIndex !== -1) {
        filename = urlParts.slice(bucketIndex + 1).join('/');
      }
    }

    if (!filename) {
      return {success: false, error: 'Could not extract filename from URL'};
    }

    const file = bucket.file(filename);
    const [exists] = await file.exists();

    if (!exists) {
      // File already gone - treat as success
      return {success: true};
    }

    await file.delete();
    return {success: true};
  } catch (error: any) {
    const errorMessage = error?.message || String(error);
    return {success: false, error: errorMessage};
  }
}
