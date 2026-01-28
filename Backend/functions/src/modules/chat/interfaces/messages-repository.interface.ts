import { MessageEntity, CreateMessageData } from '../entities';

/**
 * Messages Repository Interface
 */
export interface IMessagesRepository {
  listByConversation(
    conversationId: string,
    limit: number,
    startAfter?: string,
  ): Promise<{ items: MessageEntity[]; hasMore: boolean; nextCursor?: string }>;
  create(conversationId: string, data: CreateMessageData): Promise<MessageEntity>;
  markAsRead(conversationId: string, messageId: string, readAt: Date): Promise<MessageEntity>;
  findById(conversationId: string, messageId: string): Promise<MessageEntity | null>;
}

export const MESSAGES_REPOSITORY = 'MESSAGES_REPOSITORY';
