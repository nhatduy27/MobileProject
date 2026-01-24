import { Controller, Post, Get, Body, Query, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { VouchersService } from '../vouchers.service';
import { ValidateVoucherDto, AvailableVoucherDto } from '../dto';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { CurrentUser } from '../../../core/decorators/current-user.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';

/**
 * Customer Vouchers Controller
 *
 * Endpoints for customers to browse and validate vouchers
 * Requires Bearer token with CUSTOMER role
 *
 * Base URL: /vouchers
 */
@ApiTags('Customer Vouchers')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.CUSTOMER)
@Controller('vouchers')
export class VouchersController {
  constructor(private readonly vouchersService: VouchersService) {}

  /**
   * GET /vouchers?shopId=xxx
   * Get available vouchers for shop with per-user usage information
   */
  @Get()
  @ApiOperation({
    summary: 'Get available vouchers',
    description: 'Customer gets all active vouchers for a shop with per-user usage info (myUsageCount, myRemainingUses)',
  })
  @ApiQuery({ name: 'shopId', required: true, description: 'Shop ID' })
  @ApiResponse({
    status: 200,
    description: 'Available vouchers retrieved with per-user usage information',
    type: [AvailableVoucherDto],
    schema: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          id: { type: 'string', example: 'voucher_summer20_2024' },
          code: { type: 'string', example: 'SUMMER20' },
          shopId: { type: 'string', example: 'shop_123' },
          type: { type: 'string', example: 'PERCENTAGE', enum: ['PERCENTAGE', 'FIXED_AMOUNT', 'FREE_SHIP'] },
          value: { type: 'number', example: 20 },
          minOrderAmount: { type: 'number', example: 30000 },
          usageLimit: { type: 'number', example: 100 },
          usageLimitPerUser: { type: 'number', example: 3 },
          currentUsage: { type: 'number', example: 25, description: 'Global aggregate usage (all customers)' },
          myUsageCount: { type: 'number', example: 1, description: 'Times current user has used this voucher' },
          myRemainingUses: { type: 'number', example: 2, description: 'Remaining uses for current user' },
          validFrom: { type: 'string', example: '2026-01-21T00:00:00Z' },
          validTo: { type: 'string', example: '2026-12-31T23:59:59Z' },
          name: { type: 'string', example: 'Miễn phí ship 50%' },
          description: { type: 'string', example: 'Giảm 50% phí vận chuyển cho đơn từ 30k' },
          isActive: { type: 'boolean', example: true },
          isDeleted: { type: 'boolean', example: false },
          ownerType: { type: 'string', example: 'SHOP' },
          createdAt: { type: 'string', example: '2026-01-20T10:00:00Z' },
          updatedAt: { type: 'string', example: '2026-01-20T10:00:00Z' },
        },
      },
      example: [
        {
          id: 'voucher_summer20_2024',
          code: 'SUMMER20',
          shopId: 'shop_123',
          type: 'PERCENTAGE',
          value: 20,
          minOrderAmount: 30000,
          usageLimit: 100,
          usageLimitPerUser: 3,
          currentUsage: 25,
          myUsageCount: 1,
          myRemainingUses: 2,
          validFrom: '2026-01-21T00:00:00Z',
          validTo: '2026-12-31T23:59:59Z',
          name: 'Miễn phí ship 50%',
          description: 'Giảm 50% phí vận chuyển cho đơn từ 30k',
          isActive: true,
          isDeleted: false,
          ownerType: 'SHOP',
          createdAt: '2026-01-20T10:00:00Z',
          updatedAt: '2026-01-20T10:00:00Z',
        },
        {
          id: 'voucher_newyear_2024',
          code: 'NEWYEAR24',
          shopId: 'shop_123',
          type: 'FIXED_AMOUNT',
          value: 50000,
          minOrderAmount: 100000,
          usageLimit: 50,
          usageLimitPerUser: 1,
          currentUsage: 49,
          myUsageCount: 1,
          myRemainingUses: 0,
          validFrom: '2026-01-01T00:00:00Z',
          validTo: '2026-01-31T23:59:59Z',
          name: 'Mừng năm mới',
          description: 'Giảm 50k cho đơn từ 100k (dùng 1 lần)',
          isActive: true,
          isDeleted: false,
          ownerType: 'SHOP',
          createdAt: '2025-12-20T10:00:00Z',
          updatedAt: '2025-12-20T10:00:00Z',
        },
      ],
    },
  })
  async getAvailableVouchers(
    @Query('shopId') shopId: string,
    @CurrentUser('uid') userId: string,
  ) {
    const vouchers = await this.vouchersService.getAvailableVouchers(shopId, userId);
    return vouchers;
  }

  /**
   * POST /vouchers/validate
   * Validate voucher (preview discount)
   */
  @Post('validate')
  @ApiOperation({
    summary: 'Validate voucher',
    description: 'Customer validates voucher code and previews discount amount',
  })
  @ApiResponse({
    status: 200,
    description: 'Validation result',
    schema: {
      example: {
        success: true,
        data: {
          valid: true,
          voucherId: 'voucher_abc',
          discountAmount: 7500,
        },
      },
    },
  })
  @ApiResponse({
    status: 200,
    description: 'Validation failed',
    schema: {
      example: {
        success: true,
        data: {
          valid: false,
          voucherId: 'voucher_abc',
          discountAmount: 0,
          errorCode: 'VOUCHER_MIN_ORDER_NOT_MET',
          errorMessage: 'Đơn hàng tối thiểu 30000đ',
        },
      },
    },
  })
  async validateVoucher(@CurrentUser('uid') userId: string, @Body() dto: ValidateVoucherDto) {
    const result = await this.vouchersService.validateVoucher(userId, dto);
    return result;
  }
}
