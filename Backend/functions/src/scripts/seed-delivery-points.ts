/**
 * Seed Delivery Points Script
 *
 * Seeds 25 KTX buildings (A1-A5, B1-B5, C1-C6, D2-D6, E1-E4) to deliveryPoints collection.
 * Safe to re-run (upsert mode).
 *
 * Usage:
 *   cd Backend/functions
 *   npm run seed:delivery-points
 */

import * as admin from 'firebase-admin';
import * as path from 'path';

// Initialize Firebase Admin
const serviceAccountPath = path.join(__dirname, '../../service-account.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccountPath),
});

const firestore = admin.firestore();

/**
 * KTX Buildings with approximate coordinates
 * (These are example coordinates - replace with real values if available)
 */
const DELIVERY_POINTS = [
  // Block A (5 buildings)
  {
    buildingCode: 'A1',
    block: 'A',
    number: 1,
    latitude: 10.773589,
    longitude: 106.659924,
    address: 'Tòa A1, Ký túc xá ĐHQG TP.HCM, Khu A, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu A',
  },
  {
    buildingCode: 'A2',
    block: 'A',
    number: 2,
    latitude: 10.773712,
    longitude: 106.660134,
    address: 'Tòa A2, Ký túc xá ĐHQG TP.HCM, Khu A, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu A',
  },
  {
    buildingCode: 'A3',
    block: 'A',
    number: 3,
    latitude: 10.773835,
    longitude: 106.660344,
    address: 'Tòa A3, Ký túc xá ĐHQG TP.HCM, Khu A, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu A',
  },
  {
    buildingCode: 'A4',
    block: 'A',
    number: 4,
    latitude: 10.773958,
    longitude: 106.660554,
    address: 'Tòa A4, Ký túc xá ĐHQG TP.HCM, Khu A, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu A',
  },
  {
    buildingCode: 'A5',
    block: 'A',
    number: 5,
    latitude: 10.774081,
    longitude: 106.660764,
    address: 'Tòa A5, Ký túc xá ĐHQG TP.HCM, Khu A, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu A',
  },

  // Block B (5 buildings)
  {
    buildingCode: 'B1',
    block: 'B',
    number: 1,
    latitude: 10.772589,
    longitude: 106.659924,
    address: 'Tòa B1, Ký túc xá ĐHQG TP.HCM, Khu B, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu B',
  },
  {
    buildingCode: 'B2',
    block: 'B',
    number: 2,
    latitude: 10.772712,
    longitude: 106.660134,
    address: 'Tòa B2, Ký túc xá ĐHQG TP.HCM, Khu B, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu B',
  },
  {
    buildingCode: 'B3',
    block: 'B',
    number: 3,
    latitude: 10.772835,
    longitude: 106.660344,
    address: 'Tòa B3, Ký túc xá ĐHQG TP.HCM, Khu B, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu B',
  },
  {
    buildingCode: 'B4',
    block: 'B',
    number: 4,
    latitude: 10.772958,
    longitude: 106.660554,
    address: 'Tòa B4, Ký túc xá ĐHQG TP.HCM, Khu B, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu B',
  },
  {
    buildingCode: 'B5',
    block: 'B',
    number: 5,
    latitude: 10.773081,
    longitude: 106.660764,
    address: 'Tòa B5, Ký túc xá ĐHQG TP.HCM, Khu B, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu B',
  },

  // Block C (6 buildings)
  {
    buildingCode: 'C1',
    block: 'C',
    number: 1,
    latitude: 10.771589,
    longitude: 106.659924,
    address: 'Tòa C1, Ký túc xá ĐHQG TP.HCM, Khu C, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu C',
  },
  {
    buildingCode: 'C2',
    block: 'C',
    number: 2,
    latitude: 10.771712,
    longitude: 106.660134,
    address: 'Tòa C2, Ký túc xá ĐHQG TP.HCM, Khu C, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu C',
  },
  {
    buildingCode: 'C3',
    block: 'C',
    number: 3,
    latitude: 10.771835,
    longitude: 106.660344,
    address: 'Tòa C3, Ký túc xá ĐHQG TP.HCM, Khu C, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu C',
  },
  {
    buildingCode: 'C4',
    block: 'C',
    number: 4,
    latitude: 10.771958,
    longitude: 106.660554,
    address: 'Tòa C4, Ký túc xá ĐHQG TP.HCM, Khu C, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu C',
  },
  {
    buildingCode: 'C5',
    block: 'C',
    number: 5,
    latitude: 10.772081,
    longitude: 106.660764,
    address: 'Tòa C5, Ký túc xá ĐHQG TP.HCM, Khu C, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu C',
  },
  {
    buildingCode: 'C6',
    block: 'C',
    number: 6,
    latitude: 10.772204,
    longitude: 106.660974,
    address: 'Tòa C6, Ký túc xá ĐHQG TP.HCM, Khu C, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu C',
  },

  // Block D (5 buildings: D2-D6, no D1)
  {
    buildingCode: 'D2',
    block: 'D',
    number: 2,
    latitude: 10.770712,
    longitude: 106.660134,
    address: 'Tòa D2, Ký túc xá ĐHQG TP.HCM, Khu D, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu D',
  },
  {
    buildingCode: 'D3',
    block: 'D',
    number: 3,
    latitude: 10.770835,
    longitude: 106.660344,
    address: 'Tòa D3, Ký túc xá ĐHQG TP.HCM, Khu D, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu D',
  },
  {
    buildingCode: 'D4',
    block: 'D',
    number: 4,
    latitude: 10.770958,
    longitude: 106.660554,
    address: 'Tòa D4, Ký túc xá ĐHQG TP.HCM, Khu D, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu D',
  },
  {
    buildingCode: 'D5',
    block: 'D',
    number: 5,
    latitude: 10.771081,
    longitude: 106.660764,
    address: 'Tòa D5, Ký túc xá ĐHQG TP.HCM, Khu D, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu D',
  },
  {
    buildingCode: 'D6',
    block: 'D',
    number: 6,
    latitude: 10.771204,
    longitude: 106.660974,
    address: 'Tòa D6, Ký túc xá ĐHQG TP.HCM, Khu D, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu D',
  },

  // Block E (4 buildings)
  {
    buildingCode: 'E1',
    block: 'E',
    number: 1,
    latitude: 10.769589,
    longitude: 106.659924,
    address: 'Tòa E1, Ký túc xá ĐHQG TP.HCM, Khu E, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu E',
  },
  {
    buildingCode: 'E2',
    block: 'E',
    number: 2,
    latitude: 10.769712,
    longitude: 106.660134,
    address: 'Tòa E2, Ký túc xá ĐHQG TP.HCM, Khu E, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu E',
  },
  {
    buildingCode: 'E3',
    block: 'E',
    number: 3,
    latitude: 10.769835,
    longitude: 106.660344,
    address: 'Tòa E3, Ký túc xá ĐHQG TP.HCM, Khu E, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu E',
  },
  {
    buildingCode: 'E4',
    block: 'E',
    number: 4,
    latitude: 10.769958,
    longitude: 106.660554,
    address: 'Tòa E4, Ký túc xá ĐHQG TP.HCM, Khu E, Đông Hòa, Dĩ An, Bình Dương',
    area: 'Khu E',
  },
];

/**
 * Main seed function
 */
async function seedDeliveryPoints() {
  console.log('🌱 Seeding delivery points...');
  console.log(`Total buildings to seed: ${DELIVERY_POINTS.length}`);

  const collectionRef = firestore.collection('deliveryPoints');

  let created = 0;
  let updated = 0;

  for (const point of DELIVERY_POINTS) {
    const docRef = collectionRef.doc(point.buildingCode);
    const doc = await docRef.get();

    const timestamp = admin.firestore.FieldValue.serverTimestamp();

    if (doc.exists) {
      // Update existing
      await docRef.update({
        ...point,
        id: point.buildingCode,
        updatedAt: timestamp,
      });
      updated++;
      console.log(`✅ Updated: ${point.buildingCode} - ${point.address}`);
    } else {
      // Create new
      await docRef.set({
        ...point,
        id: point.buildingCode,
        createdAt: timestamp,
        updatedAt: timestamp,
      });
      created++;
      console.log(`🆕 Created: ${point.buildingCode} - ${point.address}`);
    }
  }

  console.log('\n✅ Seeding complete!');
  console.log(`   Created: ${created}`);
  console.log(`   Updated: ${updated}`);
  console.log(`   Total:   ${DELIVERY_POINTS.length}`);
}

// Run seed
seedDeliveryPoints()
  .then(() => {
    console.log('\n🎉 All done!');
    process.exit(0);
  })
  .catch((error) => {
    console.error('❌ Error seeding delivery points:', error);
    process.exit(1);
  });
