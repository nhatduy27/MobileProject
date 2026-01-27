import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  Query,
  UseGuards,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';
import { VouchersService } from '../vouchers.service';
import { CreateVoucherDto, UpdateVoucherDto, UpdateVoucherStatusDto } from '../dto';

/**
 * Admin Vouchers Controller
 * Manage all vouchers (shop + platform) in the system
 * Base path: /api/admin/vouchers
 */
@ApiTags('Admin - Vouchers')
@Controller('admin/vouchers')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.ADMIN)
export class AdminVouchersController {
  constructor(private readonly vouchersService: VouchersService) {}

  /**
   * Get all vouchers (admin view)
   * GET /api/admin/vouchers
   */
  @Get()
  @ApiOperation({
    summary: 'Get all vouchers (Admin)',
    description: 'List all vouchers from all shops + platform vouchers',
  })
  @ApiQuery({ name: 'shopId', required: false, description: 'Filter by shopId' })
  @ApiQuery({ name: 'isActive', required: false, enum: ['true', 'false'] })
  @HttpCode(HttpStatus.OK)
  async getAllVouchers(
    @Query('shopId') shopId?: string,
    @Query('isActive') isActive?: string,
  ) {
    const vouchers = await this.vouchersService.getAllVouchers({
      shopId,
      isActive: isActive === 'true' ? true : isActive === 'false' ? false : undefined,
    });

    return {
      success: true,
      data: vouchers,
      timestamp: new Date().toISOString(),
    };
  }

  /**
   * Get voucher by ID (admin view)
   * GET /api/admin/vouchers/:id
   */
  @Get(':id')
  @ApiOperation({
    summary: 'Get voucher detail (Admin)',
    description: 'Get full voucher details including usage stats',
  })
  @HttpCode(HttpStatus.OK)
  async getVoucherById(@Param('id') voucherId: string) {
    const voucher = await this.vouchersService.getVoucherByIdAsAdmin(voucherId);

    return {
      success: true,
      data: voucher,
      timestamp: new Date().toISOString(),
    };
  }

  /**
   * Create platform voucher
   * POST /api/admin/vouchers
   */
  @Post()
  @ApiOperation({
    summary: 'Create platform voucher (Admin)',
    description: 'Create voucher applicable to all shops (shopId = null)',
  })
  @HttpCode(HttpStatus.CREATED)
  async createPlatformVoucher(@Body() dto: CreateVoucherDto) {
    const voucher = await this.vouchersService.createPlatformVoucher(dto);

    return {
      success: true,
      message: 'Platform voucher created successfully',
      data: voucher,
      timestamp: new Date().toISOString(),
    };
  }

  /**
   * Update voucher (admin override)
   * PUT /api/admin/vouchers/:id
   */
  @Put(':id')
  @ApiOperation({
    summary: 'Update voucher (Admin)',
    description: 'Admin can update any voucher regardless of ownership',
  })
  @HttpCode(HttpStatus.OK)
  async updateVoucher(@Param('id') voucherId: string, @Body() dto: UpdateVoucherDto) {
    await this.vouchersService.updateVoucherAsAdmin(voucherId, dto);

    return {
      success: true,
      message: 'Voucher updated successfully',
      timestamp: new Date().toISOString(),
    };
  }

  /**
   * Toggle voucher status
   * PUT /api/admin/vouchers/:id/status
   */
  @Put(':id/status')
  @ApiOperation({
    summary: 'Toggle voucher status (Admin)',
    description: 'Enable/disable voucher',
  })
  @HttpCode(HttpStatus.OK)
  async toggleVoucherStatus(
    @Param('id') voucherId: string,
    @Body() dto: UpdateVoucherStatusDto,
  ) {
    await this.vouchersService.updateVoucherStatusAsAdmin(voucherId, dto.isActive);

    return {
      success: true,
      message: 'Voucher status updated successfully',
      timestamp: new Date().toISOString(),
    };
  }

  /**
   * Delete voucher (soft delete)
   * DELETE /api/admin/vouchers/:id
   */
  @Delete(':id')
  @ApiOperation({
    summary: 'Delete voucher (Admin)',
    description: 'Soft delete voucher (mark as deleted)',
  })
  @HttpCode(HttpStatus.OK)
  async deleteVoucher(@Param('id') voucherId: string) {
    await this.vouchersService.deleteVoucherAsAdmin(voucherId);

    return {
      success: true,
      message: 'Voucher deleted successfully',
      timestamp: new Date().toISOString(),
    };
  }
}
