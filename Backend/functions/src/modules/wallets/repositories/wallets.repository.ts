import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue, Timestamp } from '@google-cloud/firestore';
import { IWalletsRepository } from '../interfaces';
import { WalletEntity, WalletLedgerEntity, WalletType } from '../entities';

@Injectable()
export class WalletsRepository implements IWalletsRepository {
  private readonly walletsCollection = 'wallets';
  private readonly ledgerCollection = 'wallet_ledger';

  constructor(
    @Inject('FIRESTORE')
    private readonly firestore: Firestore,
  ) {}

  async create(wallet: WalletEntity): Promise<WalletEntity> {
    const docRef = this.firestore.collection(this.walletsCollection).doc(wallet.id);
    const now = Timestamp.now();
    
    const data = {
      ...wallet,
      createdAt: now,
      updatedAt: now,
    };

    await docRef.set(data);
    return data as WalletEntity;
  }

  async findById(id: string): Promise<WalletEntity | null> {
    const doc = await this.firestore.collection(this.walletsCollection).doc(id).get();
    
    if (!doc.exists) {
      return null;
    }

    return {
      id: doc.id,
      ...doc.data(),
    } as WalletEntity;
  }

  async findByUserId(userId: string): Promise<WalletEntity | null> {
    const snapshot = await this.firestore
      .collection(this.walletsCollection)
      .where('userId', '==', userId)
      .limit(1)
      .get();

    if (snapshot.empty) {
      return null;
    }

    const doc = snapshot.docs[0];
    return {
      id: doc.id,
      ...doc.data(),
    } as WalletEntity;
  }

  async findByUserIdAndType(userId: string, type: WalletType): Promise<WalletEntity | null> {
    // P0-FIX: Query by both userId AND type to avoid returning wrong wallet
    // If user has multiple wallets (OWNER + SHIPPER), this ensures correct one
    const snapshot = await this.firestore
      .collection(this.walletsCollection)
      .where('userId', '==', userId)
      .where('type', '==', type)
      .limit(1)
      .get();

    if (snapshot.empty) {
      return null;
    }

    const doc = snapshot.docs[0];
    return {
      id: doc.id,
      ...doc.data(),
    } as WalletEntity;
  }

  async update(id: string, data: Partial<WalletEntity>): Promise<void> {
    const docRef = this.firestore.collection(this.walletsCollection).doc(id);
    await docRef.update({
      ...data,
      updatedAt: FieldValue.serverTimestamp(),
    });
  }

  async createLedgerEntry(entry: Omit<WalletLedgerEntity, 'id'>): Promise<WalletLedgerEntity> {
    const docRef = this.firestore.collection(this.ledgerCollection).doc();
    const now = Timestamp.now();
    
    const data = {
      ...entry,
      createdAt: now,
    };

    await docRef.set(data);

    return {
      ...data,
      id: docRef.id,
    } as WalletLedgerEntity;
  }

  async findLedgerByWalletId(
    walletId: string,
    limit: number,
    offset: number,
  ): Promise<{ entries: WalletLedgerEntity[]; total: number }> {
    // Get total count
    const countSnapshot = await this.firestore
      .collection(this.ledgerCollection)
      .where('walletId', '==', walletId)
      .count()
      .get();
    
    const total = countSnapshot.data().count;

    // Get paginated entries
    const snapshot = await this.firestore
      .collection(this.ledgerCollection)
      .where('walletId', '==', walletId)
      .orderBy('createdAt', 'desc')
      .limit(limit)
      .offset(offset)
      .get();

    const entries = snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    })) as WalletLedgerEntity[];

    return { entries, total };
  }
}
