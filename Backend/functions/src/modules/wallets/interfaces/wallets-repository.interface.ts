import { WalletEntity, WalletLedgerEntity, WalletType } from '../entities';

export const WALLETS_REPOSITORY_TOKEN = 'WALLETS_REPOSITORY';

export interface IWalletsRepository {
  create(wallet: WalletEntity): Promise<WalletEntity>;
  findById(id: string): Promise<WalletEntity | null>;
  findByUserId(userId: string): Promise<WalletEntity | null>;
  findByUserIdAndType(userId: string, type: WalletType): Promise<WalletEntity | null>;
  update(id: string, data: Partial<WalletEntity>): Promise<void>;
  
  // Ledger operations
  createLedgerEntry(entry: Omit<WalletLedgerEntity, 'id'>): Promise<WalletLedgerEntity>;
  findLedgerByWalletId(
    walletId: string,
    limit: number,
    offset: number,
  ): Promise<{ entries: WalletLedgerEntity[]; total: number }>;
}
