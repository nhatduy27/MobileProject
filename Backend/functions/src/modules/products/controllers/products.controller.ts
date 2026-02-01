import { Controller, Get, Param, Query } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiQuery } from '@nestjs/swagger';
import { ProductsService } from '../services';
import { ProductFilterDto } from '../dto';

/**
 * Products Controller (Public)
 *
 * Public endpoints for customers to browse products
 * No authentication required
 *
 * Base URL: /products
 *
 * Tasks: PROD-009 to PROD-010
 */
@ApiTags('Products (Customer)')
@Controller('products')
export class ProductsController {
  constructor(private readonly productsService: ProductsService) {}

  /**
   * GET /products
   * Global product feed across all shops
   *
   * PROD-009
   */
  @Get()
  @ApiOperation({
    summary: 'Get product feed',
    description: 'Browse all products across all shops with filters and search',
  })
  @ApiQuery({ name: 'categoryId', required: false, type: String })
  @ApiQuery({ name: 'shopId', required: false, type: String })
  @ApiQuery({ name: 'q', required: false, type: String, description: 'Search keyword' })
  @ApiQuery({ name: 'minPrice', required: false, type: Number })
  @ApiQuery({ name: 'maxPrice', required: false, type: Number })
  @ApiQuery({
    name: 'sort',
    required: false,
    enum: ['newest', 'popular', 'rating', 'price'],
    example: 'popular',
  })
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
              rating: 4.5,
              totalRatings: 50,
              soldCount: 150,
              sortOrder: 0,
              isDeleted: false,
              createdAt: '2026-01-11T10:00:00.000Z',
              updatedAt: '2026-01-11T10:00:00.000Z',
            },
          ],
          total: 100,
          page: 1,
          limit: 20,
        },
      },
    },
  })
  async getProductFeed(@Query() filters: ProductFilterDto) {
    return this.productsService.getProductFeed(filters);
  }

  /**
   * GET /products/:id
   * Get product detail
   *
   * PROD-010
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
          imageUrls: ['https://...'],
          isAvailable: true,
          preparationTime: 15,
          rating: 4.5,
          totalRatings: 50,
          soldCount: 150,
          sortOrder: 0,
          isDeleted: false,
          createdAt: '2026-01-11T10:00:00.000Z',
          updatedAt: '2026-01-11T10:00:00.000Z',
        },
      },
    },
  })
  async getProductDetail(@Param('id') productId: string) {
    return this.productsService.getProductDetail(productId);
  }
}
