import { Controller, Get, Put, Param, Body, Query, UseGuards, Req } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { AuthGuard, AdminGuard } from '../../../core/guards';
import { AdminService } from '../admin.service';
import { ListShopsQueryDto, UpdateShopStatusDto, ShopAdminStatus } from '../dto';

/**
 * Admin Shops Controller
 *
 * Quản lý shops - ADMIN-012
 *
 * Base URL: /admin/shops
 */
@ApiTags('Admin - Shops')
@ApiBearerAuth()
@UseGuards(AuthGuard, AdminGuard)
@Controller('admin/shops')
export class AdminShopsController {
  constructor(private readonly adminService: AdminService) {}

  /**
   * GET /admin/shops
   * Lấy danh sách tất cả shops
   *
   * ADMIN-012: List/Manage Shops
   */
  @Get()
  @ApiOperation({ summary: 'Lấy danh sách shops' })
  @ApiQuery({ name: 'page', required: false, type: Number })
  @ApiQuery({ name: 'limit', required: false, type: Number })
  @ApiQuery({ name: 'status', required: false, enum: ShopAdminStatus })
  @ApiQuery({ name: 'search', required: false, type: String })
  @ApiResponse({ status: 200, description: 'Danh sách shops' })
  async listShops(@Query() query: ListShopsQueryDto) {
    return this.adminService.listShops(query);
  }

  /**
   * GET /admin/shops/:shopId
   * Lấy chi tiết shop
   */
  @Get(':shopId')
  @ApiOperation({ summary: 'Lấy chi tiết shop' })
  @ApiResponse({ status: 200, description: 'Chi tiết shop' })
  @ApiResponse({ status: 404, description: 'Shop không tồn tại' })
  async getShop(@Param('shopId') shopId: string) {
    return this.adminService.getShopById(shopId);
  }

  /**
   * PUT /admin/shops/:shopId/status
   * Suspend/Ban shop
   *
   * ADMIN-012: List/Manage Shops
   */
  @Put(':shopId/status')
  @ApiOperation({ summary: 'Suspend/Ban shop' })
  @ApiResponse({ status: 200, description: 'Cập nhật status thành công' })
  @ApiResponse({ status: 400, description: 'Dữ liệu không hợp lệ' })
  @ApiResponse({ status: 404, description: 'Shop không tồn tại' })
  async updateShopStatus(
    @Req() req: any,
    @Param('shopId') shopId: string,
    @Body() dto: UpdateShopStatusDto,
  ) {
    const adminId = req.user.uid;
    await this.adminService.updateShopStatus(adminId, shopId, dto);

    const messages = {
      [ShopAdminStatus.ACTIVE]: 'Shop đã được kích hoạt lại',
      [ShopAdminStatus.SUSPENDED]: 'Shop đã bị tạm ngưng',
      [ShopAdminStatus.BANNED]: 'Shop đã bị cấm',
    };

    return { message: messages[dto.status] };
  }
}
