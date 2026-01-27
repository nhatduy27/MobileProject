import { ApiProperty } from '@nestjs/swagger';
import { BuyerTier } from '../entities/shop-buyer.entity';

/**
 * Recent Order DTO
 * Used in Buyer Detail response
 */
export class RecentOrderDto {
  @ApiProperty({ example: 'order_456' })
  orderId: string;

  @ApiProperty({ example: 'ORD-0001' })
  orderNumber: string;

  @ApiProperty({ example: 125000 })
  total: number;

  @ApiProperty({ example: 'DELIVERED' })
  status: string;

  @ApiProperty({ example: '2025-01-20T14:30:00Z' })
  createdAt: string; // ISO 8601

  constructor(partial: Partial<RecentOrderDto>) {
    Object.assign(this, partial);
  }
}

/**
 * Buyer Detail DTO
 * Used in GET /owner/buyers/:customerId response
 */
export class BuyerDetailDto {
  @ApiProperty({ example: 'cust_123' })
  customerId: string;

  @ApiProperty({ example: 'Nguyễn Văn A' })
  displayName: string;

  @ApiProperty({ example: '0912345678', required: false })
  phone?: string;

  @ApiProperty({ example: 'https://...', required: false })
  avatar?: string;

  @ApiProperty({ example: 'a@example.com', required: false })
  email?: string;

  @ApiProperty({ enum: BuyerTier, example: BuyerTier.VIP })
  tier: BuyerTier;

  @ApiProperty({ example: 25 })
  totalOrders: number;

  @ApiProperty({ example: 2500000 })
  totalSpent: number;

  @ApiProperty({ example: 100000 })
  avgOrderValue: number;

  @ApiProperty({ example: '2024-06-15T10:00:00Z' })
  joinedDate: string; // ISO 8601

  @ApiProperty({ example: '2024-06-15T10:00:00Z', required: false })
  firstOrderDate?: string; // ISO 8601

  @ApiProperty({ example: '2025-01-20T14:30:00Z', required: false })
  lastOrderDate?: string; // ISO 8601

  @ApiProperty({ type: [RecentOrderDto] })
  recentOrders: RecentOrderDto[];

  constructor(partial: Partial<BuyerDetailDto>) {
    Object.assign(this, partial);
  }
}
