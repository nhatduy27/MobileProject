import { ApiProperty } from '@nestjs/swagger';
import { Timestamp } from 'firebase-admin/firestore';
import { OrderItemPreviewDto } from './order-item-preview.dto';
import { OrderCustomerSnapshotDto } from './order-customer-snapshot.dto';

/**
 * OWNER order detail response DTO
 * Used for GET /api/orders/shop/:id
 * 
 * Consistent with OWNER list response structure:
 * - Uses `customer` field (not customerSnapshot)
 * - Includes `shipperId` and optional `shipper` object
 * - phone field always present (string | null)
 * - Includes full order detail fields
 */
export class OwnerOrderDetailDto {
  @ApiProperty({
    description: 'Order ID',
    example: 'order_abc123def456',
  })
  id: string;

  @ApiProperty({
    description: 'Human-readable order number',
    example: 'ORD-1705591320000-A2B3C4',
  })
  orderNumber: string;

  @ApiProperty({
    description: 'Shop ID',
    example: 'shop_123',
  })
  shopId: string;

  @ApiProperty({
    description: 'Shop name',
    example: 'Cơm Tấm Sườn',
  })
  shopName: string;

  @ApiProperty({
    description: 'Shipper ID (null if not assigned)',
    example: 'user_ship_001',
    nullable: true,
  })
  shipperId: string | null;

  @ApiProperty({
    description: 'Ordered items (full details)',
    type: [OrderItemPreviewDto],
  })
  items: OrderItemPreviewDto[];

  @ApiProperty({
    description: 'Order subtotal (before fees/discount)',
    example: 50000,
  })
  subtotal: number;

  @ApiProperty({
    description: 'Shipping fee',
    example: 15000,
  })
  shipFee: number;

  @ApiProperty({
    description: 'Discount amount',
    example: 0,
  })
  discount: number;

  @ApiProperty({
    description: 'Order total',
    example: 65000,
  })
  total: number;

  @ApiProperty({
    description: 'Order status',
    enum: ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SHIPPING', 'DELIVERED', 'CANCELLED'],
    example: 'PENDING',
  })
  status: string;

  @ApiProperty({
    description: 'Payment status',
    enum: ['UNPAID', 'PROCESSING', 'PAID', 'REFUNDED'],
    example: 'UNPAID',
  })
  paymentStatus: string;

  @ApiProperty({
    description: 'Payment method',
    enum: ['COD', 'ZALOPAY', 'MOMO', 'SEPAY'],
    example: 'COD',
  })
  paymentMethod: string;

  @ApiProperty({
    description: 'Customer information (snapshot at order creation)',
    type: OrderCustomerSnapshotDto,
  })
  customer: OrderCustomerSnapshotDto;

  @ApiProperty({
    description: 'Shipper information (when assigned, null otherwise)',
    type: OrderCustomerSnapshotDto,
    required: false,
    nullable: true,
  })
  shipper?: {
    id: string;
    displayName?: string;
    phone?: string;
  } | null;

  @ApiProperty({
    description: 'Delivery address',
    example: {
      label: 'KTX B5',
      fullAddress: 'KTX Khu B - Tòa B5',
      building: 'B5',
      room: '101',
      note: 'Gọi trước 5 phút',
    },
  })
  deliveryAddress: {
    label?: string;
    fullAddress?: string;
    building?: string;
    room?: string;
    note?: string;
  };

  @ApiProperty({
    description: 'Delivery note',
    example: 'Call before delivery',
    required: false,
  })
  deliveryNote?: string;

  @ApiProperty({
    description: 'Order creation timestamp (ISO-8601 string)',
    example: '2026-01-18T14:00:00.000Z',
    type: String,
  })
  createdAt: string | Timestamp | undefined;

  @ApiProperty({
    description: 'Order last update timestamp (ISO-8601 string)',
    example: '2026-01-18T15:12:20.059Z',
    type: String,
  })
  updatedAt: string | Timestamp | undefined;

  @ApiProperty({
    description: 'Order confirmation timestamp (ISO-8601 string)',
    example: '2026-01-18T14:05:00.000Z',
    required: false,
    nullable: true,
    type: String,
  })
  confirmedAt?: string | Timestamp | undefined | null;

  @ApiProperty({
    description: 'Order preparation start timestamp (ISO-8601 string)',
    example: '2026-01-18T14:10:00.000Z',
    required: false,
    nullable: true,
    type: String,
  })
  preparingAt?: string | Timestamp | undefined | null;

  @ApiProperty({
    description: 'Order ready timestamp (ISO-8601 string)',
    example: '2026-01-18T14:30:00.000Z',
    required: false,
    nullable: true,
    type: String,
  })
  readyAt?: string | Timestamp | undefined | null;

  @ApiProperty({
    description: 'Order shipping start timestamp (ISO-8601 string)',
    required: false,
    nullable: true,
    type: String,
  })
  shippingAt?: string | Timestamp | undefined | null;

  @ApiProperty({
    description: 'Order delivered timestamp (ISO-8601 string)',
    required: false,
    nullable: true,
    type: String,
  })
  deliveredAt?: string | Timestamp | undefined | null;

  @ApiProperty({
    description: 'Order cancellation timestamp (ISO-8601 string)',
    example: '2026-01-18T15:12:20.059Z',
    required: false,
    nullable: true,
    type: String,
  })
  cancelledAt?: string | Timestamp | undefined | null;

  @ApiProperty({
    description: 'Cancellation reason',
    example: 'Changed my mind',
    required: false,
  })
  cancelReason?: string;

  @ApiProperty({
    description: 'Who cancelled the order',
    enum: ['CUSTOMER', 'OWNER', 'SYSTEM'],
    required: false,
  })
  cancelledBy?: string;

  @ApiProperty({
    description: 'Review ID (if reviewed)',
    required: false,
  })
  reviewId?: string;

  @ApiProperty({
    description: 'Review timestamp (ISO-8601 string)',
    required: false,
    nullable: true,
    type: String,
  })
  reviewedAt?: string | Timestamp | undefined | null;

  @ApiProperty({
    description: 'Whether order has been paid out to owner',
    example: false,
    required: false,
  })
  paidOut?: boolean;

  @ApiProperty({
    description: 'Payout timestamp (ISO-8601 string)',
    required: false,
    nullable: true,
    type: String,
  })
  paidOutAt?: string | Timestamp | undefined | null;
}
