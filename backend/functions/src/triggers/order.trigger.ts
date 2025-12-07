/**
 * Order Triggers
 *
 * Firestore event triggers cho Orders collection
 */

import {onDocumentCreated} from "firebase-functions/v2/firestore";
import {notificationService} from "../services/notification.service";
import {Order} from "../models/order.model";
import {logError} from "../utils/error.utils";

/**
 * On Order Created Trigger
 *
 * Được trigger khi order mới được tạo trong Firestore
 * Gửi notifications tới customer và restaurant owner
 */
export const onOrderCreated = onDocumentCreated(
  "orders/{orderId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;

    const orderId = event.params.orderId;
    try {
      const orderData = snapshot.data() as Order;

      console.log(`New order created: ${orderId}`);

      // TODO: Add additional logic
      //  - Log to analytics
      //  - Update statistics (daily orders count, revenue, etc.)
      //  - Trigger workflow (assign shipper, etc.)

      // Send notifications
      await notificationService.notifyNewOrder({
        ...orderData,
        id: orderId, // Override with document ID
      });

      console.log(`Notifications sent for order: ${orderId}`);
    } catch (error) {
      // Log error nhưng không throw để không block order creation
      logError("onOrderCreated", error);

      // TODO: Implement retry mechanism cho failed notifications
    }
  }
);

/**
 * TODO: Add more order triggers
 *
 * - onOrderUpdated: Handle order status changes
 * - onOrderDeleted: Clean up related data (rare case)
 *
 * Example with v2:
 * import {onDocumentUpdated} from "firebase-functions/v2/firestore";
 *
 * export const onOrderUpdated = onDocumentUpdated(
 *   "orders/{orderId}",
 *   async (event) => {
 *     const before = event.data?.before.data();
 *     const after = event.data?.after.data();
 *
 *     if (before?.status !== after?.status) {
 *       await notificationService.notifyOrderStatusChange(
 *         event.params.orderId,
 *         before.status,
 *         after.status
 *       );
 *     }
 *   }
 * );
 */
