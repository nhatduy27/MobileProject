import { Injectable, Inject, Logger } from '@nestjs/common';
import {
  IDeviceTokensRepository,
  INotificationsRepository,
  SendNotificationOptions,
} from '../interfaces';
import {
  NotificationEntity,
  NotificationType,
  DeviceTokenEntity,
  NotificationPreferencesEntity,
} from '../entities';
import { RegisterDeviceTokenDto, NotificationCategory } from '../dto';
import { FCMService } from './fcm.service';
import { INotificationPreferencesRepository } from '../interfaces/repository.interface';
import { ITopicSubscriptionsRepository } from '../repositories/firestore-topic-subscriptions.repository';

/**
 * Notifications Service
 * Core service for sending and managing notifications
 */
@Injectable()
export class NotificationsService {
  private readonly logger = new Logger(NotificationsService.name);

  constructor(
    @Inject('DEVICE_TOKENS_REPOSITORY') private deviceTokensRepository: IDeviceTokensRepository,
    @Inject('NOTIFICATIONS_REPOSITORY') private notificationsRepository: INotificationsRepository,
    @Inject('NOTIFICATION_PREFERENCES_REPOSITORY')
    private preferencesRepository: INotificationPreferencesRepository,
    @Inject('TOPIC_SUBSCRIPTIONS_REPOSITORY')
    private topicSubscriptionsRepository: ITopicSubscriptionsRepository,
    private fcmService: FCMService,
  ) {}

  /**
   * Send notification to user
   * - Check user preferences (NOTIF-013)
   * - Load user's device tokens
   * - Send FCM (best-effort)
   * - ALWAYS save notification history with delivery metadata (NOTIF-014)
   */
  async send(
    options: SendNotificationOptions & { category?: NotificationCategory },
  ): Promise<NotificationEntity> {
    const { userId, title, body, type, data, imageUrl, orderId, shopId, category } = options;

    let deliveryStatus: 'SENT' | 'FAILED' = 'SENT';
    let deliveryErrorCode: string | undefined;
    let deliveryErrorMessage: string | undefined;
    const sentAt = new Date().toISOString();

    try {
      // 1. Check user preferences (skip for transactional notifications)
      if (category && category !== NotificationCategory.TRANSACTIONAL) {
        const preferences = await this.getPreferences(userId);
        const shouldSend = this.checkPreferences(preferences, category);

        if (!shouldSend) {
          this.logger.debug(
            `Skipping notification for user ${userId} due to preferences (category: ${category})`,
          );
          // Still save notification history, but mark as not sent
          const notification = await this.notificationsRepository.create(userId, {
            title,
            body,
            type: type as NotificationType,
            data,
            imageUrl,
            read: false,
            orderId,
            shopId,
            sentAt,
            deliveryStatus: 'FAILED',
            deliveryErrorCode: 'PREFERENCES_DISABLED',
            deliveryErrorMessage: `User has disabled ${category} notifications`,
          });
          return notification;
        }
      }

      // 2. Load user's device tokens
      const deviceTokens = await this.deviceTokensRepository.findByUserId(userId);
      const tokens = deviceTokens.map((dt) => dt.token);

      // 3. Send FCM if tokens exist (best-effort, don't fail if FCM fails)
      if (tokens.length > 0) {
        try {
          await this.fcmService.sendToTokens(tokens, {
            title,
            body,
            imageUrl,
            data,
          });
        } catch (fcmError: any) {
          this.logger.error(`FCM send failed for user ${userId}:`, fcmError);
          deliveryStatus = 'FAILED';
          deliveryErrorCode = fcmError?.code || 'FCM_ERROR';
          deliveryErrorMessage = fcmError?.message || 'FCM delivery failed';
          // Continue anyway - we still need to save notification history
        }
      } else {
        this.logger.debug(`No device tokens found for user ${userId}`);
        deliveryStatus = 'FAILED';
        deliveryErrorCode = 'NO_TOKENS';
        deliveryErrorMessage = 'No device tokens registered for user';
      }

      // 4. ALWAYS save notification history with delivery metadata (even if FCM failed)
      const notification = await this.notificationsRepository.create(userId, {
        title,
        body,
        type: type as NotificationType,
        data,
        imageUrl,
        read: false,
        orderId,
        shopId,
        sentAt,
        deliveryStatus,
        deliveryErrorCode,
        deliveryErrorMessage,
      });

      return notification;
    } catch (error) {
      this.logger.error(`Error sending notification to user ${userId}:`, error);
      throw error;
    }
  }

  /**
   * Get user's notifications with pagination
   */
  async getMyNotifications(
    userId: string,
    read?: boolean,
    page: number = 1,
    limit: number = 20,
  ): Promise<{
    items: NotificationEntity[];
    total: number;
    page: number;
    limit: number;
    unreadCount: number;
  }> {
    if (process.env.DEBUG_NOTIF_QUERY === '1') {
      console.log('[NotificationsService getMyNotifications]');
      console.log('  userId:', userId);
      console.log('  read:', read, 'typeof:', typeof read);
      console.log('  read === true:', read === true);
      console.log('  read === false:', read === false);
      console.log('  read === undefined:', read === undefined);
    }

    const offset = (page - 1) * limit;

    // Get paginated notifications
    const { items, total } = await this.notificationsRepository.findByUserId(userId, {
      read,
      limit,
      offset,
    });

    // Get unread count
    const { total: unreadTotal } = await this.notificationsRepository.findByUserId(userId, {
      read: false,
    });

    return {
      items,
      total,
      page,
      limit,
      unreadCount: unreadTotal,
    };
  }

  /**
   * Mark single notification as read
   */
  async markAsRead(userId: string, notificationId: string): Promise<NotificationEntity> {
    // Verify notification belongs to user
    const notification = await this.notificationsRepository.findById(userId, notificationId);

    if (!notification) {
      throw new Error('Notification not found');
    }

    // Mark as read
    return await this.notificationsRepository.updateReadStatus(userId, notificationId);
  }

  /**
   * Mark all notifications as read
   */
  async markAllAsRead(userId: string): Promise<number> {
    return await this.notificationsRepository.markAllAsRead(userId);
  }

  // ==================== Device Token Management ====================

  /**
   * Register or update device token
   *
   * Rules:
   * - Upsert by token (update lastUsedAt if token exists)
   * - One user may have multiple tokens
   * - If token belongs to another user, transfer ownership
   */
  async registerDeviceToken(
    userId: string,
    dto: RegisterDeviceTokenDto,
  ): Promise<DeviceTokenEntity> {
    const { token, platform, deviceInfo } = dto;

    // Check if token already exists
    const existing = await this.deviceTokensRepository.findByToken(token);

    if (existing && existing.userId !== userId) {
      // Token belongs to another user - delete the old one first
      await this.deviceTokensRepository.deleteByToken(existing.userId, token);
    }

    if (existing && existing.userId === userId) {
      // Update lastUsedAt for same user
      await this.deviceTokensRepository.updateLastUsed(userId, token);
      const updated = await this.deviceTokensRepository.findByToken(token);
      return updated!;
    }

    // Create new token
    return await this.deviceTokensRepository.create(userId, {
      token,
      platform,
      deviceInfo,
    });
  }

  /**
   * Unregister device token
   *
   * Rules:
   * - User can only remove their own tokens
   * - Idempotent (no error if token doesn't exist)
   */
  async unregisterDeviceToken(userId: string, token: string): Promise<void> {
    // Verify token belongs to user (optional, but good for security)
    const existing = await this.deviceTokensRepository.findByToken(token);

    if (existing && existing.userId !== userId) {
      this.logger.warn(`User ${userId} attempted to delete token belonging to ${existing.userId}`);
      // Don't reveal that token exists, just return silently (idempotent)
      return;
    }

    if (existing) {
      await this.deviceTokensRepository.deleteByToken(userId, token);
    }
  }

  // ==================== Notification Preferences (NOTIF-013) ====================

  /**
   * Get user's notification preferences
   * Returns defaults if not found
   */
  async getPreferences(userId: string): Promise<NotificationPreferencesEntity> {
    const existing = await this.preferencesRepository.findByUserId(userId);

    if (existing) {
      return existing;
    }

    // Return defaults
    return {
      userId,
      transactional: true,
      informational: true,
      marketing: true,
      updatedAt: new Date().toISOString(),
    };
  }

  /**
   * Update user's notification preferences
   * transactional is always true and cannot be modified
   */
  async updatePreferences(
    userId: string,
    data: { informational?: boolean; marketing?: boolean },
  ): Promise<NotificationPreferencesEntity> {
    const existing = await this.preferencesRepository.findByUserId(userId);

    if (!existing) {
      // Create new with defaults
      return await this.preferencesRepository.create(userId, {
        transactional: true,
        informational: data.informational ?? true,
        marketing: data.marketing ?? true,
      });
    }

    // Update existing (transactional is always true)
    return await this.preferencesRepository.update(userId, {
      transactional: true, // Force to true
      informational: data.informational,
      marketing: data.marketing,
    });
  }

  /**
   * Check if notification should be sent based on preferences
   */
  private checkPreferences(
    preferences: NotificationPreferencesEntity,
    category: NotificationCategory,
  ): boolean {
    switch (category) {
      case NotificationCategory.TRANSACTIONAL:
        return true; // Always send transactional
      case NotificationCategory.INFORMATIONAL:
        return preferences.informational;
      case NotificationCategory.MARKETING:
        return preferences.marketing;
      default:
        return true;
    }
  }

  // ==================== Admin Batch Send (NOTIF-011) ====================

  /**
   * Send notification to multiple users (admin only)
   * Returns summary of results
   */
  async adminBatchSend(options: {
    userIds: string[];
    title: string;
    body: string;
    type: NotificationType;
    category?: NotificationCategory;
    data?: Record<string, unknown>;
    imageUrl?: string;
  }): Promise<{
    requestedCount: number;
    successCount: number;
    failureCount: number;
    failures?: Array<{ userId: string; reason: string }>;
  }> {
    const { userIds, title, body, type, category, data, imageUrl } = options;

    let successCount = 0;
    let failureCount = 0;
    const failures: Array<{ userId: string; reason: string }> = [];

    // Process each user
    for (const userId of userIds) {
      try {
        await this.send({
          userId,
          title,
          body,
          type,
          category,
          data,
          imageUrl,
        });
        successCount++;
      } catch (error: any) {
        failureCount++;
        failures.push({
          userId,
          reason: error?.message || 'Unknown error',
        });
        this.logger.error(`Failed to send batch notification to user ${userId}:`, error);
      }
    }

    return {
      requestedCount: userIds.length,
      successCount,
      failureCount,
      failures: failures.length > 0 ? failures : undefined,
    };
  }

  // ==================== Topic Management (NOTIF-012) ====================

  /**
   * Subscribe users/tokens to topic
   */
  async subscribeToTopic(options: {
    topic: string;
    userIds?: string[];
    tokens?: string[];
  }): Promise<{ subscribedCount: number }> {
    const { topic, userIds, tokens: directTokens } = options;

    const allTokens: Array<{ token: string; userId?: string }> = [];

    // Resolve userIds to tokens
    if (userIds && userIds.length > 0) {
      for (const userId of userIds) {
        const deviceTokens = await this.deviceTokensRepository.findByUserId(userId);
        for (const dt of deviceTokens) {
          // Skip invalid/test tokens (FCM tokens are typically 150+ chars)
          if (dt.token && dt.token.length > 50) {
            allTokens.push({ token: dt.token, userId });
          } else {
            this.logger.warn(`Skipping invalid token for user ${userId}: ${dt.token?.substring(0, 20)}...`);
          }
        }
      }
    }

    // Add direct tokens (also validate)
    if (directTokens && directTokens.length > 0) {
      const validTokens = directTokens.filter(token => token && token.length > 50);
      allTokens.push(...validTokens.map((token) => ({ token })));
    }

    if (allTokens.length === 0) {
      this.logger.warn(`No tokens to subscribe to topic ${topic}`);
      return { subscribedCount: 0 };
    }

    // Subscribe to FCM topic
    const tokenStrings = allTokens.map((t) => t.token);
    await this.fcmService.subscribeToTopic(tokenStrings, topic);

    // Save subscriptions to database (for tracking which users are subscribed)
    for (const { token, userId } of allTokens) {
      if (userId) {
        try {
          await this.topicSubscriptionsRepository.create({
            topic,
            userId,
            token,
          });
        } catch (error) {
          this.logger.error(`Failed to save topic subscription for user ${userId}:`, error);
        }
      }
    }

    return { subscribedCount: allTokens.length };
  }

  /**
   * Unsubscribe users/tokens from topic
   */
  async unsubscribeFromTopic(options: {
    topic: string;
    userIds?: string[];
    tokens?: string[];
  }): Promise<{ unsubscribedCount: number }> {
    const { topic, userIds, tokens: directTokens } = options;

    const allTokens: Array<{ token: string; userId?: string }> = [];

    // Resolve userIds to tokens
    if (userIds && userIds.length > 0) {
      for (const userId of userIds) {
        const deviceTokens = await this.deviceTokensRepository.findByUserId(userId);
        for (const dt of deviceTokens) {
          // Skip invalid/test tokens (FCM tokens are typically 150+ chars)
          if (dt.token && dt.token.length > 50) {
            allTokens.push({ token: dt.token, userId });
          } else {
            this.logger.warn(`Skipping invalid token for user ${userId}: ${dt.token?.substring(0, 20)}...`);
          }
        }
      }
    }

    // Add direct tokens (also validate)
    if (directTokens && directTokens.length > 0) {
      const validTokens = directTokens.filter(token => token && token.length > 50);
      allTokens.push(...validTokens.map((token) => ({ token })));
    }

    if (allTokens.length === 0) {
      this.logger.warn(`No tokens to unsubscribe from topic ${topic}`);
      return { unsubscribedCount: 0 };
    }

    // Unsubscribe from FCM topic
    const tokenStrings = allTokens.map((t) => t.token);
    await this.fcmService.unsubscribeFromTopic(tokenStrings, topic);

    // Remove subscriptions from database
    for (const { token, userId } of allTokens) {
      try {
        if (userId) {
          await this.topicSubscriptionsRepository.deleteByTopicAndUserId(topic, userId);
        } else {
          await this.topicSubscriptionsRepository.delete(topic, token);
        }
      } catch (error) {
        this.logger.error(`Failed to delete topic subscription:`, error);
      }
    }

    return { unsubscribedCount: allTokens.length };
  }

  /**
   * Send notification to topic (admin only)
   * Also persists notification history for subscribed users
   */
  async sendToTopic(options: {
    topic: string;
    title: string;
    body: string;
    type: NotificationType;
    category?: NotificationCategory;
    data?: Record<string, unknown>;
    imageUrl?: string;
  }): Promise<{ sentToFCM: boolean; persistedCount: number }> {
    const { topic, title, body, type, data, imageUrl } = options;

    // 1. Send to FCM topic
    let sentToFCM = false;
    try {
      await this.fcmService.sendToTopic(topic, {
        title,
        body,
        imageUrl,
        data,
      });
      sentToFCM = true;
    } catch (error) {
      this.logger.error(`Failed to send to FCM topic ${topic}:`, error);
    }

    // 2. Persist notification history for subscribed users
    const subscriptions = await this.topicSubscriptionsRepository.findByTopic(topic);
    const uniqueUserIds = [...new Set(subscriptions.map((s) => s.userId))];

    let persistedCount = 0;
    for (const userId of uniqueUserIds) {
      try {
        await this.notificationsRepository.create(userId, {
          title,
          body,
          type,
          data,
          imageUrl,
          read: false,
          sentAt: new Date().toISOString(),
          deliveryStatus: sentToFCM ? 'SENT' : 'FAILED',
          deliveryErrorCode: sentToFCM ? undefined : 'FCM_TOPIC_SEND_FAILED',
          deliveryErrorMessage: sentToFCM
            ? undefined
            : `Failed to send notification to topic ${topic}`,
        });
        persistedCount++;
      } catch (error) {
        this.logger.error(`Failed to persist topic notification for user ${userId}:`, error);
      }
    }

    return { sentToFCM, persistedCount };
  }
}
