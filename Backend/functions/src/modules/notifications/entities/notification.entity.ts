/**
 * Notification Entity
 * Collection: notifications
 * Stores notification history for users
 */
export class NotificationEntity {
  id: string;
  userId: string;

  // Content
  title: string;
  body: string;
  imageUrl?: string;

  // Type & Data
  type: NotificationType;
  data?: Record<string, unknown>; // Deep link data, metadata

  // Status
  read: boolean;
  readAt?: string; // ISO 8601

  // Related entities
  orderId?: string;
  shopId?: string;

  // Delivery metadata (NOTIF-014)
  sentAt?: string; // ISO 8601
  deliveryStatus?: 'SENT' | 'FAILED';
  deliveryErrorCode?: string;
  deliveryErrorMessage?: string;

  createdAt: string; // ISO 8601
}

export enum NotificationType {
  // Order
  ORDER_CONFIRMED = 'ORDER_CONFIRMED',
  ORDER_PREPARING = 'ORDER_PREPARING',
  ORDER_READY = 'ORDER_READY',
  ORDER_SHIPPING = 'ORDER_SHIPPING',
  ORDER_DELIVERED = 'ORDER_DELIVERED',
  ORDER_CANCELLED = 'ORDER_CANCELLED',

  // Payment
  PAYMENT_SUCCESS = 'PAYMENT_SUCCESS',
  PAYMENT_FAILED = 'PAYMENT_FAILED',
  PAYMENT_REFUNDED = 'PAYMENT_REFUNDED',

  // Shipper
  SHIPPER_ASSIGNED = 'SHIPPER_ASSIGNED',
  SHIPPER_APPLICATION_APPROVED = 'SHIPPER_APPLICATION_APPROVED',
  SHIPPER_APPLICATION_REJECTED = 'SHIPPER_APPLICATION_REJECTED',

  // Owner
  NEW_ORDER = 'NEW_ORDER',
  SHIPPER_APPLIED = 'SHIPPER_APPLIED',
  DAILY_SUMMARY = 'DAILY_SUMMARY',
  SUBSCRIPTION_EXPIRING = 'SUBSCRIPTION_EXPIRING',

  // Promotions
  PROMOTION = 'PROMOTION',
  VOUCHER_AVAILABLE = 'VOUCHER_AVAILABLE',

  // Chat
  CHAT = 'CHAT',
}
