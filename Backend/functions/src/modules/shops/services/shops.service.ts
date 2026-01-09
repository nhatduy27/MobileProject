import {
  Injectable,
  Inject,
  NotFoundException,
  ConflictException,
  BadRequestException,
} from '@nestjs/common';
import { IShopsRepository, SHOPS_REPOSITORY } from '../interfaces';
import { ShopEntity, SubscriptionStatus } from '../entities/shop.entity';
import { CreateShopDto, UpdateShopDto } from '../dto';

@Injectable()
export class ShopsService {
  constructor(
    @Inject(SHOPS_REPOSITORY)
    private readonly shopsRepository: IShopsRepository,
  ) {}

  // ==================== Owner Operations ====================

  /**
   * Create a new shop
   * Business Rule: 1 Owner = 1 Shop
   */
  async createShop(
    ownerId: string,
    ownerName: string,
    dto: CreateShopDto,
  ): Promise<ShopEntity> {
    // Check if owner already has a shop
    const existingShop = await this.shopsRepository.findByOwnerId(ownerId);
    if (existingShop) {
      throw new ConflictException({
        code: 'SHOP_001',
        message: 'Bạn đã có shop rồi. Mỗi chủ shop chỉ được tạo 1 shop.',
        statusCode: 409,
      });
    }

    // Validate time range
    if (dto.openTime >= dto.closeTime) {
      throw new BadRequestException({
        code: 'SHOP_002',
        message: 'Giờ đóng cửa phải sau giờ mở cửa',
        statusCode: 400,
      });
    }

    return this.shopsRepository.create(ownerId, ownerName, dto);
  }

  /**
   * Get owner's shop
   */
  async getMyShop(ownerId: string): Promise<ShopEntity> {
    const shop = await this.shopsRepository.findByOwnerId(ownerId);
    if (!shop) {
      throw new NotFoundException({
        code: 'SHOP_003',
        message: 'Bạn chưa có shop nào',
        statusCode: 404,
      });
    }
    return shop;
  }

  /**
   * Update shop information
   */
  async updateShop(ownerId: string, dto: UpdateShopDto): Promise<ShopEntity> {
    const shop = await this.getMyShop(ownerId);

    // Validate time range if both provided
    if (dto.openTime || dto.closeTime) {
      const openTime = dto.openTime || shop.openTime;
      const closeTime = dto.closeTime || shop.closeTime;
      if (openTime >= closeTime) {
        throw new BadRequestException({
          code: 'SHOP_002',
          message: 'Giờ đóng cửa phải sau giờ mở cửa',
          statusCode: 400,
        });
      }
    }

    return this.shopsRepository.update(shop.id, dto);
  }

  /**
   * Toggle shop open/close status
   * Business Rule: Can only open if subscription is ACTIVE
   */
  async toggleShopStatus(ownerId: string, isOpen: boolean): Promise<void> {
    const shop = await this.getMyShop(ownerId);

    // If trying to open, check subscription status
    if (isOpen && shop.subscription.status !== SubscriptionStatus.ACTIVE) {
      throw new BadRequestException({
        code: 'SHOP_004',
        message: 'Không thể mở shop. Subscription chưa active hoặc đã hết hạn.',
        statusCode: 400,
      });
    }

    await this.shopsRepository.toggleStatus(shop.id, isOpen);
  }

  // ==================== Customer Operations ====================

  /**
   * Get all shops (customer view)
   */
  async getAllShops(params: {
    page: number;
    limit: number;
    status?: string;
    search?: string;
  }): Promise<{ shops: ShopEntity[]; total: number; page: number; limit: number }> {
    const result = await this.shopsRepository.findAll(params);

    return {
      ...result,
      page: params.page,
      limit: params.limit,
    };
  }

  /**
   * Get shop detail by ID
   */
  async getShopById(shopId: string): Promise<ShopEntity> {
    const shop = await this.shopsRepository.findById(shopId);
    if (!shop) {
      throw new NotFoundException({
        code: 'SHOP_005',
        message: 'Không tìm thấy shop',
        statusCode: 404,
      });
    }
    return shop;
  }

  // ==================== Utility Methods ====================

  /**
   * Verify shop exists and belongs to owner
   */
  async verifyShopOwnership(shopId: string, ownerId: string): Promise<ShopEntity> {
    const shop = await this.getShopById(shopId);
    if (shop.ownerId !== ownerId) {
      throw new ConflictException({
        code: 'SHOP_006',
        message: 'Bạn không phải chủ của shop này',
        statusCode: 403,
      });
    }
    return shop;
  }

  /**
   * Update shop statistics
   */
  async updateShopStats(
    shopId: string,
    stats: {
      totalOrders?: number;
      totalRevenue?: number;
      rating?: number;
      totalRatings?: number;
    },
  ): Promise<void> {
    await this.shopsRepository.updateStats(shopId, stats);
  }
}
