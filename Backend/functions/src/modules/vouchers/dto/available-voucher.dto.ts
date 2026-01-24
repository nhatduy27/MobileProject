import { ApiProperty } from '@nestjs/swagger';
import { VoucherEntity, VoucherType, OwnerType } from '../entities';

/**
 * Available Voucher Response DTO
 * Used by GET /vouchers (Customer endpoint)
 * 
 * Extends VoucherEntity with per-user usage information.
 * All fields match the backend response.
 */
export class AvailableVoucherDto implements Partial<VoucherEntity> {
  @ApiProperty({
    example: 'voucher_summer20_2024',
    description: 'Unique voucher identifier',
  })
  id: string;

  @ApiProperty({
    example: 'SUMMER20',
    description: 'Voucher code (uppercase, 6-10 chars)',
  })
  code: string;

  @ApiProperty({
    example: 'shop_123',
    description: 'Shop ID (null for admin vouchers)',
    nullable: true,
  })
  shopId: string | null;

  @ApiProperty({
    example: 'PERCENTAGE',
    description: 'Voucher type: PERCENTAGE, FIXED_AMOUNT, or FREE_SHIP',
    enum: VoucherType,
  })
  type: VoucherType;

  @ApiProperty({
    example: 20,
    description: 'Discount value (percentage or amount)',
  })
  value: number;

  @ApiProperty({
    example: 50000,
    description: 'Maximum discount amount (for PERCENTAGE type)',
    required: false,
    nullable: true,
  })
  maxDiscount?: number;

  @ApiProperty({
    example: 30000,
    description: 'Minimum order amount to use voucher (optional)',
    required: false,
    nullable: true,
  })
  minOrderAmount?: number;

  @ApiProperty({
    example: 100,
    description: 'Total usage limit across all customers',
  })
  usageLimit: number;

  @ApiProperty({
    example: 3,
    description: 'Usage limit per customer',
  })
  usageLimitPerUser: number;

  @ApiProperty({
    example: 25,
    description: 'Current total usage count (global aggregate)',
  })
  currentUsage: number;

  @ApiProperty({
    example: '2026-01-21T00:00:00Z',
    description: 'Validity start date (ISO 8601)',
  })
  validFrom: string;

  @ApiProperty({
    example: '2026-12-31T23:59:59Z',
    description: 'Validity end date (ISO 8601)',
  })
  validTo: string;

  @ApiProperty({
    example: true,
    description: 'Whether voucher is active',
  })
  isActive: boolean;

  @ApiProperty({
    example: false,
    description: 'Whether voucher is deleted (soft delete)',
  })
  isDeleted: boolean;

  @ApiProperty({
    example: 'SHOP',
    description: 'Owner type: SHOP or ADMIN',
    enum: OwnerType,
  })
  ownerType: OwnerType;

  @ApiProperty({
    example: 'Miễn phí ship 50%',
    description: 'Voucher name (optional)',
    required: false,
    nullable: true,
  })
  name?: string;

  @ApiProperty({
    example: 'Giảm 50% phí vận chuyển cho đơn từ 30k',
    description: 'Voucher description (optional)',
    required: false,
    nullable: true,
  })
  description?: string;

  @ApiProperty({
    example: '2026-01-20T10:00:00Z',
    description: 'Created timestamp (ISO 8601)',
  })
  createdAt: string;

  @ApiProperty({
    example: '2026-01-20T10:00:00Z',
    description: 'Last updated timestamp (ISO 8601)',
  })
  updatedAt: string;

  // ==================== PER-USER USAGE FIELDS ====================
  // These fields are dynamically added by the service when userId is provided
  // They help customers understand how many times they've used each voucher

  @ApiProperty({
    example: 1,
    required: false,
    description: 'Times current user has used this voucher',
  })
  myUsageCount?: number;

  @ApiProperty({
    example: 2,
    required: false,
    description: 'Remaining uses for current user (usageLimitPerUser - myUsageCount, min 0)',
  })
  myRemainingUses?: number;
}
