import { ApiProperty } from '@nestjs/swagger';
import { BuyerTier } from '../entities/shop-buyer.entity';

/**
 * Buyer List Item DTO
 * Used in GET /owner/buyers response
 */
export class BuyerListItemDto {
  @ApiProperty({ example: 'cust_123' })
  customerId: string;

  @ApiProperty({ example: 'Nguyễn Văn A' })
  displayName: string;

  @ApiProperty({ example: '0912345678', required: false })
  phone?: string;

  @ApiProperty({ example: 'https://...', required: false })
  avatar?: string;

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

  @ApiProperty({ example: '2025-01-20T14:30:00Z', required: false })
  lastOrderDate?: string; // ISO 8601

  constructor(partial: Partial<BuyerListItemDto>) {
    Object.assign(this, partial);
  }
}

/**
 * Paginated Buyer List Response
 */
export class PaginatedBuyerListDto {
  @ApiProperty({ type: [BuyerListItemDto] })
  items: BuyerListItemDto[];

  @ApiProperty({
    example: {
      page: 1,
      limit: 20,
      total: 150,
      totalPages: 8,
    },
  })
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };

  constructor(partial: Partial<PaginatedBuyerListDto>) {
    Object.assign(this, partial);
  }
}
