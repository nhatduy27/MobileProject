#!/usr/bin/env ts-node
/**
 * Set Owner Role by Email
 *
 * Usage: npx ts-node scripts/set-owner-by-email.ts <email>
 * Example: npx ts-node scripts/set-owner-by-email.ts owner@example.com
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

async function setOwnerByEmail(email: string) {
  console.log(`🔐 Setting OWNER role for: ${email}\n`);

  try {
    // 1. Find user by email
    console.log('  🔍 Looking up user...');
    const userRecord = await auth.getUserByEmail(email);
    const uid = userRecord.uid;
    console.log(`  ✅ Found user UID: ${uid}`);

    // 2. Check current claims
    const currentClaims = userRecord.customClaims || {};
    if (currentClaims.role === 'OWNER') {
      console.log('\n⚠️  User is already an OWNER!');
      process.exit(0);
    }

    // 3. Set custom claims
    console.log('  🔑 Setting custom claims...');
    await auth.setCustomUserClaims(uid, {
      ...currentClaims,
      role: 'OWNER',
    });
    console.log('  ✅ Set role: OWNER');

    // 4. Update Firestore
    console.log('  📄 Updating Firestore...');
    const now = Timestamp.now();
    await db.collection('users').doc(uid).update({
      role: 'OWNER',
      updatedAt: now,
    });
    console.log('  ✅ Updated Firestore');

    console.log('\n🎉 Successfully set OWNER role!\n');
    console.log('================================');
    console.log(`  Email: ${userRecord.email}`);
    console.log(`  UID: ${uid}`);
    console.log(`  Role: OWNER`);
    console.log('================================\n');

    console.log('⚠️  IMPORTANT: User must get new token for changes to take effect.\n');

  } catch (error: any) {
    if (error.code === 'auth/user-not-found') {
      console.error(`\n❌ User with email "${email}" not found`);
      console.error('   Please ensure the user has registered first.\n');
    } else {
      console.error('\n❌ Error:', error.message, '\n');
    }
    throw error;
  }
}

// Parse args and run
const email = process.argv[2];
if (!email) {
  console.error('❌ Usage: npx ts-node scripts/set-owner-by-email.ts <email>');
  console.error('');
  console.error('Example:');
  console.error('  npx ts-node scripts/set-owner-by-email.ts owner@example.com');
  console.error('');
  process.exit(1);
}

setOwnerByEmail(email)
  .then(() => {
    console.log('✅ Done!');
    process.exit(0);
  })
  .catch(() => {
    process.exit(1);
  });
