/**
 * Seed Test Users Script
 * 
 * Creates test accounts for development and testing
 * 
 * Usage: node scripts/seed-test-accounts.js
 */

const admin = require('firebase-admin');
const path = require('path');

// Initialize Firebase Admin
const serviceAccountPath = path.join(__dirname, '..', '..', 'service-account.json');
const serviceAccount = require(serviceAccountPath);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const auth = admin.auth();
const db = admin.firestore();

const TEST_ACCOUNTS = [
  {
    email: 'customer1@test.com',
    password: 'Test123!',
    displayName: 'Customer One',
    role: 'CUSTOMER',
    phone: '+84901234561',
  },
  {
    email: 'customer2@test.com',
    password: 'Test123!',
    displayName: 'Customer Two',
    role: 'CUSTOMER',
    phone: '+84901234562',
  },
  {
    email: 'owner1@test.com',
    password: 'Test123!',
    displayName: 'Owner One',
    role: 'OWNER',
    phone: '+84901234563',
  },
  {
    email: 'shipper1@test.com',
    password: 'Test123!',
    displayName: 'Shipper One',
    role: 'SHIPPER',
    phone: '+84901234564',
  },
  {
    email: 'admin@ktx.com',
    password: 'Admin123!',
    displayName: 'Admin KTX',
    role: 'ADMIN',
    phone: '+84901234500',
  },
];

async function createUser(userData) {
  const { email, password, displayName, role, phone } = userData;
  
  try {
    // Check if user already exists
    try {
      const existingUser = await auth.getUserByEmail(email);
      console.log(`⚠️  User ${email} already exists (uid: ${existingUser.uid})`);
      
      // Update Firestore document if needed
      await db.doc(`users/${existingUser.uid}`).set({
        email,
        displayName,
        role,
        phone,
        status: 'ACTIVE',
        emailVerified: true,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      }, { merge: true });
      
      // Set custom claims for role
      await auth.setCustomUserClaims(existingUser.uid, { role });
      
      return existingUser.uid;
    } catch (e) {
      // User doesn't exist, create new
    }

    // Create Firebase Auth user
    const userRecord = await auth.createUser({
      email,
      password,
      displayName,
      phoneNumber: phone,
      emailVerified: true,
    });

    console.log(`✅ Created Auth user: ${email} (uid: ${userRecord.uid})`);

    // Set custom claims for role
    await auth.setCustomUserClaims(userRecord.uid, { role });

    // Create Firestore document
    await db.doc(`users/${userRecord.uid}`).set({
      email,
      displayName,
      role,
      phone,
      status: 'ACTIVE',
      emailVerified: true,
      avatarUrl: null,
      fcmTokens: [],
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    console.log(`✅ Created Firestore doc: users/${userRecord.uid}`);

    // Create settings subcollection
    await db.doc(`users/${userRecord.uid}/settings/preferences`).set({
      notificationsEnabled: true,
      language: 'vi',
    });

    return userRecord.uid;
  } catch (error) {
    console.error(`❌ Error creating ${email}:`, error.message);
    return null;
  }
}

async function main() {
  console.log('🚀 Seeding test accounts...\n');
  console.log('='.repeat(50));

  const results = [];
  
  for (const account of TEST_ACCOUNTS) {
    const uid = await createUser(account);
    results.push({
      email: account.email,
      role: account.role,
      uid,
      status: uid ? '✅' : '❌',
    });
    console.log('-'.repeat(50));
  }

  console.log('\n' + '='.repeat(50));
  console.log('📊 Summary:\n');
  console.table(results);

  console.log('\n🔑 Test Credentials:');
  console.log('─'.repeat(50));
  for (const account of TEST_ACCOUNTS) {
    console.log(`${account.role.padEnd(10)} | ${account.email.padEnd(25)} | ${account.password}`);
  }
  console.log('─'.repeat(50));

  process.exit(0);
}

main().catch((error) => {
  console.error('Fatal error:', error);
  process.exit(1);
});
