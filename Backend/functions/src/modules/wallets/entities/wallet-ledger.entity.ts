import { Timestamp } from 'firebase-admin/firestore';

export enum LedgerType {
  ORDER_PAYOUT = 'ORDER_PAYOUT',
  WITHDRAWAL = 'WITHDRAWAL',
  ADJUSTMENT = 'ADJUSTMENT',
}

export class WalletLedgerEntity {
  id?: string;
  walletId: string;
  userId: string;
  type: LedgerType;
  amount: number; // Positive for credit, negative for debit
  balanceBefore: number;
  balanceAfter: number;
  orderId?: string;
  orderNumber?: string;
  description?: string;
  createdAt: Timestamp;
}
