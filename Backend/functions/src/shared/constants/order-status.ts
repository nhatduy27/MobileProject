/**
 * Order Status Constants
 */
export enum OrderStatus {
  /** Order created, waiting for payment */
  PENDING = 'pending',

  /** Payment confirmed */
  PAID = 'paid',

  /** Owner confirmed the order */
  CONFIRMED = 'confirmed',

  /** Order is being prepared */
  PREPARING = 'preparing',

  /** Order ready for pickup */
  READY = 'ready',

  /** Shipper picked up the order */
  PICKED_UP = 'picked_up',

  /** Shipper is delivering */
  DELIVERING = 'delivering',

  /** Order delivered successfully */
  DELIVERED = 'delivered',

  /** Order cancelled */
  CANCELLED = 'cancelled',

  /** Order rejected by owner */
  REJECTED = 'rejected',
}

/**
 * Payment Status Constants
 */
export enum PaymentStatus {
  /** Waiting for payment */
  PENDING = 'pending',

  /** Payment processing */
  PROCESSING = 'processing',

  /** Payment successful */
  SUCCESS = 'success',

  /** Payment failed */
  FAILED = 'failed',

  /** Payment refunded */
  REFUNDED = 'refunded',

  /** Partial refund */
  PARTIAL_REFUND = 'partial_refund',
}

/**
 * Valid order status transitions
 */
export const OrderStatusTransitions: Record<OrderStatus, OrderStatus[]> = {
  [OrderStatus.PENDING]: [OrderStatus.PAID, OrderStatus.CANCELLED],
  [OrderStatus.PAID]: [OrderStatus.CONFIRMED, OrderStatus.REJECTED],
  [OrderStatus.CONFIRMED]: [OrderStatus.PREPARING, OrderStatus.CANCELLED],
  [OrderStatus.PREPARING]: [OrderStatus.READY],
  [OrderStatus.READY]: [OrderStatus.PICKED_UP],
  [OrderStatus.PICKED_UP]: [OrderStatus.DELIVERING],
  [OrderStatus.DELIVERING]: [OrderStatus.DELIVERED],
  [OrderStatus.DELIVERED]: [],
  [OrderStatus.CANCELLED]: [],
  [OrderStatus.REJECTED]: [],
};

/**
 * Check if status transition is valid
 */
export function isValidStatusTransition(
  currentStatus: OrderStatus,
  newStatus: OrderStatus,
): boolean {
  const validTransitions = OrderStatusTransitions[currentStatus];
  return validTransitions.includes(newStatus);
}
