import { Controller, Get, Query } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiQuery } from '@nestjs/swagger';
import { ProductSearchService } from '../services';

/**
 * Search Controller
 *
 * Provides fuzzy search endpoints for products
 * No authentication required
 *
 * Base URL: /search
 */
@ApiTags('Search')
@Controller('search')
export class SearchController {
  constructor(private readonly searchService: ProductSearchService) {}

  /**
   * GET /search/products
   * Fuzzy search products by name with Vietnamese diacritics support
   */
  @Get('products')
  @ApiOperation({
    summary: 'Fuzzy search products',
    description:
      'Search products with typo tolerance and Vietnamese diacritics support. ' +
      'E.g., searching "pho bo" will match "Phở bò"',
  })
  @ApiQuery({ name: 'q', required: true, type: String, description: 'Search query (min 2 chars)' })
  @ApiQuery({ name: 'shopId', required: false, type: String })
  @ApiQuery({ name: 'categoryId', required: false, type: String })
  @ApiQuery({ name: 'minPrice', required: false, type: Number })
  @ApiQuery({ name: 'maxPrice', required: false, type: Number })
  @ApiQuery({ name: 'limit', required: false, type: Number, example: 20 })
  @ApiResponse({
    status: 200,
    description: 'Search results',
    schema: {
      example: {
        success: true,
        data: {
          products: [
            {
              id: 'prod_abc',
              name: 'Phở bò',
              nameNormalized: 'pho bo',
              shopId: 'shop_123',
              shopName: 'Quán Phở Việt',
              categoryId: 'cat_1',
              categoryName: 'Món nước',
              price: 45000,
              imageUrls: ['https://...'],
              isAvailable: true,
              rating: 4.5,
              soldCount: 200,
            },
          ],
          total: 1,
        },
      },
    },
  })
  async searchProducts(
    @Query('q') q: string,
    @Query('shopId') shopId?: string,
    @Query('categoryId') categoryId?: string,
    @Query('minPrice') minPrice?: string,
    @Query('maxPrice') maxPrice?: string,
    @Query('limit') limit?: string,
  ) {
    const results = await this.searchService.searchProducts(q, {
      shopId,
      categoryId,
      minPrice: minPrice ? Number(minPrice) : undefined,
      maxPrice: maxPrice ? Number(maxPrice) : undefined,
      limit: limit ? Number(limit) : 20,
    });

    return results;
  }
}
