/**
 * Promotion Models
 *
 * Định nghĩa types và interfaces cho Promotion domain
 */

export type PromotionType = "PERCENTAGE" | "FIXED_AMOUNT" | "FREE_DELIVERY";

export interface Promotion {
  id: string;
  code: string;
  type: PromotionType;
  discountValue: number;
  minOrderAmount?: number;
  maxDiscountAmount?: number;
  validFrom: string;
  validUntil: string;
  usageLimit?: number;
  usedCount?: number;
  isActive: boolean;
}

/**
 * Request/Response types cho Promotion API
 */

export interface ApplyPromotionRequest {
  promotionCode: string;
  orderAmount: number;
  restaurantId?: string;
}

export interface ApplyPromotionResponse {
  isValid: boolean;
  discountAmount: number;
  finalAmount: number;
  message?: string;
}
