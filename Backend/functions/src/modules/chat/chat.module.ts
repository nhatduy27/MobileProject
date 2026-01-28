import { Module } from '@nestjs/common';
import { ChatController } from './controllers';
import { ChatService } from './services';
import { FirestoreConversationsRepository, FirestoreMessagesRepository } from './repositories';
import { CONVERSATIONS_REPOSITORY, MESSAGES_REPOSITORY } from './interfaces';
import { NotificationsModule } from '../notifications/notifications.module';

/**
 * Chat Module
 *
 * Provides 1-1 text chat functionality between users.
 * Supports text messaging, read receipts, and FCM notifications.
 */
@Module({
  imports: [NotificationsModule],
  controllers: [ChatController],
  providers: [
    ChatService,
    {
      provide: CONVERSATIONS_REPOSITORY,
      useClass: FirestoreConversationsRepository,
    },
    {
      provide: MESSAGES_REPOSITORY,
      useClass: FirestoreMessagesRepository,
    },
  ],
  exports: [ChatService],
})
export class ChatModule {}
