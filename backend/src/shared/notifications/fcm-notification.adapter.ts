import { Injectable, Logger } from '@nestjs/common';
import { NotificationPort } from './notification.port';

/**
 * Firebase Cloud Messaging Notification Adapter
 * 
 * Stub implementation for FCM notifications.
 * TODO: Integrate Firebase Admin SDK for actual push notifications
 */
@Injectable()
export class FcmNotificationAdapter extends NotificationPort {
  private readonly logger = new Logger(FcmNotificationAdapter.name);

  async sendToUser(
    userId: string,
    payload: { title: string; body: string },
  ): Promise<void> {
    // TODO: Replace with actual Firebase Admin SDK implementation
    // Example:
    // const message = {
    //   notification: {
    //     title: payload.title,
    //     body: payload.body,
    //   },
    //   token: await this.getUserFcmToken(userId),
    // };
    // await admin.messaging().send(message);

    this.logger.log(
      `[STUB] Sending notification to user ${userId}: ${payload.title} - ${payload.body}`,
    );
  }
}
