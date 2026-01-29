/**
 * Shop Customer Entity
 * For public customer view
 * Only includes fields that customers should see
 */
export class ShopCustomerEntity {
  id: string;
  name: string;
  description: string;
  address: string;
  rating: number;
  totalRatings: number;
  isOpen: boolean;
  openTime: string;
  closeTime: string;
  shipFeePerOrder: number;
  minOrderAmount: number;
  logoUrl?: string;
  coverImageUrl?: string;
}

/**
 * Shop Customer Detail Entity
 * For public customer detail view
 * Includes additional fields like phone, images, totalOrders, ownerId, ownerName
 */
export class ShopCustomerDetailEntity extends ShopCustomerEntity {
  phone: string;
  coverImageUrl?: string;
  logoUrl?: string;
  totalOrders: number;
  ownerId: string;
  ownerName: string;
}
