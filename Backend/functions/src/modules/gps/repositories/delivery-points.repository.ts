import { Injectable, Inject, Logger } from '@nestjs/common';
import { Firestore, FieldValue } from '@google-cloud/firestore';
import { DeliveryPoint } from '../entities/delivery-point.entity';

/**
 * Delivery Points Repository
 *
 * Manages fixed delivery points (KTX buildings) in Firestore.
 */
@Injectable()
export class DeliveryPointsRepository {
  private readonly logger = new Logger(DeliveryPointsRepository.name);
  private readonly collectionName = 'deliveryPoints';

  constructor(
    @Inject('FIRESTORE')
    private readonly firestore: Firestore,
  ) {}

  /**
   * Get delivery point by building code
   */
  async getByBuildingCode(buildingCode: string): Promise<DeliveryPoint | null> {
    const docRef = this.firestore.collection(this.collectionName).doc(buildingCode);
    const doc = await docRef.get();

    if (!doc.exists) {
      return null;
    }

    return doc.data() as DeliveryPoint;
  }

  /**
   * Get multiple delivery points by building codes
   */
  async getByBuildingCodes(buildingCodes: string[]): Promise<Map<string, DeliveryPoint>> {
    const result = new Map<string, DeliveryPoint>();

    if (buildingCodes.length === 0) {
      return result;
    }

    // Firestore 'in' query limit is 10, so batch if needed
    const batchSize = 10;
    const batches: string[][] = [];

    for (let i = 0; i < buildingCodes.length; i += batchSize) {
      batches.push(buildingCodes.slice(i, i + batchSize));
    }

    for (const batch of batches) {
      const snapshot = await this.firestore
        .collection(this.collectionName)
        .where('buildingCode', 'in', batch)
        .get();

      snapshot.docs.forEach((doc) => {
        const data = doc.data() as DeliveryPoint;
        result.set(data.buildingCode, data);
      });
    }

    return result;
  }

  /**
   * Upsert delivery point (create or update)
   * Used for seeding data
   */
  async upsert(buildingCode: string, data: Partial<DeliveryPoint>): Promise<void> {
    const docRef = this.firestore.collection(this.collectionName).doc(buildingCode);
    const doc = await docRef.get();

    const timestamp = FieldValue.serverTimestamp();

    if (doc.exists) {
      // Update existing
      await docRef.update({
        ...data,
        updatedAt: timestamp,
      });
      this.logger.log(`Updated delivery point: ${buildingCode}`);
    } else {
      // Create new
      await docRef.set({
        id: buildingCode,
        ...data,
        createdAt: timestamp,
        updatedAt: timestamp,
      });
      this.logger.log(`Created delivery point: ${buildingCode}`);
    }
  }

  /**
   * Get all delivery points
   */
  async getAll(): Promise<DeliveryPoint[]> {
    const snapshot = await this.firestore.collection(this.collectionName).get();
    return snapshot.docs.map((doc) => doc.data() as DeliveryPoint);
  }

  /**
   * List all active delivery points
   * Returns only delivery points where active === true
   */
  async listActiveDeliveryPoints(): Promise<DeliveryPoint[]> {
    const snapshot = await this.firestore
      .collection(this.collectionName)
      .where('active', '==', true)
      .get();

    return snapshot.docs.map((doc) => doc.data() as DeliveryPoint);
  }
}
