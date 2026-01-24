/**
 * Voucher Entity
 * Collection: vouchers
 */
export class VoucherEntity {
  id: string;
  shopId: string | null; // null for ADMIN vouchers (future)

  // Code
  code: string; // Uppercase, alphanumeric (6-10 chars)

  // Type & Value
  type: VoucherType;
  value: number;
  maxDiscount?: number; // For PERCENTAGE type

  // Conditions
  minOrderAmount?: number;

  // Limits
  usageLimit: number;
  usageLimitPerUser: number;
  currentUsage: number;

  // Validity
  validFrom: string; // ISO 8601
  validTo: string; // ISO 8601

  // Status
  isActive: boolean;
  isDeleted: boolean;

  // Ownership
  ownerType: OwnerType;

  // Metadata
  name?: string;
  description?: string;
  createdAt: string; // ISO 8601
  updatedAt: string; // ISO 8601
}

export enum VoucherType {
  PERCENTAGE = 'PERCENTAGE',
  FIXED_AMOUNT = 'FIXED_AMOUNT',
  FREE_SHIP = 'FREE_SHIP',
}

export enum OwnerType {
  SHOP = 'SHOP',
  ADMIN = 'ADMIN',
}
