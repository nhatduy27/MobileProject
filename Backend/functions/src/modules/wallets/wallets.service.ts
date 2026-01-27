import {
  Injectable,
  Inject,
  NotFoundException,
  BadRequestException,
  Logger,
} from '@nestjs/common';
import { Timestamp, Firestore } from '@google-cloud/firestore';
import { IWalletsRepository, WALLETS_REPOSITORY_TOKEN } from './interfaces';
import { WalletEntity, WalletLedgerEntity, WalletType, LedgerType } from './entities';
import { RequestPayoutDto } from './dto';

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

        if (!ownerWalletDoc.exists) {
          throw new NotFoundException(`Owner wallet ${ownerWalletId} not found`);
        }

        if (!shipperWalletDoc.exists) {
          throw new NotFoundException(`Shipper wallet ${shipperWalletId} not found`);
        }

        const ownerWallet = ownerWalletDoc.data() as WalletEntity;
        const shipperWallet = shipperWalletDoc.data() as WalletEntity;

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
  async requestPayout(
    userId: string,
    walletType: WalletType,
    dto: RequestPayoutDto,
  ): Promise<any> {
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
    this.logger.log(`Processing payout transfer: ${payoutId}, wallet: ${walletId}, amount: ${amount}đ`);

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
      this.logger.warn(`Insufficient balance for payout ${payoutId}: ${wallet.balance}đ < ${amount}đ`);
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
}
