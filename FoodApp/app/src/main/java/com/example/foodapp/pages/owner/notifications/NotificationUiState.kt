package com.example.foodapp.pages.owner.notifications

import com.example.foodapp.data.model.owner.notification.*

/**
 * UI State for Owner Notifications Screen
 */
data class NotificationUiState(
    // Loading states
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isActionLoading: Boolean = false,
    
    // Data
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val totalCount: Int = 0,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = false,
    
    // Preferences
    val preferences: NotificationPreferences? = null,
    val showPreferencesDialog: Boolean = false,
    
    // Filters
    val selectedFilter: String = FILTER_ALL,
    val searchQuery: String = "",
    
    // Messages
    val error: String? = null,
    val successMessage: String? = null
) {
    companion object {
        const val FILTER_ALL = "all"
        const val FILTER_UNREAD = "unread"
        const val FILTER_ORDER = "order"
        const val FILTER_SYSTEM = "system"
        const val FILTER_PROMOTION = "promotion"
    }
}

/**
 * Notification filter enum for UI
 */
enum class NotificationFilter(val displayName: String, val apiValue: Boolean?) {
    ALL("Tất cả", null),
    UNREAD("Chưa đọc", false),
    READ("Đã đọc", true);
}
