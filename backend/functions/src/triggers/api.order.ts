/**
 * Order API Triggers
 *
 * Callable functions cho Order operations
 */

import {onCall} from "firebase-functions/v2/https";
import {orderService} from "../services/order.service";
import {
  PlaceOrderRequest,
  PlaceOrderResponse,
  CancelOrderRequest,
  CancelOrderResponse,
} from "../models/order.model";
import {toHttpsError, logError} from "../utils/error.utils";
import {isNotEmpty, isNonEmptyArray} from "../utils/validation.utils";

/**
 * Place Order - Callable Function
 *
 * Client gọi function này để đặt hàng
 */
export const placeOrder = onCall<
  PlaceOrderRequest,
  Promise<PlaceOrderResponse>
>(async (request) => {
  try {
    const {data, auth} = request;

    // Basic validation
    if (!isNotEmpty(data.restaurantId)) {
      throw new Error("restaurantId is required");
    }

    if (!isNonEmptyArray(data.items)) {
      throw new Error("items array cannot be empty");
    }

    // TODO: More detailed validation
    //  - Validate each item structure
    //  - Validate quantities are positive integers
    //  - Validate deliveryAddress format

    // Call service layer
    const result = await orderService.placeOrder(data, auth);

    return result;
  } catch (error) {
    logError("placeOrder", error);
    throw toHttpsError(error);
  }
});

/**
 * Cancel Order - Callable Function
 *
 * Client gọi function này để hủy đơn hàng
 */
export const cancelOrder = onCall<
  CancelOrderRequest,
  Promise<CancelOrderResponse>
>(async (request) => {
  try {
    const {data, auth} = request;

    // Basic validation
    if (!isNotEmpty(data.orderId)) {
      throw new Error("orderId is required");
    }

    // TODO: More detailed validation
    //  - Validate orderId format
    //  - Validate reason if provided

    // Call service layer
    const result = await orderService.cancelOrder(data, auth);

    return result;
  } catch (error) {
    logError("cancelOrder", error);
    throw toHttpsError(error);
  }
});
