import { VoucherEntity, VoucherUsageEntity } from '../entities';

export interface IVouchersRepository {
  /**
   * Create a new voucher
   * @param shopId - Shop ID or null for platform vouchers
   */
  create(shopId: string | null, data: Partial<VoucherEntity>): Promise<VoucherEntity>;

  /**
   * Find voucher by ID
   */
  findById(id: string): Promise<VoucherEntity | null>;

  /**
   * Find voucher by shop + code (unique constraint)
   * @param shopId - Shop ID or null for platform vouchers
   */
  findByShopAndCode(shopId: string | null, code: string): Promise<VoucherEntity | null>;

  /**
   * Find vouchers by shop (with optional filters)
   */
  findByShopId(
    shopId: string,
    filters?: {
      isActive?: boolean;
      limit?: number;
      orderBy?: 'createdAt' | 'validTo';
      orderDir?: 'asc' | 'desc';
    },
  ): Promise<VoucherEntity[]>;

  /**
   * Find all vouchers (ADMIN - with optional filters)
   */
  findAll(filters?: {
    shopId?: string;
    isActive?: boolean;
    limit?: number;
    orderBy?: 'createdAt' | 'validTo';
    orderDir?: 'asc' | 'desc';
  }): Promise<VoucherEntity[]>;

  /**
   * Update voucher fields (partial)
   */
  update(id: string, data: Partial<VoucherEntity>): Promise<void>;

  /**
   * Delete voucher (soft delete via isDeleted flag)
   */
  delete(id: string): Promise<void>;

  /**
   * Count voucher usage for a specific voucher
   */
  countUsage(voucherId: string): Promise<number>;

  /**
   * Count voucher usage per user
   */
  countUsageByUser(voucherId: string, userId: string): Promise<number>;

  /**
   * Count voucher usage per user for multiple vouchers (batch)
   * Avoids N+1 problem by chunking queries (Firestore 'in' limit)
   * @returns Map of voucherId -> usage count for the given user
   */
  countUsageByUserBatch(voucherIds: string[], userId: string): Promise<Record<string, number>>;

  /**
   * Get voucher usage record (deterministic ID)
   */
  getUsage(voucherId: string, userId: string, orderId: string): Promise<VoucherUsageEntity | null>;

  /**
   * Create voucher usage record (idempotent via deterministic ID)
   */
  createUsage(data: VoucherUsageEntity): Promise<void>;

  /**
   * Run atomic validation + apply in Firestore transaction
   * Returns final voucher state after increment
   */
  applyVoucherAtomic(
    voucherId: string,
    userId: string,
    orderId: string,
    discountAmount: number,
  ): Promise<VoucherEntity>;

  /**
   * Get paginated voucher usage history for a user
   * @param userId Customer user ID
   * @param filters Optional filters (shopId, date range)
   * @param page 1-based page number
   * @param limit Items per page
   * @returns { items: VoucherUsageEntity[], total: number }
   */
  getUsageHistory(
    userId: string,
    filters?: {
      shopId?: string;
      from?: string;
      to?: string;
    },
    page?: number,
    limit?: number,
  ): Promise<{ items: VoucherUsageEntity[]; total: number }>;

  /**
   * Get paginated usage records for a specific voucher (owner view)
   * @param voucherId Voucher ID
   * @param page 1-based page number
   * @param limit Items per page
   * @param from Optional start date
   * @param to Optional end date
   * @returns { items: VoucherUsageEntity[], total: number }
   */
  getVoucherUsageByVoucherId(
    voucherId: string,
    page?: number,
    limit?: number,
    from?: string,
    to?: string,
  ): Promise<{ items: VoucherUsageEntity[]; total: number }>;

  /**
   * Get aggregated statistics for a voucher
   * @param voucherId Voucher ID
   * @returns { totalUses, totalDiscountAmount, uniqueUsers, lastUsedAt }
   */
  getVoucherStats(voucherId: string): Promise<{
    totalUses: number;
    totalDiscountAmount: number;
    uniqueUsers: number;
    lastUsedAt: string | null;
  }>;

  /**
   * Mark vouchers as inactive where validTo < now (expiration sweep)
   * Used by scheduled job or manual maintenance command
   * @param now Current timestamp (ISO 8601)
   * @returns { updatedCount: number } - Number of vouchers marked as inactive
   */
  expireVouchersBeforeDate(now: string): Promise<{ updatedCount: number }>;
}
