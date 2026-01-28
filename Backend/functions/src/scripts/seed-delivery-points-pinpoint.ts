/**
 * Seed Delivery Points Script (GPS Module)
 *
 * Đọc dữ liệu từ PinPoint.txt và UPSERT vào Firestore collection deliveryPoints.
 * Tất cả 25 tòa nhà KTX: A1-A5, B1-B5, C1-C6, D2-D6, E1-E4
 *
 * Tính năng:
 * - Dry-run mode: Xem các thay đổi mà không commit (DRY_RUN=true)
 * - Verify mode: Kiểm tra tổng số documents đã seed
 * - Safe to re-run: UPSERT logic (update nếu tồn tại, create nếu không)
 *
 * Usage:
 *   # Bình thường (seed vào Firestore)
 *   npm run seed:delivery-points
 *
 *   # Dry-run (xem kết quả mà không lưu)
 *   DRY_RUN=true npm run seed:delivery-points
 *
 *   # Verify sau khi seed (kiểm tra)
 *   VERIFY_ONLY=true npm run seed:delivery-points
 */

import * as admin from 'firebase-admin';
import * as path from 'path';
import * as fs from 'fs';

// === PARSE PINPOINT FILE ===

interface DeliveryPointData {
  buildingCode: string;
  name: string; // e.g., "Tòa A1"
  note: string; // Vietnamese description
  location: {
    lat: number;
    lng: number;
  };
  active: boolean;
}

function parsePinPointFile(filePath: string): DeliveryPointData[] {
  const content = fs.readFileSync(filePath, 'utf-8');
  const lines = content.split('\n');

  const points: DeliveryPointData[] = [];

  for (const line of lines) {
    const trimmed = line.trim();

    // Skip empty lines and comments
    if (!trimmed || trimmed.startsWith('#')) {
      continue;
    }

    // Parse: buildingCode | lat | lng | note
    const parts = trimmed.split('|').map((p) => p.trim());

    if (parts.length < 4) {
      console.warn(`⚠️  Skip invalid line: ${line}`);
      continue;
    }

    const buildingCode = parts[0];
    const lat = parseFloat(parts[1]);
    const lng = parseFloat(parts[2]);
    const note = parts[3];

    // Validate
    if (!buildingCode || isNaN(lat) || isNaN(lng)) {
      console.warn(`⚠️  Skip invalid data: ${line}`);
      continue;
    }

    const point: DeliveryPointData = {
      buildingCode,
      name: `Tòa ${buildingCode}`, // e.g., "Tòa A1"
      note,
      location: {
        lat,
        lng,
      },
      active: true,
    };

    points.push(point);
  }

  return points;
}

// === EXPECTED BUILDING CODES ===

const EXPECTED_CODES = [
  // Block A (5)
  'A1', 'A2', 'A3', 'A4', 'A5',
  // Block B (5)
  'B1', 'B2', 'B3', 'B4', 'B5',
  // Block C (6)
  'C1', 'C2', 'C3', 'C4', 'C5', 'C6',
  // Block D (5) - Note: No D1
  'D2', 'D3', 'D4', 'D5', 'D6',
  // Block E (4)
  'E1', 'E2', 'E3', 'E4',
];

// === MAIN SEED FUNCTION ===

async function seedDeliveryPoints() {
  const isDryRun = process.env.DRY_RUN === 'true';
  const verifyOnly = process.env.VERIFY_ONLY === 'true';

  console.log('═══════════════════════════════════════════════════════════════');
  console.log('🌍 GPS Module - Seed Delivery Points');
  console.log('═══════════════════════════════════════════════════════════════');

  // Initialize Firebase Admin
  console.log('\n📍 Initializing Firebase Admin...');
  const serviceAccountPath = path.join(__dirname, '../../service-account.json');

  if (!fs.existsSync(serviceAccountPath)) {
    console.error(`❌ Service account file not found: ${serviceAccountPath}`);
    process.exit(1);
  }

  if (!admin.apps.length) {
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccountPath),
    });
  }

  const firestore = admin.firestore();
  console.log('✓ Firebase Admin initialized');

  // Parse PinPoint file
  console.log('\n📄 Reading PinPoint.txt...');
  const pinPointPath = path.join(__dirname, '../../PinPoint.txt');

  if (!fs.existsSync(pinPointPath)) {
    console.error(`❌ PinPoint.txt not found: ${pinPointPath}`);
    process.exit(1);
  }

  const points = parsePinPointFile(pinPointPath);
  console.log(`✓ Parsed ${points.length} points from PinPoint.txt`);

  // Verify data
  if (verifyOnly) {
    console.log('\n🔍 VERIFY MODE: Checking existing data...');
    await verifyDeliveryPoints(firestore);
    return;
  }

  // Show plan
  console.log('\n📋 Seed Plan:');
  console.log(`   • Building codes: ${points.length}`);
  console.log(`   • Mode: ${isDryRun ? 'DRY-RUN (no writes)' : 'COMMIT'}`);
  console.log(`   • Expected total: ${EXPECTED_CODES.length}`);

  if (isDryRun) {
    console.log('\n⚠️  DRY-RUN MODE - No changes will be saved\n');
    console.log('Sample upserts that would be performed:');
    points.slice(0, 3).forEach((p) => {
      console.log(`   • ${p.buildingCode}: ${p.note}`);
    });
    console.log(`   ... and ${points.length - 3} more`);
    console.log('\nTo confirm, run without DRY_RUN=true');
    return;
  }

  // Perform upserts
  console.log('\n💾 Upserting delivery points...');

  let created = 0;
  let updated = 0;
  const errors: string[] = [];

  for (const point of points) {
    try {
      const docRef = firestore.collection('deliveryPoints').doc(point.buildingCode);
      const doc = await docRef.get();

      const timestamp = admin.firestore.FieldValue.serverTimestamp();

      if (doc.exists) {
        // Update
        await docRef.update({
          name: point.name,
          note: point.note,
          location: point.location,
          active: point.active,
          updatedAt: timestamp,
        });
        updated++;
      } else {
        // Create
        await docRef.set({
          id: point.buildingCode,
          buildingCode: point.buildingCode,
          name: point.name,
          note: point.note,
          location: point.location,
          active: point.active,
          createdAt: timestamp,
          updatedAt: timestamp,
        });
        created++;
      }

      process.stdout.write('.');
    } catch (error: any) {
      errors.push(`${point.buildingCode}: ${error.message}`);
      process.stdout.write('✗');
    }
  }

  console.log('\n\n✅ Upsert Complete!\n');
  console.log(`📊 Results:`);
  console.log(`   • Created: ${created}`);
  console.log(`   • Updated: ${updated}`);
  console.log(`   • Total:   ${created + updated}/${points.length}`);

  if (errors.length > 0) {
    console.log(`\n⚠️  Errors (${errors.length}):`);
    errors.forEach((err) => console.log(`   • ${err}`));
  }

  // Verify after seed
  console.log('\n🔍 Verifying seeded data...');
  await verifyDeliveryPoints(firestore);

  console.log('\n═══════════════════════════════════════════════════════════════');
  console.log('✨ Seeding complete!');
  console.log('═══════════════════════════════════════════════════════════════\n');
}

// === VERIFY FUNCTION ===

async function verifyDeliveryPoints(firestore: admin.firestore.Firestore) {
  // Get all documents
  const snapshot = await firestore.collection('deliveryPoints').get();
  const docs = snapshot.docs;

  console.log(`\n📈 Verification Results:\n`);
  console.log(`   • Documents found: ${docs.length}`);
  console.log(`   • Expected codes:  ${EXPECTED_CODES.length}`);

  // Check each expected code
  const found = new Set<string>();
  const missing: string[] = [];

  EXPECTED_CODES.forEach((code) => {
    const doc = docs.find((d) => d.id === code);
    if (doc) {
      found.add(code);
    } else {
      missing.push(code);
    }
  });

  if (missing.length > 0) {
    console.log(`\n   ⚠️  Missing codes (${missing.length}):`);
    missing.forEach((code) => console.log(`      • ${code}`));
  } else {
    console.log(`\n   ✅ All ${EXPECTED_CODES.length} expected codes found!`);
  }

  // Show sample documents
  if (docs.length > 0) {
    console.log(`\n   📋 Sample documents:`);
    docs.slice(0, 3).forEach((doc) => {
      const data = doc.data();
      console.log(
        `      • ${doc.id}: "${data.name}" (${data.location?.lat?.toFixed(6) || '?'}, ${data.location?.lng?.toFixed(6) || '?'})`,
      );
    });
    if (docs.length > 3) {
      console.log(`      ... and ${docs.length - 3} more`);
    }
  }

  console.log();
}

// === RUN ===

seedDeliveryPoints()
  .then(() => {
    process.exit(0);
  })
  .catch((error) => {
    console.error('\n❌ Error during seeding:');
    console.error(error);
    process.exit(1);
  });
