import {
  Controller,
  Get,
  Post,
  Put,
  Body,
  Param,
  Req,
  Query,
  HttpCode,
  HttpStatus,
  UseGuards,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiCreatedResponse,
  ApiOkResponse,
  ApiBadRequestResponse,
  ApiForbiddenResponse,
  ApiNotFoundResponse,
  ApiUnauthorizedResponse,
} from '@nestjs/swagger';
import { ChatService } from '../services';
import {
  CreateConversationDto,
  SendMessageDto,
  MarkAsReadDto,
  ListConversationsQueryDto,
  ListMessagesQueryDto,
} from '../dto';
import { ConversationEntity, MessageEntity } from '../entities';
import { AuthGuard } from '../../../core/guards/auth.guard';

/**
 * Chat Controller
 *
 * 1-1 text chat endpoints for all authenticated users.
 * All endpoints require authentication.
 *
 * Base URL: /api/chat
 */
@ApiTags('Chat')
@ApiBearerAuth()
@UseGuards(AuthGuard)
@Controller('chat')
export class ChatController {
  constructor(private readonly chatService: ChatService) {}

  // ========================
  // CONVERSATIONS
  // ========================

  @Post('conversations')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({
    summary: 'Create or get conversation',
    description:
      'Create a new conversation with another user or get existing one. Idempotent - returns existing if found.',
  })
  @ApiCreatedResponse({
    description: 'Conversation created or retrieved successfully',
    schema: {
      example: {
        id: 'user123__user456',
        participants: ['user123', 'user456'],
        lastMessage: '',
        lastMessageAt: '2026-01-28T10:00:00Z',
        lastSenderId: '',
        createdAt: '2026-01-28T10:00:00Z',
        updatedAt: '2026-01-28T10:00:00Z',
      },
    },
  })
  @ApiBadRequestResponse({ description: 'Invalid participant ID or trying to chat with yourself' })
  @ApiUnauthorizedResponse({ description: 'Missing or invalid authorization token' })
  async createConversation(
    @Req() req: any,
    @Body() dto: CreateConversationDto,
  ): Promise<ConversationEntity> {
    const userId = req.user.uid;
    return this.chatService.getOrCreateConversation(userId, dto);
  }

  @Get('conversations')
  @ApiOperation({
    summary: 'List my conversations',
    description:
      'Get list of all conversations for the authenticated user. Sorted by lastMessageAt DESC.',
  })
  @ApiOkResponse({
    description: 'Conversations retrieved successfully',
    schema: {
      example: {
        items: [
          {
            id: 'user123__user456',
            participants: ['user123', 'user456'],
            lastMessage: 'Hello!',
            lastMessageAt: '2026-01-28T10:05:00Z',
            lastSenderId: 'user456',
          },
        ],
        hasMore: true,
        nextCursor: 'user123__user789',
      },
    },
  })
  @ApiUnauthorizedResponse({ description: 'Missing or invalid authorization token' })
  async listConversations(
    @Req() req: any,
    @Query() query: ListConversationsQueryDto,
  ): Promise<{ items: ConversationEntity[]; hasMore: boolean; nextCursor?: string }> {
    const userId = req.user.uid;
    return this.chatService.listConversations(userId, query);
  }

  @Get('conversations/:id')
  @ApiOperation({
    summary: 'Get conversation by ID',
    description: 'Get a specific conversation. User must be a participant.',
  })
  @ApiOkResponse({ description: 'Conversation retrieved successfully' })
  @ApiForbiddenResponse({ description: 'User is not a participant in this conversation' })
  @ApiNotFoundResponse({ description: 'Conversation not found' })
  async getConversation(
    @Req() req: any,
    @Param('id') conversationId: string,
  ): Promise<ConversationEntity> {
    const userId = req.user.uid;
    return this.chatService.getConversation(userId, conversationId);
  }

  // ========================
  // MESSAGES
  // ========================

  @Get('conversations/:id/messages')
  @ApiOperation({
    summary: 'List messages in conversation',
    description: 'Get messages in a conversation with pagination. Sorted by createdAt DESC.',
  })
  @ApiOkResponse({
    description: 'Messages retrieved successfully',
    schema: {
      example: {
        items: [
          {
            id: 'msg_abc123',
            senderId: 'user456',
            text: 'Hello!',
            status: 'READ',
            readAt: '2026-01-28T10:06:00Z',
            createdAt: '2026-01-28T10:05:00Z',
          },
        ],
        hasMore: true,
        nextCursor: 'msg_ghi789',
      },
    },
  })
  @ApiForbiddenResponse({ description: 'User is not a participant in this conversation' })
  @ApiUnauthorizedResponse({ description: 'Missing or invalid authorization token' })
  async listMessages(
    @Req() req: any,
    @Param('id') conversationId: string,
    @Query() query: ListMessagesQueryDto,
  ): Promise<{ items: MessageEntity[]; hasMore: boolean; nextCursor?: string }> {
    const userId = req.user.uid;
    return this.chatService.listMessages(userId, conversationId, query);
  }

  @Post('messages')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({
    summary: 'Send message',
    description:
      'Send a text message in a conversation. Max 1000 characters. Sends FCM notification to recipient.',
  })
  @ApiCreatedResponse({
    description: 'Message sent successfully',
    schema: {
      example: {
        id: 'msg_xyz789',
        senderId: 'user123',
        text: 'Hello there!',
        status: 'SENT',
        createdAt: '2026-01-28T10:07:00Z',
      },
    },
  })
  @ApiBadRequestResponse({ description: 'Invalid message or text too long (max 1000 chars)' })
  @ApiForbiddenResponse({ description: 'User is not a participant in this conversation' })
  @ApiNotFoundResponse({ description: 'Conversation not found' })
  async sendMessage(@Req() req: any, @Body() dto: SendMessageDto): Promise<MessageEntity> {
    const userId = req.user.uid;
    return this.chatService.sendMessage(userId, dto);
  }

  @Put('messages/:id/read')
  @ApiOperation({
    summary: 'Mark message as read',
    description: 'Mark a message as read. Only the recipient (not sender) can mark as read.',
  })
  @ApiOkResponse({
    description: 'Message marked as read',
    schema: {
      example: {
        id: 'msg_abc123',
        senderId: 'user456',
        text: 'Hello!',
        status: 'READ',
        readAt: '2026-01-28T10:09:00Z',
        createdAt: '2026-01-28T10:05:00Z',
      },
    },
  })
  @ApiBadRequestResponse({ description: 'Cannot mark own message as read' })
  @ApiForbiddenResponse({ description: 'User is not a participant in this conversation' })
  @ApiNotFoundResponse({ description: 'Message not found' })
  async markAsRead(
    @Req() req: any,
    @Param('id') messageId: string,
    @Body() dto: MarkAsReadDto,
  ): Promise<MessageEntity> {
    const userId = req.user.uid;
    return this.chatService.markAsRead(userId, dto.conversationId, messageId);
  }
}
