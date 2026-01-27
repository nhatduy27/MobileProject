// com.example.foodapp.pages.client.notifications.NotificationsViewModel.kt
package com.example.foodapp.pages.client.notifications

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.remote.client.response.notification.ApiResult
import com.example.foodapp.data.remote.client.response.notification.MarkAllNotificationsReadResponse
import com.example.foodapp.data.remote.client.response.notification.MarkNotificationReadResponse
import com.example.foodapp.data.remote.client.response.notification.NotificationResponse
import com.example.foodapp.data.repository.client.notification.NotificationRepository
import kotlinx.coroutines.launch

// ============== NOTIFICATION STATES ==============

sealed class NotificationsState {
    object Idle : NotificationsState()
    object Loading : NotificationsState()
    data class Success(val notifications: List<NotificationResponse>) : NotificationsState()
    data class Error(val message: String) : NotificationsState()
}

sealed class UnreadCountState {
    object Idle : UnreadCountState()
    object Loading : UnreadCountState()
    data class Success(val count: Int) : UnreadCountState()
    data class Error(val message: String) : UnreadCountState()
}

sealed class NotificationByIdState {
    object Idle : NotificationByIdState()
    object Loading : NotificationByIdState()
    data class Success(val notification: NotificationResponse?) : NotificationByIdState()
    data class Error(val message: String) : NotificationByIdState()
}

sealed class AllNotificationsState {
    object Idle : AllNotificationsState()
    object Loading : AllNotificationsState()
    data class Success(val notifications: List<NotificationResponse>) : AllNotificationsState()
    data class Error(val message: String) : AllNotificationsState()
}

sealed class OrderNotificationsState {
    object Idle : OrderNotificationsState()
    object Loading : OrderNotificationsState()
    data class Success(val notifications: List<NotificationResponse>) : OrderNotificationsState()
    data class Error(val message: String) : OrderNotificationsState()
}

// ============== MARK READ STATES ==============

sealed class MarkReadState {
    object Idle : MarkReadState()
    object Loading : MarkReadState()
    data class Success(val notification: MarkNotificationReadResponse) : MarkReadState()
    data class Error(val message: String) : MarkReadState()
}

sealed class MarkAllReadState {
    object Idle : MarkAllReadState()
    object Loading : MarkAllReadState()
    data class Success(val updatedCount: Int) : MarkAllReadState()
    data class Error(val message: String) : MarkAllReadState()
}

// ============== NOTIFICATIONS VIEW MODEL ==============

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val context: Context
) : ViewModel() {

    private val authManager = AuthManager(context)

    private val _notificationsState = MutableLiveData<NotificationsState>(NotificationsState.Idle)
    val notificationsState: LiveData<NotificationsState> = _notificationsState

    private val _currentNotifications = MutableLiveData<List<NotificationResponse>>(emptyList())
    val currentNotifications: LiveData<List<NotificationResponse>> = _currentNotifications

    private val _unreadCount = MutableLiveData<Int>(0)
    val unreadCount: LiveData<Int> = _unreadCount

    private val _unreadCountState = MutableLiveData<UnreadCountState>(UnreadCountState.Idle)
    val unreadCountState: LiveData<UnreadCountState> = _unreadCountState

    private val _notificationByIdState = MutableLiveData<NotificationByIdState>(NotificationByIdState.Idle)
    val notificationByIdState: LiveData<NotificationByIdState> = _notificationByIdState

    private val _allNotificationsState = MutableLiveData<AllNotificationsState>(AllNotificationsState.Idle)
    val allNotificationsState: LiveData<AllNotificationsState> = _allNotificationsState

    private val _orderNotificationsState = MutableLiveData<OrderNotificationsState>(OrderNotificationsState.Idle)
    val orderNotificationsState: LiveData<OrderNotificationsState> = _orderNotificationsState

    private val _markReadState = MutableLiveData<MarkReadState>(MarkReadState.Idle)
    val markReadState: LiveData<MarkReadState> = _markReadState

    private val _markAllReadState = MutableLiveData<MarkAllReadState>(MarkAllReadState.Idle)
    val markAllReadState: LiveData<MarkAllReadState> = _markAllReadState

    // Load danh sách thông báo ngay khi vào
    init {
        fetchNotifications()
    }

    // User refresh thủ công
    fun refreshNotifications() {
        fetchNotifications(refresh = true)
    }

    // Lấy danh sách thông báo
    fun fetchNotifications(refresh: Boolean = false) {
        println("DEBUG: [NotificationsViewModel] Fetching notifications, refresh: $refresh")

        _notificationsState.value = NotificationsState.Loading

        viewModelScope.launch {
            try {
                // Lấy access token từ AuthManager
                val accessToken = authManager.getCurrentToken()

                if (accessToken.isNullOrEmpty()) {
                    _notificationsState.value = NotificationsState.Error("Vui lòng đăng nhập để xem thông báo")
                    return@launch
                }

                println("DEBUG: [NotificationsViewModel] Access token: ${accessToken.take(20)}...")

                val result = notificationRepository.getNotifications(
                    accessToken = "$accessToken",
                    page = 1,
                    limit = 20,
                    read = null
                )
                println("DEBUG: [NotificationsViewModel] Result type: ${result::class.simpleName}")

                when (result) {
                    is ApiResult.Success -> {
                        println("DEBUG: [NotificationsViewModel] Fetch successful")
                        val response = result.data

                        // Xử lý dữ liệu
                        val notifications = response.items ?: emptyList()

                        _currentNotifications.value = notifications
                        _unreadCount.value = response.unreadCount

                        _notificationsState.value = NotificationsState.Success(notifications)
                    }
                    is ApiResult.Failure -> {
                        println("DEBUG: [NotificationsViewModel] Fetch failed: ${result.exception.message}")
                        _notificationsState.value = NotificationsState.Error(
                            result.exception.message ?: "Không thể tải danh sách thông báo"
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                println("DEBUG: [NotificationsViewModel] Exception in fetchNotifications: ${e.message}")
                e.printStackTrace()
                _notificationsState.value = NotificationsState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    // Lấy tất cả thông báo (lặp qua các trang)
    fun fetchAllNotifications() {
        println("DEBUG: [NotificationsViewModel] Fetching all notifications")

        _allNotificationsState.value = AllNotificationsState.Loading

        viewModelScope.launch {
            try {
                // Lấy access token từ AuthManager
                val accessToken = authManager.getCurrentToken()

                if (accessToken.isNullOrEmpty()) {
                    _allNotificationsState.value = AllNotificationsState.Error("Vui lòng đăng nhập")
                    return@launch
                }

                val result = notificationRepository.getAllNotifications("Bearer $accessToken")
                println("DEBUG: [NotificationsViewModel] All notifications result: ${result::class.simpleName}")

                when (result) {
                    is ApiResult.Success -> {
                        println("DEBUG: [NotificationsViewModel] Fetch all notifications successful")
                        _allNotificationsState.value = AllNotificationsState.Success(result.data)
                    }
                    is ApiResult.Failure -> {
                        println("DEBUG: [NotificationsViewModel] Fetch all notifications failed: ${result.exception.message}")
                        _allNotificationsState.value = AllNotificationsState.Error(
                            result.exception.message ?: "Không thể tải tất cả thông báo"
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                println("DEBUG: [NotificationsViewModel] Exception in fetchAllNotifications: ${e.message}")
                e.printStackTrace()
                _allNotificationsState.value = AllNotificationsState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    // ============== MARK AS READ FUNCTIONS ==============

    /**
     * Đánh dấu một thông báo đã đọc
     * @param notificationId ID của thông báo cần đánh dấu
     */
    fun markNotificationAsRead(notificationId: String) {
        println("DEBUG: [NotificationsViewModel] Marking notification as read: $notificationId")

        _markReadState.value = MarkReadState.Loading

        viewModelScope.launch {
            try {
                // Lấy access token từ AuthManager
                val accessToken = authManager.getCurrentToken()

                if (accessToken.isNullOrEmpty()) {
                    _markReadState.value = MarkReadState.Error("Vui lòng đăng nhập")
                    return@launch
                }

                // Gọi repository để đánh dấu đã đọc
                val result = notificationRepository.markNotificationAsRead(
                    accessToken = accessToken,
                    notificationId = notificationId
                )

                when (result) {
                    is ApiResult.Success -> {
                        println("DEBUG: [NotificationsViewModel] Mark as read successful: ${result.data.id}")

                        // Cập nhật trạng thái
                        _markReadState.value = MarkReadState.Success(result.data)

                        // Cập nhật danh sách thông báo hiện tại
                        updateNotificationInList(result.data)

                        // Cập nhật số lượng chưa đọc
                        updateUnreadCountAfterMarkRead()
                    }
                    is ApiResult.Failure -> {
                        println("DEBUG: [NotificationsViewModel] Mark as read failed: ${result.exception.message}")
                        _markReadState.value = MarkReadState.Error(
                            result.exception.message ?: "Không thể đánh dấu đã đọc"
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                println("DEBUG: [NotificationsViewModel] Exception in markNotificationAsRead: ${e.message}")
                e.printStackTrace()
                _markReadState.value = MarkReadState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    fun markAllNotificationsAsRead() {
        println("DEBUG: [NotificationsViewModel] Marking all notifications as read")

        _markAllReadState.value = MarkAllReadState.Loading

        viewModelScope.launch {
            try {
                // Lấy access token từ AuthManager
                val accessToken = authManager.getCurrentToken()

                if (accessToken.isNullOrEmpty()) {
                    _markAllReadState.value = MarkAllReadState.Error("Vui lòng đăng nhập")
                    return@launch
                }

                // Gọi repository để đánh dấu tất cả đã đọc
                val result = notificationRepository.markAllNotificationsAsRead(
                    accessToken = accessToken
                )

                when (result) {
                    is ApiResult.Success -> {
                        println("DEBUG: [NotificationsViewModel] Mark all as read successful, updated: ${result.data.updated}")

                        // Cập nhật trạng thái
                        _markAllReadState.value = MarkAllReadState.Success(result.data.updated)

                        // Cập nhật danh sách thông báo
                        markAllInCurrentList()

                        // Reset số lượng chưa đọc về 0
                        _unreadCount.value = 0
                    }
                    is ApiResult.Failure -> {
                        println("DEBUG: [NotificationsViewModel] Mark all as read failed: ${result.exception.message}")
                        _markAllReadState.value = MarkAllReadState.Error(
                            result.exception.message ?: "Không thể đánh dấu tất cả đã đọc"
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                println("DEBUG: [NotificationsViewModel] Exception in markAllNotificationsAsRead: ${e.message}")
                e.printStackTrace()
                _markAllReadState.value = MarkAllReadState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    // ============== HELPER FUNCTIONS ==============

    /**
     * Cập nhật thông báo trong danh sách hiện tại sau khi đánh dấu đọc
     */
    private fun updateNotificationInList(updatedNotification: MarkNotificationReadResponse) {
        val currentList = _currentNotifications.value ?: emptyList()
        val updatedList = currentList.map { notification ->
            if (notification.id == updatedNotification.id) {
                // Chuyển đổi từ MarkNotificationReadResponse sang NotificationResponse
                NotificationResponse(
                    id = updatedNotification.id,
                    userId = updatedNotification.userId,
                    title = updatedNotification.title,
                    body = updatedNotification.body,
                    imageUrl = null,
                    type = updatedNotification.type,
                    data = null,
                    read = updatedNotification.read,
                    readAt = updatedNotification.readAt,
                    orderId = null,
                    shopId = null,
                    createdAt = updatedNotification.createdAt,
                    sentAt = null,
                    deliveryStatus = null,
                    deliveryErrorCode = null,
                    deliveryErrorMessage = null
                )
            } else {
                notification
            }
        }

        _currentNotifications.value = updatedList
    }

    /**
     * Đánh dấu tất cả thông báo trong danh sách hiện tại là đã đọc
     */
    private fun markAllInCurrentList() {
        val currentList = _currentNotifications.value ?: emptyList()
        val updatedList = currentList.map { notification ->
            notification.copy(
                read = true,
                readAt = java.time.Instant.now().toString()
            )
        }

        _currentNotifications.value = updatedList
    }

    /**
     * Cập nhật số lượng chưa đọc sau khi đánh dấu một thông báo đã đọc
     */
    private fun updateUnreadCountAfterMarkRead() {
        val currentCount = _unreadCount.value ?: 0
        if (currentCount > 0) {
            _unreadCount.value = currentCount - 1
        }
    }

    /**
     * Reset trạng thái mark read
     */
    fun resetMarkReadState() {
        _markReadState.value = MarkReadState.Idle
    }

    /**
     * Reset trạng thái mark all read
     */
    fun resetMarkAllReadState() {
        _markAllReadState.value = MarkAllReadState.Idle
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val notificationRepository = NotificationRepository()
                NotificationsViewModel(
                    notificationRepository = notificationRepository,
                    context = context
                )
            }
        }
    }
}