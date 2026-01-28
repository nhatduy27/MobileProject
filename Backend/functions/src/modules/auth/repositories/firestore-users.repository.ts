import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue } from 'firebase-admin/firestore';
import { FirestoreBaseRepository } from '../../../core/database/firestore/firestore-base.repository';
import { UserEntity, UserStatus } from '../entities';
import { IUsersRepository } from '../interfaces';

/**
 * Firestore Users Repository
 *
 * Firestore implementation of IUsersRepository.
 * Collection: users
 */
@Injectable()
export class FirestoreUsersRepository
  extends FirestoreBaseRepository<UserEntity>
  implements IUsersRepository
{
  constructor(@Inject('FIRESTORE') firestore: Firestore) {
    super(firestore, 'users');
  }

  /**
   * Find user by email
   */
  async findByEmail(email: string): Promise<UserEntity | null> {
    const snapshot = await this.collection.where('email', '==', email).limit(1).get();

    if (snapshot.empty) {
      return null;
    }

    return this.mapDocToEntity(snapshot.docs[0]);
  }

  /**
   * Find user by phone number
   */
  async findByPhone(phone: string): Promise<UserEntity | null> {
    const snapshot = await this.collection.where('phone', '==', phone).limit(1).get();

    if (snapshot.empty) {
      return null;
    }

    return this.mapDocToEntity(snapshot.docs[0]);
  }

  /**
   * Mark email as verified
   */
  async markEmailVerified(userId: string): Promise<void> {
    await this.getDocRef(userId).update({
      emailVerified: true,
      updatedAt: FieldValue.serverTimestamp(),
    });
  }

  /**
   * Update last login timestamp and increment login count
   */
  async updateLastLogin(userId: string): Promise<void> {
    await this.getDocRef(userId).update({
      lastLoginAt: FieldValue.serverTimestamp(),
      loginCount: FieldValue.increment(1),
      updatedAt: FieldValue.serverTimestamp(),
    });
  }

  /**
   * Add FCM token (array union to avoid duplicates)
   */
  async addFcmToken(userId: string, fcmToken: string): Promise<void> {
    await this.getDocRef(userId).update({
      fcmTokens: FieldValue.arrayUnion(fcmToken),
      updatedAt: FieldValue.serverTimestamp(),
    });
  }

  /**
   * Remove FCM token
   */
  async removeFcmToken(userId: string, fcmToken: string): Promise<void> {
    await this.getDocRef(userId).update({
      fcmTokens: FieldValue.arrayRemove(fcmToken),
      updatedAt: FieldValue.serverTimestamp(),
    });
  }

  /**
   * Update user status (ban/unban)
   */
  async updateStatus(
    userId: string,
    status: UserStatus,
    adminId: string,
    reason?: string,
  ): Promise<UserEntity> {
    const updateData: any = {
      status,
      updatedAt: FieldValue.serverTimestamp(),
    };

    if (status === UserStatus.BANNED) {
      updateData.bannedAt = FieldValue.serverTimestamp();
      updateData.bannedBy = adminId;
      if (reason) {
        updateData.bannedReason = reason;
      }
    } else if (status === UserStatus.ACTIVE) {
      updateData.unbannedAt = FieldValue.serverTimestamp();
      updateData.unbannedBy = adminId;
    }

    await this.getDocRef(userId).update(updateData);

    const updated = await this.findById(userId);
    if (!updated) {
      throw new Error('User not found after update');
    }

    return updated;
  }

  /**
   * Find user by email, excluding soft-deleted accounts
   * 
   * Returns users with status in [ACTIVE, BANNED] only.
   * Excludes DELETED users to allow re-registration after account deletion.
   * 
   * IMPORTANT: Soft-deleted users (status=DELETED) are excluded from registration
   * uniqueness checks, allowing re-registration with the same email.
   * However, BANNED users still block registration (can be unbanned later).
   */
  async findActiveByEmail(email: string): Promise<UserEntity | null> {
    const snapshot = await this.collection
      .where('email', '==', email)
      .where('status', 'in', [UserStatus.ACTIVE, UserStatus.BANNED])
      .limit(1)
      .get();

    if (snapshot.empty) {
      return null;
    }

    return this.mapDocToEntity(snapshot.docs[0]);
  }

  /**
   * Find user by phone, excluding soft-deleted accounts
   * 
   * Returns users with status in [ACTIVE, BANNED] only.
   * Excludes DELETED users to allow re-registration after account deletion.
   * 
   * IMPORTANT: Soft-deleted users (status=DELETED) are excluded from registration
   * uniqueness checks, allowing re-registration with the same phone.
   * However, BANNED users still block registration (can be unbanned later).
   */
  async findActiveByPhone(phone: string): Promise<UserEntity | null> {
    const snapshot = await this.collection
      .where('phone', '==', phone)
      .where('status', 'in', [UserStatus.ACTIVE, UserStatus.BANNED])
      .limit(1)
      .get();

    if (snapshot.empty) {
      return null;
    }

    return this.mapDocToEntity(snapshot.docs[0]);
  }

  /**
   * Check if email exists
   */
  async emailExists(email: string): Promise<boolean> {
    const user = await this.findByEmail(email);
    return user !== null;
  }
}
