import { ApiProperty } from '@nestjs/swagger';
import { Timestamp } from 'firebase-admin/firestore';
import { OrderItemPreviewDto } from './order-item-preview.dto';
import { OrderCustomerSnapshotDto } from './order-customer-snapshot.dto';
import { OrderShipperSnapshotDto } from './order-shipper-snapshot.dto';

export class OrderListItemDto {
  @ApiProperty({
    description: 'Order ID',
    example: 'order_123',
  })
  id: string;

  @ApiProperty({
    description: 'Human-readable order number',
    example: 'ORD-001',
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
    description: 'Total order amount',
    example: 65000,
  })
  total: number;

  @ApiProperty({
    description: 'Number of items in order',
    example: 2,
  })
  itemCount: number;

  @ApiProperty({
    description: 'Order creation timestamp (ISO-8601 string)',
    example: '2026-01-18T15:12:20.059Z',
    type: String,
  })
  createdAt: string | Timestamp | undefined;

  @ApiProperty({
    description: 'Preview of ordered items (max 3 items for OWNER list)',
    type: [OrderItemPreviewDto],
    required: false,
  })
  itemsPreview?: OrderItemPreviewDto[];

  @ApiProperty({
    description: 'Number of items included in preview (max 3)',
    example: 3,
    required: false,
  })
  itemsPreviewCount?: number;

  @ApiProperty({
    description: 'Customer information snapshot (for OWNER list only)',
    type: OrderCustomerSnapshotDto,
    required: false,
  })
  customer?: OrderCustomerSnapshotDto;

  @ApiProperty({
    description: 'Payment method',
    enum: ['COD', 'ZALOPAY', 'MOMO', 'SEPAY'],
    example: 'COD',
    required: false,
  })
  paymentMethod?: string;

  @ApiProperty({
    description: 'Delivery address snippet (for OWNER list overview)',
    example: {
      label: 'home',
      fullAddress: '268 Lý Thường Kiệt, Phường 14, Quận 10, TP.HCM',
      building: 'A1',
      room: '101',
    },
    required: false,
  })
  deliveryAddress?: {
    label?: string;
    fullAddress?: string;
    building?: string;
    room?: string;
  };

  @ApiProperty({
    description: 'Shipper ID (if assigned, null if unassigned)',
    example: 'shipper_456',
    nullable: true,
    required: false,
  })
  shipperId?: string | null;

  @ApiProperty({
    description: 'Shipper information snapshot (for OWNER list only, when shipper is assigned)',
    type: OrderShipperSnapshotDto,
    required: false,
  })
  shipper?: OrderShipperSnapshotDto;

  @ApiProperty({
    description: 'Last update timestamp (ISO-8601 string)',
    example: '2026-01-19T10:30:00.000Z',
    type: String,
    required: false,
  })
  updatedAt?: string | Timestamp | undefined;
}
