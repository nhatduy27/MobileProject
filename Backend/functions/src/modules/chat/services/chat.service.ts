import {
  Injectable,
  Inject,
  Logger,
  ForbiddenException,
  BadRequestException,
  NotFoundException,
} from '@nestjs/common';
import * as admin from 'firebase-admin';
import {
  IConversationsRepository,
  CONVERSATIONS_REPOSITORY,
  IMessagesRepository,
  MESSAGES_REPOSITORY,
} from '../interfaces';
import { ConversationEntity, MessageEntity, MessageStatus } from '../entities';
import {
  CreateConversationDto,
  SendMessageDto,
  ListConversationsQueryDto,
  ListMessagesQueryDto,
} from '../dto';
import { NotificationsService } from '../../notifications/services';
import { NotificationType } from '../../notifications/entities';
import { NotificationCategory } from '../../notifications/dto';

/**
 * Chat Service
 *
 * Core business logic for 1-1 text chat functionality.
 * Handles conversation management, message sending, and read receipts.
 */
@Injectable()
export class ChatService {
  private readonly logger = new Logger(ChatService.name);

  constructor(
    @Inject(CONVERSATIONS_REPOSITORY)
    private readonly conversationsRepo: IConversationsRepository,
    @Inject(MESSAGES_REPOSITORY)
    private readonly messagesRepo: IMessagesRepository,
    private readonly notificationsService: NotificationsService,
    @Inject('FIREBASE_APP') private readonly firebaseApp: admin.app.App,
  ) {}

  /**
   * Generate deterministic conversation ID from two user IDs.
   * Format: {minUid}__{maxUid} (sorted alphabetically)
   */
  generateConversationId(userId1: string, userId2: string): string {
    return [userId1, userId2].sort().join('__');
  }

  /**
   * Parse conversation ID to get participant IDs.
   */
  parseConversationId(conversationId: string): [string, string] {
    const parts = conversationId.split('__');
    if (parts.length !== 2) {
      throw new BadRequestException('Invalid conversation ID format');
    }
    return [parts[0], parts[1]];
  }

  /**
   * Check if user is a participant in the conversation.
   */
  isParticipant(conversationId: string, userId: string): boolean {
    const [uid1, uid2] = this.parseConversationId(conversationId);
    return uid1 === userId || uid2 === userId;
  }

  /**
   * Get the other participant in a conversation.
   */
  getOtherParticipant(conversationId: string, userId: string): string {
    const [uid1, uid2] = this.parseConversationId(conversationId);
    return uid1 === userId ? uid2 : uid1;
  }

  /**
   * Create or get existing conversation (idempotent).
   * Returns existing conversation if found, creates new one if not.
   */
  async getOrCreateConversation(
    userId: string,
    dto: CreateConversationDto,
  ): Promise<ConversationEntity> {
    const { participantId } = dto;

    // Validate: cannot chat with yourself
    if (userId === participantId) {
      throw new BadRequestException('Cannot create conversation with yourself');
    }

    const conversationId = this.generateConversationId(userId, participantId);

    // Check if conversation already exists
    const existing = await this.conversationsRepo.findById(conversationId);
    if (existing) {
      this.logger.debug(`Conversation ${conversationId} already exists`);
      return existing;
    }

    // Create new conversation
    this.logger.log(`Creating new conversation: ${conversationId}`);
    return this.conversationsRepo.create({
      id: conversationId,
      participants: [userId, participantId],
    });
  }

  /**
   * Get conversation by ID.
   * Validates that user is a participant.
   */
  async getConversation(userId: string, conversationId: string): Promise<ConversationEntity> {
    // Validate user is participant
    if (!this.isParticipant(conversationId, userId)) {
      throw new ForbiddenException('You are not a participant in this conversation');
    }

    const conversation = await this.conversationsRepo.findById(conversationId);
    if (!conversation) {
      throw new NotFoundException('Conversation not found');
    }

    return conversation;
  }

  /**
   * List conversations for a user with pagination.
   */
  async listConversations(
    userId: string,
    query: ListConversationsQueryDto,
  ): Promise<{ items: ConversationEntity[]; hasMore: boolean; nextCursor?: string }> {
    const { limit = 20, startAfter } = query;
    return this.conversationsRepo.listByUser(userId, limit, startAfter);
  }

  /**
   * List messages in a conversation with pagination.
   * Validates that user is a participant.
   */
  async listMessages(
    userId: string,
    conversationId: string,
    query: ListMessagesQueryDto,
  ): Promise<{ items: MessageEntity[]; hasMore: boolean; nextCursor?: string }> {
    // Validate user is participant
    if (!this.isParticipant(conversationId, userId)) {
      throw new ForbiddenException('You are not a participant in this conversation');
    }

    const { limit = 20, startAfter } = query;
    return this.messagesRepo.listByConversation(conversationId, limit, startAfter);
  }

  /**
   * Send a message in a conversation.
   * Uses Firestore transaction to atomically create message + update conversation.
   * Sends FCM notification to recipient asynchronously.
   */
  async sendMessage(userId: string, dto: SendMessageDto): Promise<MessageEntity> {
    const { conversationId, text } = dto;

    // Validate user is participant
    if (!this.isParticipant(conversationId, userId)) {
      throw new ForbiddenException('You are not a participant in this conversation');
    }

    // Validate conversation exists
    const conversation = await this.conversationsRepo.findById(conversationId);
    if (!conversation) {
      throw new NotFoundException('Conversation not found');
    }

    // Use Firestore transaction for atomicity
    const db = this.firebaseApp.firestore();
    const messageRef = db.collection('conversations').doc(conversationId).collection('messages').doc();
    const conversationRef = db.collection('conversations').doc(conversationId);
    const now = admin.firestore.FieldValue.serverTimestamp();

    await db.runTransaction(async (transaction) => {
      // Create message
      transaction.set(messageRef, {
        senderId: userId,
        text,
        status: MessageStatus.SENT,
        createdAt: now,
      });

      // Update conversation's last message
      transaction.update(conversationRef, {
        lastMessage: text.substring(0, 100),
        lastSenderId: userId,
        lastMessageAt: now,
        updatedAt: now,
      });
    });

    // Fetch the created message
    const messageDoc = await messageRef.get();
    const message: MessageEntity = {
      id: messageDoc.id,
      senderId: userId,
      text,
      status: MessageStatus.SENT,
      createdAt: messageDoc.data()?.createdAt?.toDate() || new Date(),
    };

    // Send FCM notification to recipient (async, non-blocking)
    const recipientId = this.getOtherParticipant(conversationId, userId);
    this.sendNotificationToRecipient(recipientId, userId, text, conversationId).catch((err) => {
      this.logger.error(`Failed to send notification: ${err.message}`);
    });

    this.logger.log(`Message sent: ${message.id} in ${conversationId}`);
    return message;
  }

  /**
   * Mark a message as read.
   * Validates that user is participant AND not the sender.
   */
  async markAsRead(
    userId: string,
    conversationId: string,
    messageId: string,
  ): Promise<MessageEntity> {
    // Validate user is participant
    if (!this.isParticipant(conversationId, userId)) {
      throw new ForbiddenException('You are not a participant in this conversation');
    }

    // Get message
    const message = await this.messagesRepo.findById(conversationId, messageId);
    if (!message) {
      throw new NotFoundException('Message not found');
    }

    // Validate user is not the sender
    if (message.senderId === userId) {
      throw new BadRequestException('Cannot mark your own message as read');
    }

    // Skip if already read
    if (message.status === MessageStatus.READ) {
      return message;
    }

    // Mark as read
    return this.messagesRepo.markAsRead(conversationId, messageId, new Date());
  }

  /**
   * Send FCM notification to recipient.
   * This is called asynchronously and should not block message sending.
   */
  private async sendNotificationToRecipient(
    recipientId: string,
    senderId: string,
    text: string,
    conversationId: string,
  ): Promise<void> {
    try {
      // Get sender info for notification
      const auth = this.firebaseApp.auth();
      const senderRecord = await auth.getUser(senderId);
      const senderName = senderRecord.displayName || 'Someone';

      // Send notification
      await this.notificationsService.send({
        userId: recipientId,
        title: `New message from ${senderName}`,
        body: text.length > 100 ? text.substring(0, 97) + '...' : text,
        type: NotificationType.CHAT,
        category: NotificationCategory.TRANSACTIONAL,
        data: {
          conversationId,
          senderId,
          type: 'CHAT_MESSAGE',
        },
      });
    } catch (error) {
      this.logger.error(`Failed to send chat notification: ${error}`);
      // Don't throw - notification failure should not affect message sending
    }
  }
}
