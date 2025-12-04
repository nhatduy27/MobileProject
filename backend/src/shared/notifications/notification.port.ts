/**
 * Notification Port (Abstraction)
 * 
 * This abstract class defines the contract for sending notifications.
 * Implementations can use Firebase Cloud Messaging, AWS SNS, etc.
 */
export abstract class NotificationPort {
  /**
   * Send a notification to a specific user
   * @param userId - Target user ID
   * @param payload - Notification content
   */
  abstract sendToUser(
    userId: string,
    payload: { title: string; body: string },
  ): Promise<void>;
}
