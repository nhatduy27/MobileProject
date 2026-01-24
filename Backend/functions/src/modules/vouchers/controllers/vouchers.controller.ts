import { Controller, Post, Get, Body, Query, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { VouchersService } from '../vouchers.service';
import { ValidateVoucherDto } from '../dto';
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
   * Get available vouchers for shop
   */
  @Get()
  @ApiOperation({
    summary: 'Get available vouchers',
    description: 'Customer gets all active vouchers for a shop',
  })
  @ApiQuery({ name: 'shopId', required: true, description: 'Shop ID' })
  @ApiResponse({
    status: 200,
    description: 'Available vouchers retrieved',
    schema: {
      example: {
        success: true,
        data: [
          {
            id: 'voucher_abc',
            code: 'FREESHIP50',
            shopId: 'shop_123',
            type: 'FREE_SHIP',
            value: 50,
            minOrderAmount: 30000,
            usageLimit: 100,
            currentUsage: 25,
            validFrom: '2026-01-21T00:00:00Z',
            validTo: '2026-12-31T23:59:59Z',
            name: 'Miễn phí ship 50%',
            description: 'Giảm 50% phí vận chuyển cho đơn từ 30k',
          },
        ],
      },
    },
  })
  async getAvailableVouchers(@Query('shopId') shopId: string) {
    const vouchers = await this.vouchersService.getAvailableVouchers(shopId);
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
