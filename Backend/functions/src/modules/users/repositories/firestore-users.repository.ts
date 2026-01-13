import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue } from 'firebase-admin/firestore';
import { IUsersRepository } from '../interfaces';
import { UserEntity, UserSettings, FcmToken, UserStatus } from '../entities';

const MAX_FCM_TOKENS = 5;

@Injectable()
export class FirestoreUsersRepository implements IUsersRepository {
  private readonly collection = 'users';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async findById(userId: string): Promise<UserEntity | null> {
    const doc = await this.firestore.collection(this.collection).doc(userId).get();
    if (!doc.exists) return null;

    return this.mapToEntity(doc.id, doc.data());
  }

  async findByEmail(email: string): Promise<UserEntity | null> {
    const snapshot = await this.firestore
      .collection(this.collection)
      .where('email', '==', email)
      .limit(1)
      .get();

    if (snapshot.empty) return null;

    const doc = snapshot.docs[0];
    return this.mapToEntity(doc.id, doc.data());
  }

  async updateProfile(userId: string, data: Partial<UserEntity>): Promise<UserEntity> {
    const updateData: Record<string, any> = {
      ...data,
      updatedAt: FieldValue.serverTimestamp(),
    };

    // Remove undefined values
    Object.keys(updateData).forEach((key) => {
      if (updateData[key] === undefined) {
        delete updateData[key];
      }
    });

    await this.firestore.collection(this.collection).doc(userId).update(updateData);

    const updated = await this.findById(userId);
    if (!updated) throw new Error('User not found after update');
    return updated;
  }

  async updateSettings(userId: string, settings: UserSettings): Promise<UserSettings> {
    // Use dot notation for nested object updates to avoid prototype issues
    const updateData: Record<string, unknown> = {
      'settings.notifications.orderUpdates': settings.notifications?.orderUpdates ?? true,
      'settings.notifications.promotions': settings.notifications?.promotions ?? true,
      'settings.notifications.email': settings.notifications?.email ?? true,
      'settings.notifications.push': settings.notifications?.push ?? true,
      'settings.language': settings.language ?? 'vi',
      'settings.currency': settings.currency ?? 'VND',
      updatedAt: FieldValue.serverTimestamp(),
    };
    await this.firestore.collection(this.collection).doc(userId).update(updateData);
    return settings;
  }

  async getSettings(userId: string): Promise<UserSettings | null> {
    const user = await this.findById(userId);
    return user?.settings || null;
  }

  async updateAvatarUrl(userId: string, avatarUrl: string): Promise<void> {
    await this.firestore.collection(this.collection).doc(userId).update({
      avatarUrl,
      updatedAt: FieldValue.serverTimestamp(),
    });
  }

  async addFcmToken(userId: string, token: string, deviceId: string): Promise<void> {
    const userRef = this.firestore.collection(this.collection).doc(userId);

    await this.firestore.runTransaction(async (transaction) => {
      const doc = await transaction.get(userRef);
      if (!doc.exists) throw new Error('User not found');

      const data = doc.data();
      let fcmTokens: FcmToken[] = data?.fcmTokens || [];

      // Remove existing token with same deviceId
      fcmTokens = fcmTokens.filter((t) => t.deviceId !== deviceId);

      // Add new token
      fcmTokens.push({
        token,
        deviceId,
        createdAt: new Date(),
      });

      // Keep only last MAX_FCM_TOKENS
      if (fcmTokens.length > MAX_FCM_TOKENS) {
        fcmTokens = fcmTokens.slice(-MAX_FCM_TOKENS);
      }

      transaction.update(userRef, {
        fcmTokens,
        updatedAt: FieldValue.serverTimestamp(),
      });
    });
  }

  async removeFcmToken(userId: string, token: string): Promise<void> {
    await this.firestore
      .collection(this.collection)
      .doc(userId)
      .update({
        fcmTokens: FieldValue.arrayRemove({ token } as any),
        updatedAt: FieldValue.serverTimestamp(),
      });
  }

  async softDelete(userId: string): Promise<void> {
    await this.firestore.collection(this.collection).doc(userId).update({
      status: UserStatus.DELETED,
      deletedAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    });
  }

  private mapToEntity(id: string, data: any): UserEntity {
    return new UserEntity({
      id,
      email: data.email,
      displayName: data.displayName,
      phone: data.phone,
      avatarUrl: data.avatarUrl,
      role: data.role,
      status: data.status,
      emailVerified: data.emailVerified,
      shopId: data.shopId,
      shipperInfo: data.shipperInfo
        ? {
            ...data.shipperInfo,
            joinedAt: data.shipperInfo.joinedAt?.toDate?.() || data.shipperInfo.joinedAt,
          }
        : undefined,
      settings: data.settings,
      fcmTokens: data.fcmTokens,
      bannedAt: data.bannedAt?.toDate?.(),
      bannedBy: data.bannedBy,
      bannedReason: data.bannedReason,
      createdAt: data.createdAt?.toDate?.() || new Date(),
      updatedAt: data.updatedAt?.toDate?.() || new Date(),
    });
  }
}
