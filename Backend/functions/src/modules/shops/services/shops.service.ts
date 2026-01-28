import {
  Injectable,
  Inject,
  NotFoundException,
  ConflictException,
  BadRequestException,
} from '@nestjs/common';
import { IShopsRepository, SHOPS_REPOSITORY } from '../interfaces';
import { ShopEntity, SubscriptionStatus } from '../entities/shop.entity';
import { ShopCustomerEntity, ShopCustomerDetailEntity } from '../entities/shop-customer.entity';
import { CreateShopDto } from '../dto';
import { StorageService } from '../../../shared/services/storage.service';

@Injectable()
export class ShopsService {
  constructor(
    @Inject(SHOPS_REPOSITORY)
    private readonly shopsRepository: IShopsRepository,
    private readonly storageService: StorageService,
  ) {}

  // ==================== Owner Operations ====================

  /**
   * Create a new shop with file uploads
   * Business Rule: 1 Owner = 1 Shop
   */
  async createShop(
    ownerId: string,
    ownerName: string,
    dto: CreateShopDto,
    coverImageFile: Express.Multer.File,
    logoFile: Express.Multer.File,
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

    // Validate image types
    const validMimeTypes = ['image/jpeg', 'image/jpg', 'image/png'];
    if (
      !validMimeTypes.includes(coverImageFile.mimetype) ||
      !validMimeTypes.includes(logoFile.mimetype)
    ) {
      throw new BadRequestException('Chỉ chấp nhận file ảnh định dạng JPG, JPEG, PNG');
    }

    // Validate image sizes (max 5MB each)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (coverImageFile.size > maxSize || logoFile.size > maxSize) {
      throw new BadRequestException('Kích thước mỗi ảnh không được vượt quá 5MB');
    }

    // Generate temporary shopId for upload path
    const tempShopId = `shop_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

    // Upload images to Firebase Storage
    let coverImageUrl: string;
    let logoUrl: string;

    try {
      // Upload both images in parallel
      [coverImageUrl, logoUrl] = await Promise.all([
        this.storageService.uploadShopImage(
          tempShopId,
          'coverImage',
          coverImageFile.buffer,
          coverImageFile.mimetype,
        ),
        this.storageService.uploadShopImage(tempShopId, 'logo', logoFile.buffer, logoFile.mimetype),
      ]);
    } catch (error) {
      throw new BadRequestException('Upload ảnh thất bại. Vui lòng thử lại');
    }

    // Create shop with uploaded URLs
    const createData = {
      ...dto,
      coverImageUrl,
      logoUrl,
    };

    return this.shopsRepository.create(ownerId, ownerName, createData);
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
   * Update shop information with optional file uploads
   */
  async updateShop(
    ownerId: string,
    data: Record<string, any>,
    coverImageFile?: Express.Multer.File,
    logoFile?: Express.Multer.File,
  ): Promise<void> {
    const shop = await this.getMyShop(ownerId);

    // Validate time range if both provided
    if (data.openTime || data.closeTime) {
      const openTime = data.openTime || shop.openTime;
      const closeTime = data.closeTime || shop.closeTime;
      if (openTime >= closeTime) {
        throw new BadRequestException({
          code: 'SHOP_002',
          message: 'Giờ đóng cửa phải sau giờ mở cửa',
          statusCode: 400,
        });
      }
    }

    // Prepare update data
    const updateData: Record<string, any> = { ...data };

    // Handle file uploads if provided
    if (coverImageFile || logoFile) {
      // Validate image types
      const validMimeTypes = ['image/jpeg', 'image/jpg', 'image/png'];

      if (coverImageFile && !validMimeTypes.includes(coverImageFile.mimetype)) {
        throw new BadRequestException('Chỉ chấp nhận file ảnh bìa định dạng JPG, JPEG, PNG');
      }

      if (logoFile && !validMimeTypes.includes(logoFile.mimetype)) {
        throw new BadRequestException('Chỉ chấp nhận file logo định dạng JPG, JPEG, PNG');
      }

      // Validate image sizes (max 5MB each)
      const maxSize = 5 * 1024 * 1024; // 5MB

      if (coverImageFile && coverImageFile.size > maxSize) {
        throw new BadRequestException('Kích thước ảnh bìa không được vượt quá 5MB');
      }

      if (logoFile && logoFile.size > maxSize) {
        throw new BadRequestException('Kích thước logo không được vượt quá 5MB');
      }

      // Upload new images
      try {
        const uploadPromises: Promise<string>[] = [];

        if (coverImageFile) {
          uploadPromises.push(
            this.storageService.uploadShopImage(
              shop.id,
              'coverImage',
              coverImageFile.buffer,
              coverImageFile.mimetype,
            ),
          );
        }

        if (logoFile) {
          uploadPromises.push(
            this.storageService.uploadShopImage(
              shop.id,
              'logo',
              logoFile.buffer,
              logoFile.mimetype,
            ),
          );
        }

        const uploadedUrls = await Promise.all(uploadPromises);
        let urlIndex = 0;

        if (coverImageFile) {
          (updateData as any).coverImageUrl = uploadedUrls[urlIndex++];
        }

        if (logoFile) {
          (updateData as any).logoUrl = uploadedUrls[urlIndex++];
        }
      } catch (error) {
        throw new BadRequestException('Upload ảnh thất bại. Vui lòng thử lại');
      }
    }

    await this.shopsRepository.update(shop.id, updateData);
  }

  /**   * Toggle shop open/close status
   * Business Rule: Can only open if subscription is ACTIVE or TRIAL
   */
  async toggleShopStatus(ownerId: string, isOpen: boolean): Promise<void> {
    const shop = await this.getMyShop(ownerId);

    // If trying to open, check subscription status
    // Allow opening during TRIAL (7 days free) and ACTIVE (paid)
    const validStatuses = [SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL];

    if (isOpen && !validStatuses.includes(shop.subscription.status)) {
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
  }): Promise<{ shops: ShopCustomerEntity[]; total: number; page: number; limit: number }> {
    const result = await this.shopsRepository.findAll(params);

    // Map to customer entity (exclude sensitive fields)
    const customerShops = result.shops.map((shop) => ({
      id: shop.id,
      name: shop.name,
      description: shop.description,
      address: shop.address,
      rating: shop.rating,
      totalRatings: shop.totalRatings,
      isOpen: shop.isOpen,
      openTime: shop.openTime,
      closeTime: shop.closeTime,
      shipFeePerOrder: shop.shipFeePerOrder,
      minOrderAmount: shop.minOrderAmount,
      logoUrl: shop.logoUrl,
      coverImageUrl: shop.coverImageUrl,
    }));

    return {
      shops: customerShops,
      total: result.total,
      page: params.page,
      limit: params.limit,
    };
  }

  /**
   * Get shop detail by ID
   */
  async getShopById(shopId: string): Promise<ShopCustomerDetailEntity> {
    const shop = await this.shopsRepository.findById(shopId);
    if (!shop) {
      throw new NotFoundException({
        code: 'SHOP_005',
        message: 'Không tìm thấy shop',
        statusCode: 404,
      });
    }

    // Map to customer detail entity (exclude owner info, subscription, etc.)
    return {
      id: shop.id,
      name: shop.name,
      description: shop.description,
      address: shop.address,
      phone: shop.phone,
      coverImageUrl: shop.coverImageUrl,
      logoUrl: shop.logoUrl,
      rating: shop.rating,
      totalRatings: shop.totalRatings,
      isOpen: shop.isOpen,
      openTime: shop.openTime,
      closeTime: shop.closeTime,
      shipFeePerOrder: shop.shipFeePerOrder,
      minOrderAmount: shop.minOrderAmount,
      totalOrders: shop.totalOrders,
    };
  }

  // ==================== Utility Methods ====================

  /**
   * Verify shop exists and belongs to owner
   */
  async verifyShopOwnership(shopId: string, ownerId: string): Promise<ShopEntity> {
    const shop = await this.shopsRepository.findById(shopId);
    if (!shop) {
      throw new NotFoundException({
        code: 'SHOP_005',
        message: 'Không tìm thấy shop',
        statusCode: 404,
      });
    }
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
