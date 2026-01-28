/**
 * Chat Message Entity
 *
 * Represents a message in a conversation stored in Firestore.
 * Path: conversations/{conversationId}/messages/{messageId}
 */

export enum MessageStatus {
  PENDING = 'PENDING',
  SENT = 'SENT',
  READ = 'READ',
}

export interface MessageEntity {
  id: string;
  senderId: string;
  text: string; // Max 1000 chars
  status: MessageStatus;
  readAt?: Date;
  createdAt: Date;
}

export interface CreateMessageData {
  senderId: string;
  text: string;
}
