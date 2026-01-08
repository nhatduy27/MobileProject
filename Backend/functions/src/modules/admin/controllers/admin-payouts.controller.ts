import {
  Controller,
  Get,
  Post,
  Param,
  Body,
  Query,
  UseGuards,
  Req,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiQuery,
} from '@nestjs/swagger';
import { AuthGuard, AdminGuard } from '../../../core/guards';
import { AdminService } from '../admin.service';
import {
  ListPayoutsQueryDto,
  RejectPayoutDto,
  MarkTransferredDto,
  PayoutStatus,
} from '../dto';

/**
 * Admin Payouts Controller
 *
 * Quản lý payout requests - ADMIN-008, 009, 010, 011
 *
 * Base URL: /admin/payouts
 */
@ApiTags('Admin - Payouts')
@ApiBearerAuth()
@UseGuards(AuthGuard, AdminGuard)
@Controller('admin/payouts')
export class AdminPayoutsController {
  constructor(private readonly adminService: AdminService) {}

  /**
   * GET /admin/payouts
   * Lấy danh sách payout requests
   *
   * ADMIN-008: List Payout Requests
   */
  @Get()
  @ApiOperation({ summary: 'Lấy danh sách payout requests (⛔ BLOCKED — cần: WalletModule, OrderModule)' })
  @ApiQuery({ name: 'page', required: false, type: Number })
  @ApiQuery({ name: 'limit', required: false, type: Number })
  @ApiQuery({ name: 'status', required: false, enum: PayoutStatus })
  @ApiResponse({ status: 200, description: 'Danh sách payouts' })
  async listPayouts(@Query() query: ListPayoutsQueryDto) {
    const result = await this.adminService.listPayouts(query);
    return {
      success: true,
      data: result.data,
      meta: result.meta,
    };
  }

  /**
   * GET /admin/payouts/:payoutId
   * Lấy chi tiết payout
   */
  @Get(':payoutId')
  @ApiOperation({ summary: 'Lấy chi tiết payout (⛔ BLOCKED — cần: WalletModule)' })
  @ApiResponse({ status: 200, description: 'Chi tiết payout' })
  @ApiResponse({ status: 404, description: 'Payout không tồn tại' })
  async getPayout(@Param('payoutId') payoutId: string) {
    const payout = await this.adminService.getPayoutById(payoutId);
    return {
      success: true,
      data: payout,
    };
  }

  /**
   * POST /admin/payouts/:payoutId/approve
   * Approve payout request
   *
   * ADMIN-009: Approve Payout Request
   */
  @Post(':payoutId/approve')
  @ApiOperation({ summary: 'Approve payout request (⛔ BLOCKED — cần: WalletModule)' })
  @ApiResponse({ status: 200, description: 'Payout đã được approve' })
  @ApiResponse({ status: 404, description: 'Payout không tồn tại' })
  @ApiResponse({ status: 409, description: 'Payout đã được xử lý' })
  async approvePayout(
    @Req() req: any,
    @Param('payoutId') payoutId: string,
  ) {
    const adminId = req.user.uid;
    await this.adminService.approvePayout(adminId, payoutId);
    return {
      success: true,
      message: 'Payout đã được approve',
    };
  }

  /**
   * POST /admin/payouts/:payoutId/reject
   * Reject payout request
   *
   * ADMIN-010: Reject Payout Request
   */
  @Post(':payoutId/reject')
  @ApiOperation({ summary: 'Reject payout request (⛔ BLOCKED — cần: WalletModule)' })
  @ApiResponse({ status: 200, description: 'Payout đã bị reject' })
  @ApiResponse({ status: 400, description: 'Lý do là bắt buộc' })
  @ApiResponse({ status: 404, description: 'Payout không tồn tại' })
  @ApiResponse({ status: 409, description: 'Payout đã được xử lý' })
  async rejectPayout(
    @Req() req: any,
    @Param('payoutId') payoutId: string,
    @Body() dto: RejectPayoutDto,
  ) {
    const adminId = req.user.uid;
    await this.adminService.rejectPayout(adminId, payoutId, dto.reason);
    return {
      success: true,
      message: 'Payout đã bị reject',
    };
  }

  /**
   * POST /admin/payouts/:payoutId/transferred
   * Đánh dấu đã chuyển khoản
   *
   * ADMIN-011: Mark Payout as Transferred
   */
  @Post(':payoutId/transferred')
  @ApiOperation({ summary: 'Đánh dấu payout đã chuyển khoản (⛔ BLOCKED — cần: WalletModule)' })
  @ApiResponse({ status: 200, description: 'Đã đánh dấu transferred' })
  @ApiResponse({ status: 404, description: 'Payout không tồn tại' })
  @ApiResponse({ status: 409, description: 'Payout chưa được approve' })
  async markTransferred(
    @Req() req: any,
    @Param('payoutId') payoutId: string,
    @Body() dto: MarkTransferredDto,
  ) {
    const adminId = req.user.uid;
    await this.adminService.markPayoutTransferred(
      adminId,
      payoutId,
      dto.transferNote,
    );
    return {
      success: true,
      message: 'Đã đánh dấu payout là transferred',
    };
  }
}
