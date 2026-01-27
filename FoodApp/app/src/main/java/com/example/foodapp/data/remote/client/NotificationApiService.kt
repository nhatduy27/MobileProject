package com.example.foodapp.data.remote.client

import com.example.foodapp.data.remote.client.response.notification.*
import retrofit2.http.*

interface NotificationApiService {

    @POST("notifications/tokens")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceTokenRequest
    ): RegisterDeviceTokenResponse

    @DELETE("notifications/tokens/{token}")
    suspend fun unRegisterDeviceToken(
        @Header("Authorization") authHeader: String,
        @Path("token") token: String
    )

    // Notifications List
    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") authHeader: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("read") read: Boolean? = null
    ): NotificationListResponse

    @PUT("notifications/{id}/read")
    suspend fun markNotificationAsRead(
        @Header("Authorization") authHeader: String,
        @Path("id") notificationId: String
    ): MarkNotificationReadResponse

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsAsRead(
        @Header("Authorization") authHeader: String
    ): MarkAllNotificationsReadResponse

    @GET("notifications/preferences")
    suspend fun getNotificationPreferences(
        @Header("Authorization") authHeader: String
    ): BaseResponse<NotificationPreferencesData>  // Sửa ở đây

    @PUT("notifications/preferences")
    suspend fun updateNotificationPreferences(
        @Header("Authorization") authHeader: String,
        @Body request: UpdateNotificationPreferencesRequest
    ): BaseResponse<UpdateNotificationPreferencesData>

}