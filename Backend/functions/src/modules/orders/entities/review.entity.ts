/**
 * Product Review inside an Order Review
 * Used when customer rates individual products in their order
 */
export interface ProductReview {
  productId: string;
  productName: string; // Denormalized for display
  rating: number; // 1-5
  comment?: string;
}

export class ReviewEntity {
  id?: string;
  orderId: string;
  customerId: string;
  customerName?: string; // Denormalized for display
  shopId: string;
  rating: number; // 1-5 (shop rating)
  comment?: string; // shop comment
  
  // Individual product reviews
  productReviews?: ProductReview[];
  
  ownerReply?: string;
  ownerRepliedAt?: any;
  createdAt?: any;
  updatedAt?: any;
}
