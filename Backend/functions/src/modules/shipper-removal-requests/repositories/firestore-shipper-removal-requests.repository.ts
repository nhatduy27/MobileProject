import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue } from '@google-cloud/firestore';
import {
  ShipperRemovalRequestEntity,
  RemovalRequestStatus,
} from '../entities/shipper-removal-request.entity';

export interface IShipperRemovalRequestsRepository {
  create(data: Partial<ShipperRemovalRequestEntity>): Promise<ShipperRemovalRequestEntity>;
  findById(id: string): Promise<ShipperRemovalRequestEntity | null>;
  findByShipperId(
    shipperId: string,
    status?: RemovalRequestStatus,
  ): Promise<ShipperRemovalRequestEntity[]>;
  findByShopId(
    shopId: string,
    status?: RemovalRequestStatus,
  ): Promise<ShipperRemovalRequestEntity[]>;
  findPendingRequest(
    shipperId: string,
    shopId: string,
  ): Promise<ShipperRemovalRequestEntity | null>;
  updateStatus(
    id: string,
    status: RemovalRequestStatus,
    processedBy: string,
    rejectionReason?: string,
  ): Promise<void>;
}

@Injectable()
export class FirestoreShipperRemovalRequestsRepository
  implements IShipperRemovalRequestsRepository
{
  private readonly collection = 'shipper_removal_requests';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async create(
    data: Partial<ShipperRemovalRequestEntity>,
  ): Promise<ShipperRemovalRequestEntity> {
    const docRef = this.firestore.collection(this.collection).doc();

    const requestData = {
      ...data,
      createdAt: FieldValue.serverTimestamp(),
    };

    await docRef.set(requestData);

    const doc = await docRef.get();
    const docData = doc.data()!;

    return new ShipperRemovalRequestEntity({
      id: doc.id,
      ...docData,
      createdAt: docData.createdAt?.toDate?.() || docData.createdAt,
    } as ShipperRemovalRequestEntity);
  }

  async findById(id: string): Promise<ShipperRemovalRequestEntity | null> {
    const doc = await this.firestore.collection(this.collection).doc(id).get();

    if (!doc.exists) {
      return null;
    }

    const data = doc.data()!;
    return new ShipperRemovalRequestEntity({
      id: doc.id,
      ...data,
      createdAt: data.createdAt?.toDate?.() || data.createdAt,
      processedAt: data.processedAt?.toDate?.() || data.processedAt,
    } as ShipperRemovalRequestEntity);
  }

  async findByShipperId(
    shipperId: string,
    status?: RemovalRequestStatus,
  ): Promise<ShipperRemovalRequestEntity[]> {
    let query: FirebaseFirestore.Query = this.firestore
      .collection(this.collection)
      .where('shipperId', '==', shipperId);

    if (status) {
      query = query.where('status', '==', status);
    }

    const snapshot = await query.orderBy('createdAt', 'desc').get();

    return snapshot.docs.map((doc) => {
      const data = doc.data();
      return new ShipperRemovalRequestEntity({
        id: doc.id,
        ...data,
        createdAt: data.createdAt?.toDate?.() || data.createdAt,
        processedAt: data.processedAt?.toDate?.() || data.processedAt,
      } as ShipperRemovalRequestEntity);
    });
  }

  async findByShopId(
    shopId: string,
    status?: RemovalRequestStatus,
  ): Promise<ShipperRemovalRequestEntity[]> {
    let query: FirebaseFirestore.Query = this.firestore
      .collection(this.collection)
      .where('shopId', '==', shopId);

    if (status) {
      query = query.where('status', '==', status);
    }

    const snapshot = await query.orderBy('createdAt', 'desc').get();

    return snapshot.docs.map((doc) => {
      const data = doc.data();
      return new ShipperRemovalRequestEntity({
        id: doc.id,
        ...data,
        createdAt: data.createdAt?.toDate?.() || data.createdAt,
        processedAt: data.processedAt?.toDate?.() || data.processedAt,
      } as ShipperRemovalRequestEntity);
    });
  }

  async findPendingRequest(
    shipperId: string,
    shopId: string,
  ): Promise<ShipperRemovalRequestEntity | null> {
    const snapshot = await this.firestore
      .collection(this.collection)
      .where('shipperId', '==', shipperId)
      .where('shopId', '==', shopId)
      .where('status', '==', RemovalRequestStatus.PENDING)
      .limit(1)
      .get();

    if (snapshot.empty) {
      return null;
    }

    const doc = snapshot.docs[0];
    const data = doc.data();
    return new ShipperRemovalRequestEntity({
      id: doc.id,
      ...data,
      createdAt: data.createdAt?.toDate?.() || data.createdAt,
      processedAt: data.processedAt?.toDate?.() || data.processedAt,
    } as ShipperRemovalRequestEntity);
  }

  async updateStatus(
    id: string,
    status: RemovalRequestStatus,
    processedBy: string,
    rejectionReason?: string,
  ): Promise<void> {
    const updateData: Record<string, unknown> = {
      status,
      processedBy,
      processedAt: FieldValue.serverTimestamp(),
    };

    if (rejectionReason) {
      updateData.rejectionReason = rejectionReason;
    }

    await this.firestore.collection(this.collection).doc(id).update(updateData);
  }
}
