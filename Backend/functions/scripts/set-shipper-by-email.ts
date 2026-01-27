#!/usr/bin/env ts-node
/**
 * Set Shipper Role by Email
 */

import * as admin from 'firebase-admin';
import * as path from 'path';

const serviceAccountPath = path.join(__dirname, '..', '..', 'service-account.json');
const serviceAccount = require(serviceAccountPath);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const auth = admin.auth();
const db = admin.firestore();
const { Timestamp } = admin.firestore;

async function setShipperByEmail(email: string) {
  console.log(`🔐 Setting SHIPPER role for: ${email}\n`);

  try {
    const userRecord = await auth.getUserByEmail(email);
    const uid = userRecord.uid;
    console.log(`  ✅ Found user UID: ${uid}`);

    const currentClaims = userRecord.customClaims || {};
    if (currentClaims.role === 'SHIPPER') {
      console.log('\n⚠️  User is already a SHIPPER!');
      process.exit(0);
    }

    await auth.setCustomUserClaims(uid, {
      ...currentClaims,
      role: 'SHIPPER',
    });
    console.log('  ✅ Set role: SHIPPER');

    const now = Timestamp.now();
    await db.collection('users').doc(uid).update({
      role: 'SHIPPER',
      updatedAt: now,
    });
    console.log('  ✅ Updated Firestore');

    console.log('\n🎉 Successfully set SHIPPER role!\n');

  } catch (error: any) {
    console.error('\n❌ Error:', error.message, '\n');
    throw error;
  }
}

const email = process.argv[2];
if (!email) {
  console.error('❌ Usage: npx ts-node scripts/set-shipper-by-email.ts <email>');
  process.exit(1);
}

setShipperByEmail(email)
  .then(() => {
    console.log('✅ Done!');
    process.exit(0);
  })
  .catch(() => {
    process.exit(1);
  });
