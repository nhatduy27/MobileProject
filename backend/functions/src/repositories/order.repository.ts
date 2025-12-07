/**
 * Order Repository
 *
 * Data access layer cho Orders collection
 */

import {getFirestore} from "firebase-admin/firestore";
import {Order, PlaceOrderRequest, OrderStatus} from "../models/order.model";

/**
 * Firestore data access for Orders collection.
 */
export class OrderRepository {
  private db = getFirestore();
  private collection = "orders";

  /**
   * Tạo order mới trong Firestore
   * TODO: Implement full logic với transaction, validation, etc.
   * @param {PlaceOrderRequest} _data Order payload
   * @param {string} _userId ID của user tạo order
   */
  async createOrder(_data: PlaceOrderRequest, _userId: string): Promise<Order> {
    void _data;
    void _userId;
    // TODO: Validate data
    // TODO: Calculate totalAmount from items
    // TODO: Use transaction to ensure atomicity
    // TODO: Update stock/availability

    throw new Error("createOrder: Not implemented");
  }

  /**
   * Lấy order theo ID
  * @param {string} orderId ID của order
   */
  async getOrderById(orderId: string): Promise<Order | null> {
    // TODO: Implement Firestore get
    const doc = await this.db.collection(this.collection).doc(orderId).get();

    if (!doc.exists) {
      return null;
    }

    return {
      id: doc.id,
      ...doc.data(),
    } as Order;
  }

  /**
  * Cancel order
  * TODO: Implement với business rules (only PENDING/CONFIRMED can be
  * cancelled)
   * @param {string} _orderId ID của order
   * @param {string} _userId ID của user
   */
  async cancelOrder(_orderId: string, _userId: string): Promise<void> {
    void _orderId;
    void _userId;
    // TODO: Validate user owns this order
    // TODO: Check order status
    // TODO: Update order status to CANCELLED
    // TODO: Handle refund logic
    // TODO: Restore stock

    throw new Error("cancelOrder: Not implemented");
  }

  /**
   * Update order status
   * @param {string} orderId ID của order
   * @param {OrderStatus} status trạng thái mới
   */
  async updateOrderStatus(
    orderId: string,
    status: OrderStatus
  ): Promise<void> {
    await this.db.collection(this.collection).doc(orderId).update({
      status,
      updatedAt: new Date().toISOString(),
    });
  }

  /**
   * Get orders by user
  * @param {string} userId ID của user
   */
  async getOrdersByUser(userId: string): Promise<Order[]> {
    // TODO: Implement with pagination
    const snapshot = await this.db
      .collection(this.collection)
      .where("userId", "==", userId)
      .orderBy("createdAt", "desc")
      .limit(50)
      .get();

    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    } as Order));
  }
}

// Singleton instance
export const orderRepository = new OrderRepository();
