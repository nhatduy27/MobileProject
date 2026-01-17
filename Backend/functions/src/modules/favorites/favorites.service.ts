import { Injectable, Inject, ConflictException, NotFoundException } from '@nestjs/common';
import { IFavoritesRepository, FAVORITES_REPOSITORY, PaginatedResult } from './interfaces';
import { FavoriteEntity } from './entities';
import { ProductsService } from '../products/services/products.service';

/**
 * Mock product data structure
 * In production, this would come from ProductsService
 */
interface ProductData {
  id: string;
  name: string;
  price: number;
  imageUrl?: string;
  shopId: string;
  shopName: string;
}

@Injectable()
export class FavoritesService {
  constructor(
    @Inject(FAVORITES_REPOSITORY)
    private readonly favoritesRepository: IFavoritesRepository,
    private readonly productsService: ProductsService,
  ) {}

  /**
   * Add product to favorites
   *
   * USER-006
   */
  async addFavorite(userId: string, productId: string): Promise<FavoriteEntity> {
    // Check if already favorited
    const existing = await this.favoritesRepository.findByUserAndProduct(userId, productId);
    if (existing) {
      throw new ConflictException('Product already in favorites');
    }

    // TODO: Get real product data from ProductsService
    // For now, use placeholder data
    const productData = await this.getProductData(productId);

    return this.favoritesRepository.add(userId, {
      productId,
      productName: productData.name,
      productPrice: productData.price,
      productImage: productData.imageUrl,
      shopId: productData.shopId,
      shopName: productData.shopName,
    });
  }

  /**
   * Remove product from favorites
   *
   * USER-006
   */
  async removeFavorite(userId: string, productId: string): Promise<void> {
    const existing = await this.favoritesRepository.findByUserAndProduct(userId, productId);
    if (!existing) {
      throw new NotFoundException('Product not in favorites');
    }

    await this.favoritesRepository.remove(userId, productId);
  }

  /**
   * List user favorites with pagination
   *
   * USER-006
   */
  async listFavorites(
    userId: string,
    page: number = 1,
    limit: number = 20,
  ): Promise<PaginatedResult<FavoriteEntity>> {
    return this.favoritesRepository.findByUserId(userId, page, limit);
  }

  /**
   * Check if product is favorited
   *
   * USER-006
   */
  async isFavorited(userId: string, productId: string): Promise<boolean> {
    return this.favoritesRepository.isFavorited(userId, productId);
  }

  /**
   * Get product data for denormalization
   * TODO: Replace with real ProductsService call
   */
  private async getProductData(productId: string): Promise<ProductData> {
  try {
    // Gọi ProductsService để lấy thông tin sản phẩm
    const product = await this.productsService.findOne(productId);

    if (!product) {
      throw new Error('Product not found');
    }

    return {
      id: product.id,
      name: product.name || 'Product Name',
      price: product.price || 0,
      // Sửa: product.imageUrl là string, không phải array
      imageUrl: product.imageUrl || undefined,
      shopId: product.shopId || 'shop_placeholder',
      // Sửa: product.shopName là trường trực tiếp
      shopName: product.shopName || 'Shop Name',
    };
  } catch (error) {
    // Xử lý lỗi - có thể log hoặc throw custom exception
    console.error(`Error fetching product data for ${productId}:`, error);
    
    // Fallback về placeholder data nếu có lỗi
    return {
      id: productId,
      name: 'Product Name',
      price: 0,
      imageUrl: undefined,
      shopId: 'shop_placeholder',
      shopName: 'Shop Name',
    };
  }
}
}