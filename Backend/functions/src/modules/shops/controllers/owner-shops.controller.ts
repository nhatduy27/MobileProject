import {
  Controller,
  Get,
  Post,
  Put,
  Body,
  UseGuards,
  HttpCode,
  HttpStatus,
  Query,
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
} from '@nestjs/swagger';
import { FileFieldsInterceptor } from '@nestjs/platform-express';
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
        data: {
          id: 'shop_abc123',
          ownerId: 'uid_owner',
          ownerName: 'Nguyễn Văn A',
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
          isOpen: false,
          status: 'OPEN',
          rating: 0,
          totalRatings: 0,
          totalOrders: 0,
          totalRevenue: 0,
          subscription: {
            status: 'TRIAL',
            startDate: '2026-01-11T10:00:00.000Z',
            trialEndDate: '2026-01-18T10:00:00.000Z',
            currentPeriodEnd: '2026-01-18T10:00:00.000Z',
            nextBillingDate: null,
            autoRenew: true,
          },
          createdAt: '2026-01-11T10:00:00.000Z',
          updatedAt: '2026-01-11T10:00:00.000Z',
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
  @UseInterceptors(
    FileFieldsInterceptor([
      { name: 'coverImage', maxCount: 1 },
      { name: 'logo', maxCount: 1 },
    ]),
  )
  @ApiConsumes('multipart/form-data')
  async createShop(
    @CurrentUser('uid') ownerId: string,
    @CurrentUser('displayName') ownerName: string,
    @Body() dto: CreateShopDto,
    @UploadedFiles()
    files: {
      coverImage?: Express.Multer.File[];
      logo?: Express.Multer.File[];
    },
  ) {
    // Validate files
    if (!files?.coverImage?.[0] || !files?.logo?.[0]) {
      throw new BadRequestException('Vui lòng upload đầy đủ 2 ảnh: Ảnh bìa và Logo');
    }

    return this.shopsService.createShop(
      ownerId,
      ownerName,
      dto,
      files.coverImage[0],
      files.logo[0],
    );
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
  @UseInterceptors(
    FileFieldsInterceptor([
      { name: 'coverImage', maxCount: 1 },
      { name: 'logo', maxCount: 1 },
    ]),
  )
  @ApiConsumes('multipart/form-data')
  @ApiOperation({
    summary: 'Update shop',
    description: 'Update shop information (name, description, operating hours, images, etc.)',
  })
  @ApiResponse({
    status: 200,
    description: 'Shop updated successfully',
  })
  async updateShop(
    @CurrentUser('uid') ownerId: string,
    @Body() dto: UpdateShopDto,
    @UploadedFiles()
    files?: {
      coverImage?: Express.Multer.File[];
      logo?: Express.Multer.File[];
    },
  ) {
    await this.shopsService.updateShop(ownerId, dto, files?.coverImage?.[0], files?.logo?.[0]);
    return { message: 'Cập nhật shop thành công' };
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
    description: 'Open or close shop. Can only open if subscription is active/trial.',
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
  async toggleShopStatus(@CurrentUser('uid') ownerId: string, @Body() dto: ToggleShopStatusDto) {
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
    description: `Get shop analytics including revenue, orders, top products, and recent orders.
    
    BEHAVIOR:
    - When from/to parameters are provided (both required):
      * "today" = orders delivered on the 'to' date
      * "thisWeek" = orders delivered in the 7 days ending on 'to' date
      * "thisMonth" = orders delivered from 1st of 'to' month to 'to' date
      * All time buckets are clamped within [from, to] range
    - When from/to are NOT provided:
      * Time buckets calculated relative to server's current time
    
    TIMESTAMP RULES:
    - Revenue/orderCount use deliveredAt (when order was delivered)
    - Only DELIVERED orders count toward revenue/orderCount/topProducts
    - ordersByStatus counts all orders (by createdAt)
    - recentOrders sorted by createdAt desc (shows order activity)
    - pendingOrders counts orders with status in [PENDING, CONFIRMED, PREPARING, READY, SHIPPING] created within "today" bucket
    
    DATE FORMAT: YYYY-MM-DD (e.g., 2026-01-01)
    `,
  })
  @ApiQuery({ 
    name: 'from', 
    required: false, 
    type: String, 
    example: '2026-01-01',
    description: 'Start date (YYYY-MM-DD). Must be provided with "to" parameter.'
  })
  @ApiQuery({ 
    name: 'to', 
    required: false, 
    type: String, 
    example: '2026-01-31',
    description: 'End date (YYYY-MM-DD). Must be provided with "from" parameter. Time buckets anchor to this date.'
  })
  @ApiResponse({
    status: 200,
    description: 'Shop analytics',
    schema: {
      example: {
        success: true,
        data: {
          today: {
            revenue: 500000,
            orderCount: 25,
            avgOrderValue: 20000,
            pendingOrders: 3,
          },
          thisWeek: {
            revenue: 2500000,
            orderCount: 120,
            avgOrderValue: 20833,
          },
          thisMonth: {
            revenue: 10000000,
            orderCount: 500,
          },
          ordersByStatus: {
            PENDING: 3,
            CONFIRMED: 5,
            PREPARING: 2,
            READY: 1,
            DELIVERING: 4,
            COMPLETED: 140,
            CANCELLED: 5,
          },
          topProducts: [
            {
              id: 'prod_1',
              name: 'Cơm sườn',
              soldCount: 50,
              revenue: 1750000,
            },
          ],
          recentOrders: [
            {
              id: 'order_1',
              orderNumber: 'ORD-20260111-001',
              status: 'COMPLETED',
              total: 50000,
              createdAt: '2026-01-11T10:00:00.000Z',
            },
          ],
        },
      },
    },
  })
  async getShopDashboard(
    @CurrentUser('uid') ownerId: string,
    @Query('from') from?: string,
    @Query('to') to?: string,
  ) {
    const shop = await this.shopsService.getMyShop(ownerId);
    return this.analyticsService.getShopAnalytics(shop.id, from, to);
  }
}
