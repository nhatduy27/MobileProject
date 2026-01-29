const admin = require('firebase-admin');
const serviceAccount = require('./service-account.json');

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });
}

const db = admin.firestore();

(async () => {
  try {
    console.log('Checking shipperTrips collection...\n');

    const snapshot = await db.collection('shipperTrips').limit(5).get();

    if (snapshot.empty) {
      console.log('❌ No trips found in shipperTrips collection');
      return;
    }

    console.log(`✅ Found ${snapshot.size} trips:\n`);

    snapshot.docs.forEach((doc) => {
      const trip = doc.data();
      console.log(`Trip ID: ${doc.id}`);
      console.log(`Shipper: ${trip.shipperId}`);
      console.log(`Status: ${trip.status}`);
      console.log(`Buildings: ${trip.totalBuildings}, Orders: ${trip.totalOrders}`);
      console.log(`Distance: ${trip.totalDistance}m, Duration: ${trip.totalDuration}s`);
      console.log(`Created: ${trip.createdAt?.toDate?.() || trip.createdAt}`);
      console.log('---\n');
    });
  } catch (error) {
    console.error('Error:', error.message);
  } finally {
    process.exit(0);
  }
})();
