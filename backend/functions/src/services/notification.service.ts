/**
 * Notification Service
 *
 * Business logic layer cho Notification operations
 */

import {Order} from "../models/order.model";

/**
 * Business logic for notifications.
 */
export class NotificationService {
  /**
   * Gửi notification khi có order mới
   * @param {Order} order Order data
   */
  async notifyNewOrder(order: Order): Promise<void> {
    // TODO: Get restaurant owner's FCM token from Firestore
    //  - Query users collection where role = SELLER and restaurantId =
    //    order.restaurantId

    // TODO: Get customer's FCM token
    //  - Query users collection by order.userId

    // TODO: Send FCM notification to restaurant owner
    //  - Title: "Đơn hàng mới"
    //  - Body: "Bạn có đơn hàng mới #${order.id}"
    //  - Data: { orderId, type: "NEW_ORDER" }

    // TODO: Send order confirmation to customer
    //  - Title: "Đơn hàng đã được tạo"
    //  - Body: "Đơn hàng #${order.id} của bạn đang được xử lý"

    // TODO: Update badge count in user document

    // TODO: Log notification activity

    console.log(`[STUB] Would send notification for order: ${order.id}`);
  }

  /**
   * Gửi notification khi order status thay đổi
   * @param {string} orderId Order ID
   * @param {string} oldStatus Trạng thái cũ
   * @param {string} newStatus Trạng thái mới
   */
  async notifyOrderStatusChange(
    orderId: string,
    oldStatus: string,
    newStatus: string
  ): Promise<void> {
    // TODO: Implement similar logic for status change notifications

    console.log(
      `[STUB] Order ${orderId} status changed: ${oldStatus} -> ${newStatus}`
    );
  }

  /**
   * Gửi notification khi order bị cancel
   * @param {Order} order Order data
   * @param {string} [reason] Lý do
   */
  async notifyOrderCancelled(order: Order, reason?: string): Promise<void> {
    // TODO: Notify restaurant owner
    // TODO: Notify customer
    // TODO: Notify shipper if applicable

    console.log(
      `[STUB] Order ${order.id} cancelled. Reason: ${reason || "N/A"}`
    );
  }
}

// Singleton instance
export const notificationService = new NotificationService();
