package com.example.foodapp.data.remote.owner

import com.example.foodapp.data.remote.owner.response.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service for Owner Notification Management
 * 
 * Endpoints:
 * - POST /notifications/tokens - Register device token
 * - DELETE /notifications/tokens/{token} - Unregister device token
 * - GET /notifications - Get my notifications
 * - PUT /notifications/{id}/read - Mark notification as read
 * - PUT /notifications/read-all - Mark all notifications as read
 * - GET /notifications/preferences - Get notification preferences
 * - PUT /notifications/preferences - Update notification preferences
 */
interface NotificationApiService {

    /**
     * POST /notifications/tokens
     * Register a device token for push notifications
     */
    @POST("notifications/tokens")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceTokenRequest
    ): Response<WrappedDeviceTokenResponse>

    /**
     * DELETE /notifications/tokens/{token}
     * Unregister a device token
     */
    @DELETE("notifications/tokens/{token}")
    suspend fun unregisterDeviceToken(
        @Path("token") token: String
    ): Response<Unit>

    /**
     * GET /notifications
     * Get user's notifications with pagination
     */
    @GET("notifications")
    suspend fun getNotifications(
        @Query("read") read: Boolean? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 20
    ): Response<WrappedNotificationsResponse>

    /**
     * PUT /notifications/{id}/read
     * Mark a single notification as read
     */
    @PUT("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: String
    ): Response<WrappedNotificationResponse>

    /**
     * PUT /notifications/read-all
     * Mark all unread notifications as read
     */
    @PUT("notifications/read-all")
    suspend fun markAllAsRead(): Response<WrappedMarkAllReadResponse>

    /**
     * GET /notifications/preferences
     * Get current user's notification preferences
     */
    @GET("notifications/preferences")
    suspend fun getPreferences(): Response<WrappedNotificationPreferencesResponse>

    /**
     * PUT /notifications/preferences
     * Update notification preferences
     */
    @PUT("notifications/preferences")
    suspend fun updatePreferences(
        @Body request: UpdateNotificationPreferencesRequest
    ): Response<WrappedNotificationPreferencesResponse>
}
