import { Injectable, Inject, NotFoundException, BadRequestException, Logger } from '@nestjs/common';
import { Timestamp, Firestore } from '@google-cloud/firestore';
import { IWalletsRepository, WALLETS_REPOSITORY_TOKEN } from './interfaces';
import { WalletEntity, WalletLedgerEntity, WalletType, LedgerType } from './entities';
import { RequestPayoutDto, RevenuePeriod, RevenueStatsDto, DailyRevenueDto } from './dto';

@Injectable()
export class WalletsService {
  private readonly logger = new Logger(WalletsService.name);

  constructor(
    @Inject(WALLETS_REPOSITORY_TOKEN)
    private readonly walletsRepo: IWalletsRepository,
    @Inject('FIRESTORE')
    private readonly firestore: Firestore,
  ) {}

  /**
   * Initialize wallet for user
   * Idempotent: returns existing wallet if already created
   */
  async initializeWallet(userId: string, type: WalletType): Promise<WalletEntity> {
    const walletId = `wallet_${type.toLowerCase()}_${userId}`;

    // Check if wallet already exists (idempotent)
    const existing = await this.walletsRepo.findById(walletId);
    if (existing) {
      this.logger.log(`Wallet ${walletId} already exists (idempotent)`);
      return existing;
    }

    // Create new wallet
    const wallet: WalletEntity = {
      id: walletId,
      userId,
      type,
      balance: 0,
      totalEarned: 0,
      totalWithdrawn: 0,
      createdAt: Timestamp.now(),
      updatedAt: Timestamp.now(),
    };

    await this.walletsRepo.create(wallet);
    this.logger.log(`Wallet ${walletId} created for user ${userId}`);

    return wallet;
  }

  /**
   * Get wallet by user ID and type
   * P0-FIX: Always specify type to avoid returning wrong wallet if user has multiple
   */
  async getWalletByUserIdAndType(userId: string, type: WalletType): Promise<WalletEntity> {
    const wallet = await this.walletsRepo.findByUserIdAndType(userId, type);

    if (!wallet) {
      throw new NotFoundException({
        code: 'WALLET_001',
        message: `Wallet not found for user (type: ${type})`,
        statusCode: 404,
      });
    }

    return wallet;
  }

  /**
   * Get wallet by user ID (legacy - prefer getWalletByUserIdAndType)
   * @deprecated Use getWalletByUserIdAndType instead
   */
  async getWalletByUserId(userId: string): Promise<WalletEntity> {
    const wallet = await this.walletsRepo.findByUserId(userId);

    if (!wallet) {
      throw new NotFoundException({
        code: 'WALLET_001',
        message: 'Wallet not found',
        statusCode: 404,
      });
    }

    return wallet;
  }

  /**
   * Get ledger history for wallet
   * P0-FIX: Accept type parameter to get correct wallet
   */
  async getLedger(
    userId: string,
    type: WalletType,
    page: number = 1,
    limit: number = 20,
  ): Promise<{
    entries: WalletLedgerEntity[];
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  }> {
    const wallet = await this.getWalletByUserIdAndType(userId, type);

    const offset = (page - 1) * limit;
    const { entries, total } = await this.walletsRepo.findLedgerByWalletId(
      wallet.id,
      limit,
      offset,
    );

    return {
      entries,
      page,
      limit,
      total,
      totalPages: Math.ceil(total / limit),
    };
  }

  /**
   * Process payout for delivered order
   * P0-FIX: Uses Firestore transaction for FULL atomicity including Order.paidOut update
   *
   * @param orderId - Order ID
   * @param orderNumber - Order number for ledger reference
   * @param ownerId - Shop owner user ID
   * @param ownerAmount - Amount to credit owner (order.total - order.shipperPayout)
   * @param shipperId - Shipper user ID
   * @param shipperAmount - Amount to credit shipper (order.shipperPayout)
   */
  async processOrderPayout(
    orderId: string,
    orderNumber: string,
    ownerId: string,
    ownerAmount: number,
    shipperId: string,
    shipperAmount: number,
  ): Promise<void> {
    const ownerWalletId = `wallet_owner_${ownerId}`;
    const shipperWalletId = `wallet_shipper_${shipperId}`;

    try {
      await this.firestore.runTransaction(async (transaction: any) => {
        const ownerWalletRef = this.firestore.collection('wallets').doc(ownerWalletId);
        const shipperWalletRef = this.firestore.collection('wallets').doc(shipperWalletId);
        const orderRef = this.firestore.collection('orders').doc(orderId);

        // P0-FIX: Read order document first to check paidOut status (idempotent check)
        const orderDoc = await transaction.get(orderRef);
        if (!orderDoc.exists) {
          throw new NotFoundException(`Order ${orderId} not found`);
        }

        const order = orderDoc.data();
        if (order.paidOut === true) {
          // Already paid out - idempotent, skip transaction
          this.logger.warn(`Order ${orderNumber} already paid out (idempotent), skipping payout`);
          return;
        }

        // Get current wallet states
        const ownerWalletDoc = await transaction.get(ownerWalletRef);
        const shipperWalletDoc = await transaction.get(shipperWalletRef);

        // Auto-create owner wallet if not exists
        let ownerWallet: WalletEntity;
        if (!ownerWalletDoc.exists) {
          this.logger.log(`Auto-creating owner wallet ${ownerWalletId} for payout`);
          ownerWallet = {
            id: ownerWalletId,
            userId: ownerId,
            type: WalletType.OWNER,
            balance: 0,
            totalEarned: 0,
            totalWithdrawn: 0,
            createdAt: Timestamp.now() as any,
            updatedAt: Timestamp.now() as any,
          };
          transaction.set(ownerWalletRef, ownerWallet);
        } else {
          ownerWallet = ownerWalletDoc.data() as WalletEntity;
        }

        // Auto-create shipper wallet if not exists
        let shipperWallet: WalletEntity;
        if (!shipperWalletDoc.exists) {
          this.logger.log(`Auto-creating shipper wallet ${shipperWalletId} for payout`);
          shipperWallet = {
            id: shipperWalletId,
            userId: shipperId,
            type: WalletType.SHIPPER,
            balance: 0,
            totalEarned: 0,
            totalWithdrawn: 0,
            createdAt: Timestamp.now() as any,
            updatedAt: Timestamp.now() as any,
          };
          transaction.set(shipperWalletRef, shipperWallet);
        } else {
          shipperWallet = shipperWalletDoc.data() as WalletEntity;
        }

        // Calculate new balances
        const ownerBalanceBefore = ownerWallet.balance;
        const ownerBalanceAfter = ownerBalanceBefore + ownerAmount;

        const shipperBalanceBefore = shipperWallet.balance;
        const shipperBalanceAfter = shipperBalanceBefore + shipperAmount;

        // Update owner wallet
        transaction.update(ownerWalletRef, {
          balance: ownerBalanceAfter,
          totalEarned: (ownerWallet.totalEarned || 0) + ownerAmount,
          updatedAt: Timestamp.now(),
        });

        // Update shipper wallet
        transaction.update(shipperWalletRef, {
          balance: shipperBalanceAfter,
          totalEarned: (shipperWallet.totalEarned || 0) + shipperAmount,
          updatedAt: Timestamp.now(),
        });

        // P0-FIX: Update order.paidOut IN SAME TRANSACTION (critical for atomicity)
        transaction.update(orderRef, {
          paidOut: true,
          paidOutAt: Timestamp.now(),
        });

        // Create ledger entries
        const ownerLedgerRef = this.firestore.collection('walletLedger').doc();
        transaction.set(ownerLedgerRef, {
          walletId: ownerWalletId,
          userId: ownerId,
          type: LedgerType.ORDER_PAYOUT,
          amount: ownerAmount,
          balanceBefore: ownerBalanceBefore,
          balanceAfter: ownerBalanceAfter,
          orderId,
          orderNumber,
          description: `Order ${orderNumber} payout`,
          createdAt: Timestamp.now(),
        });

        const shipperLedgerRef = this.firestore.collection('walletLedger').doc();
        transaction.set(shipperLedgerRef, {
          walletId: shipperWalletId,
          userId: shipperId,
          type: LedgerType.ORDER_PAYOUT,
          amount: shipperAmount,
          balanceBefore: shipperBalanceBefore,
          balanceAfter: shipperBalanceAfter,
          orderId,
          orderNumber,
          description: `Order ${orderNumber} delivery payout`,
          createdAt: Timestamp.now(),
        });
      });

      this.logger.log(
        `Payout processed for order ${orderNumber}: Owner +${ownerAmount}, Shipper +${shipperAmount}`,
      );
    } catch (error) {
      this.logger.error(`Failed to process payout for order ${orderNumber}:`, error);
      throw error;
    }
  }

  /**
   * Request payout (withdraw funds)
   * Create payout request for admin approval
   */
  async requestPayout(userId: string, walletType: WalletType, dto: RequestPayoutDto): Promise<any> {
    // Get wallet
    const wallet = await this.getWalletByUserIdAndType(userId, walletType);

    // Validate balance
    if (wallet.balance < dto.amount) {
      throw new BadRequestException({
        code: 'WALLET_002',
        message: `Insufficient balance. Available: ${wallet.balance}đ, Requested: ${dto.amount}đ`,
        statusCode: 400,
      });
    }

    // Minimum withdrawal amounts
    const minAmount = walletType === WalletType.OWNER ? 100000 : 50000;
    if (dto.amount < minAmount) {
      throw new BadRequestException({
        code: 'WALLET_003',
        message: `Minimum payout amount is ${minAmount.toLocaleString()}đ`,
        statusCode: 400,
      });
    }

    // Create payout request
    const payoutRequestRef = this.firestore.collection('payoutRequests').doc();
    const payoutRequest = {
      id: payoutRequestRef.id,
      userId,
      walletId: wallet.id,
      walletType,
      amount: dto.amount,
      bankCode: dto.bankCode,
      accountNumber: dto.accountNumber,
      accountName: dto.accountName,
      note: dto.note || '',
      status: 'PENDING', // PENDING | APPROVED | REJECTED | TRANSFERRED
      createdAt: Timestamp.now(),
      updatedAt: Timestamp.now(),
    };

    await payoutRequestRef.set(payoutRequest);

    this.logger.log(
      `Payout request created: ${payoutRequestRef.id} for user ${userId}, amount: ${dto.amount}đ`,
    );

    return {
      id: payoutRequest.id,
      amount: payoutRequest.amount,
      status: payoutRequest.status,
      bankCode: payoutRequest.bankCode,
      accountNumber: payoutRequest.accountNumber,
      accountName: payoutRequest.accountName,
      createdAt: payoutRequest.createdAt,
    };
  }

  /**
   * Process payout transfer - deduct wallet balance when payout is completed
   * Called by AdminService when marking payout as TRANSFERRED
   */
  async processPayoutTransfer(
    payoutId: string,
    userId: string,
    walletId: string,
    amount: number,
  ): Promise<void> {
    this.logger.log(
      `Processing payout transfer: ${payoutId}, wallet: ${walletId}, amount: ${amount}đ`,
    );

    const walletRef = this.firestore.collection('wallets').doc(walletId);
    const walletDoc = await walletRef.get();

    if (!walletDoc.exists) {
      throw new NotFoundException({
        code: 'WALLET_001',
        message: 'Wallet not found',
        statusCode: 404,
      });
    }

    const wallet = walletDoc.data();
    if (!wallet) {
      throw new NotFoundException({
        code: 'WALLET_001',
        message: 'Wallet data not found',
        statusCode: 404,
      });
    }

    // Validate sufficient balance
    if (wallet.balance < amount) {
      this.logger.warn(
        `Insufficient balance for payout ${payoutId}: ${wallet.balance}đ < ${amount}đ`,
      );
      throw new BadRequestException({
        code: 'WALLET_002',
        message: `Insufficient balance. Available: ${wallet.balance}đ, Required: ${amount}đ`,
        statusCode: 400,
      });
    }

    // Deduct balance and update totalWithdrawn
    const newBalance = wallet.balance - amount;
    const newTotalWithdrawn = (wallet.totalWithdrawn || 0) + amount;

    await walletRef.update({
      balance: newBalance,
      totalWithdrawn: newTotalWithdrawn,
      updatedAt: Timestamp.now(),
    });

    // Create ledger entry
    const ledgerRef = this.firestore.collection('walletLedger').doc();
    await ledgerRef.set({
      id: ledgerRef.id,
      walletId,
      userId,
      type: 'PAYOUT',
      amount: -amount, // Negative for withdrawal
      balanceBefore: wallet.balance,
      balanceAfter: newBalance,
      status: 'COMPLETED',
      referenceType: 'PAYOUT',
      referenceId: payoutId,
      description: `Withdrawal via payout ${payoutId}`,
      createdAt: Timestamp.now(),
    });

    this.logger.log(`Payout transfer processed: ${payoutId}, new balance: ${newBalance}đ`);
  }

  /**
   * Get all wallets for admin statistics
   * Used by AdminService to calculate total wallet balances
   */
  async getAllWalletsForAdmin(): Promise<WalletEntity[]> {
    this.logger.log('Fetching all wallets for admin stats');

    try {
      const snapshot = await this.firestore.collection('wallets').get();
      const wallets: WalletEntity[] = [];

      snapshot.forEach((doc) => {
        wallets.push({ id: doc.id, ...doc.data() } as WalletEntity);
      });

      this.logger.log(`Found ${wallets.length} wallets for admin stats`);
      return wallets;
    } catch (error) {
      this.logger.error('Failed to fetch wallets for admin stats:', error);
      return [];
    }
  }

  /**
   * Get revenue statistics for user (SHIPPER or OWNER)
   * Calculates revenue from ledger entries with amount > 0 (income)
   * Groups by day/week/month/year based on period parameter
   */
  async getRevenueStats(
    userId: string,
    type: WalletType,
    period: RevenuePeriod = RevenuePeriod.MONTH,
  ): Promise<RevenueStatsDto> {
    // Auto-create wallet if doesn't exist (returns 0 revenue instead of 404)
    let wallet = await this.walletsRepo.findByUserIdAndType(userId, type);
    if (!wallet) {
      this.logger.log(`Wallet not found for user ${userId} (${type}), auto-creating...`);
      wallet = await this.initializeWallet(userId, type);
    }

    // Fetch ALL ledger entries (for accurate calculation)
    // Note: In production, consider caching or pagination for large datasets
    const allEntries = await this.getAllLedgerEntries(wallet.id);

    // Filter only income entries (amount > 0)
    const revenueEntries = allEntries.filter((entry) => entry.amount > 0);

    // Calculate time boundaries
    const now = new Date();
    const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const startOfWeek = this.getStartOfWeek(now);
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const startOfYear = new Date(now.getFullYear(), 0, 1);

    // Calculate revenue by period
    const today = this.sumEntriesInRange(revenueEntries, startOfToday, now);
    const week = this.sumEntriesInRange(revenueEntries, startOfWeek, now);
    const month = this.sumEntriesInRange(revenueEntries, startOfMonth, now);
    const year = this.sumEntriesInRange(revenueEntries, startOfYear, now);
    const all = revenueEntries.reduce((sum, entry) => sum + entry.amount, 0);

    // Generate daily breakdown based on period
    let dailyBreakdown: DailyRevenueDto[] = [];
    if (period === RevenuePeriod.WEEK || period === RevenuePeriod.TODAY) {
      dailyBreakdown = this.generateDailyBreakdown(revenueEntries, 7);
    } else if (period === RevenuePeriod.MONTH) {
      dailyBreakdown = this.generateDailyBreakdown(revenueEntries, 30);
    } else if (period === RevenuePeriod.YEAR) {
      // For year, show monthly breakdown instead
      dailyBreakdown = this.generateMonthlyBreakdown(revenueEntries, 12);
    } else {
      // ALL - show last 30 days
      dailyBreakdown = this.generateDailyBreakdown(revenueEntries, 30);
    }

    return {
      today,
      week,
      month,
      year,
      all,
      dailyBreakdown,
      calculatedAt: new Date().toISOString(),
    };
  }

  /**
   * Fetch all ledger entries for a wallet
   * Used for revenue calculation (needs all entries for accurate stats)
   */
  private async getAllLedgerEntries(walletId: string): Promise<WalletLedgerEntity[]> {
    const allEntries: WalletLedgerEntity[] = [];
    let hasMore = true;
    let offset = 0;
    const limit = 100;

    while (hasMore) {
      const { entries, total } = await this.walletsRepo.findLedgerByWalletId(
        walletId,
        limit,
        offset,
      );
      allEntries.push(...entries);
      offset += limit;
      hasMore = offset < total;
    }

    return allEntries;
  }

  /**
   * Get start of week (Monday)
   */
  private getStartOfWeek(date: Date): Date {
    const d = new Date(date);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1); // Adjust for Sunday
    return new Date(d.getFullYear(), d.getMonth(), diff, 0, 0, 0, 0);
  }

  /**
   * Sum entries within date range
   */
  private sumEntriesInRange(
    entries: WalletLedgerEntity[],
    startDate: Date,
    endDate: Date,
  ): number {
    return entries
      .filter((entry) => {
        const entryDate = (entry.createdAt as Timestamp).toDate();
        return entryDate >= startDate && entryDate <= endDate;
      })
      .reduce((sum, entry) => sum + entry.amount, 0);
  }

  /**
   * Generate daily breakdown for last N days
   */
  private generateDailyBreakdown(
    entries: WalletLedgerEntity[],
    days: number,
  ): DailyRevenueDto[] {
    const result: DailyRevenueDto[] = [];
    const now = new Date();

    for (let i = days - 1; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(now.getDate() - i);
      const startOfDay = new Date(date.getFullYear(), date.getMonth(), date.getDate());
      const endOfDay = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 23, 59, 59);

      const dayEntries = entries.filter((entry) => {
        const entryDate = (entry.createdAt as Timestamp).toDate();
        return entryDate >= startOfDay && entryDate <= endOfDay;
      });

      const amount = dayEntries.reduce((sum, entry) => sum + entry.amount, 0);
      const orderCount = dayEntries.filter((entry) => entry.orderId).length;

      result.push({
        date: startOfDay.toISOString().split('T')[0], // YYYY-MM-DD
        amount,
        orderCount,
      });
    }

    return result;
  }

  /**
   * Generate monthly breakdown for last N months
   */
  private generateMonthlyBreakdown(
    entries: WalletLedgerEntity[],
    months: number,
  ): DailyRevenueDto[] {
    const result: DailyRevenueDto[] = [];
    const now = new Date();

    for (let i = months - 1; i >= 0; i--) {
      const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const startOfMonth = new Date(date.getFullYear(), date.getMonth(), 1);
      const endOfMonth = new Date(date.getFullYear(), date.getMonth() + 1, 0, 23, 59, 59);

      const monthEntries = entries.filter((entry) => {
        const entryDate = (entry.createdAt as Timestamp).toDate();
        return entryDate >= startOfMonth && entryDate <= endOfMonth;
      });

      const amount = monthEntries.reduce((sum, entry) => sum + entry.amount, 0);
      const orderCount = monthEntries.filter((entry) => entry.orderId).length;

      result.push({
        date: `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`, // YYYY-MM
        amount,
        orderCount,
      });
    }

    return result;
  }
}
