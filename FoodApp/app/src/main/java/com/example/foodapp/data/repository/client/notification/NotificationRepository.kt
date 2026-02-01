package com.example.foodapp.data.repository.client.notification

import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.NotificationApiService
import com.example.foodapp.data.remote.client.response.notification.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class NotificationRepository() {

    private val notificationApiService: NotificationApiService = ApiClient.notificationApiService

    suspend fun registerDeviceToken(
        token: String,
        platform: String,
        model: String? = null,
        osVersion: String? = null
    ): ApiResult<RegisterDeviceTokenResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val request = RegisterDeviceTokenRequest(
                    token = token,
                    platform = platform,
                    deviceInfo = DeviceInfo(
                        model = model,
                        osVersion = osVersion
                    )
                )

                val baseResponse = notificationApiService.registerDeviceToken(request)
                // Unwrap BaseResponse để lấy data thực sự
                if (baseResponse.success) {
                    ApiResult.Success(baseResponse.data)
                } else {
                    ApiResult.Failure(Exception("API returned success=false"))
                }
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

    suspend fun unRegisterDeviceToken(
        accessToken: String,
        fcmToken: String
    ) {
        try {
            val authHeader = "Bearer $accessToken"
            notificationApiService.unRegisterDeviceToken(
                authHeader = authHeader,
                token = fcmToken
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getNotifications(
        accessToken: String,
        page: Int? = null,
        limit: Int? = null,
        read: Boolean? = null
    ): ApiResult<NotificationListResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val authHeader = "Bearer $accessToken"
                val baseResponse = notificationApiService.getNotifications(
                    authHeader = authHeader,
                    page = page,
                    limit = limit,
                    read = read
                )
                // Unwrap BaseResponse to get the actual data
                if (baseResponse.success) {
                    ApiResult.Success(baseResponse.data)
                } else {
                    ApiResult.Failure(Exception("API returned success=false"))
                }
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

    suspend fun getAllNotifications(
        accessToken: String
    ): ApiResult<List<NotificationResponse>> {
        return try {
            withContext(Dispatchers.IO) {
                val allNotifications = mutableListOf<NotificationResponse>()
                var currentPage = 1
                val pageSize = 50
                var hasMore = true

                while (hasMore) {
                    val result = getNotifications(
                        accessToken = accessToken,
                        page = currentPage,
                        limit = pageSize
                    )

                    when (result) {
                        is ApiResult.Success -> {
                            val response = result.data
                            allNotifications.addAll(response.items)

                            val loadedItems = currentPage * pageSize
                            hasMore = loadedItems < response.total && currentPage < 10
                            currentPage++
                        }
                        is ApiResult.Failure -> {
                            return@withContext ApiResult.Failure(result.exception)
                        }
                        else -> {
                            hasMore = false
                        }
                    }
                }

                ApiResult.Success(allNotifications)
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

    suspend fun markNotificationAsRead(
        accessToken: String,
        notificationId: String
    ): ApiResult<MarkNotificationReadResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val authHeader = "Bearer $accessToken"
                val baseResponse = notificationApiService.markNotificationAsRead(
                    authHeader = authHeader,
                    notificationId = notificationId
                )
                // Unwrap BaseResponse để lấy data thực sự
                if (baseResponse.success) {
                    ApiResult.Success(baseResponse.data)
                } else {
                    ApiResult.Failure(Exception("API returned success=false"))
                }
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

    suspend fun markAllNotificationsAsRead(
        accessToken: String
    ): ApiResult<MarkAllNotificationsReadResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val authHeader = "Bearer $accessToken"
                val baseResponse = notificationApiService.markAllNotificationsAsRead(
                    authHeader = authHeader
                )
                // Unwrap BaseResponse để lấy data thực sự
                if (baseResponse.success) {
                    ApiResult.Success(baseResponse.data)
                } else {
                    ApiResult.Failure(Exception("API returned success=false"))
                }
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

    /**
     * Lấy notification preferences của user hiện tại
     * GET /notifications/preferences
     */
    suspend fun getNotificationPreferences(
        accessToken: String
    ): ApiResult<NotificationPreferencesResponse> {  // Vẫn trả về NotificationPreferencesResponse
        return try {
            withContext(Dispatchers.IO) {
                val authHeader = "Bearer $accessToken"
                val baseResponse = notificationApiService.getNotificationPreferences(
                    authHeader = authHeader
                )

                // Tạo NotificationPreferencesResponse từ NotificationPreferencesData
                val preferencesData = baseResponse.data
                val preferencesResponse = NotificationPreferencesResponse(
                    userId = preferencesData.userId,
                    transactional = preferencesData.transactional,
                    informational = preferencesData.informational,
                    marketing = preferencesData.marketing,
                    updatedAt = preferencesData.updatedAt
                )

                ApiResult.Success(preferencesResponse)
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

    suspend fun updateNotificationPreferences(
        accessToken: String,
        informational: Boolean? = null,
        marketing: Boolean? = null
    ): ApiResult<UpdateNotificationPreferencesData> {
        return try {
            withContext(Dispatchers.IO) {
                val authHeader = "Bearer $accessToken"

                // Tạo request (transactional luôn true)
                val request = UpdateNotificationPreferencesRequest(
                    informational = informational,
                    marketing = marketing
                )

                // Gọi API - nhận BaseResponse
                val baseResponse = notificationApiService.updateNotificationPreferences(
                    authHeader = authHeader,
                    request = request
                )

                // Kiểm tra success và trả về data
                if (baseResponse.success) {
                    ApiResult.Success(baseResponse.data)
                } else {
                    ApiResult.Failure(Exception("Cập nhật thất bại"))
                }
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }
}