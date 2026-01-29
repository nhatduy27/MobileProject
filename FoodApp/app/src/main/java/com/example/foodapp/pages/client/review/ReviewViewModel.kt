package com.example.foodapp.pages.client.review

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.remote.client.response.review.*
import com.example.foodapp.data.repository.client.review.ReviewRepository
import com.example.foodapp.data.repository.firebase.AuthManager
import kotlinx.coroutines.launch

// Sealed class cho các trạng thái
sealed class ReviewsState {
    object Idle : ReviewsState()
    object Loading : ReviewsState()
    data class Success(val reviews: List<MyOrderReviewApiModel>) : ReviewsState()
    data class Error(val message: String) : ReviewsState()
}

// Sealed class cho trạng thái xóa review
sealed class DeleteReviewState {
    object Idle : DeleteReviewState()
    object Loading : DeleteReviewState()
    data class Success(val message: String, val reviewId: String) : DeleteReviewState()
    data class Error(val message: String) : DeleteReviewState()
}

class ReviewsViewModel(
    private val reviewRepository: ReviewRepository,
    private val context: Context
) : ViewModel() {

    private val authManager = AuthManager(context)
    // State cho việc lấy danh sách reviews
    private val _reviewsState = MutableLiveData<ReviewsState>(ReviewsState.Idle)
    val reviewsState: LiveData<ReviewsState> = _reviewsState

    // State cho việc xóa review
    private val _deleteReviewState = MutableLiveData<DeleteReviewState>(DeleteReviewState.Idle)
    val deleteReviewState: LiveData<DeleteReviewState> = _deleteReviewState

    // Current reviews data
    private val _reviews = MutableLiveData<List<MyOrderReviewApiModel>>(emptyList())
    val reviews: LiveData<List<MyOrderReviewApiModel>> = _reviews

    // Current page
    private var currentPage = 1
    private val pageSize = 20
    private var hasMore = true
    private var isLoading = false

    // Load reviews khi khởi tạo
    init {
        loadReviews()
    }

    // Lấy danh sách reviews từ API
    fun loadReviews(refresh: Boolean = false) {
        if (isLoading) return

        if (refresh) {
            currentPage = 1
            hasMore = true
            _reviews.value = emptyList()
        }

        if (!hasMore && !refresh) return

        viewModelScope.launch {
            isLoading = true

            if (currentPage == 1 && !refresh) {
                _reviewsState.value = ReviewsState.Loading
            }

            try {
                val customertoken = authManager.getCurrentToken()
                if (customertoken == null) {
                    _reviewsState.value = ReviewsState.Error("Vui lòng đăng nhập để xem đánh giá")
                    return@launch
                }

                // Sử dụng API mới
                val result = reviewRepository.getMyOrderReviews(
                    token = customertoken,
                    page = currentPage,
                    limit = pageSize
                )

                when (result) {
                    is ApiResult.Success -> {
                        val newReviews = result.data

                        if (refresh || currentPage == 1) {
                            _reviews.value = newReviews
                        } else {
                            val current = _reviews.value ?: emptyList()
                            _reviews.value = current + newReviews
                        }

                        // Kiểm tra còn dữ liệu không
                        hasMore = newReviews.size == pageSize
                        if (hasMore) {
                            currentPage++
                        }

                        _reviewsState.value = ReviewsState.Success(_reviews.value ?: emptyList())
                    }
                    is ApiResult.Failure -> {
                        if (currentPage == 1) {
                            _reviewsState.value = ReviewsState.Error(
                                result.exception.message ?: "Lỗi không xác định"
                            )
                        }
                        // Không thay đổi state nếu đang load more
                    }
                }
            } catch (e: Exception) {
                if (currentPage == 1) {
                    _reviewsState.value = ReviewsState.Error(
                        e.message ?: "Lỗi không xác định"
                    )
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Refresh reviews
    fun refreshReviews() {
        loadReviews(refresh = true)
    }

    // Xóa review
    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            _deleteReviewState.value = DeleteReviewState.Loading

            try {
                // TODO: Implement API delete review
                // val result = reviewRepository.deleteReview(getTokenFromPrefs(), reviewId)

                // Tạm thời xóa local
                val currentReviews = _reviews.value ?: emptyList()
                _reviews.value = currentReviews.filter { it.id != reviewId }
                _deleteReviewState.value = DeleteReviewState.Success(
                    "Đã xóa đánh giá thành công",
                    reviewId
                )

            } catch (e: Exception) {
                _deleteReviewState.value = DeleteReviewState.Error(
                    e.message ?: "Xóa đánh giá thất bại"
                )
            }
        }
    }

    // Reset delete state
    fun resetDeleteState() {
        _deleteReviewState.value = DeleteReviewState.Idle
    }

    // Load more reviews
    fun loadMoreReviews() {
        if (!isLoading && hasMore) {
            loadReviews()
        }
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val reviewRepository = ReviewRepository()
                ReviewsViewModel(reviewRepository, context)
            }
        }
    }
}