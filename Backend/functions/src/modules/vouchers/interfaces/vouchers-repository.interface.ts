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
}
