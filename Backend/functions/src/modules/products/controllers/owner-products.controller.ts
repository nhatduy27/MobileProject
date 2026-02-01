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
  UploadedFiles,
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
import { CloudFunctionFilesInterceptor, BodyValidationInterceptor } from '../../../core/interceptors';
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
  @UseInterceptors(BodyValidationInterceptor, CloudFunctionFilesInterceptor('images', 10))
  @ApiConsumes('multipart/form-data')
  @ApiOperation({
    summary: 'Create product',
    description: `Owner creates a new product for their shop.

**IMPORTANT: Request Format**
- Content-Type: multipart/form-data (do NOT set manually - let client library set it)
- All fields are sent as form fields, images as file parts

**Common Mistakes:**
- ❌ Manually setting Content-Type header with FormData (breaks boundary)
- ❌ Sending JSON string in body with FormData Content-Type
- ❌ Mixing JSON body with multipart Content-Type

**Correct Usage (Android/Retrofit):**
\`\`\`kotlin
@Multipart
@POST("owner/products")
suspend fun createProduct(
    @Part("name") name: RequestBody,
    @Part("price") price: RequestBody,
    @Part images: List<MultipartBody.Part>
): Response<ProductResponse>
\`\`\``,
  })
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        name: { type: 'string', example: 'Cơm sườn nướng' },
        description: { type: 'string', example: 'Cơm sườn nướng mật ong + trứng' },
        price: { type: 'number', example: 35000 },
        categoryId: { type: 'string', example: 'cat_123' },
        preparationTime: { type: 'number', example: 15 },
        images: {
          type: 'array',
          items: {
            type: 'string',
            format: 'binary',
          },
        },
      },
      required: ['name', 'description', 'price', 'categoryId', 'preparationTime', 'images'],
    },
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
          imageUrls: ['https://...'],
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
    @UploadedFiles() files?: Express.Multer.File[],
  ) {
    // Debug logging
    console.log('=== CREATE PRODUCT REQUEST ===');
    console.log('ownerId:', ownerId);
    console.log('dto:', JSON.stringify(dto, null, 2));
    console.log('dto.price:', dto.price, 'type:', typeof dto.price);
    console.log('dto.preparationTime:', dto.preparationTime, 'type:', typeof dto.preparationTime);
    console.log(
      'files:',
      files && files.length > 0
        ? files.map((f) => ({ filename: f.originalname, size: f.size }))
        : 'No files',
    );
    console.log('==============================');

    if (!files || files.length === 0) {
      throw new BadRequestException('Vui lòng upload ảnh sản phẩm');
    }
    return this.productsService.createProduct(ownerId, dto, files);
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
              imageUrls: ['https://...'],
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
  @UseInterceptors(BodyValidationInterceptor, CloudFunctionFilesInterceptor('images', 10))
  @ApiConsumes('multipart/form-data')
  @ApiOperation({
    summary: 'Update product',
    description: 'Update product information. Cannot change price when shop is open.',
  })
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        name: { type: 'string', example: 'Com su?n nu?ng' },
        description: { type: 'string', example: 'Com su?n nu?ng m?t ong + tr?ng' },
        price: { type: 'number', example: 35000 },
        categoryId: { type: 'string', example: 'cat_123' },
        preparationTime: { type: 'number', example: 15 },
        images: {
          type: 'array',
          items: {
            type: 'string',
            format: 'binary',
          },
        },
      },
    },
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
    @UploadedFiles() files?: Express.Multer.File[],
  ) {
    await this.productsService.updateProduct(ownerId, productId, dto, files);
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
   * POST /owner/products/:id/images
   * Upload multiple product images
   */
  @Post(':id/images')
  @UseInterceptors(CloudFunctionFilesInterceptor('images', 10))
  @ApiConsumes('multipart/form-data')
  @ApiOperation({
    summary: 'Upload multiple product images',
    description: 'Upload multiple product images. Accepts JPEG/PNG. Max 5MB each.',
  })
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        images: {
          type: 'array',
          items: {
            type: 'string',
            format: 'binary',
          },
        },
      },
    },
  })
  @ApiResponse({
    status: 200,
    description: 'Images uploaded successfully',
    schema: {
      example: {
        success: true,
        data: {
          imageUrls: ['https://firebasestorage.googleapis.com/...'],
        },
      },
    },
  })
  @ApiResponse({
    status: 400,
    description: 'Invalid files',
  })
  async uploadProductImages(
    @CurrentUser('uid') ownerId: string,
    @Param('id') productId: string,
    @UploadedFiles() files: Express.Multer.File[],
  ) {
    if (!files || files.length === 0) {
      throw new BadRequestException('No files uploaded');
    }

    const imageUrls = await this.productsService.uploadProductImages(ownerId, productId, files);

    return { imageUrls };
  }
}


