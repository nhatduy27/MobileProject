package com.example.foodapp.pages.owner.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.owner.notification.*
import com.example.foodapp.data.remote.owner.response.UpdateNotificationPreferencesRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val repository = RepositoryProvider.getNotificationRepository()

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    /**
     * Load notifications from API
     */
    fun loadNotifications(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = !refresh,
                    isRefreshing = refresh,
                    error = null
                ) 
            }

            // Determine read filter based on selected filter
            val readFilter = when (_uiState.value.selectedFilter) {
                NotificationUiState.FILTER_UNREAD -> false
                else -> null // Get all for other filters (will filter client-side)
            }

            val result = repository.getNotifications(read = readFilter, page = 1, limit = 50)

            result.fold(
                onSuccess = { data ->
                    _uiState.update { 
                        it.copy(
                            notifications = data.items,
                            unreadCount = data.unreadCount,
                            totalCount = data.total,
                            currentPage = data.page,
                            hasMorePages = data.items.size < data.total,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error.message ?: "Không thể tải thông báo"
                        )
                    }
                }
            )
        }
    }

    /**
     * Refresh notifications (pull-to-refresh)
     */
    fun refresh() {
        loadNotifications(refresh = true)
    }

    /**
     * Filter change handler
     */
    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        // Client-side filtering, no need to reload
    }

    /**
     * Search query change handler
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Get filtered notifications (client-side filtering)
     */
    fun getFilteredNotifications(): List<Notification> {
        val state = _uiState.value
        var filtered = state.notifications

        // Apply filter by type
        filtered = when (state.selectedFilter) {
            NotificationUiState.FILTER_UNREAD -> filtered.filter { !it.read }
            NotificationUiState.FILTER_ORDER -> filtered.filter { 
                it.type.name.contains("ORDER", ignoreCase = true) || it.type == NotificationType.NEW_ORDER
            }
            NotificationUiState.FILTER_SYSTEM -> filtered.filter { 
                it.type == NotificationType.DAILY_SUMMARY || 
                it.type == NotificationType.SUBSCRIPTION_EXPIRING
            }
            NotificationUiState.FILTER_PROMOTION -> filtered.filter { 
                it.type == NotificationType.PROMOTION || 
                it.type == NotificationType.VOUCHER_AVAILABLE
            }
            else -> filtered
        }

        // Apply search
        val query = state.searchQuery.trim()
        if (query.isNotBlank()) {
            filtered = filtered.filter { notification ->
                notification.title.contains(query, ignoreCase = true) ||
                notification.body.contains(query, ignoreCase = true)
            }
        }

        return filtered.sortedByDescending { it.createdAt }
    }

    /**
     * Mark a single notification as read
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }

            val result = repository.markAsRead(notificationId)

            result.fold(
                onSuccess = { updatedNotification ->
                    // Update local state
                    _uiState.update { state ->
                        val updatedList = state.notifications.map { 
                            if (it.id == notificationId) updatedNotification else it 
                        }
                        state.copy(
                            notifications = updatedList,
                            unreadCount = updatedList.count { !it.read },
                            isActionLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false,
                            error = error.message ?: "Không thể đánh dấu đã đọc"
                        )
                    }
                }
            )
        }
    }

    /**
     * Mark all notifications as read
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }

            val result = repository.markAllAsRead()

            result.fold(
                onSuccess = { count ->
                    // Update local state - mark all as read
                    _uiState.update { state ->
                        val updatedList = state.notifications.map { it.copy(read = true) }
                        state.copy(
                            notifications = updatedList,
                            unreadCount = 0,
                            isActionLoading = false,
                            successMessage = "Đã đánh dấu $count thông báo là đã đọc"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false,
                            error = error.message ?: "Không thể đánh dấu tất cả đã đọc"
                        )
                    }
                }
            )
        }
    }

    // ==================== Preferences ====================

    /**
     * Load notification preferences
     */
    fun loadPreferences() {
        viewModelScope.launch {
            val result = repository.getPreferences()
            result.fold(
                onSuccess = { prefs ->
                    _uiState.update { it.copy(preferences = prefs) }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(error = error.message ?: "Không thể tải cài đặt thông báo")
                    }
                }
            )
        }
    }

    /**
     * Show preferences dialog
     */
    fun showPreferencesDialog() {
        loadPreferences()
        _uiState.update { it.copy(showPreferencesDialog = true) }
    }

    /**
     * Dismiss preferences dialog
     */
    fun dismissPreferencesDialog() {
        _uiState.update { it.copy(showPreferencesDialog = false) }
    }

    /**
     * Update notification preferences
     */
    fun updatePreferences(informational: Boolean?, marketing: Boolean?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }

            val request = UpdateNotificationPreferencesRequest(
                informational = informational,
                marketing = marketing
            )

            val result = repository.updatePreferences(request)

            result.fold(
                onSuccess = { prefs ->
                    _uiState.update { 
                        it.copy(
                            preferences = prefs,
                            isActionLoading = false,
                            showPreferencesDialog = false,
                            successMessage = "Đã cập nhật cài đặt thông báo"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false,
                            error = error.message ?: "Không thể cập nhật cài đặt"
                        )
                    }
                }
            )
        }
    }

    // ==================== Statistics ====================

    fun getUnreadCount(): Int = _uiState.value.unreadCount

    fun getTotalNotifications(): Int = _uiState.value.notifications.size

    fun getOrderNotificationsCount(): Int = _uiState.value.notifications.count { 
        it.type.name.contains("ORDER", ignoreCase = true) || it.type == NotificationType.NEW_ORDER
    }

    // ==================== Clear Messages ====================

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
