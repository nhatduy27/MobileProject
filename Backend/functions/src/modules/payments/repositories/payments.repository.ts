import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue, Timestamp } from '@google-cloud/firestore';
import { IPaymentsRepository } from '../interfaces';
import { PaymentEntity } from '../entities';

@Injectable()
export class PaymentsRepository implements IPaymentsRepository {
  private readonly collection = 'payments';

  constructor(
    @Inject('FIRESTORE')
    private readonly firestore: Firestore,
  ) {}

  async create(payment: Omit<PaymentEntity, 'id'>): Promise<PaymentEntity> {
    const docRef = this.firestore.collection(this.collection).doc();
    const now = Timestamp.now();
    
    const data = {
      ...payment,
      createdAt: now,
      updatedAt: now,
    };

    await docRef.set(data);

    return {
      ...data,
      id: docRef.id,
    } as PaymentEntity;
  }

  async findById(id: string): Promise<PaymentEntity | null> {
    const doc = await this.firestore.collection(this.collection).doc(id).get();
    
    if (!doc.exists) {
      return null;
    }

    return {
      id: doc.id,
      ...doc.data(),
    } as PaymentEntity;
  }

  async findByOrderId(orderId: string): Promise<PaymentEntity | null> {
    const snapshot = await this.firestore
      .collection(this.collection)
      .where('orderId', '==', orderId)
      .limit(1)
      .get();

    if (snapshot.empty) {
      return null;
    }

    const doc = snapshot.docs[0];
    return {
      id: doc.id,
      ...doc.data(),
    } as PaymentEntity;
  }

  async update(id: string, data: Partial<PaymentEntity>): Promise<void> {
    const docRef = this.firestore.collection(this.collection).doc(id);
    await docRef.update({
      ...data,
      updatedAt: FieldValue.serverTimestamp(),
    });
  }
}
