import { Controller, Post, Get, Put, Delete, Body, Param, Query, UseGuards } from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiQuery,
  ApiParam,
  ApiBody,
  ApiExtraModels,
  getSchemaPath,
} from '@nestjs/swagger';
import { VouchersService } from '../vouchers.service';
import { CreateVoucherDto, UpdateVoucherDto, UpdateVoucherStatusDto } from '../dto';
import { ShopsService } from '../../shops/services/shops.service';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { CurrentUser } from '../../../core/decorators/current-user.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';

/**
 * Owner Vouchers Controller
 *
 * Endpoints for shop owners to manage vouchers
 * Requires Bearer token with OWNER role
 *
 * Base URL: /owner/vouchers
 */
@ApiTags('Owner Vouchers')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.OWNER)
@ApiExtraModels(CreateVoucherDto)
@Controller('owner/vouchers')
export class OwnerVouchersController {
  constructor(
    private readonly vouchersService: VouchersService,
    private readonly shopsService: ShopsService,
  ) {}

  /**
   * POST /owner/vouchers
   * Create voucher
   */
  @Post()
  @ApiOperation({
    summary: 'Create voucher',
    description: 'Owner creates a new voucher for their shop',
  })
  @ApiBody({
    description: 'Voucher creation request with conditional maxDiscount field based on type',
    schema: {
      oneOf: [
        {
          allOf: [
            { $ref: getSchemaPath(CreateVoucherDto) },
            {
              properties: {
                type: { enum: ['PERCENTAGE'] },
                maxDiscount: {
                  type: 'number',
                  description: 'REQUIRED for PERCENTAGE - max discount cap in currency units',
                  example: 25000,
                },
              },
              required: ['type', 'maxDiscount'],
            },
          ],
        },
        {
          allOf: [
            { $ref: getSchemaPath(CreateVoucherDto) },
            {
              properties: {
                type: { enum: ['FIXED_AMOUNT'] },
                maxDiscount: {
                  oneOf: [
                    { type: 'number' },
                    { type: 'null' },
                  ],
                  description: 'OPTIONAL for FIXED_AMOUNT - ignored if provided',
                  example: null,
                },
              },
              required: ['type'],
            },
          ],
        },
      ],
    },
    examples: {
      PERCENTAGE: {
        summary: 'PERCENTAGE type (20% off, max capped at 25k) - maxDiscount REQUIRED',
        value: {
          code: 'SUMMER20',
          type: 'PERCENTAGE',
          value: 20,
          maxDiscount: 25000,
          minOrderAmount: 50000,
          usageLimit: 500,
          usageLimitPerUser: 3,
          validFrom: '2026-06-01T00:00:00Z',
          validTo: '2026-08-31T23:59:59Z',
          name: 'Hè ơi giảm 20%',
          description: 'Giảm 20% giá trị đơn hàng, tối đa 25k cho đơn từ 50k trở lên',
        },
      },
      FIXED_AMOUNT: {
        summary: 'FIXED_AMOUNT type (fixed 15k off) - maxDiscount OPTIONAL',
        value: {
          code: 'NEWYEAR15',
          type: 'FIXED_AMOUNT',
          value: 15000,
          minOrderAmount: 30000,
          usageLimit: 100,
          usageLimitPerUser: 1,
          validFrom: '2026-01-01T00:00:00Z',
          validTo: '2026-01-31T23:59:59Z',
          name: 'Năm mới giảm 15k',
          description: 'Giảm cố định 15k cho đơn từ 30k',
        },
      },
    },
  })
  @ApiResponse({
    status: 201,
    description: 'Voucher created successfully',
    schema: {
      examples: {
        PERCENTAGE: {
          summary: 'PERCENTAGE voucher created',
          value: {
            success: true,
            data: {
              id: 'voucher_summer20_001',
              code: 'SUMMER20',
              shopId: 'shop_ktx_001',
              type: 'PERCENTAGE',
              value: 20,
              maxDiscount: 25000,
              minOrderAmount: 50000,
              usageLimit: 500,
              usageLimitPerUser: 3,
              currentUsage: 0,
              validFrom: '2026-06-01T00:00:00Z',
              validTo: '2026-08-31T23:59:59Z',
              isActive: true,
              ownerType: 'SHOP',
              name: 'Hè ơi giảm 20%',
              description: 'Giảm 20% giá trị đơn hàng, tối đa 25k cho đơn từ 50k trở lên',
              isDeleted: false,
              createdAt: '2026-01-24T10:00:00.000Z',
              updatedAt: '2026-01-24T10:00:00.000Z',
            },
            timestamp: '2026-01-24T10:00:00.000Z',
          },
        },
        FIXED_AMOUNT: {
          summary: 'FIXED_AMOUNT voucher created',
          value: {
            success: true,
            data: {
              id: 'voucher_newyear15_002',
              code: 'NEWYEAR15',
              shopId: 'shop_ktx_001',
              type: 'FIXED_AMOUNT',
              value: 15000,
              maxDiscount: null,
              minOrderAmount: 30000,
              usageLimit: 100,
              usageLimitPerUser: 1,
              currentUsage: 0,
              validFrom: '2026-01-01T00:00:00Z',
              validTo: '2026-01-31T23:59:59Z',
              isActive: true,
              ownerType: 'SHOP',
              name: 'Năm mới giảm 15k',
              description: 'Giảm cố định 15k cho đơn từ 30k',
              isDeleted: false,
              createdAt: '2026-01-24T11:00:00.000Z',
              updatedAt: '2026-01-24T11:00:00.000Z',
            },
            timestamp: '2026-01-24T11:00:00.000Z',
          },
        },
        FREE_SHIP: {
          summary: 'FREE_SHIP voucher created',
          value: {
            success: true,
            data: {
              id: 'voucher_freeship50_003',
              code: 'FREESHIP50',
              shopId: 'shop_ktx_001',
              type: 'FREE_SHIP',
              value: 50,
              maxDiscount: null,
              minOrderAmount: 40000,
              usageLimit: 200,
              usageLimitPerUser: 2,
              currentUsage: 0,
              validFrom: '2026-01-21T00:00:00Z',
              validTo: '2026-12-31T23:59:59Z',
              isActive: true,
              ownerType: 'SHOP',
              name: 'Miễn phí ship 50%',
              description: 'Giảm 50% phí vận chuyển cho đơn từ 40k',
              isDeleted: false,
              createdAt: '2026-01-24T12:00:00.000Z',
              updatedAt: '2026-01-24T12:00:00.000Z',
            },
            timestamp: '2026-01-24T12:00:00.000Z',
          },
        },
      },
    },
  })
  @ApiResponse({ status: 409, description: 'Voucher code already exists' })
  async createVoucher(@CurrentUser('uid') ownerId: string, @Body() dto: CreateVoucherDto) {
    const shop = await this.shopsService.getMyShop(ownerId);
    const voucher = await this.vouchersService.createVoucher(shop.id, dto);
    return voucher;
  }

  /**
   * GET /owner/vouchers
   * Get all vouchers
   */
  @Get()
  @ApiOperation({
    summary: 'Get all vouchers',
    description: 'Owner gets all vouchers of their shop',
  })
  @ApiQuery({ name: 'isActive', required: false, enum: ['true', 'false'] })
  @ApiResponse({
    status: 200,
    description: 'Vouchers retrieved successfully',
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
            currentUsage: 25,
            usageLimit: 100,
            validFrom: '2026-01-21T00:00:00Z',
            validTo: '2026-12-31T23:59:59Z',
            isActive: true,
          },
        ],
      },
    },
  })
  async getMyVouchers(@CurrentUser('uid') ownerId: string, @Query('isActive') isActive?: string) {
    const shop = await this.shopsService.getMyShop(ownerId);
    const vouchers = await this.vouchersService.getMyVouchers(shop.id, { isActive });
    return vouchers;
  }

  /**
   * PUT /owner/vouchers/:id
   * Update voucher
   */
  @Put(':id')
  @ApiOperation({
    summary: 'Update voucher',
    description: 'Owner updates voucher details (limited fields)',
  })
  @ApiParam({ name: 'id', description: 'Voucher ID' })
  @ApiResponse({ status: 200, description: 'Voucher updated successfully' })
  @ApiResponse({ status: 404, description: 'Voucher not found' })
  async updateVoucher(
    @CurrentUser('uid') ownerId: string,
    @Param('id') voucherId: string,
    @Body() dto: UpdateVoucherDto,
  ) {
    const shop = await this.shopsService.getMyShop(ownerId);
    await this.vouchersService.updateVoucher(shop.id, voucherId, dto);
    return {
      message: 'Voucher đã được cập nhật',
    };
  }

  /**
   * PUT /owner/vouchers/:id/status
   * Update voucher status
   */
  @Put(':id/status')
  @ApiOperation({
    summary: 'Update voucher status',
    description: 'Owner toggles voucher active/inactive',
  })
  @ApiParam({ name: 'id', description: 'Voucher ID' })
  @ApiResponse({ status: 200, description: 'Voucher status updated' })
  @ApiResponse({ status: 404, description: 'Voucher not found' })
  async updateVoucherStatus(
    @CurrentUser('uid') ownerId: string,
    @Param('id') voucherId: string,
    @Body() dto: UpdateVoucherStatusDto,
  ) {
    const shop = await this.shopsService.getMyShop(ownerId);
    await this.vouchersService.updateVoucherStatus(shop.id, voucherId, dto);
    return {
      message: 'Trạng thái voucher đã được cập nhật',
    };
  }

  /**
   * DELETE /owner/vouchers/:id
   * Delete voucher
   */
  @Delete(':id')
  @ApiOperation({
    summary: 'Delete voucher',
    description: 'Owner soft-deletes voucher',
  })
  @ApiParam({ name: 'id', description: 'Voucher ID' })
  @ApiResponse({ status: 200, description: 'Voucher deleted successfully' })
  @ApiResponse({ status: 404, description: 'Voucher not found' })
  async deleteVoucher(@CurrentUser('uid') ownerId: string, @Param('id') voucherId: string) {
    const shop = await this.shopsService.getMyShop(ownerId);
    await this.vouchersService.deleteVoucher(shop.id, voucherId);
    return {
      message: 'Voucher đã được xóa',
    };
  }
}
