package com.example.foodapp.pages.client.shopdetail

import android.content.Context
import androidx.lifecycle.*
import com.example.foodapp.data.remote.client.response.review.*
import com.example.foodapp.data.remote.client.response.shop.ShopDetailApiModel
import com.example.foodapp.data.repository.client.chat.ChatRepository
import com.example.foodapp.data.repository.client.review.ReviewRepository
import com.example.foodapp.data.repository.client.shop.ShopRepository
import com.example.foodapp.data.repository.firebase.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ShopDetailState {
    object Idle : ShopDetailState()
    object Loading : ShopDetailState()
    data class Success(val shop: ShopDetailApiModel) : ShopDetailState()
    data class Error(val message: String) : ShopDetailState()
}

sealed class ReviewsState {
    object Idle : ReviewsState()
    object Loading : ReviewsState()
    data class Success(val reviews: List<ShopOrderReviewApiModel>) : ReviewsState()
    data class Error(val message: String) : ReviewsState()
    object Empty : ReviewsState()
}

// Data class để tính toán và lưu trữ metadata từ reviews
data class ReviewsMetadata(
    val reviews: List<ShopOrderReviewApiModel>,
    val totalReviews: Int,
    val averageRating: Float,
    val averageProductRating: Float? = null
)

class ShopDetailViewModel(
    private val shopRepository: ShopRepository,
    private val reviewRepository: ReviewRepository,
    private val chatRepository: ChatRepository,
    private val context: Context
) : ViewModel() {

    private val authManager = AuthManager(context)

    // Store ownerID if needed for other purposes
    private var _ownerId: String? = null
    val ownerId: String? get() = _ownerId

    private val _shopDetailState = MutableLiveData<ShopDetailState>(ShopDetailState.Idle)
    val shopDetailState: LiveData<ShopDetailState> = _shopDetailState

    private val _reviewsState = MutableLiveData<ReviewsState>(ReviewsState.Idle)
    val reviewsState: LiveData<ReviewsState> = _reviewsState

    private val _shop = MutableLiveData<ShopDetailApiModel?>()
    val shop: LiveData<ShopDetailApiModel?> = _shop

    // Lưu cả reviews gốc và metadata
    private val _reviews = MutableLiveData<List<ShopOrderReviewApiModel>?>()
    val reviews: LiveData<List<ShopOrderReviewApiModel>?> = _reviews

    private val _reviewsMetadata = MutableLiveData<ReviewsMetadata?>()
    val reviewsMetadata: LiveData<ReviewsMetadata?> = _reviewsMetadata

    private val _chatLoading = MutableLiveData<Boolean>(false)
    val chatLoading: LiveData<Boolean> = _chatLoading

    private val _chatError = MutableLiveData<String?>()
    val chatError: LiveData<String?> = _chatError

    fun getShopDetail(id: String) {
        if (_shopDetailState.value == ShopDetailState.Loading) return

        _shopDetailState.value = ShopDetailState.Loading

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    shopRepository.getShopDetail(id)
                }

                when (result) {
                    is com.example.foodapp.data.remote.client.response.shop.ApiResult.Success -> {
                        val shopData = result.data
                        _shop.value = shopData
                        _ownerId = shopData.ownerId   // Store ownerID

                        _shopDetailState.value = ShopDetailState.Success(shopData)
                        // Sau khi lấy được thông tin shop, lấy reviews
                        getShopReviews(id)
                    }
                    is com.example.foodapp.data.remote.client.response.shop.ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Không thể tải thông tin cửa hàng"
                        _shopDetailState.value = ShopDetailState.Error(errorMessage)
                    }
                }
            } catch (e: Exception) {
                _shopDetailState.value = ShopDetailState.Error(
                    e.message ?: "Đã xảy ra lỗi khi tải thông tin cửa hàng"
                )
            }
        }
    }

    /**
     * Lấy danh sách đánh giá của shop
     */
    fun getShopReviews(shopId: String) {
        if (_reviewsState.value == ReviewsState.Loading) return

        _reviewsState.value = ReviewsState.Loading

        viewModelScope.launch {
            try {
                val token = authManager.getCurrentToken()
                if (token == null) {
                    _reviewsState.value = ReviewsState.Error("Vui lòng đăng nhập để xem đánh giá")
                    return@launch
                }

                val result = withContext(Dispatchers.IO) {
                    reviewRepository.getShopOrderReviews(token, shopId)
                }

                when (result) {
                    is ApiResult.Success -> {
                        // result.data BÂY GIỜ LÀ ShopOrderReviewsData (không phải GetShopOrderReviewsResponse)
                        val reviewsData = result.data

                        // Lấy reviews list từ reviewsData
                        val reviewsList = reviewsData.reviews
                        _reviews.value = reviewsList

                        // Tính toán metadata - SỬA LẠI VÌ FIELD NAMES ĐÃ THAY ĐỔI
                        val metadata = calculateReviewsMetadata(reviewsList)
                        _reviewsMetadata.value = metadata

                        // Lưu tổng số reviews và average rating
                        _reviewsMetadata.value = ReviewsMetadata(
                            reviews = reviewsList,
                            totalReviews = reviewsData.total,
                            averageRating = reviewsData.avgRating.toFloat()
                        )

                        // Xác định state
                        if (reviewsList.isEmpty()) {
                            _reviewsState.value = ReviewsState.Empty
                        } else {
                            _reviewsState.value = ReviewsState.Success(reviewsList)
                        }
                    }
                    is ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Không thể tải đánh giá"

                        // Xử lý trường hợp không có review (404)
                        if (errorMessage.contains("404") ||
                            errorMessage.contains("Không có review") ||
                            errorMessage.contains("Shop không có review")) {

                            _reviews.value = emptyList()
                            _reviewsMetadata.value = ReviewsMetadata(emptyList(), 0, 0f)
                            _reviewsState.value = ReviewsState.Empty
                        } else {
                            _reviewsState.value = ReviewsState.Error(errorMessage)
                        }
                    }
                }
            } catch (e: Exception) {
                _reviewsState.value = ReviewsState.Error(
                    e.message ?: "Đã xảy ra lỗi khi tải đánh giá"
                )
            }
        }
    }

    /**
     * Tính toán metadata từ reviews - CẬP NHẬT FIELD NAMES
     */
    private fun calculateReviewsMetadata(reviews: List<ShopOrderReviewApiModel>): ReviewsMetadata {
        if (reviews.isEmpty()) {
            return ReviewsMetadata(emptyList(), 0, 0f, 0f)
        }

        // Tính tổng số reviews
        val totalReviews = reviews.size

        // Tính average shop rating - SỬA: shopRating thành rating
        val shopRatings = reviews.map { it.rating.toFloat() } // CHỈNH SỬA TỪ shopRating -> rating
        val averageShopRating = shopRatings.average().toFloat()

        // CHÚ Ý: Response mới KHÔNG CÓ productReviews nên bỏ phần này
        // Hoặc nếu API trả về product reviews trong response mới, cần điều chỉnh
        val averageProductRating: Float? = null // Không có product reviews trong response mới

        return ReviewsMetadata(
            reviews = reviews,
            totalReviews = totalReviews,
            averageRating = averageShopRating,
            averageProductRating = averageProductRating
        )
    }

    fun refreshReviews(shopId: String) {
        getShopReviews(shopId)
    }

    /**
     * Tạo conversation với shop và gọi callback khi thành công
     * Callback trả về: shopId, conversationId, shopName
     */
    fun startChatWithShop(
        shopId: String,
        shopName: String,
        onChatCreated: (shopId: String, conversationId: String, shopName: String) -> Unit
    ) {
        if (_chatLoading.value == true) return

        _chatLoading.value = true
        _chatError.value = null

        viewModelScope.launch {
            try {
                // Lấy token từ auth manager
                val token = authManager.getCurrentToken()
                if (token == null) {
                    _chatLoading.value = false
                    _chatError.value = "Vui lòng đăng nhập để nhắn tin"
                    return@launch
                }

                // Kiểm tra ownerId có tồn tại không
                val currentOwnerId = _ownerId
                if (currentOwnerId == null) {
                    _chatLoading.value = false
                    _chatError.value = "Không tìm thấy thông tin chủ cửa hàng"
                    return@launch
                }

                // Gọi API tạo/lấy conversation
                val response = withContext(Dispatchers.IO) {
                    chatRepository.createOrGetConversation(token, currentOwnerId)
                }

                _chatLoading.value = false

                if (response.isSuccessful) {
                    val conversationResponse = response.body()
                    if (conversationResponse?.success == true) {
                        val conversation = conversationResponse.data
                        if (conversation != null) {
                            // Gọi callback với shopId, conversationId và shopName
                            onChatCreated(shopId, conversation.id, shopName)
                        } else {
                            _chatError.value = "Không thể tạo cuộc trò chuyện"
                        }
                    } else {
                        _chatError.value = "Lỗi không xác định"
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Phiên đăng nhập hết hạn"
                        403 -> "Không có quyền truy cập"
                        else -> "Lỗi kết nối: ${response.code()}"
                    }
                    _chatError.value = errorMessage
                }
            } catch (e: Exception) {
                _chatLoading.value = false
                _chatError.value = "Đã xảy ra lỗi: ${e.message}"
            }
        }
    }

    fun clearChatError() {
        _chatError.value = null
    }

    fun clear() {
        _shop.value = null
        _reviews.value = null
        _reviewsMetadata.value = null
        _ownerId = null
        _shopDetailState.value = ShopDetailState.Idle
        _reviewsState.value = ReviewsState.Idle
        _chatLoading.value = false
        _chatError.value = null
    }

    companion object {
        fun factory(
            shopRepository: ShopRepository,
            reviewRepository: ReviewRepository,
            chatRepository: ChatRepository,
            context: Context
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ShopDetailViewModel(shopRepository, reviewRepository, chatRepository, context) as T
                }
            }
        }
    }
}