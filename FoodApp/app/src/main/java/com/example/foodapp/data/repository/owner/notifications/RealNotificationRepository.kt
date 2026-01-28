package com.example.foodapp.data.repository.owner.notifications

import com.example.foodapp.data.model.owner.notification.*
import com.example.foodapp.data.remote.owner.NotificationApiService
import com.example.foodapp.data.remote.owner.response.RegisterDeviceTokenRequest
import com.example.foodapp.data.remote.owner.response.UpdateNotificationPreferencesRequest
import com.example.foodapp.data.repository.owner.base.OwnerNotificationRepository
import retrofit2.Response

/**
 * Real implementation of OwnerNotificationRepository using backend API
 */
class RealNotificationRepository(
    private val apiService: NotificationApiService
) : OwnerNotificationRepository {

    override suspend fun registerDeviceToken(request: RegisterDeviceTokenRequest): Result<DeviceToken> {
        return try {
            val response = apiService.registerDeviceToken(request)
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success && wrapper.data != null) {
                    Result.success(wrapper.data.toDeviceToken())
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to register device token"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unregisterDeviceToken(token: String): Result<Unit> {
        return try {
            val response = apiService.unregisterDeviceToken(token)
            if (response.isSuccessful || response.code() == 204) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNotifications(
        read: Boolean?,
        page: Int,
        limit: Int
    ): Result<PaginatedNotifications> {
        return try {
            val response = apiService.getNotifications(read, page, limit)
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success && wrapper.data != null) {
                    val data = wrapper.data
                    Result.success(
                        PaginatedNotifications(
                            items = data.items.map { it.toNotification() },
                            total = data.total,
                            page = data.page,
                            limit = data.limit,
                            unreadCount = data.unreadCount
                        )
                    )
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to load notifications"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Notification> {
        return try {
            val response = apiService.markAsRead(notificationId)
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success && wrapper.data != null) {
                    Result.success(wrapper.data.toNotification())
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to mark notification as read"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(): Result<Int> {
        return try {
            val response = apiService.markAllAsRead()
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success && wrapper.data != null) {
                    Result.success(wrapper.data.updated)
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to mark all as read"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPreferences(): Result<NotificationPreferences> {
        return try {
            val response = apiService.getPreferences()
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success && wrapper.data != null) {
                    Result.success(wrapper.data.toNotificationPreferences())
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to load preferences"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePreferences(request: UpdateNotificationPreferencesRequest): Result<NotificationPreferences> {
        return try {
            val response = apiService.updatePreferences(request)
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success && wrapper.data != null) {
                    Result.success(wrapper.data.toNotificationPreferences())
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to update preferences"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> getErrorMessage(response: Response<T>): String {
        return try {
            response.errorBody()?.string() ?: "Unknown error occurred"
        } catch (e: Exception) {
            "Error: ${response.code()}"
        }
    }
}
