/**
 * Script để migrate data từ collection 'walletLedger' sang 'wallet_ledger'
 *
 * Chạy script:
 *   node scripts/migrate-wallet-ledger.js
 */

const admin = require('firebase-admin');
const path = require('path');

// Initialize Firebase Admin
const serviceAccountPath = path.join(__dirname, '../service-account.json');
admin.initializeApp({
  credential: admin.credential.cert(require(serviceAccountPath)),
});

const db = admin.firestore();

async function migrateLedger() {
  console.log('🚀 Starting migration from walletLedger to wallet_ledger...\n');

  const sourceCollection = 'walletLedger';
  const targetCollection = 'wallet_ledger';

  try {
    // Get all documents from source collection
    const snapshot = await db.collection(sourceCollection).get();

    if (snapshot.empty) {
      console.log('⚠️  No documents found in walletLedger collection.');
      return;
    }

    console.log(`📦 Found ${snapshot.size} documents to migrate.\n`);

    let successCount = 0;
    let errorCount = 0;

    // Migrate each document
    for (const doc of snapshot.docs) {
      const data = doc.data();
      const docId = doc.id;

      try {
        // Check if document already exists in target
        const existingDoc = await db.collection(targetCollection).doc(docId).get();

        if (existingDoc.exists) {
          console.log(`⏭️  Skipping ${docId} - already exists in wallet_ledger`);
          continue;
        }

        // Copy to target collection with same ID
        await db.collection(targetCollection).doc(docId).set(data);

        console.log(`✅ Migrated: ${docId}`);
        console.log(`   - walletId: ${data.walletId}`);
        console.log(`   - type: ${data.type}`);
        console.log(`   - amount: ${data.amount}`);
        console.log('');

        successCount++;
      } catch (error) {
        console.error(`❌ Error migrating ${docId}:`, error.message);
        errorCount++;
      }
    }

    console.log('\n========================================');
    console.log('📊 Migration Summary:');
    console.log(`   ✅ Success: ${successCount}`);
    console.log(`   ❌ Errors: ${errorCount}`);
    console.log(`   ⏭️  Skipped: ${snapshot.size - successCount - errorCount}`);
    console.log('========================================\n');

    // Ask if user wants to delete source collection
    console.log('💡 Note: Source collection (walletLedger) was NOT deleted.');
    console.log('   Run with --delete flag to delete after migration.\n');
  } catch (error) {
    console.error('❌ Migration failed:', error);
    process.exit(1);
  }
}

async function deleteSourceCollection() {
  console.log('\n🗑️  Deleting source collection (walletLedger)...\n');

  const sourceCollection = 'walletLedger';
  const snapshot = await db.collection(sourceCollection).get();

  if (snapshot.empty) {
    console.log('⚠️  No documents to delete.');
    return;
  }

  const batch = db.batch();
  snapshot.docs.forEach((doc) => {
    batch.delete(doc.ref);
  });

  await batch.commit();
  console.log(`✅ Deleted ${snapshot.size} documents from walletLedger.`);
}

async function main() {
  const args = process.argv.slice(2);
  const shouldDelete = args.includes('--delete');

  await migrateLedger();

  if (shouldDelete) {
    await deleteSourceCollection();
  }

  console.log('🎉 Done!');
  process.exit(0);
}

main();
