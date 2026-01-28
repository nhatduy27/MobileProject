import { Injectable, Inject, Logger } from '@nestjs/common';
import { Firestore, FieldValue } from '@google-cloud/firestore';
import { ShipperTrip, TripStatus } from '../entities/shipper-trip.entity';

/**
 * Shipper Trips Repository
 *
 * Manages shipper trips in Firestore.
 */
@Injectable()
export class ShipperTripsRepository {
  private readonly logger = new Logger(ShipperTripsRepository.name);
  private readonly collectionName = 'shipperTrips';

  constructor(
    @Inject('FIRESTORE')
    private readonly firestore: Firestore,
  ) {}

  /**
   * Create new trip
   */
  async create(tripData: Omit<ShipperTrip, 'id' | 'createdAt' | 'updatedAt'>): Promise<ShipperTrip> {
    const docRef = this.firestore.collection(this.collectionName).doc();
    const timestamp = FieldValue.serverTimestamp();

    const trip: Omit<ShipperTrip, 'createdAt' | 'updatedAt'> = {
      id: docRef.id,
      ...tripData,
    };

    await docRef.set({
      ...trip,
      createdAt: timestamp,
      updatedAt: timestamp,
    });

    this.logger.log(`Created trip: ${docRef.id} for shipper: ${tripData.shipperId}`);

    // Return with mock timestamps (Firestore will set server timestamp)
    return {
      ...trip,
      createdAt: new Date() as any,
      updatedAt: new Date() as any,
    } as ShipperTrip;
  }

  /**
   * Get trip by ID
   */
  async getById(tripId: string): Promise<ShipperTrip | null> {
    const docRef = this.firestore.collection(this.collectionName).doc(tripId);
    const doc = await docRef.get();

    if (!doc.exists) {
      return null;
    }

    return doc.data() as ShipperTrip;
  }

  /**
   * Update trip
   */
  async update(tripId: string, data: Partial<ShipperTrip>): Promise<void> {
    const docRef = this.firestore.collection(this.collectionName).doc(tripId);
    await docRef.update({
      ...data,
      updatedAt: FieldValue.serverTimestamp(),
    });

    this.logger.log(`Updated trip: ${tripId}`);
  }

  /**
   * Get trips by shipper ID with pagination and optional status filter
   */
  async getByShipperIdPaginated(
    shipperId: string,
    page: number,
    limit: number,
    status?: TripStatus,
  ): Promise<{ trips: ShipperTrip[]; total: number }> {
    // Build base query
    let query = this.firestore
      .collection(this.collectionName)
      .where('shipperId', '==', shipperId);

    // Add status filter if provided
    if (status) {
      query = query.where('status', '==', status) as any;
    }

    // Get total count
    const countSnapshot = await query.get();
    const total = countSnapshot.size;

    // Apply pagination and sorting
    const offset = (page - 1) * limit;
    const paginatedQuery = query
      .orderBy('createdAt', 'desc')
      .offset(offset)
      .limit(limit);

    const snapshot = await paginatedQuery.get();
    const trips = snapshot.docs.map((doc) => doc.data() as ShipperTrip);

    return { trips, total };
  }

  /**
   * Get trips by shipper ID with optional status filter
   */
  async getByShipperId(
    shipperId: string,
    status?: TripStatus,
    limit: number = 20,
  ): Promise<ShipperTrip[]> {
    let query = this.firestore
      .collection(this.collectionName)
      .where('shipperId', '==', shipperId)
      .orderBy('createdAt', 'desc')
      .limit(limit);

    if (status) {
      query = query.where('status', '==', status) as any;
    }

    const snapshot = await query.get();
    return snapshot.docs.map((doc) => doc.data() as ShipperTrip);
  }
}
