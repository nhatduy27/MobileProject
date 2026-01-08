import {
  Injectable,
  Inject,
  ForbiddenException,
  BadRequestException,
  Logger,
} from '@nestjs/common';

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

// TODO: Import khi có các modules (EPIC tương ứng)
// import { NotificationsService } from '../notifications/notifications.service';
// import { WalletsService } from '../wallets/wallets.service';

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

    // TODO: Inject khi có các modules (EPIC tương ứng)
    // private readonly notificationsService: NotificationsService,
    // private readonly walletsService: WalletsService,
  ) {}

  // ============================================
  // USER MANAGEMENT - ADMIN-006, ADMIN-007
  // ============================================

  /**
   * List all users with pagination and filters
   * ADMIN-006: List All Users
   */
  async listUsers(
    query: ListUsersQueryDto,
  ): Promise<PaginatedResult<AdminUserEntity>> {
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
    this.logger.log(
      `Admin ${adminId} updating user ${userId} status to ${dto.status}`,
    );

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
  async listPayouts(
    query: ListPayoutsQueryDto,
  ): Promise<PaginatedResult<AdminPayoutEntity>> {
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
   */
  async approvePayout(
    adminId: string,
    payoutId: string,
  ): Promise<AdminPayoutEntity> {
    this.logger.log(`Admin ${adminId} approving payout ${payoutId}`);

    const payout = await this.payoutsRepository.approve(payoutId, adminId);

    // TODO: Send notification khi có NotificationsService (EPIC 11)
    // await this.notificationsService.sendToUser(payout.userId, {
    //   title: 'Yêu cầu rút tiền đã được duyệt',
    //   body: 'Admin sẽ chuyển khoản cho bạn trong 24h',
    // });

    return payout;
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
   * Mark payout as transferred
   * ADMIN-011: Mark Payout as Transferred
   */
  async markPayoutTransferred(
    adminId: string,
    payoutId: string,
    transferNote: string,
  ): Promise<AdminPayoutEntity> {
    this.logger.log(`Admin ${adminId} marking payout ${payoutId} as transferred`);

    const payout = await this.payoutsRepository.markTransferred(
      payoutId,
      adminId,
      transferNote,
    );

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
  async listShops(
    query: ListShopsQueryDto,
  ): Promise<PaginatedResult<AdminShopEntity>> {
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
    this.logger.log(
      `Admin ${adminId} updating shop ${shopId} status to ${dto.status}`,
    );

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
}
