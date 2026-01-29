import {
  Injectable,
  Inject,
  NotFoundException,
  ConflictException,
  BadRequestException,
} from '@nestjs/common';
import { IProductsRepository } from '../interfaces';
import { ProductEntity } from '../entities';
import {
  CreateProductDto,
  UpdateProductDto,
  ProductFilterDto,
  ToggleAvailabilityDto,
} from '../dto';
import { ShopsService } from '../../shops/services/shops.service';
import { StorageService } from '../../../shared/services/storage.service';
import { CategoriesService } from '../../categories/categories.service';

@Injectable()
export class ProductsService {
  constructor(
    @Inject('PRODUCTS_REPOSITORY')
    private readonly productsRepository: IProductsRepository,
    private readonly shopsService: ShopsService,
    private readonly storageService: StorageService,
    private readonly categoriesService: CategoriesService,
  ) {}

  /**
   * Find product by ID (public access - for other services)
   */
  async findOne(productId: string): Promise<ProductEntity> {
    return await this.getProductById(productId);
  }

  // ==================== Owner Operations ====================

  /**
   * Create a new product with file upload
   * PROD-001
   */
  async createProduct(
    ownerId: string,
    dto: CreateProductDto,
    imageFile: Express.Multer.File,
  ): Promise<ProductEntity> {
    // Get owner's shop
    const shop = await this.shopsService.getMyShop(ownerId);

    // Get category name from Categories service
    const category = await this.categoriesService.findById(dto.categoryId);
    const categoryName = category.name;

    // Validate image type
    const validMimeTypes = ['image/jpeg', 'image/jpg', 'image/png'];
    if (!validMimeTypes.includes(imageFile.mimetype)) {
      throw new BadRequestException('Chỉ chấp nhận file ảnh định dạng JPG, JPEG, PNG');
    }

    // Validate image size (max 5MB)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (imageFile.size > maxSize) {
      throw new BadRequestException('Kích thước ảnh không được vượt quá 5MB');
    }

    // Generate temporary productId for upload path
    const tempProductId = `prod_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

    // Upload image to Firebase Storage
    let imageUrl: string;
    try {
      imageUrl = await this.storageService.uploadProductImage(
        shop.id,
        tempProductId,
        imageFile.buffer,
        imageFile.mimetype,
      );
    } catch (error) {
      throw new BadRequestException('Upload ảnh thất bại. Vui lòng thử lại');
    }

    // Create product with uploaded URL
    const createData = {
      ...dto,
      imageUrl,
    };

    return await this.productsRepository.create(shop.id, shop.name, categoryName, createData);
  }

  /**
   * Get all products of owner's shop
   * PROD-002
   */
  async getMyProducts(
    ownerId: string,
    filters: { categoryId?: string; isAvailable?: string; page?: number; limit?: number },
  ): Promise<{ products: ProductEntity[]; total: number; page: number; limit: number }> {
    const shop = await this.shopsService.getMyShop(ownerId);

    const result = await this.productsRepository.findByShopId(shop.id, filters);

    return {
      ...result,
      page: filters.page || 1,
      limit: filters.limit || 20,
    };
  }

  /**
   * Get product by ID (owner must own the product)
   * PROD-003
   */
  async getMyProduct(ownerId: string, productId: string): Promise<ProductEntity> {
    const product = await this.getProductById(productId);
    const shop = await this.shopsService.getMyShop(ownerId);

    if (product.shopId !== shop.id) {
      throw new ConflictException({
        code: 'PRODUCT_004',
        message: 'Bạn không phải chủ của sản phẩm này',
        statusCode: 403,
      });
    }

    return product;
  }

  /**
   * Update product with optional file upload
   * PROD-006 - Price Lock Rule: Cannot change price when shop is open
   */
  async updateProduct(
    ownerId: string,
    productId: string,
    dto: UpdateProductDto,
    imageFile?: Express.Multer.File,
  ): Promise<void> {
    const product = await this.getMyProduct(ownerId, productId);

    // PROD-006: Price Lock Rule
    const shop = await this.shopsService.getShopById(product.shopId);
    if (dto.price !== undefined && dto.price !== product.price && shop.isOpen) {
      throw new ConflictException({
        code: 'PRODUCT_002',
        message: 'Không thể thay đổi giá khi shop đang mở',
        statusCode: 409,
      });
    }

    // Get category name if categoryId changed
    let categoryName = product.categoryName;
    if (dto.categoryId && dto.categoryId !== product.categoryId) {
      const category = await this.categoriesService.findById(dto.categoryId);
      categoryName = category.name;
    }

    // Prepare update data
    const updateData: any = {
      ...dto,
      categoryName,
    };

    // Handle file upload if provided
    if (imageFile) {
      // Validate image type
      const validMimeTypes = ['image/jpeg', 'image/jpg', 'image/png'];
      if (!validMimeTypes.includes(imageFile.mimetype)) {
        throw new BadRequestException('Chỉ chấp nhận file ảnh định dạng JPG, JPEG, PNG');
      }

      // Validate image size (max 5MB)
      const maxSize = 5 * 1024 * 1024; // 5MB
      if (imageFile.size > maxSize) {
        throw new BadRequestException('Kích thước ảnh không được vượt quá 5MB');
      }

      // Upload new image
      try {
        updateData.imageUrl = await this.storageService.uploadProductImage(
          product.shopId,
          productId,
          imageFile.buffer,
          imageFile.mimetype,
        );
      } catch (error) {
        throw new BadRequestException('Upload ảnh thất bại. Vui lòng thử lại');
      }
    }

    await this.productsRepository.update(productId, updateData);
  }

  /**   * Toggle product availability
   * PROD-007
   */
  async toggleAvailability(
    ownerId: string,
    productId: string,
    dto: ToggleAvailabilityDto,
  ): Promise<void> {
    await this.getMyProduct(ownerId, productId);
    await this.productsRepository.toggleAvailability(productId, dto.isAvailable);
  }

  /**
   * Delete product (soft delete)
   * PROD-008
   */
  async deleteProduct(ownerId: string, productId: string): Promise<void> {
    await this.getMyProduct(ownerId, productId);
    await this.productsRepository.softDelete(productId);
  }

  // ==================== Customer Operations ====================

  /**
   * Get global product feed (all shops)
   * PROD-009
   */
  async getProductFeed(
    filters: ProductFilterDto,
  ): Promise<{ products: ProductEntity[]; total: number; page: number; limit: number }> {
    const result = await this.productsRepository.searchGlobal(filters);

    const currentHour = new Date().getHours();

    // Các khung giờ ưu tiên cơm
    const ricePriorityHours = [
      { start: 5, end: 8 }, // 5h-8h sáng
      { start: 10, end: 21 }, // 10h-14h trưa
    ];

    // Kiểm tra xem có đang trong khung giờ ưu tiên cơm không
    const isRicePriorityTime = ricePriorityHours.some(
      ({ start, end }) => currentHour >= start && currentHour < end,
    );

    let sortedProducts = [...result.products];

    if (isRicePriorityTime) {
      // Ưu tiên các món thuộc category "cơm" trong khung giờ 5-8h và 10-14h
      sortedProducts.sort((a, b) => {
        const aIsRice = this.isRiceCategory(a.categoryName);
        const bIsRice = this.isRiceCategory(b.categoryName);

        if (aIsRice && !bIsRice) return -1; // a là cơm, ưu tiên lên đầu
        if (!aIsRice && bIsRice) return 1; // b là cơm, a xuống dưới
        return 0; // Giữ nguyên thứ tự nếu cùng loại
      });
    } else {
      // Các khung giờ khác: ưu tiên tráng miệng và trà sữa
      sortedProducts.sort((a, b) => {
        const aIsDessert = this.isDessertOrBubbleTeaCategory(a.categoryName);
        const bIsDessert = this.isDessertOrBubbleTeaCategory(b.categoryName);

        if (aIsDessert && !bIsDessert) return -1; // a là tráng miệng/trà sữa, ưu tiên lên đầu
        if (!aIsDessert && bIsDessert) return 1; // b là tráng miệng/trà sữa, a xuống dưới
        return 0; // Giữ nguyên thứ tự nếu cùng loại
      });
    }

    return {
      products: sortedProducts,
      total: result.total,
      page: filters.page || 1,
      limit: filters.limit || 20,
    };
  }

  // Helper method để kiểm tra category cơm
  private isRiceCategory(category?: string): boolean {
    if (!category) return false;

    const riceKeywords = ['Cơm', 'Phở & Bún', 'Mì'];
    const categoryLower = category.toLowerCase();

    return riceKeywords.some((keyword) => categoryLower.includes(keyword.toLowerCase()));
  }

  // Helper method để kiểm tra category tráng miệng hoặc trà sữa
  private isDessertOrBubbleTeaCategory(category?: string): boolean {
    if (!category) return false;

    const dessertKeywords = ['Tráng miệng', 'Đồ ăn vặt', 'Trà sữa & Đồ uống', 'Coffee'];

    const categoryLower = category.toLowerCase();

    return dessertKeywords.some((keyword) => categoryLower.includes(keyword.toLowerCase()));
  }

  /**
   * Get product detail (customer view)
   * PROD-010
   */
  async getProductDetail(productId: string): Promise<ProductEntity> {
    const product = await this.getProductById(productId);

    if (product.isDeleted) {
      throw new NotFoundException({
        code: 'PRODUCT_001',
        message: 'Không tìm thấy sản phẩm',
        statusCode: 404,
      });
    }

    return product;
  }

  // ==================== Utility Methods ====================

  /**
   * Get product by ID (internal)
   */
  async getProductById(productId: string): Promise<ProductEntity> {
    const product = await this.productsRepository.findById(productId);

    if (!product) {
      throw new NotFoundException({
        code: 'PRODUCT_001',
        message: 'Không tìm thấy sản phẩm',
        statusCode: 404,
      });
    }

    return product;
  }

  /**
   * Update product stats (for Order/Review modules)
   */
  async updateProductStats(
    productId: string,
    stats: {
      rating?: number;
      totalRatings?: number;
      soldCount?: number;
    },
  ): Promise<void> {
    await this.productsRepository.updateStats(productId, stats);
  }

  /**
   * Increment soldCount for order items (called when order is delivered)
   */
  async incrementSoldCount(items: Array<{ productId: string; quantity: number }>): Promise<void> {
    await this.productsRepository.incrementSoldCount(items);
  }

  /**
   * Decrement soldCount for order items (called when delivered order is cancelled)
   */
  async decrementSoldCount(items: Array<{ productId: string; quantity: number }>): Promise<void> {
    await this.productsRepository.decrementSoldCount(items);
  }

  /**
   * Upload product image
   * PROD-005
   */
  async uploadProductImage(
    ownerId: string,
    productId: string,
    buffer: Buffer,
    mimetype: string,
  ): Promise<string> {
    const product = await this.getMyProduct(ownerId, productId);

    // Upload to Storage
    const imageUrl = await this.storageService.uploadProductImage(
      product.shopId,
      productId,
      buffer,
      mimetype,
    );

    // Update product imageUrl
    await this.productsRepository.update(productId, { imageUrl });

    return imageUrl;
  }
}
