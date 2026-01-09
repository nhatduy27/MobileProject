import {
  Controller,
  Get,
  Post,
  Put,
  Body,
  UseGuards,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth } from '@nestjs/swagger';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { CurrentUser } from '../../../core/decorators/current-user.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';
import { ShopsService } from '../services/shops.service';
import { AnalyticsService } from '../services/analytics.service';
import { CreateShopDto, UpdateShopDto, ToggleShopStatusDto } from '../dto';

/**
 * Owner Shops Controller
 *
 * Handles shop management for owners
 * All endpoints require OWNER role
 *
 * Base URL: /owner/shop
 *
 * Tasks: SHOP-002 to SHOP-008
 */
@ApiTags('Owner - Shop Management')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.OWNER)
@Controller('owner/shop')
export class OwnerShopsController {
  constructor(
    private readonly shopsService: ShopsService,
    private readonly analyticsService: AnalyticsService,
  ) {}

  // ==================== CRUD Operations ====================

  /**
   * POST /owner/shop
   * Create a new shop
   * Business Rule: 1 Owner = 1 Shop
   *
   * SHOP-002
   */
  @Post()
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({
    summary: 'Create shop',
    description: 'Create a new shop. One owner can only have one shop.',
  })
  @ApiResponse({
    status: 201,
    description: 'Shop created successfully',
    schema: {
      example: {
        success: true,
        message: 'Tạo shop thành công',
        data: {
          id: 'shop_abc123',
          name: 'Quán Phở Việt',
          ownerId: 'uid_owner',
          subscription: {
            status: 'TRIAL',
            trialEndDate: '2026-01-16T00:00:00Z',
          },
        },
      },
    },
  })
  @ApiResponse({
    status: 409,
    description: 'Owner already has a shop',
    schema: {
      example: {
        success: false,
        code: 'SHOP_001',
        message: 'Bạn đã có shop rồi. Mỗi chủ shop chỉ được tạo 1 shop.',
        statusCode: 409,
      },
    },
  })
  async createShop(
    @CurrentUser('uid') ownerId: string,
    @CurrentUser('displayName') ownerName: string,
    @Body() dto: CreateShopDto,
  ) {
    const shop = await this.shopsService.createShop(ownerId, ownerName, dto);
    return shop;
  }

  /**
   * GET /owner/shop
   * Get my shop information
   *
   * SHOP-003
   */
  @Get()
  @ApiOperation({
    summary: 'Get my shop',
    description: 'Get shop information for current owner',
  })
  @ApiResponse({
    status: 200,
    description: 'Shop information',
  })
  @ApiResponse({
    status: 404,
    description: 'Shop not found',
    schema: {
      example: {
        success: false,
        code: 'SHOP_003',
        message: 'Bạn chưa có shop nào',
        statusCode: 404,
      },
    },
  })
  async getMyShop(@CurrentUser('uid') ownerId: string) {
    return this.shopsService.getMyShop(ownerId);
  }

  /**
   * PUT /owner/shop
   * Update shop information
   *
   * SHOP-004
   */
  @Put()
  @ApiOperation({
    summary: 'Update shop',
    description: 'Update shop information (name, description, operating hours, etc.)',
  })
  @ApiResponse({
    status: 200,
    description: 'Shop updated successfully',
  })
  async updateShop(@CurrentUser('uid') ownerId: string, @Body() dto: UpdateShopDto) {
    return this.shopsService.updateShop(ownerId, dto);
  }

  /**
   * PUT /owner/shop/status
   * Toggle shop open/close
   *
   * SHOP-005
   */
  @Put('status')
  @ApiOperation({
    summary: 'Toggle shop status',
    description: 'Open or close shop. Can only open if subscription is active.',
  })
  @ApiResponse({
    status: 200,
    description: 'Shop status updated',
  })
  @ApiResponse({
    status: 400,
    description: 'Cannot open shop (subscription not active)',
    schema: {
      example: {
        success: false,
        code: 'SHOP_004',
        message: 'Không thể mở shop. Subscription chưa active hoặc đã hết hạn.',
        statusCode: 400,
      },
    },
  })
  async toggleShopStatus(
    @CurrentUser('uid') ownerId: string,
    @Body() dto: ToggleShopStatusDto,
  ) {
    await this.shopsService.toggleShopStatus(ownerId, dto.isOpen);
    return { message: dto.isOpen ? 'Mở shop thành công' : 'Đóng shop thành công' };
  }

  // ==================== Dashboard & Analytics ====================

  /**
   * GET /owner/shop/dashboard
   * Get shop analytics for dashboard
   *
   * SHOP-006
   */
  @Get('dashboard')
  @ApiOperation({
    summary: 'Get shop dashboard',
    description:
      'Get shop analytics including revenue, orders, top products, and recent reviews',
  })
  @ApiResponse({
    status: 200,
    description: 'Shop analytics',
    schema: {
      example: {
        success: true,
        data: {
          totalRevenue: 10000000,
          todayRevenue: 500000,
          weekRevenue: 2000000,
          monthRevenue: 5000000,
          totalOrders: 150,
          pendingOrders: 5,
          completedOrders: 140,
          cancelledOrders: 5,
          topProducts: [
            {
              productId: 'prod_1',
              productName: 'Cơm sườn',
              soldCount: 50,
              revenue: 1750000,
            },
          ],
          averageRating: 4.5,
          totalRatings: 50,
        },
      },
    },
  })
  async getShopDashboard(@CurrentUser('uid') ownerId: string) {
    const shop = await this.shopsService.getMyShop(ownerId);
    return this.analyticsService.getShopAnalytics(shop.id);
  }
}
