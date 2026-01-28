import { ConversationEntity, CreateConversationData, UpdateLastMessageData } from '../entities';

/**
 * Conversations Repository Interface
 */
export interface IConversationsRepository {
  findById(id: string): Promise<ConversationEntity | null>;
  findByParticipants(userId1: string, userId2: string): Promise<ConversationEntity | null>;
  listByUser(
    userId: string,
    limit: number,
    startAfter?: string,
  ): Promise<{ items: ConversationEntity[]; hasMore: boolean; nextCursor?: string }>;
  create(data: CreateConversationData): Promise<ConversationEntity>;
  updateLastMessage(id: string, data: UpdateLastMessageData): Promise<void>;
}

export const CONVERSATIONS_REPOSITORY = 'CONVERSATIONS_REPOSITORY';
