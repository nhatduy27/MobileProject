/**
 * Promotion API Triggers
 *
 * Callable functions cho Promotion operations
 */

import {onCall} from "firebase-functions/v2/https";
import {promotionService} from "../services/promotion.service";
import {
  ApplyPromotionRequest,
  ApplyPromotionResponse,
} from "../models/promotion.model";
import {toHttpsError, logError} from "../utils/error.utils";
import {isNotEmpty, isPositiveNumber} from "../utils/validation.utils";

/**
 * Apply Promotion - Callable Function
 *
 * Client gọi function này để áp dụng mã khuyến mãi
 */
export const applyPromotion = onCall<
  ApplyPromotionRequest,
  Promise<ApplyPromotionResponse>
>(async (request) => {
  try {
    const {data, auth} = request;

    // Basic validation
    if (!isNotEmpty(data.promotionCode)) {
      throw new Error("promotionCode is required");
    }

    if (!isPositiveNumber(data.orderAmount)) {
      throw new Error("orderAmount must be a positive number");
    }

    // TODO: More detailed validation
    //  - Validate promotionCode format (uppercase, no spaces)
    //  - Validate restaurantId if provided

    // Call service layer
    const result = await promotionService.applyPromotion(data, auth);

    return result;
  } catch (error) {
    logError("applyPromotion", error);
    throw toHttpsError(error);
  }
});
