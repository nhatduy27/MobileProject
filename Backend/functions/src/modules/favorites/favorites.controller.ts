import {
  Controller,
  Get,
  Post,
  Delete,
  Param,
  Query,
  UseGuards,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { AuthGuard } from '../../core/guards/auth.guard';
import { CurrentUser } from '../../core/decorators/current-user.decorator';
import { FavoritesService } from './favorites.service';

/**
 * Favorites Controller
 *
 * Manages user favorite products
 * All endpoints require authentication
 *
 * Base URL: /me/favorites
 *
 * Task: USER-006
 */
@ApiTags('Favorites')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard)
@Controller('me/favorites')
export class FavoritesController {
  constructor(private readonly favoritesService: FavoritesService) {}

  /**
   * POST /me/favorites/products/:productId
   * Add product to favorites
   */
  @Post('products/:productId')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({
    summary: 'Add to favorites',
    description: 'Add a product to user favorites list',
  })
  @ApiResponse({ status: 201, description: 'Added to favorites' })
  @ApiResponse({ status: 409, description: 'Already in favorites' })
  async addFavorite(@CurrentUser() user: any, @Param('productId') productId: string) {
    await this.favoritesService.addFavorite(user.uid, productId);
    return {
      success: true,
      message: 'Đã thêm vào yêu thích',
    };
  }

  /**
   * DELETE /me/favorites/products/:productId
   * Remove product from favorites
   */
  @Delete('products/:productId')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Remove from favorites',
    description: 'Remove a product from user favorites list',
  })
  @ApiResponse({ status: 200, description: 'Removed from favorites' })
  @ApiResponse({ status: 404, description: 'Not in favorites' })
  async removeFavorite(@CurrentUser() user: any, @Param('productId') productId: string) {
    await this.favoritesService.removeFavorite(user.uid, productId);
    return {
      success: true,
      message: 'Đã xóa khỏi yêu thích',
    };
  }

  /**
   * GET /me/favorites/products
   * List favorite products with pagination
   */
  @Get('products')
  @ApiOperation({
    summary: 'List favorites',
    description: 'Get paginated list of favorite products',
  })
  @ApiQuery({ name: 'page', required: false, type: Number, example: 1 })
  @ApiQuery({ name: 'limit', required: false, type: Number, example: 20 })
  @ApiResponse({
    status: 200,
    description: 'Favorites list',
    schema: {
      example: {
        success: true,
        data: [
          {
            productId: 'prod_123',
            productName: 'Cơm sườn',
            productPrice: 35000,
            productImage: '...',
            shopId: 'shop_abc',
            shopName: 'Quán A Mập',
            createdAt: '2026-01-05T10:00:00Z',
          },
        ],
        pagination: {
          total: 10,
          page: 1,
          limit: 20,
          hasMore: false,
        },
      },
    },
  })
  async listFavorites(
    @CurrentUser() user: any,
    @Query('page') page?: string,
    @Query('limit') limit?: string,
  ) {
    const result = await this.favoritesService.listFavorites(
      user.uid,
      page ? parseInt(page, 10) : 1,
      limit ? parseInt(limit, 10) : 20,
    );
    return {
      success: true,
      data: result.data,
      pagination: result.pagination,
    };
  }

  /**
   * GET /me/favorites/products/:productId
   * Check if product is favorited
   */
  @Get('products/:productId')
  @ApiOperation({
    summary: 'Check if favorited',
    description: 'Check if a specific product is in favorites',
  })
  @ApiResponse({
    status: 200,
    description: 'Favorite status',
    schema: {
      example: {
        success: true,
        data: {
          isFavorited: true,
        },
      },
    },
  })
  async checkFavorite(@CurrentUser() user: any, @Param('productId') productId: string) {
    const isFavorited = await this.favoritesService.isFavorited(user.uid, productId);
    return {
      success: true,
      data: {
        isFavorited,
      },
    };
  }
}
