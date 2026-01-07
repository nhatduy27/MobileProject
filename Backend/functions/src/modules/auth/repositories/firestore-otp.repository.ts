import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue, Timestamp } from 'firebase-admin/firestore';
import { FirestoreBaseRepository } from '../../../core/database/firestore/firestore-base.repository';
import { OTPEntity, OTPType } from '../entities';
import { IOTPRepository } from '../interfaces';

/**
 * Firestore OTP Repository
 * 
 * Firestore implementation of IOTPRepository.
 * Collection: otps
 */
@Injectable()
export class FirestoreOTPRepository 
  extends FirestoreBaseRepository<OTPEntity>
  implements IOTPRepository {
  
  constructor(@Inject('FIRESTORE') firestore: Firestore) {
    super(firestore, 'otps');
  }

  /**
   * Find latest OTP for email and type
   */
  async findLatestByEmail(email: string, type: OTPType): Promise<OTPEntity | null> {
    const snapshot = await this.collection
      .where('email', '==', email)
      .where('type', '==', type)
      .where('verified', '==', false)
      .orderBy('createdAt', 'desc')
      .limit(1)
      .get();

    if (snapshot.empty) {
      return null;
    }

    return this.mapDocToEntity(snapshot.docs[0]);
  }

  /**
   * Mark OTP as verified
   */
  async markVerified(otpId: string): Promise<void> {
    await this.getDocRef(otpId).update({
      verified: true,
      updatedAt: FieldValue.serverTimestamp(),
    });
  }

  /**
   * Increment verification attempts
   */
  async incrementAttempts(otpId: string): Promise<void> {
    await this.getDocRef(otpId).update({
      attempts: FieldValue.increment(1),
      updatedAt: FieldValue.serverTimestamp(),
    });
  }

  /**
   * Delete all OTPs for email and type
   */
  async deleteByEmail(email: string, type: OTPType): Promise<void> {
    const snapshot = await this.collection
      .where('email', '==', email)
      .where('type', '==', type)
      .get();

    const batch = this.firestore.batch();
    snapshot.docs.forEach(doc => {
      batch.delete(doc.ref);
    });

    await batch.commit();
  }

  /**
   * Delete expired OTPs (cleanup job)
   */
  async deleteExpired(): Promise<number> {
    const now = Timestamp.now();
    const snapshot = await this.collection
      .where('expiresAt', '<', now.toDate())
      .get();

    if (snapshot.empty) {
      return 0;
    }

    const batch = this.firestore.batch();
    snapshot.docs.forEach(doc => {
      batch.delete(doc.ref);
    });

    await batch.commit();
    return snapshot.size;
  }

  /**
   * Check if user has requested OTP recently (rate limiting)
   */
  async hasRecentRequest(email: string, type: OTPType, seconds: number): Promise<boolean> {
    const cutoffTime = new Date(Date.now() - seconds * 1000);
    
    const snapshot = await this.collection
      .where('email', '==', email)
      .where('type', '==', type)
      .where('createdAt', '>', cutoffTime)
      .limit(1)
      .get();

    return !snapshot.empty;
  }
}
