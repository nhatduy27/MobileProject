package com.example.foodapp.pages.owner.reviews

import com.example.foodapp.data.model.owner.review.Review

/**
 * UI State for Reviews Screen
 */
data class ReviewUiState(
    // Data
    val reviews: List<Review> = emptyList(),
    val totalReviews: Int = 0,
    val avgRating: Double = 0.0,
    val shopId: String? = null,
    
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isActionLoading: Boolean = false,
    
    // Filter
    val selectedFilter: String = FILTER_ALL,
    val searchQuery: String = "",
    
    // Dialog states
    val showReplyDialog: Boolean = false,
    val selectedReview: Review? = null,
    
    // Messages
    val error: String? = null,
    val successMessage: String? = null
) {
    companion object {
        const val FILTER_ALL = "Tất cả"
        const val FILTER_5_STAR = "5 ⭐"
        const val FILTER_4_STAR = "4 ⭐"
        const val FILTER_3_STAR = "3 ⭐"
        const val FILTER_LOW = "1-2 ⭐"
        const val FILTER_NO_REPLY = "Chưa phản hồi"
        
        val filters = listOf(FILTER_ALL, FILTER_5_STAR, FILTER_4_STAR, FILTER_3_STAR, FILTER_LOW, FILTER_NO_REPLY)
    }
}
