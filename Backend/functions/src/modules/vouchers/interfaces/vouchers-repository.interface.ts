import { VoucherEntity, VoucherUsageEntity } from '../entities';

export interface IVouchersRepository {
  /**
   * Create a new voucher
   */
  create(shopId: string, data: Partial<VoucherEntity>): Promise<VoucherEntity>;

  /**
   * Find voucher by ID
   */
  findById(id: string): Promise<VoucherEntity | null>;

  /**
   * Find voucher by shop + code (unique constraint)
   */
  findByShopAndCode(shopId: string, code: string): Promise<VoucherEntity | null>;

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
