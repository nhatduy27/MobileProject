/**
 * Promotion Service
 *
 * Business logic layer cho Promotion operations
 */

import {CallableRequest} from "firebase-functions/v2/https";
import {
  ApplyPromotionRequest,
  ApplyPromotionResponse,
} from "../models/promotion.model";
import {promotionRepository} from "../repositories/promotion.repository";

// Type alias cho auth context
type CallableRequestContext = CallableRequest["auth"];

/**
 * Business logic cho Promotion.
 */
export class PromotionService {
  /**
   * Apply promotion code và tính discount
   * @param {ApplyPromotionRequest} data Payload apply promotion
   * @param {CallableRequestContext} context Auth context
   */
  async applyPromotion(
    data: ApplyPromotionRequest,
    context: CallableRequestContext
  ): Promise<ApplyPromotionResponse> {
    void context;
    // TODO: Validate user is authenticated (optional based on business rules)
    // if (!context?.uid) {
    //   throw new Error("Unauthenticated");
    // }

    // TODO: Validate request data
    //  - promotionCode is provided and not empty
    //  - orderAmount is positive number

    // TODO: Get promotion by code
    const promotion = await promotionRepository.getPromotionByCode(
      data.promotionCode
    );

    if (!promotion) {
      return {
        isValid: false,
        discountAmount: 0,
        finalAmount: data.orderAmount,
        message: "Mã khuyến mãi không tồn tại",
      };
    }

    // TODO: Validate promotion
    //  - Check isActive
    //  - Check validity period (validFrom, validUntil)
    //  - Check usage limit (usedCount < usageLimit)
    //  - Check minimum order amount requirement
    //  - Check if restaurant-specific (if applicable)

    // TODO: Calculate discount based on promotion type
    //  - PERCENTAGE: orderAmount * (discountValue / 100)
    //  - FIXED_AMOUNT: discountValue
    //  - FREE_DELIVERY: deliveryFee amount
    //  - Apply maxDiscountAmount cap if set

    // TODO: Check if user already used this promotion (user-specific limit)

    // STUB: Return mock valid response
    const discountAmount = 0; // Calculate based on promotion logic
    const finalAmount = data.orderAmount - discountAmount;

    return {
      isValid: true,
      discountAmount,
      finalAmount,
      message: "Áp dụng mã khuyến mãi thành công",
    };
  }
}

// Singleton instance
export const promotionService = new PromotionService();
