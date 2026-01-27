import {
  Injectable,
  Inject,
  ForbiddenException,
  BadRequestException,
  Logger,
} from '@nestjs/common';
import axios from 'axios';

import {
  ListUsersQueryDto,
  UpdateUserStatusDto,
  ListPayoutsQueryDto,
  ListShopsQueryDto,
  UpdateShopStatusDto,
  UserStatus as DtoUserStatus,
  ShopAdminStatus,
} from './dto';

import {
  PaginatedResult,
  DashboardStats,
  UserStats,
  OrderStats,
  RevenueStats,
  IAdminUsersRepository,
  IAdminShopsRepository,
  IAdminPayoutsRepository,
  ADMIN_USERS_REPOSITORY_TOKEN,
  ADMIN_SHOPS_REPOSITORY_TOKEN,
  ADMIN_PAYOUTS_REPOSITORY_TOKEN,
} from './interfaces';

import {
  AdminUserEntity,
  AdminShopEntity,
  AdminPayoutEntity,
  UserRole,
  UserStatus,
  ShopStatus,
  PayoutStatus,
} from './entities';

import { WalletsService } from '../wallets/wallets.service';
import { ConfigService } from '../../core/config/config.service';

// TODO: Import khi có các modules (EPIC tương ứng)
// import { NotificationsService } from '../notifications/notifications.service';

/**
 * AdminService - Business logic cho Admin module
 *
 * Tuân thủ SOLID - Dependency Inversion:
 * - Inject repositories qua INTERFACE tokens
 * - Không depend trực tiếp vào Firestore implementations
 *
 * Responsibilities:
 * - User management (list, ban/unban)
 * - Payout management (list, approve, reject, mark transferred)
 * - Shop management (list, suspend/ban)
 * - Dashboard statistics
 */
@Injectable()
export class AdminService {
  private readonly logger = new Logger(AdminService.name);

  constructor(
    @Inject(ADMIN_USERS_REPOSITORY_TOKEN)
    private readonly usersRepository: IAdminUsersRepository,

    @Inject(ADMIN_SHOPS_REPOSITORY_TOKEN)
    private readonly shopsRepository: IAdminShopsRepository,

    @Inject(ADMIN_PAYOUTS_REPOSITORY_TOKEN)
    private readonly payoutsRepository: IAdminPayoutsRepository,

    private readonly walletsService: WalletsService,
    private readonly configService: ConfigService,

    // TODO: Inject khi có các modules (EPIC tương ứng)
    // private readonly notificationsService: NotificationsService,
  ) {}

  // ============================================
  // USER MANAGEMENT - ADMIN-006, ADMIN-007
  // ============================================

  /**
   * List all users with pagination and filters
   * ADMIN-006: List All Users
   */
  async listUsers(query: ListUsersQueryDto): Promise<PaginatedResult<AdminUserEntity>> {
    this.logger.log(`Listing users with query: ${JSON.stringify(query)}`);

    const { page = 1, limit = 20, role, status, search } = query;

    // Convert DTO enum to Entity enum via string value
    return this.usersRepository.findWithFilters(
      {
        role: role ? (role as unknown as UserRole) : undefined,
        status: status ? (status as unknown as UserStatus) : undefined,
        search,
      },
      { pagination: { page, limit } },
    );
  }

  /**
   * Get user by ID
   */
  async getUserById(userId: string): Promise<AdminUserEntity> {
    this.logger.log(`Getting user: ${userId}`);
    return this.usersRepository.findByIdOrThrow(userId);
  }

  /**
   * Update user status (ban/unban)
   * ADMIN-007: Update User Status (Ban/Unban)
   */
  async updateUserStatus(
    adminId: string,
    userId: string,
    dto: UpdateUserStatusDto,
  ): Promise<AdminUserEntity> {
    this.logger.log(`Admin ${adminId} updating user ${userId} status to ${dto.status}`);

    // 1. Kiểm tra user tồn tại và không phải admin
    const user = await this.usersRepository.findByIdOrThrow(userId);

    // Check if user is admin (handle both role string and roles array)
    const isAdmin = user.roles?.includes(UserRole.ADMIN) || (user as any).role === UserRole.ADMIN;
    if (isAdmin) {
      throw new ForbiddenException('Không thể ban admin khác');
    }

    // 2. Validate reason khi ban (compare via string value)
    if (dto.status === DtoUserStatus.BANNED && !dto.reason) {
      throw new BadRequestException('Lý do là bắt buộc khi ban user');
    }

    // 3. Update status via repository (convert DTO enum to Entity enum)
    const updatedUser = await this.usersRepository.updateStatus(
      userId,
      dto.status as unknown as UserStatus,
      adminId,
      dto.reason,
    );

    // TODO: Disable/Enable Firebase Auth khi có Firebase Admin service (EPIC 02)
    // await this.firebaseAuth.updateUser(userId, {
    //   disabled: dto.status === DtoUserStatus.BANNED,
    // });

    // TODO: Send notification khi có NotificationsService (EPIC 11)
    // await this.notificationsService.sendToUser(userId, {
    //   title: dto.status === DtoUserStatus.BANNED
    //     ? 'Tài khoản bị khóa'
    //     : 'Tài khoản đã mở khóa',
    //   body: dto.status === DtoUserStatus.BANNED
    //     ? `Lý do: ${dto.reason}`
    //     : 'Bạn có thể đăng nhập lại',
    // });

    return updatedUser;
  }

  // ============================================
  // PAYOUT MANAGEMENT - ADMIN-008 to ADMIN-011
  // ============================================

  /**
   * List payout requests
   * ADMIN-008: List Payout Requests
   */
  async listPayouts(query: ListPayoutsQueryDto): Promise<PaginatedResult<AdminPayoutEntity>> {
    this.logger.log(`Listing payouts with query: ${JSON.stringify(query)}`);

    const { page = 1, limit = 20, status } = query;

    return this.payoutsRepository.findWithFilters(
      { status: status as PayoutStatus },
      { pagination: { page, limit } },
    );
  }

  /**
   * Get payout by ID
   */
  async getPayoutById(payoutId: string): Promise<AdminPayoutEntity> {
    this.logger.log(`Getting payout: ${payoutId}`);
    return this.payoutsRepository.findByIdOrThrow(payoutId);
  }

  /**
   * Approve payout request
   * ADMIN-009: Approve Payout Request
   * 
   * Returns payout with QR URL for admin to scan and transfer money
   */
  async approvePayout(adminId: string, payoutId: string): Promise<AdminPayoutEntity & { qrUrl?: string }> {
    this.logger.log(`Admin ${adminId} approving payout ${payoutId}`);

    const payout = await this.payoutsRepository.approve(payoutId, adminId);

    // Generate QR URL for admin to transfer money TO user
    const qrUrl = this.generatePayoutQrUrl(payout);

    // Start auto-polling in background (non-blocking)
    this.autoCompletePayoutIfDetected(
      payoutId,
      payout.userId,
      payout.walletId,
      payout.amount,
      payout.accountNumber || '',
    )
      .catch(err => {
        this.logger.error(`Auto-complete polling failed for payout ${payoutId}:`, err);
      });

    // TODO: Send notification khi có NotificationsService (EPIC 11)
    // await this.notificationsService.sendToUser(payout.userId, {
    //   title: 'Yêu cầu rút tiền đã được duyệt',
    //   body: 'Admin sẽ chuyển khoản cho bạn trong 24h',
    // });

    return { ...payout, qrUrl };
  }

  /**
   * Reject payout request
   * ADMIN-010: Reject Payout Request
   */
  async rejectPayout(
    adminId: string,
    payoutId: string,
    reason: string,
  ): Promise<AdminPayoutEntity> {
    this.logger.log(`Admin ${adminId} rejecting payout ${payoutId}`);

    const payout = await this.payoutsRepository.reject(payoutId, adminId, reason);

    // TODO: Unlock funds - cần WalletsService (EPIC 10)
    // await this.walletsService.unlockFunds(payout.userId, payout.amount);

    // TODO: Send notification khi có NotificationsService (EPIC 11)
    // await this.notificationsService.sendToUser(payout.userId, {
    //   title: 'Yêu cầu rút tiền bị từ chối',
    //   body: `Lý do: ${reason}. Tiền đã được hoàn lại vào ví.`,
    // });

    return payout;
  }

  /**
   * Verify payout transfer via SePay
   * Checks if matching outgoing transaction exists, auto-completes if found
   */
  async verifyPayoutTransfer(
    adminId: string,
    payoutId: string,
  ): Promise<{ matched: boolean; status: string; payout: AdminPayoutEntity }> {
    this.logger.log(`Admin ${adminId} verifying payout ${payoutId}`);

    // Get payout
    const payout = await this.payoutsRepository.findByIdOrThrow(payoutId);

    // Check if already transferred (idempotent)
    if (payout.status === 'TRANSFERRED') {
      return {
        matched: true,
        status: 'TRANSFERRED',
        payout,
      };
    }

    // Must be APPROVED to verify
    if (payout.status !== 'APPROVED') {
      throw new BadRequestException(
        `Payout must be APPROVED to verify. Current status: ${payout.status}`,
      );
    }

    // Generate expected content (same as QR generation)
    const expectedContent = `PAYOUT${payoutId.substring(0, 8).toUpperCase()}`;

    // Poll SePay API to check for outgoing transaction
    const detected = await this.pollOutgoingTransaction(
      payout.amount,
      payout.accountNumber || '',
      expectedContent,
    );

    if (detected) {
      this.logger.log(`✅ Transfer detected for payout ${payoutId}! Auto-completing...`);

      // Mark as transferred
      const updatedPayout = await this.payoutsRepository.markTransferred(
        payoutId,
        'SYSTEM_AUTO',
        `Auto-verified by admin ${adminId}`,
      );

      // Process wallet deduction
      try {
        await this.walletsService.processPayoutTransfer(
          updatedPayout.id!,
          updatedPayout.userId,
          updatedPayout.walletId,
          updatedPayout.amount,
        );
        this.logger.log(`Wallet balance deducted for verified payout ${payoutId}`);
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : String(error);
        this.logger.error(`Failed to deduct wallet for verified payout ${payoutId}:`, errorMessage);
      }

      return {
        matched: true,
        status: 'TRANSFERRED',
        payout: updatedPayout,
      };
    }

    // Not detected yet
    return {
      matched: false,
      status: 'APPROVED',
      payout,
    };
  }

  /**
   * Mark payout as transferred
   * ADMIN-011: Mark Payout as Transferred
   */
  async markPayoutTransferred(
    adminId: string,
    payoutId: string,
    transferNote: string,
  ): Promise<AdminPayoutEntity> {
    this.logger.log(`Admin ${adminId} marking payout ${payoutId} as transferred`);

    const payout = await this.payoutsRepository.markTransferred(payoutId, adminId, transferNote);

    // Process wallet deduction
    try {
      if (!payout.id) {
        throw new Error('Payout ID is missing');
      }
      await this.walletsService.processPayoutTransfer(
        payout.id,
        payout.userId,
        payout.walletId,
        payout.amount,
      );
      this.logger.log(`Wallet balance deducted for payout ${payoutId}`);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error);
      this.logger.error(`Failed to deduct wallet balance for payout ${payoutId}:`, errorMessage);
      // Note: Payout status already updated, this is a data inconsistency that needs manual fix
    }

    // TODO: Send notification khi có NotificationsService (EPIC 11)
    // await this.notificationsService.sendToUser(payout.userId, {
    //   title: 'Đã chuyển khoản',
    //   body: `${payout.amount}đ đã được chuyển đến tài khoản của bạn`,
    // });

    return payout;
  }

  // ============================================
  // SHOP MANAGEMENT - ADMIN-012
  // ============================================

  /**
   * List all shops
   * ADMIN-012: List/Manage Shops
   */
  async listShops(query: ListShopsQueryDto): Promise<PaginatedResult<AdminShopEntity>> {
    this.logger.log(`Listing shops with query: ${JSON.stringify(query)}`);

    const { page = 1, limit = 20, status, search } = query;

    return this.shopsRepository.findWithFilters(
      {
        status: status ? (status as unknown as ShopStatus) : undefined,
        search,
      },
      { pagination: { page, limit } },
    );
  }

  /**
   * Get shop by ID
   */
  async getShopById(shopId: string): Promise<AdminShopEntity> {
    this.logger.log(`Getting shop: ${shopId}`);
    return this.shopsRepository.findByIdOrThrow(shopId);
  }

  /**
   * Update shop status
   * ADMIN-012: List/Manage Shops
   */
  async updateShopStatus(
    adminId: string,
    shopId: string,
    dto: UpdateShopStatusDto,
  ): Promise<AdminShopEntity> {
    this.logger.log(`Admin ${adminId} updating shop ${shopId} status to ${dto.status}`);

    // Validate reason for suspend/ban (compare via DTO enum)
    if (
      (dto.status === ShopAdminStatus.SUSPENDED || dto.status === ShopAdminStatus.BANNED) &&
      !dto.reason
    ) {
      throw new BadRequestException('Lý do là bắt buộc khi suspend/ban shop');
    }

    const shop = await this.shopsRepository.updateStatus(
      shopId,
      dto.status as unknown as ShopStatus,
      adminId,
      dto.reason,
    );

    // TODO: Notify owner khi có NotificationsService (EPIC 11)
    // await this.notificationsService.sendToUser(shop.ownerId, {
    //   title: `Shop ${dto.status === ShopAdminStatus.ACTIVE ? 'đã kích hoạt' : 'bị ' + dto.status}`,
    //   body: dto.reason || 'Liên hệ admin để biết thêm chi tiết',
    // });

    return shop;
  }

  // ============================================
  // DASHBOARD - ADMIN-013
  // ============================================

  /**
   * Get dashboard stats
   * ADMIN-013: Admin Dashboard
   */
  async getDashboardStats(): Promise<DashboardStats> {
    this.logger.log('Getting dashboard stats');

    const [userStats, shopStats, payoutStats] = await Promise.all([
      this.getUserStats(),
      this.getShopStats(),
      this.getPayoutStats(),
    ]);

    // TODO: Get order/revenue stats khi có OrdersRepository (EPIC 07)
    const orderStats = await this.getOrderStats();
    const revenueStats = await this.getRevenueStats();

    return {
      users: userStats,
      shops: shopStats,
      orders: orderStats,
      revenue: revenueStats,
      payouts: payoutStats,
    };
  }

  /**
   * Get detailed user stats
   */
  async getUserStats(): Promise<UserStats> {
    this.logger.log('Getting user stats');

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const [total, customers, owners, shippers, newToday] = await Promise.all([
      this.usersRepository.count(),
      this.usersRepository.countByRole(UserRole.CUSTOMER),
      this.usersRepository.countByRole(UserRole.OWNER),
      this.usersRepository.countByRole(UserRole.SHIPPER),
      this.usersRepository.countCreatedAfter(today),
    ]);

    return {
      total,
      customers,
      owners,
      shippers,
      newToday,
    };
  }

  /**
   * Get shop stats
   */
  async getShopStats(): Promise<{ total: number; active: number; pendingApproval: number }> {
    this.logger.log('Getting shop stats');

    const [total, active, pendingApproval] = await Promise.all([
      this.shopsRepository.count(),
      this.shopsRepository.countByStatus(ShopStatus.ACTIVE),
      this.shopsRepository.countPendingApproval(),
    ]);

    return { total, active, pendingApproval };
  }

  /**
   * Get payout stats
   */
  async getPayoutStats(): Promise<{ pending: number; totalPendingAmount: number }> {
    this.logger.log('Getting payout stats');

    const [pending, totalPendingAmount] = await Promise.all([
      this.payoutsRepository.countPending(),
      this.payoutsRepository.sumPendingAmount(),
    ]);

    return { pending, totalPendingAmount };
  }

  /**
   * Get detailed order stats
   * TODO: Implement khi có OrdersRepository (EPIC 07)
   */
  async getOrderStats(): Promise<OrderStats> {
    this.logger.log('Getting order stats');

    // TODO: Implement khi có OrdersRepository (EPIC 07)
    // const today = new Date();
    // today.setHours(0, 0, 0, 0);
    //
    // const weekStart = new Date(today);
    // weekStart.setDate(today.getDate() - today.getDay());
    //
    // const monthStart = new Date(today.getFullYear(), today.getMonth(), 1);
    //
    // const [todayCount, weekCount, monthCount] = await Promise.all([
    //   this.ordersRepository.countCreatedAfter(today),
    //   this.ordersRepository.countCreatedAfter(weekStart),
    //   this.ordersRepository.countCreatedAfter(monthStart),
    // ]);

    return {
      today: 0,
      thisWeek: 0,
      thisMonth: 0,
    };
  }

  /**
   * Get detailed revenue stats
   * TODO: Implement khi có OrdersRepository (EPIC 07)
   */
  async getRevenueStats(): Promise<RevenueStats> {
    this.logger.log('Getting revenue stats');

    // TODO: Implement khi có OrdersRepository (EPIC 07)
    // const today = new Date();
    // today.setHours(0, 0, 0, 0);
    //
    // const weekStart = new Date(today);
    // weekStart.setDate(today.getDate() - today.getDay());
    //
    // const monthStart = new Date(today.getFullYear(), today.getMonth(), 1);
    //
    // const [todayRevenue, weekRevenue, monthRevenue] = await Promise.all([
    //   this.ordersRepository.sumRevenueAfter(today),
    //   this.ordersRepository.sumRevenueAfter(weekStart),
    //   this.ordersRepository.sumRevenueAfter(monthStart),
    // ]);

    return {
      today: 0,
      thisWeek: 0,
      thisMonth: 0,
    };
  }

  // ============================================
  // PAYOUT AUTO-COMPLETE HELPERS
  // ============================================

  /**
   * Generate QR URL for admin to transfer money to user
   */
  private generatePayoutQrUrl(payout: AdminPayoutEntity): string {
    const qrTemplate = this.configService.get('SEPAY_TEMPLATE_QR', 'https://qr.sepay.vn/img?acc={account}&bank={bank}&amount={amount}&des={content}&template=compact');
    
    // Generate content for payout tracking
    const payoutId = payout.id || 'UNKNOWN';
    const content = `PAYOUT${payoutId.substring(0, 8).toUpperCase()}`;

    // Replace placeholders with payout data
    const bankCode = payout.bankCode || 'ICB'; // Default to ICB if not set
    const qrUrl = qrTemplate
      .replace('{account}', payout.accountNumber || '') // User's account (recipient)
      .replace('{bank}', bankCode)
      .replace('{amount}', payout.amount.toString())
      .replace('{content}', content);

    this.logger.log(`Generated QR for payout ${payoutId}: ${content}`);
    
    return qrUrl;
  }

  /**
   * Auto-complete payout if outgoing transaction detected
   * Polls Sepay API for 3 minutes, every 5 seconds
   */
  private async autoCompletePayoutIfDetected(
    payoutId: string,
    userId: string,
    walletId: string,
    amount: number,
    recipientAccount: string,
  ): Promise<void> {
    const maxDuration = 3 * 60 * 1000; // 3 minutes
    const pollInterval = 5 * 1000; // 5 seconds
    const startTime = Date.now();

    this.logger.log(`Starting auto-polling for payout ${payoutId} (3min, 5s interval)`);

    const poll = async (): Promise<boolean> => {
      try {
        // Generate expected content for matching
        const expectedContent = `PAYOUT${payoutId.substring(0, 8).toUpperCase()}`;
        const detected = await this.pollOutgoingTransaction(amount, recipientAccount, expectedContent);
        
        if (detected) {
          this.logger.log(`✅ Transaction detected for payout ${payoutId}! Auto-completing...`);
          
          // Mark as transferred (using dummy admin ID for auto-complete)
          await this.payoutsRepository.markTransferred(
            payoutId,
            'SYSTEM_AUTO',
            `Auto-detected transfer to ${recipientAccount}`,
          );
          
          // Process wallet deduction
          try {
            if (!userId || !walletId) {
              this.logger.error(`Cannot deduct wallet: missing userId or walletId for payout ${payoutId}`);
              return true;
            }
            await this.walletsService.processPayoutTransfer(
              payoutId,
              userId,
              walletId,
              amount,
            );
            this.logger.log(`Wallet balance deducted for auto-completed payout ${payoutId}`);
          } catch (error) {
            const errorMessage = error instanceof Error ? error.message : String(error);
            this.logger.error(`Failed to deduct wallet for auto-completed payout ${payoutId}:`, errorMessage);
          }
          
          return true;
        }
        
        return false;
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : String(error);
        this.logger.warn(`Polling error for payout ${payoutId}:`, errorMessage);
        return false;
      }
    };

    // Poll repeatedly
    while (Date.now() - startTime < maxDuration) {
      const detected = await poll();
      
      if (detected) {
        this.logger.log(`Auto-complete successful for payout ${payoutId}`);
        return;
      }

      // Wait before next poll
      await new Promise(resolve => setTimeout(resolve, pollInterval));
    }

    this.logger.log(`Auto-polling timeout for payout ${payoutId} (no transaction detected)`);
  }

  /**
   * Poll Sepay API to detect outgoing transaction
   * Returns true if matching transaction found
   */
  private async pollOutgoingTransaction(
    amount: number,
    _recipientAccount: string,
    expectedContent: string,
  ): Promise<boolean> {
    try {
      const secretKey = this.configService.get('SEPAY_SECRET_KEY');
      const accountNumber = this.configService.get('SEPAY_ACCOUNT_NUMBER');
      const apiUrl = this.configService.get('SEPAY_API_URL', 'https://my.sepay.vn/userapi');

      if (!secretKey || !accountNumber) {
        this.logger.warn('Sepay config missing, cannot poll transactions');
        return false;
      }

      // Call Sepay API
      const url = `${apiUrl}/transactions/list`;
      const response = await axios.get(url, {
        params: {
          account_number: accountNumber,
          limit: 20, // Check last 20 transactions
        },
        headers: {
          Authorization: `Bearer ${secretKey}`,
        },
        timeout: 10000,
      });

      // Parse transactions
      let transactions: any[] = [];
      if (response.data?.transactions) {
        transactions = response.data.transactions;
      } else if (response.data?.data?.transactions) {
        transactions = response.data.data.transactions;
      } else if (Array.isArray(response.data)) {
        transactions = response.data;
      }

      // Log transactions for debugging
      this.logger.log(`Checking ${transactions.length} transactions for payout content: ${expectedContent}`);

      // Look for OUTGOING transaction matching amount AND content
      // Note: For payout verification, we only check amount + content (content is unique per payout)
      // Recipient info may not be available in SePay outgoing transactions
      for (const txn of transactions) {
        // Check if it's an outgoing transaction (amount_out field)
        const txnAmountOut = parseFloat(txn.amount_out || '0');
        const txnContent = txn.transaction_content || txn.description || '';
        
        // Match amount and transaction content
        const amountMatch = txnAmountOut === amount;
        const contentMatch = txnContent.toLowerCase().includes(expectedContent.toLowerCase());
        
        this.logger.log(`Transaction ${txn.id}: amount_out=${txnAmountOut}, content="${txnContent}", amountMatch=${amountMatch}, contentMatch=${contentMatch}`);
        
        if (amountMatch && contentMatch) {
          this.logger.log(`✅ Found matching outgoing transaction: ${txn.id}, amount: ${txnAmountOut}, content: ${txnContent}`);
          return true;
        }
      }

      this.logger.log(`No matching outgoing transaction found for content: ${expectedContent}`);
      return false;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error);
      this.logger.error('Error polling outgoing transactions:', errorMessage);
      return false;
    }
  }
}
