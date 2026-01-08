import { FavoriteEntity } from '../entities';

export interface PaginatedResult<T> {
  data: T[];
  pagination: {
    total: number;
    page: number;
    limit: number;
    hasMore: boolean;
  };
}

/**
 * Favorites Repository Interface
 *
 * Abstraction for favorites data access (SOLID - Dependency Inversion)
 */
export interface IFavoritesRepository {
  /**
   * Add product to favorites
   */
  add(userId: string, favorite: Partial<FavoriteEntity>): Promise<FavoriteEntity>;

  /**
   * Remove product from favorites
   */
  remove(userId: string, productId: string): Promise<void>;

  /**
   * Check if product is favorited
   */
  isFavorited(userId: string, productId: string): Promise<boolean>;

  /**
   * Get favorite by user and product
   */
  findByUserAndProduct(userId: string, productId: string): Promise<FavoriteEntity | null>;

  /**
   * List user favorites with pagination
   */
  findByUserId(
    userId: string,
    page: number,
    limit: number,
  ): Promise<PaginatedResult<FavoriteEntity>>;

  /**
   * Count user favorites
   */
  countByUserId(userId: string): Promise<number>;
}

export const FAVORITES_REPOSITORY = 'IFavoritesRepository';
