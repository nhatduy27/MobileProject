/**
 * Seed Test Users
 * 
 * Creates 3 test users with different roles for testing
 * All users are pre-verified (emailVerified: true)
 * 
 * Usage:
 *   node seed-test-users.js
 */

const admin = require('firebase-admin');
const serviceAccount = require('../../service-account.json');

// Initialize Firebase Admin
if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

const firestore = admin.firestore();

const TEST_USERS = [
  {
    email: 'customer@test.com',
    password: 'Test123!',
    displayName: 'Test Customer',
    role: 'CUSTOMER',
    phone: '+84901000001',
  },
  {
    email: 'owner@test.com', 
    password: 'Test123!',
    displayName: 'Test Owner',
    role: 'OWNER',
    phone: '+84901000002',
  },
  {
    email: 'shipper@test.com',
    password: 'Test123!',
    displayName: 'Test Shipper',
    role: 'SHIPPER',
    phone: '+84901000003',
  }
];

async function seedUser(userData) {
  const { email, password, displayName, role, phone } = userData;
  
  console.log(`\n📝 Processing: ${email} (${role})`);
  
  try {
    // Check if user exists
    let uid;
    try {
      const existingUser = await admin.auth().getUserByEmail(email);
      uid = existingUser.uid;
      console.log(`   ⚠️  User exists: ${uid}`);
      
      // Update email verified if not already
      if (!existingUser.emailVerified) {
        await admin.auth().updateUser(uid, { emailVerified: true });
        console.log(`   ✅ Email verified`);
      }
    } catch (err) {
      if (err.code === 'auth/user-not-found') {
        // Create new user
        const newUser = await admin.auth().createUser({
          email,
          password,
          displayName,
          emailVerified: true, // Pre-verified
        });
        uid = newUser.uid;
        console.log(`   ✅ Created Auth user: ${uid}`);
      } else {
        throw err;
      }
    }
    
    // Create/update Firestore document
    const userDoc = {
      id: uid,
      email,
      displayName,
      phone,
      role,
      status: 'ACTIVE',
      emailVerified: true,
      settings: {
        notifications: {
          orderUpdates: true,
          promotions: true,
          email: true,
          push: true,
        },
        language: 'vi',
        currency: 'VND',
      },
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    };
    
    await firestore.collection('users').doc(uid).set(userDoc, { merge: true });
    console.log(`   ✅ Firestore doc updated`);
    
    return { email, password, role, uid, success: true };
  } catch (error) {
    console.error(`   ❌ Error: ${error.message}`);
    return { email, role, success: false, error: error.message };
  }
}

async function main() {
  console.log('🌱 Seeding Test Users...\n');
  console.log('='.repeat(60));
  
  const results = [];
  for (const user of TEST_USERS) {
    const result = await seedUser(user);
    results.push(result);
  }
  
  console.log('\n' + '='.repeat(60));
  console.log('📋 SUMMARY - Test Credentials');
  console.log('='.repeat(60));
  
  console.log('\n| Email                | Password  | Role     |');
  console.log('|----------------------|-----------|----------|');
  for (const user of TEST_USERS) {
    console.log(`| ${user.email.padEnd(20)} | ${user.password.padEnd(9)} | ${user.role.padEnd(8)} |`);
  }
  
  console.log('\n' + '='.repeat(60));
  console.log('✅ All users are pre-verified (emailVerified: true)');
  console.log('💡 Use these credentials with POST /api/auth/login');
  console.log('='.repeat(60) + '\n');
  
  process.exit(0);
}

main().catch(console.error);
