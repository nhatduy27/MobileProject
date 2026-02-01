/**
 * Product Entity
 * Collection: products
 */
export class ProductEntity {
  id: string;
  shopId: string;
  shopName: string; // Denormalized

  name: string;
  description: string;
  price: number;

  categoryId: string;
  categoryName: string; // Denormalized

  imageUrls?: string[];

  isAvailable: boolean;
  preparationTime: number; // minutes

  // Stats
  rating: number;
  totalRatings: number;
  soldCount: number;

  // Display order
  sortOrder: number;

  // Soft delete
  isDeleted: boolean;

  // Timestamps (ISO 8601 strings)
  createdAt: string;
  updatedAt: string;
}
