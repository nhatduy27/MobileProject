package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.notification.*
import com.example.foodapp.data.remote.owner.response.RegisterDeviceTokenRequest
import com.example.foodapp.data.remote.owner.response.UpdateNotificationPreferencesRequest

/**
 * Repository interface for Owner Notification operations
 */
interface OwnerNotificationRepository {

    /**
     * Register device token for push notifications
     */
    suspend fun registerDeviceToken(request: RegisterDeviceTokenRequest): Result<DeviceToken>

    /**
     * Unregister device token
     */
    suspend fun unregisterDeviceToken(token: String): Result<Unit>

    /**
     * Get notifications with pagination and optional read status filter
     * @param read null = all, true = only read, false = only unread
     * @param page page number (1-based)
     * @param limit items per page
     */
    suspend fun getNotifications(
        read: Boolean? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<PaginatedNotifications>

    /**
     * Mark a single notification as read
     */
    suspend fun markAsRead(notificationId: String): Result<Notification>

    /**
     * Mark all unread notifications as read
     * @return number of notifications updated
     */
    suspend fun markAllAsRead(): Result<Int>

    /**
     * Get notification preferences
     */
    suspend fun getPreferences(): Result<NotificationPreferences>

    /**
     * Update notification preferences
     */
    suspend fun updatePreferences(request: UpdateNotificationPreferencesRequest): Result<NotificationPreferences>
}
