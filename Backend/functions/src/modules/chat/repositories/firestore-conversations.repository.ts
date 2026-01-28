import { Injectable, Inject } from '@nestjs/common';
import * as admin from 'firebase-admin';
import { IConversationsRepository } from '../interfaces';
import { ConversationEntity, CreateConversationData, UpdateLastMessageData } from '../entities';

/**
 * Firestore Conversations Repository
 *
 * Handles CRUD operations for conversations collection in Firestore.
 * Collection path: conversations/{conversationId}
 */
@Injectable()
export class FirestoreConversationsRepository implements IConversationsRepository {
  private readonly db: admin.firestore.Firestore;
  private readonly collection: admin.firestore.CollectionReference;

  constructor(@Inject('FIREBASE_APP') firebaseApp: admin.app.App) {
    this.db = firebaseApp.firestore();
    this.collection = this.db.collection('conversations');
  }

  async findById(id: string): Promise<ConversationEntity | null> {
    const doc = await this.collection.doc(id).get();
    if (!doc.exists) {
      return null;
    }
    return this.mapDocToEntity(doc);
  }

  async findByParticipants(userId1: string, userId2: string): Promise<ConversationEntity | null> {
    // Generate deterministic ID: sorted
    const id = [userId1, userId2].sort().join('__');
    return this.findById(id);
  }

  async listByUser(
    userId: string,
    limit: number = 20,
    startAfter?: string,
  ): Promise<{ items: ConversationEntity[]; hasMore: boolean; nextCursor?: string }> {
    let query = this.collection
      .where('participants', 'array-contains', userId)
      .orderBy('lastMessageAt', 'desc')
      .limit(limit + 1); // Fetch one extra to check if there are more

    if (startAfter) {
      const startDoc = await this.collection.doc(startAfter).get();
      if (startDoc.exists) {
        query = query.startAfter(startDoc);
      }
    }

    const snapshot = await query.get();
    const docs = snapshot.docs;

    const hasMore = docs.length > limit;
    const items = docs.slice(0, limit).map((doc) => this.mapDocToEntity(doc));
    const nextCursor = hasMore && items.length > 0 ? items[items.length - 1].id : undefined;

    return { items, hasMore, nextCursor };
  }

  async create(data: CreateConversationData): Promise<ConversationEntity> {
    const now = admin.firestore.FieldValue.serverTimestamp();
    const docRef = this.collection.doc(data.id);

    const docData = {
      participants: data.participants.sort(),
      lastMessage: '',
      lastMessageAt: now,
      lastSenderId: '',
      createdAt: now,
      updatedAt: now,
    };

    await docRef.set(docData);

    // Return the created entity
    const doc = await docRef.get();
    return this.mapDocToEntity(doc);
  }

  async updateLastMessage(id: string, data: UpdateLastMessageData): Promise<void> {
    await this.collection.doc(id).update({
      lastMessage: data.lastMessage.substring(0, 100), // Truncate preview
      lastSenderId: data.lastSenderId,
      lastMessageAt: admin.firestore.Timestamp.fromDate(data.lastMessageAt),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  }

  private mapDocToEntity(doc: admin.firestore.DocumentSnapshot): ConversationEntity {
    const data = doc.data()!;
    return {
      id: doc.id,
      participants: data.participants || [],
      lastMessage: data.lastMessage || '',
      lastMessageAt: data.lastMessageAt?.toDate() || new Date(),
      lastSenderId: data.lastSenderId || '',
      createdAt: data.createdAt?.toDate() || new Date(),
      updatedAt: data.updatedAt?.toDate() || new Date(),
    };
  }
}
