import { Controller, Get, Param, Query, ParseIntPipe, DefaultValuePipe } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiQuery } from '@nestjs/swagger';
import { ShopsService } from '../services/shops.service';

/**
 * Shops Controller (Public)
 *
 * Public endpoints for customers to browse shops
 * No authentication required
 *
 * Base URL: /shops
 *
 * Tasks: SHOP-009 to SHOP-014
 */
@ApiTags('Shops (Customer)')
@Controller('shops')
export class ShopsController {
  constructor(private readonly shopsService: ShopsService) {}

  /**
   * GET /shops
   * Get all shops with pagination, search, and filters
   *
   * SHOP-009, SHOP-014
   */
  @Get()
  @ApiOperation({
    summary: 'Get all shops',
    description: 'Browse all shops with pagination, search, and filters',
  })
  @ApiQuery({ name: 'page', required: false, type: Number, example: 1 })
  @ApiQuery({ name: 'limit', required: false, type: Number, example: 20 })
  @ApiQuery({ name: 'status', required: false, type: String, example: 'OPEN' })
  @ApiQuery({ name: 'search', required: false, type: String, example: 'phở' })
  @ApiResponse({
    status: 200,
    description: 'List of shops',
    schema: {
      example: {
        success: true,
        data: {
          shops: [
            {
              id: 'shop_abc',
              name: 'Quán Phở Việt',
              description: 'Phở ngon nhất KTX',
              address: 'Tòa A, Tầng 1',
              rating: 4.5,
              totalRatings: 50,
              isOpen: true,
              openTime: '07:00',
              closeTime: '21:00',
              shipFeePerOrder: 5000,
              minOrderAmount: 20000,
              logoUrl: 'https://...',
              coverImageUrl: 'https://...',
            },
          ],
          total: 15,
          page: 1,
          limit: 20,
        },
      },
    },
  })
  async getAllShops(
    @Query('page', new DefaultValuePipe(1), ParseIntPipe) page: number,
    @Query('limit', new DefaultValuePipe(20), ParseIntPipe) limit: number,
    @Query('status') status?: string,
    @Query('search') search?: string,
  ) {
    return this.shopsService.getAllShops({
      page,
      limit,
      status,
      search,
    });
  }

  /**
   * GET /shops/:id
   * Get shop detail by ID
   *
   * SHOP-010
   */
  @Get(':id')
  @ApiOperation({
    summary: 'Get shop detail',
    description: 'Get detailed information of a specific shop',
  })
  @ApiResponse({
    status: 200,
    description: 'Shop details',
    schema: {
      example: {
        success: true,
        data: {
          id: 'shop_abc',
          name: 'Quán Phở Việt',
          description: 'Phở ngon nhất KTX',
          address: 'Tòa A, Tầng 1',
          phone: '0901234567',
          coverImageUrl: 'https://...',
          logoUrl: 'https://...',
          openTime: '07:00',
          closeTime: '21:00',
          shipFeePerOrder: 5000,
          minOrderAmount: 20000,
          isOpen: true,
          rating: 4.5,
          totalRatings: 50,
          totalOrders: 150,
        },
      },
    },
  })
  @ApiResponse({
    status: 404,
    description: 'Shop not found',
    schema: {
      example: {
        success: false,
        code: 'SHOP_005',
        message: 'Không tìm thấy shop',
        statusCode: 404,
      },
    },
  })
  async getShopById(@Param('id') shopId: string) {
    return this.shopsService.getShopById(shopId);
  }
}
