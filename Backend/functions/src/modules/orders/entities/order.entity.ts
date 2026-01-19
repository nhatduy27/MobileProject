import { Timestamp } from 'firebase-admin/firestore';
import { OrderItem } from './order-item.entity';
import { DeliveryAddress } from './delivery-address.entity';

export interface CustomerSnapshot {
  id: string;
  displayName?: string;
  phone?: string;
}

export interface ShipperSnapshot {
  id: string;
  displayName?: string;
  phone?: string;
}

export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PREPARING = 'PREPARING',
  READY = 'READY',
  SHIPPING = 'SHIPPING',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
}

export enum PaymentStatus {
  UNPAID = 'UNPAID',
  PROCESSING = 'PROCESSING',
  PAID = 'PAID',
  REFUNDED = 'REFUNDED',
}

export class OrderEntity {
  id?: string;
  orderNumber: string;

  // Relations
  customerId: string;
  customerSnapshot?: CustomerSnapshot; // Snapshot at order creation (for OWNER list display)
  customer?: CustomerSnapshot; // Alias for customerSnapshot (used in detail responses)
  shopId: string;
  shopName: string;
  shipperId: string | null; // FIX-002: Explicitly allow null to indicate unassigned orders (required for shipper available query)
  shipperSnapshot?: ShipperSnapshot; // Snapshot when shipper is assigned (for OWNER list display)
  shipper?: ShipperSnapshot; // Alias for shipperSnapshot (used in detail responses)

  // Items (locked from cart)
  items: OrderItem[];

  // Amounts
  subtotal: number;
  shipFee: number;
  discount: number;
  total: number;

  // Status
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  paymentMethod: 'COD' | 'ZALOPAY' | 'MOMO' | 'SEPAY';

  // Timestamps
  createdAt?: Timestamp;
  updatedAt?: Timestamp;
  confirmedAt?: Timestamp;
  preparingAt?: Timestamp;
  readyAt?: Timestamp;
  shippingAt?: Timestamp;
  deliveredAt?: Timestamp;
  cancelledAt?: Timestamp;

  // Delivery
  deliveryAddress: DeliveryAddress;
  deliveryNote?: string;

  // Cancellation
  cancelReason?: string;
  cancelledBy?: 'CUSTOMER' | 'OWNER' | 'SYSTEM';

  // Review
  reviewId?: string;
  reviewedAt?: Timestamp;

  // Payout
  paidOut?: boolean;
  paidOutAt?: Timestamp;
}
