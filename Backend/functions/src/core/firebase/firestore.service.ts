import { Injectable } from '@nestjs/common';
import { FirebaseService } from './firebase.service';
import { FieldValue, Query, DocumentData } from 'firebase-admin/firestore';

/**
 * Base interface for all Firestore documents
 */
export interface BaseDocument {
  id?: string;
  createdAt?: FirebaseFirestore.Timestamp;
  updatedAt?: FirebaseFirestore.Timestamp;
}

/**
 * Pagination options
 */
export interface PaginationOptions {
  limit?: number;
  startAfter?: string;
  orderBy?: string;
  orderDirection?: 'asc' | 'desc';
}

/**
 * Paginated result
 */
export interface PaginatedResult<T> {
  data: T[];
  hasMore: boolean;
  lastId?: string;
}

/**
 * Firestore Service
 *
 * Generic service cho CRUD operations trên Firestore.
 * Implement Repository Pattern.
 */
@Injectable()
export class FirestoreService {
  constructor(private readonly firebase: FirebaseService) {}

  /**
   * Get Firestore instance
   */
  get db() {
    return this.firebase.firestore;
  }

  /**
   * Get collection reference
   */
  collection(name: string) {
    return this.db.collection(name);
  }

  /**
   * Find document by ID
   */
  async findById<T extends BaseDocument>(
    collectionName: string,
    id: string,
  ): Promise<T | null> {
    const doc = await this.db.collection(collectionName).doc(id).get();
    if (!doc.exists) return null;
    return { id: doc.id, ...doc.data() } as T;
  }

  /**
   * Find all documents in collection
   */
  async findAll<T extends BaseDocument>(
    collectionName: string,
    options?: PaginationOptions,
  ): Promise<PaginatedResult<T>> {
    let query: Query<DocumentData> = this.db.collection(collectionName);

    // Order by
    const orderBy = options?.orderBy || 'createdAt';
    const orderDirection = options?.orderDirection || 'desc';
    query = query.orderBy(orderBy, orderDirection);

    // Pagination
    const limit = options?.limit || 20;
    query = query.limit(limit + 1); // +1 to check hasMore

    // Start after
    if (options?.startAfter) {
      const startDoc = await this.db.collection(collectionName).doc(options.startAfter).get();
      if (startDoc.exists) {
        query = query.startAfter(startDoc);
      }
    }

    const snapshot = await query.get();
    const docs = snapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() }) as T);

    const hasMore = docs.length > limit;
    if (hasMore) docs.pop(); // Remove the extra item

    return {
      data: docs,
      hasMore,
      lastId: docs.length > 0 ? docs[docs.length - 1].id : undefined,
    };
  }

  /**
   * Find documents by field value
   */
  async findByField<T extends BaseDocument>(
    collectionName: string,
    field: string,
    value: unknown,
  ): Promise<T[]> {
    const snapshot = await this.db
      .collection(collectionName)
      .where(field, '==', value)
      .get();

    return snapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() }) as T);
  }

  /**
   * Create new document
   */
  async create<T extends BaseDocument>(
    collectionName: string,
    data: Omit<T, 'id' | 'createdAt' | 'updatedAt'>,
    id?: string,
  ): Promise<T> {
    const docRef = id
      ? this.db.collection(collectionName).doc(id)
      : this.db.collection(collectionName).doc();

    const now = FieldValue.serverTimestamp();
    const docData = {
      ...data,
      createdAt: now,
      updatedAt: now,
    };

    await docRef.set(docData);
    return this.findById<T>(collectionName, docRef.id) as Promise<T>;
  }

  /**
   * Update document
   */
  async update<T extends BaseDocument>(
    collectionName: string,
    id: string,
    data: Partial<Omit<T, 'id' | 'createdAt' | 'updatedAt'>>,
  ): Promise<T | null> {
    const docRef = this.db.collection(collectionName).doc(id);
    const doc = await docRef.get();

    if (!doc.exists) return null;

    await docRef.update({
      ...data,
      updatedAt: FieldValue.serverTimestamp(),
    });

    return this.findById<T>(collectionName, id);
  }

  /**
   * Delete document
   */
  async delete(collectionName: string, id: string): Promise<boolean> {
    const docRef = this.db.collection(collectionName).doc(id);
    const doc = await docRef.get();

    if (!doc.exists) return false;

    await docRef.delete();
    return true;
  }

  /**
   * Check if document exists
   */
  async exists(collectionName: string, id: string): Promise<boolean> {
    const doc = await this.db.collection(collectionName).doc(id).get();
    return doc.exists;
  }

  /**
   * Count documents in collection (with optional filter)
   */
  async count(
    collectionName: string,
    field?: string,
    value?: unknown,
  ): Promise<number> {
    let query: Query<DocumentData> = this.db.collection(collectionName);

    if (field && value !== undefined) {
      query = query.where(field, '==', value);
    }

    const snapshot = await query.count().get();
    return snapshot.data().count;
  }

  /**
   * Batch write operations
   */
  createBatch() {
    return this.db.batch();
  }

  /**
   * Run transaction
   */
  async runTransaction<T>(
    updateFunction: (transaction: FirebaseFirestore.Transaction) => Promise<T>,
  ): Promise<T> {
    return this.db.runTransaction(updateFunction);
  }
}
