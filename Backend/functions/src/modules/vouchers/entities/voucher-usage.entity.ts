/**
 * Voucher Usage Entity
 * Collection: voucherUsages
 */
export class VoucherUsageEntity {
  id: string; // Deterministic: {voucherId}_{userId}_{orderId}
  voucherId: string;
  userId: string;
  orderId: string;
  discountAmount: number;
  createdAt: string; // ISO 8601
}
