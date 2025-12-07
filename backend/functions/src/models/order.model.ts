/**
 * Order Models
 *
 * Định nghĩa types và interfaces cho Order domain
 */

export type OrderStatus =
  | "PENDING"
  | "CONFIRMED"
  | "CANCELLED"
  | "DELIVERING"
  | "COMPLETED";

export interface OrderItem {
  menuItemId: string;
  quantity: number;
  unitPrice: number;
  // Có thể thêm: name, totalPrice
}

export interface Order {
  id: string;
  userId: string;
  restaurantId: string;
  items: OrderItem[];
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
  // Có thể thêm: updatedAt, deliveryAddress, notes, etc.
}

/**
 * Request/Response types cho API
 */

export interface PlaceOrderRequest {
  restaurantId: string;
  items: Array<{
    menuItemId: string;
    quantity: number;
  }>;
  deliveryAddress?: string;
  notes?: string;
  promotionCode?: string;
}

export interface PlaceOrderResponse {
  orderId: string;
  status: OrderStatus;
  totalAmount: number;
  estimatedDeliveryTime?: string;
}

export interface CancelOrderRequest {
  orderId: string;
  reason?: string;
}

export interface CancelOrderResponse {
  orderId: string;
  status: OrderStatus;
  refundAmount?: number;
}
