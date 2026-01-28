import { Injectable, Inject } from '@nestjs/common';
import * as admin from 'firebase-admin';
import { IMessagesRepository } from '../interfaces';
import { MessageEntity, MessageStatus, CreateMessageData } from '../entities';

/**
 * Firestore Messages Repository
 *
 * Handles CRUD operations for messages subcollection in Firestore.
 * Collection path: conversations/{conversationId}/messages/{messageId}
 */
@Injectable()
export class FirestoreMessagesRepository implements IMessagesRepository {
  private readonly db: admin.firestore.Firestore;

  constructor(@Inject('FIREBASE_APP') firebaseApp: admin.app.App) {
    this.db = firebaseApp.firestore();
  }

  private getMessagesCollection(conversationId: string) {
    return this.db.collection('conversations').doc(conversationId).collection('messages');
  }

  async findById(conversationId: string, messageId: string): Promise<MessageEntity | null> {
    const doc = await this.getMessagesCollection(conversationId).doc(messageId).get();
    if (!doc.exists) {
      return null;
    }
    return this.mapDocToEntity(doc);
  }

  async listByConversation(
    conversationId: string,
    limit: number = 20,
    startAfter?: string,
  ): Promise<{ items: MessageEntity[]; hasMore: boolean; nextCursor?: string }> {
    const messagesRef = this.getMessagesCollection(conversationId);

    let query = messagesRef.orderBy('createdAt', 'desc').limit(limit + 1);

    if (startAfter) {
      const startDoc = await messagesRef.doc(startAfter).get();
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

  async create(conversationId: string, data: CreateMessageData): Promise<MessageEntity> {
    const messagesRef = this.getMessagesCollection(conversationId);
    const now = admin.firestore.FieldValue.serverTimestamp();

    const docRef = messagesRef.doc();
    const docData = {
      senderId: data.senderId,
      text: data.text,
      status: MessageStatus.SENT,
      createdAt: now,
    };

    await docRef.set(docData);

    // Return the created entity
    const doc = await docRef.get();
    return this.mapDocToEntity(doc);
  }

  async markAsRead(
    conversationId: string,
    messageId: string,
    readAt: Date,
  ): Promise<MessageEntity> {
    const docRef = this.getMessagesCollection(conversationId).doc(messageId);

    await docRef.update({
      status: MessageStatus.READ,
      readAt: admin.firestore.Timestamp.fromDate(readAt),
    });

    const doc = await docRef.get();
    return this.mapDocToEntity(doc);
  }

  private mapDocToEntity(doc: admin.firestore.DocumentSnapshot): MessageEntity {
    const data = doc.data()!;
    return {
      id: doc.id,
      senderId: data.senderId,
      text: data.text || '',
      status: data.status || MessageStatus.SENT,
      readAt: data.readAt?.toDate(),
      createdAt: data.createdAt?.toDate() || new Date(),
    };
  }
}
