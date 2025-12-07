/**
 * Order Service
 *
 * Business logic layer cho Order operations
 */

import {CallableRequest} from "firebase-functions/v2/https";
import {
  PlaceOrderRequest,
  PlaceOrderResponse,
  CancelOrderRequest,
  CancelOrderResponse,
  OrderStatus,
} from "../models/order.model";
import {orderRepository} from "../repositories/order.repository";
import {restaurantRepository} from "../repositories/restaurant.repository";
import {promotionRepository} from "../repositories/promotion.repository";

// Type alias cho auth context
type CallableRequestContext = CallableRequest["auth"];

/**
 * Business logic cho Order.
 */
export class OrderService {
  /**
   * Place a new order
   * @param {PlaceOrderRequest} data Order payload
   * @param {CallableRequestContext} context Auth context
   */
  async placeOrder(
    data: PlaceOrderRequest,
    context: CallableRequestContext
  ): Promise<PlaceOrderResponse> {
    void orderRepository;
    void restaurantRepository;
    void promotionRepository;

    // TODO: Validate user is authenticated
    if (!context?.uid) {
      throw new Error("Unauthenticated");
    }

    const userId = context.uid;
    void userId;

    // TODO: Validate request data
    //  - restaurantId exists and is open
    //  - items array is not empty
    //  - all menuItemIds exist and are available
    //  - quantities are positive integers

    // TODO: Calculate total amount
    //  - Fetch menu items and prices
    //  - Calculate subtotal
    //  - Apply promotion if promotionCode provided
    //  - Add delivery fee, taxes, etc.

    // TODO: Check stock/availability for all items

    // TODO: Apply promotion code if provided
    //  - Validate promotion code
    //  - Check validity period
    //  - Check usage limit
    //  - Calculate discount

    // TODO: Create order in Firestore using transaction
    //  - Create order document
    //  - Decrement stock for items (if applicable)
    //  - Increment promotion usage count

    // TODO: Send notifications
    //  - Notify restaurant owner
    //  - Notify customer (order confirmation)

    // STUB: Return mock response
    return {
      orderId: "mock_order_id",
      status: "PENDING" as OrderStatus,
      totalAmount: 0,
      estimatedDeliveryTime: new Date(
        Date.now() + 30 * 60 * 1000
      ).toISOString(),
    };
  }

  /**
   * Cancel an existing order
   * @param {CancelOrderRequest} data Cancel payload
   * @param {CallableRequestContext} context Auth context
   */
  async cancelOrder(
    data: CancelOrderRequest,
    context: CallableRequestContext
  ): Promise<CancelOrderResponse> {
    // TODO: Validate user is authenticated
    if (!context?.uid) {
      throw new Error("Unauthenticated");
    }

    const userId = context.uid;
    void userId;

    // TODO: Validate request data
    //  - orderId is provided
    //  - reason is optional but should be validated

    // TODO: Get order and verify ownership
    //  - User must own the order OR be an admin

    // TODO: Check order status
    //  - Only PENDING or CONFIRMED orders can be cancelled
    //  - Cannot cancel DELIVERING, COMPLETED, or already CANCELLED orders

    // TODO: Cancel order
    //  - Update order status to CANCELLED
    //  - Restore stock for items
    //  - Restore promotion usage count if applicable
    //  - Process refund if payment was made

    // TODO: Send notifications
    //  - Notify restaurant owner
    //  - Notify customer (cancellation confirmation)
    //  - Notify shipper if order was being delivered

    // STUB: Return mock response
    return {
      orderId: data.orderId,
      status: "CANCELLED" as OrderStatus,
      refundAmount: 0,
    };
  }
}

// Singleton instance with dependencies injected
export const orderService = new OrderService();
