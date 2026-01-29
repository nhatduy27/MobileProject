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
    const orderId = 'jSN11A8EteKVdYbdvEpn';
    const orderDoc = await db.collection('orders').doc(orderId).get();

    if (!orderDoc.exists) {
      console.log('❌ Order not found:', orderId);
      return;
    }

    const order = orderDoc.data();
    console.log('✅ Order found:', orderId);
    console.log('Status:', order.status);
    console.log('DeliveryAddress:', JSON.stringify(order.deliveryAddress, null, 2));
    console.log('Building:', order.deliveryAddress?.building);

    // Check if delivery point exists
    if (order.deliveryAddress?.building) {
      const building = order.deliveryAddress.building.trim().toUpperCase();
      console.log('Looking for delivery point with building code:', building);
      const dpDoc = await db.collection('deliveryPoints').doc(building).get();

      if (dpDoc.exists) {
        console.log('✅ Delivery point found for building:', building);
        const dpData = dpDoc.data();
        console.log('DeliveryPoint:', JSON.stringify(dpData, null, 2));
      } else {
        console.log('❌ Delivery point NOT found for building:', building);

        // Check all delivery points
        console.log('\nChecking all delivery points...');
        const allDPs = await db.collection('deliveryPoints').limit(5).get();
        console.log('Sample delivery points:');
        allDPs.docs.forEach((doc) => {
          console.log(`  - ${doc.id}:`, doc.data().buildingCode);
        });
      }
    } else {
      console.log('❌ Order has no building in deliveryAddress');
    }
  } catch (error) {
    console.error('Error:', error.message);
  } finally {
    process.exit(0);
  }
})();
