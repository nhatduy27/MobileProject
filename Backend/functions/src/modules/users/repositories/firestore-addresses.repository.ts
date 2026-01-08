import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue } from 'firebase-admin/firestore';
import { IAddressesRepository } from '../interfaces';
import { AddressEntity } from '../entities';

@Injectable()
export class FirestoreAddressesRepository implements IAddressesRepository {
  private readonly collection = 'userAddresses';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async create(userId: string, data: Partial<AddressEntity>): Promise<AddressEntity> {
    const docRef = this.firestore.collection(this.collection).doc();

    const addressData: Record<string, any> = {
      id: docRef.id,
      userId,
      label: data.label,
      fullAddress: data.fullAddress,
      building: data.building || null,
      room: data.room || null,
      note: data.note || null,
      isDefault: data.isDefault || false,
      createdAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    };

    await docRef.set(addressData);

    return new AddressEntity({
      id: addressData.id,
      userId: addressData.userId,
      label: addressData.label,
      fullAddress: addressData.fullAddress,
      building: addressData.building,
      room: addressData.room,
      note: addressData.note,
      isDefault: addressData.isDefault,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  }

  async findByUserId(userId: string): Promise<AddressEntity[]> {
    const snapshot = await this.firestore
      .collection(this.collection)
      .where('userId', '==', userId)
      .orderBy('createdAt', 'desc')
      .get();

    return snapshot.docs.map((doc) => this.mapToEntity(doc.id, doc.data()));
  }

  async findById(addressId: string): Promise<AddressEntity | null> {
    const doc = await this.firestore.collection(this.collection).doc(addressId).get();
    if (!doc.exists) return null;

    return this.mapToEntity(doc.id, doc.data());
  }

  async update(addressId: string, data: Partial<AddressEntity>): Promise<AddressEntity> {
    const updateData: Record<string, any> = {
      ...data,
      updatedAt: FieldValue.serverTimestamp(),
    };

    // Remove undefined values and id
    delete updateData.id;
    delete updateData.userId;
    Object.keys(updateData).forEach((key) => {
      if (updateData[key] === undefined) {
        delete updateData[key];
      }
    });

    await this.firestore.collection(this.collection).doc(addressId).update(updateData);

    const updated = await this.findById(addressId);
    if (!updated) throw new Error('Address not found after update');
    return updated;
  }

  async delete(addressId: string): Promise<void> {
    await this.firestore.collection(this.collection).doc(addressId).delete();
  }

  async setDefault(userId: string, addressId: string): Promise<void> {
    const batch = this.firestore.batch();

    // Unset all current defaults for this user
    const currentDefaults = await this.firestore
      .collection(this.collection)
      .where('userId', '==', userId)
      .where('isDefault', '==', true)
      .get();

    currentDefaults.docs.forEach((doc) => {
      batch.update(doc.ref, {
        isDefault: false,
        updatedAt: FieldValue.serverTimestamp(),
      });
    });

    // Set new default
    const addressRef = this.firestore.collection(this.collection).doc(addressId);
    batch.update(addressRef, {
      isDefault: true,
      updatedAt: FieldValue.serverTimestamp(),
    });

    await batch.commit();
  }

  async getDefault(userId: string): Promise<AddressEntity | null> {
    const snapshot = await this.firestore
      .collection(this.collection)
      .where('userId', '==', userId)
      .where('isDefault', '==', true)
      .limit(1)
      .get();

    if (snapshot.empty) return null;

    const doc = snapshot.docs[0];
    return this.mapToEntity(doc.id, doc.data());
  }

  private mapToEntity(id: string, data: any): AddressEntity {
    return new AddressEntity({
      id,
      userId: data.userId,
      label: data.label,
      fullAddress: data.fullAddress,
      building: data.building,
      room: data.room,
      note: data.note,
      isDefault: data.isDefault || false,
      createdAt: data.createdAt?.toDate?.() || new Date(),
      updatedAt: data.updatedAt?.toDate?.() || new Date(),
    });
  }
}
