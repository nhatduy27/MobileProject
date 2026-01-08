/**
 * Favorite Entity
 *
 * User favorite product (denormalized for performance)
 */

export class FavoriteEntity {
  id: string; // Composite: userId_productId
  userId: string;
  productId: string;

  // Denormalized product data
  productName: string;
  productPrice: number;
  productImage?: string;
  shopId: string;
  shopName: string;

  createdAt: Date;

  constructor(partial: Partial<FavoriteEntity>) {
    Object.assign(this, partial);
  }
}
