package com.example.foodapp.pages.owner.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.owner.review.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReviewsViewModel : ViewModel() {

    private val repository = RepositoryProvider.getReviewRepository()

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    /**
     * Set shop ID and load reviews
     */
    fun setShopId(shopId: String) {
        _uiState.update { it.copy(shopId = shopId) }
        loadReviews()
    }

    /**
     * Load reviews from API
     */
    fun loadReviews(refresh: Boolean = false) {
        val shopId = _uiState.value.shopId ?: return
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = !refresh,
                    isRefreshing = refresh,
                    error = null
                ) 
            }

            val result = repository.getShopReviews(shopId)

            result.fold(
                onSuccess = { data ->
                    _uiState.update { 
                        it.copy(
                            reviews = data.reviews,
                            totalReviews = data.total,
                            avgRating = data.avgRating,
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
                            error = error.message ?: "Không thể tải đánh giá"
                        )
                    }
                }
            )
        }
    }

    /**
     * Refresh reviews (pull-to-refresh)
     */
    fun refresh() {
        loadReviews(refresh = true)
    }

    /**
     * Filter change handler
     */
    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    /**
     * Search query change handler
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Get filtered reviews (client-side filtering)
     */
    fun getFilteredReviews(): List<Review> {
        val state = _uiState.value
        var filtered = state.reviews

        // Apply filter by rating
        filtered = when (state.selectedFilter) {
            ReviewUiState.FILTER_5_STAR -> filtered.filter { it.rating == 5 }
            ReviewUiState.FILTER_4_STAR -> filtered.filter { it.rating == 4 }
            ReviewUiState.FILTER_3_STAR -> filtered.filter { it.rating == 3 }
            ReviewUiState.FILTER_LOW -> filtered.filter { it.rating <= 2 }
            ReviewUiState.FILTER_NO_REPLY -> filtered.filter { it.ownerReply == null }
            else -> filtered
        }

        // Apply search
        val query = state.searchQuery.trim()
        if (query.isNotBlank()) {
            filtered = filtered.filter { review ->
                review.customerName.contains(query, ignoreCase = true) ||
                review.comment?.contains(query, ignoreCase = true) == true
            }
        }

        return filtered.sortedByDescending { it.createdAt }
    }

    // ==================== Dialog Management ====================

    fun showReplyDialog(review: Review) {
        _uiState.update { 
            it.copy(
                showReplyDialog = true,
                selectedReview = review
            )
        }
    }

    fun dismissReplyDialog() {
        _uiState.update { 
            it.copy(
                showReplyDialog = false,
                selectedReview = null
            )
        }
    }

    // ==================== Reply Operation ====================

    /**
     * Reply to a review
     */
    fun replyToReview(reply: String) {
        val review = _uiState.value.selectedReview ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }

            val result = repository.replyToReview(review.id, reply)

            result.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false,
                            showReplyDialog = false,
                            selectedReview = null,
                            successMessage = "Phản hồi thành công"
                        )
                    }
                    loadReviews()
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false,
                            error = error.message ?: "Không thể gửi phản hồi"
                        )
                    }
                }
            )
        }
    }

    // ==================== Statistics ====================

    fun getRatingDistribution(): Map<Int, Int> {
        val reviews = _uiState.value.reviews
        return (1..5).associateWith { rating ->
            reviews.count { it.rating == rating }
        }
    }

    fun getUnrepliedCount(): Int = _uiState.value.reviews.count { it.ownerReply == null }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
