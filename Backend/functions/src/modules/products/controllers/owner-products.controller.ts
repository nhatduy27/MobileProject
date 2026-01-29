import {
  Controller,
  Post,
  Get,
  Put,
  Delete,
  Body,
  Param,
  Query,
  UseGuards,
  UseInterceptors,
  UploadedFile,
  BadRequestException,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiQuery,
  ApiConsumes,
  ApiBody,
} from '@nestjs/swagger';
import { FileInterceptor } from '@nestjs/platform-express';
import { ProductsService } from '../services';
import {
  CreateProductDto,
  UpdateProductDto,
  ToggleAvailabilityDto,
  OwnerProductFilterDto,
} from '../dto';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { CurrentUser } from '../../../core/decorators/current-user.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';

/**
 * Owner Products Controller
 *
 * Authenticated endpoints for owners to manage their shop's products
 * Requires Bearer token with OWNER role
 *
 * Base URL: /owner/products
 *
 * Tasks: PROD-001 to PROD-008
 */
@ApiTags('Owner Products')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.OWNER)
@Controller('owner/products')
export class OwnerProductsController {
  constructor(private readonly productsService: ProductsService) {}

  /**
   * POST /owner/products
   * Create a new product
   *
   * PROD-001
   */
  @Post()
  @UseInterceptors(FileInterceptor('image'))
  @ApiConsumes('multipart/form-data')
  @ApiOperation({
    summary: 'Create product',
    description: 'Owner creates a new product for their shop',
  })
  @ApiResponse({
    status: 201,
    description: 'Product created successfully',
    schema: {
      example: {
        success: true,
        data: {
          id: 'prod_abc',
          shopId: 'shop_123',
          shopName: 'Quán Phở Việt',
          name: 'Cơm sườn nướng',
          description: 'Cơm sườn nướng mật ong + trứng',
          price: 35000,
          categoryId: 'cat_1',
          categoryName: 'Cơm',
          imageUrl: 'https://...',
          isAvailable: true,
          preparationTime: 15,
          rating: 0,
          totalRatings: 0,
          soldCount: 0,
          sortOrder: 0,
          isDeleted: false,
          createdAt: '2026-01-11T10:00:00.000Z',
          updatedAt: '2026-01-11T10:00:00.000Z',
        },
      },
    },
  })
  async createProduct(
    @CurrentUser('uid') ownerId: string,
    @Body() dto: CreateProductDto,
    @UploadedFile() file?: Express.Multer.File,
  ) {
    // Debug logging
    console.log('=== CREATE PRODUCT REQUEST ===');
    console.log('ownerId:', ownerId);
    console.log('dto:', JSON.stringify(dto, null, 2));
    console.log('dto.price:', dto.price, 'type:', typeof dto.price);
    console.log('dto.preparationTime:', dto.preparationTime, 'type:', typeof dto.preparationTime);
    console.log('file:', file ? { filename: file.originalname, size: file.size } : 'No file');
    console.log('==============================');
    
    if (!file) {
      throw new BadRequestException('Vui lòng upload ảnh sản phẩm');
    }
    return this.productsService.createProduct(ownerId, dto, file);
  }

  /**
   * GET /owner/products
   * Get all products of owner's shop
   *
   * PROD-002
   */
  @Get()
  @ApiOperation({
    summary: 'Get my products',
    description: "Get all products of owner's shop with filters",
  })
  @ApiQuery({ name: 'categoryId', required: false, type: String })
  @ApiQuery({ name: 'isAvailable', required: false, type: String, enum: ['true', 'false'] })
  @ApiQuery({ name: 'page', required: false, type: Number, example: 1 })
  @ApiQuery({ name: 'limit', required: false, type: Number, example: 20 })
  @ApiResponse({
    status: 200,
    description: 'List of products',
    schema: {
      example: {
        success: true,
        data: {
          products: [
            {
              id: 'prod_abc',
              shopId: 'shop_123',
              shopName: 'Quán Phở Việt',
              name: 'Cơm sườn nướng',
              description: 'Cơm sườn nướng mật ong + trứng',
              price: 35000,
              categoryId: 'cat_1',
              categoryName: 'Cơm',
              imageUrl: 'https://...',
              isAvailable: true,
              preparationTime: 15,
              rating: 0,
              totalRatings: 0,
              soldCount: 50,
              sortOrder: 0,
              isDeleted: false,
              createdAt: '2026-01-11T10:00:00.000Z',
              updatedAt: '2026-01-11T10:00:00.000Z',
            },
          ],
          total: 15,
          page: 1,
          limit: 20,
        },
      },
    },
  })
  async getMyProducts(
    @CurrentUser('uid') ownerId: string,
    @Query() filters: OwnerProductFilterDto,
  ) {
    return this.productsService.getMyProducts(ownerId, filters);
  }

  /**
   * GET /owner/products/:id
   * Get product detail
   *
   * PROD-003
   */
  @Get(':id')
  @ApiOperation({
    summary: 'Get product detail',
    description: 'Get detailed information of a specific product',
  })
  @ApiResponse({
    status: 200,
    description: 'Product details',
    schema: {
      example: {
        success: true,
        data: {
          id: 'prod_abc',
          shopId: 'shop_123',
          shopName: 'Quán Phở Việt',
          name: 'Cơm sườn nướng',
          description: 'Cơm sườn nướng mật ong + trứng',
          price: 35000,
          categoryId: 'cat_1',
          categoryName: 'Cơm',
          imageUrl: 'https://...',
          isAvailable: true,
          preparationTime: 15,
          rating: 0,
          totalRatings: 0,
          soldCount: 0,
          sortOrder: 0,
          isDeleted: false,
          createdAt: '2026-01-11T10:00:00.000Z',
          updatedAt: '2026-01-11T10:00:00.000Z',
        },
      },
    },
  })
  async getMyProduct(@CurrentUser('uid') ownerId: string, @Param('id') productId: string) {
    return this.productsService.getMyProduct(ownerId, productId);
  }

  /**
   * PUT /owner/products/:id
   * Update product
   *
   * PROD-006 - Price Lock Rule applies
   */
  @Put(':id')
  @UseInterceptors(FileInterceptor('image'))
  @ApiConsumes('multipart/form-data')
  @ApiOperation({
    summary: 'Update product',
    description: 'Update product information. Cannot change price when shop is open.',
  })
  @ApiResponse({
    status: 200,
    description: 'Product updated successfully',
  })
  @ApiResponse({
    status: 409,
    description: 'Cannot change price when shop is open',
  })
  async updateProduct(
    @CurrentUser('uid') ownerId: string,
    @Param('id') productId: string,
    @Body() dto: UpdateProductDto,
    @UploadedFile() file?: Express.Multer.File,
  ) {
    await this.productsService.updateProduct(ownerId, productId, dto, file);
    return { message: 'Cập nhật sản phẩm thành công' };
  }

  /**
   * PUT /owner/products/:id/availability
   * Toggle product availability
   *
   * PROD-007
   */
  @Put(':id/availability')
  @ApiOperation({
    summary: 'Toggle product availability',
    description: 'Mark product as available or unavailable',
  })
  @ApiResponse({
    status: 200,
    description: 'Availability updated successfully',
  })
  async toggleAvailability(
    @CurrentUser('uid') ownerId: string,
    @Param('id') productId: string,
    @Body() dto: ToggleAvailabilityDto,
  ) {
    await this.productsService.toggleAvailability(ownerId, productId, dto);
    return { message: 'Cập nhật trạng thái sản phẩm thành công' };
  }

  /**
   * DELETE /owner/products/:id
   * Delete product (soft delete)
   *
   * PROD-008
   */
  @Delete(':id')
  @ApiOperation({
    summary: 'Delete product',
    description: 'Soft delete a product (set isDeleted = true)',
  })
  @ApiResponse({
    status: 200,
    description: 'Product deleted successfully',
  })
  async deleteProduct(@CurrentUser('uid') ownerId: string, @Param('id') productId: string) {
    await this.productsService.deleteProduct(ownerId, productId);
    return { message: 'Xóa sản phẩm thành công' };
  }

  /**
   * POST /owner/products/:id/image
   * Upload product image
   *
   * PROD-005
   */
  @Post(':id/image')
  @UseInterceptors(FileInterceptor('image'))
  @ApiConsumes('multipart/form-data')
  @ApiOperation({
    summary: 'Upload product image',
    description: 'Upload product image. Accepts JPEG/PNG. Max 5MB.',
  })
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        image: {
          type: 'string',
          format: 'binary',
        },
      },
    },
  })
  @ApiResponse({
    status: 200,
    description: 'Image uploaded successfully',
    schema: {
      example: {
        success: true,
        data: {
          imageUrl: 'https://firebasestorage.googleapis.com/...',
        },
      },
    },
  })
  @ApiResponse({
    status: 400,
    description: 'Invalid file',
  })
  async uploadProductImage(
    @CurrentUser('uid') ownerId: string,
    @Param('id') productId: string,
    @UploadedFile() file: Express.Multer.File,
  ) {
    if (!file) {
      throw new BadRequestException('No file uploaded');
    }

    // Validate file type
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png'];
    if (!allowedTypes.includes(file.mimetype)) {
      throw new BadRequestException('Only JPEG and PNG images are allowed');
    }

    // Validate file size (5MB max)
    const maxSize = 5 * 1024 * 1024;
    if (file.size > maxSize) {
      throw new BadRequestException('File size must not exceed 5MB');
    }

    const imageUrl = await this.productsService.uploadProductImage(
      ownerId,
      productId,
      file.buffer,
      file.mimetype,
    );

    return { imageUrl };
  }
}
