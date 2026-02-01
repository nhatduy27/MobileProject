import {
  Controller,
  Get,
  Post,
  Body,
  Query,
  UseGuards,
  HttpCode,
  HttpStatus,
  Req,
} from '@nestjs/common';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { CurrentUser } from '../../../core/decorators/current-user.decorator';
import { UserRole } from '../../users/entities/user.entity';
import { WalletsService } from '../wallets.service';
import { WalletType } from '../entities';
import { GetLedgerDto, RequestPayoutDto, GetRevenueDto, RevenuePeriod } from '../dto';

@Controller('wallets')
@UseGuards(AuthGuard, RolesGuard)
export class WalletsController {
  constructor(private readonly walletsService: WalletsService) {}

  /**
   * Get my wallet (OWNER or SHIPPER)
   * GET /api/wallets/me
   * P0-FIX: Determine wallet type from user role to avoid returning wrong wallet
   */
  @Get('me')
  @Roles(UserRole.OWNER, UserRole.SHIPPER)
  @HttpCode(HttpStatus.OK)
  async getMyWallet(@CurrentUser('uid') userId: string, @Req() req: any) {
    // P0-FIX: Get wallet type from user's role (from custom claims)
    const userRole = req.user?.role as string;
    const walletType = userRole === 'OWNER' ? WalletType.OWNER : WalletType.SHIPPER;

    const wallet = await this.walletsService.getWalletByUserIdAndType(userId, walletType);

    return {
      wallet: {
        id: wallet.id,
        type: wallet.type,
        balance: wallet.balance,
        totalEarned: wallet.totalEarned,
        totalWithdrawn: wallet.totalWithdrawn,
        createdAt: wallet.createdAt?.toDate?.()?.toISOString?.() ?? wallet.createdAt,
        updatedAt: wallet.updatedAt?.toDate?.()?.toISOString?.() ?? wallet.updatedAt,
      },
    };
  }

  /**
   * Get my wallet ledger history
   * GET /api/wallets/ledger
   * P0-FIX: Determine wallet type from user role
   */
  @Get('ledger')
  @Roles(UserRole.OWNER, UserRole.SHIPPER)
  @HttpCode(HttpStatus.OK)
  async getMyLedger(
    @CurrentUser('uid') userId: string,
    @Req() req: any,
    @Query() dto: GetLedgerDto,
  ) {
    // P0-FIX: Get wallet type from user's role
    const userRole = req.user?.role as string;
    const walletType = userRole === 'OWNER' ? WalletType.OWNER : WalletType.SHIPPER;

    const result = await this.walletsService.getLedger(
      userId,
      walletType,
      dto.page || 1,
      dto.limit || 20,
    );

    return {
      entries: result.entries.map((entry) => ({
        id: entry.id,
        type: entry.type,
        amount: entry.amount,
        balanceBefore: entry.balanceBefore,
        balanceAfter: entry.balanceAfter,
        orderId: entry.orderId,
        orderNumber: entry.orderNumber,
        description: entry.description,
        createdAt: entry.createdAt?.toDate?.()?.toISOString?.() ?? entry.createdAt,
      })),
      page: result.page,
      limit: result.limit,
      total: result.total,
      totalPages: result.totalPages,
    };
  }

  /**
   * Request payout (withdraw funds)
   * POST /api/wallets/payout
   */
  @Post('payout')
  @Roles(UserRole.OWNER, UserRole.SHIPPER)
  @HttpCode(HttpStatus.CREATED)
  async requestPayout(
    @CurrentUser('uid') userId: string,
    @Req() req: any,
    @Body() dto: RequestPayoutDto,
  ) {
    // Get wallet type from user's role
    const userRole = req.user?.role as string;
    const walletType = userRole === 'OWNER' ? WalletType.OWNER : WalletType.SHIPPER;

    const payoutRequest = await this.walletsService.requestPayout(userId, walletType, dto);

    return {
      message: 'Payout request submitted successfully',
      payoutRequest,
    };
  }

  /**
   * Get revenue statistics
   * GET /api/wallets/revenue?period=month
   *
   * Calculates revenue from ledger entries (amount > 0)
   * Returns aggregated data by day/week/month/year
   */
  @Get('revenue')
  @Roles(UserRole.OWNER, UserRole.SHIPPER)
  @HttpCode(HttpStatus.OK)
  async getRevenue(
    @CurrentUser('uid') userId: string,
    @Req() req: any,
    @Query() dto: GetRevenueDto,
  ) {
    // Get wallet type from user's role
    const userRole = req.user?.role as string;
    const walletType = userRole === 'OWNER' ? WalletType.OWNER : WalletType.SHIPPER;

    const stats = await this.walletsService.getRevenueStats(
      userId,
      walletType,
      dto.period || RevenuePeriod.MONTH,
    );

    return stats;
  }
}
