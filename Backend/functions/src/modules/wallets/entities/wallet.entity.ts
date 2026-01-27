import { Timestamp } from 'firebase-admin/firestore';

export enum WalletType {
  OWNER = 'OWNER',
  SHIPPER = 'SHIPPER',
}

export class WalletEntity {
  id: string; // Format: wallet_{role}_{userId}
  userId: string;
  type: WalletType;
  balance: number;
  totalEarned: number;
  totalWithdrawn: number;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
