package com.example.foodapp.pages.client.orderdetail

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.remote.client.response.order.*
import com.example.foodapp.data.remote.client.response.review.ProductReviewRequest
import com.example.foodapp.data.repository.client.order.OrderRepository
import com.example.foodapp.data.repository.client.review.*
import com.example.foodapp.data.repository.client.review.ReviewRepository
import kotlinx.coroutines.launch

// ============== ORDER DETAIL STATES ==============

sealed class OrderDetailState {
    object Idle : OrderDetailState()
    object Loading : OrderDetailState()
    data class Success(val order: OrderApiModel) : OrderDetailState()
    data class Error(val message: String) : OrderDetailState()
    object Empty : OrderDetailState()
}

sealed class CancelOrderState {
    object Idle : CancelOrderState()
    object Loading : CancelOrderState()
    data class Success(val orderId: String) : CancelOrderState()
    data class Error(val message: String) : CancelOrderState()
}

// ============== REVIEW STATES ==============

sealed class ReviewState {
    object Idle : ReviewState()
    object Loading : ReviewState()
    data class Success(val message: String) : ReviewState()
    data class Error(val message: String) : ReviewState()
}

// Data class for product review in UI
data class ProductReviewUI(
    val productId: String,
    val productName: String,
    val rating: Int = 0,
    val comment: String = ""
)

// ============== ORDER DETAIL VIEW MODEL ==============

class OrderDetailViewModel(
    private val orderRepository: OrderRepository,
    private val reviewRepository: ReviewRepository,
    private val context: Context
) : ViewModel() {

    private val authManager = AuthManager(context)
    private val _orderDetailState = MutableLiveData<OrderDetailState>(OrderDetailState.Idle)
    val orderDetailState: LiveData<OrderDetailState> = _orderDetailState

    private val _cancelOrderState = MutableLiveData<CancelOrderState>(CancelOrderState.Idle)
    val cancelOrderState: LiveData<CancelOrderState> = _cancelOrderState

    private val _reviewState = MutableLiveData<ReviewState>(ReviewState.Idle)
    val reviewState: LiveData<ReviewState> = _reviewState

    private val _currentOrder = MutableLiveData<OrderApiModel?>(null)
    val currentOrder: LiveData<OrderApiModel?> = _currentOrder

    private val _hasReviewed = MutableLiveData<Boolean>(false)
    val hasReviewed: LiveData<Boolean> = _hasReviewed

    // ============== ORDER DETAIL FUNCTIONS ==============

    fun fetchOrderDetail(orderId: String) {
        println("DEBUG: [OrderDetailViewModel] Fetching order detail for: $orderId")

        _orderDetailState.value = OrderDetailState.Loading

        viewModelScope.launch {
            try {
                val result = orderRepository.getOrderById(orderId)
                println("DEBUG: [OrderDetailViewModel] Result type: ${result::class.simpleName}")

                when (result) {
                    is ApiResult.Success<*> -> {
                        println("DEBUG: [OrderDetailViewModel] Entered Success branch")
                        val order = (result.data as? OrderApiModel) ?: run {
                            _orderDetailState.value = OrderDetailState.Empty
                            return@launch
                        }

                        println("DEBUG: [OrderDetailViewModel] Order loaded: ${order.orderNumber}")
                        _currentOrder.value = order
                        _orderDetailState.value = OrderDetailState.Success(order)

                        // Kiểm tra xem đã đánh giá chưa (nếu đơn hàng đã giao)
                        if (order.status == "DELIVERED") {
                            checkIfReviewed(orderId)
                        }

                        println("DEBUG: [OrderDetailViewModel] State set to Success")
                    }
                    is ApiResult.Failure -> {
                        println("DEBUG: [OrderDetailViewModel] Entered Failure branch: ${result.exception.message}")
                        _orderDetailState.value = OrderDetailState.Error(
                            result.exception.message ?: "Không thể tải chi tiết đơn hàng"
                        )
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: [OrderDetailViewModel] Exception in fetchOrderDetail: ${e.message}")
                e.printStackTrace()
                _orderDetailState.value = OrderDetailState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    // ============== REVIEW FUNCTIONS ==============

    private fun checkIfReviewed(orderId: String) {
        viewModelScope.launch {
            try {
                // TODO: Implement API check if order has been reviewed
                // Tạm thời để false
                _hasReviewed.value = false
            } catch (e: Exception) {
                // Bỏ qua lỗi, giữ giá trị mặc định
            }
        }
    }

    fun createOrderReview(
        orderId: String,
        shopRating: Int,
        shopComment: String?,
        productReviews: List<ProductReviewRequest>  // Loại bỏ shipperRating và shipperComment
    ) {
        println("DEBUG: [OrderDetailViewModel] Creating review for order: $orderId")

        _reviewState.value = ReviewState.Loading

        viewModelScope.launch {
            try {
                val accessToken = authManager.getCurrentToken()

                if (accessToken.isNullOrEmpty()) {
                    _reviewState.value = ReviewState.Error("Bạn cần đăng nhập để đánh giá")
                    return@launch
                }

                val result = reviewRepository.createOrderReview(
                    accessToken,
                    orderId,
                    shopRating,
                    shopComment,
                    productReviews  // Chỉ truyền productReviews
                )

                when (result) {
                    is ApiResult.Success<*> -> {
                        println("DEBUG: [OrderDetailViewModel] Review created successfully")
                        _reviewState.value = ReviewState.Success("Đánh giá thành công!")
                        _hasReviewed.value = true

                        // Refresh order detail
                        fetchOrderDetail(orderId)
                    }
                    is ApiResult.Failure -> {
                        println("DEBUG: [OrderDetailViewModel] Review failed: ${result.exception.message}")
                        _reviewState.value = ReviewState.Error(
                            result.exception.message ?: "Tạo đánh giá thất bại"
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                println("DEBUG: [OrderDetailViewModel] Exception in createReview: ${e.message}")
                e.printStackTrace()
                _reviewState.value = ReviewState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    fun resetReviewState() {
        _reviewState.value = ReviewState.Idle
    }

    // ============== ORDER ACTIONS FUNCTIONS ==============

    fun cancelOrder(orderId: String) {
        println("DEBUG: [OrderDetailViewModel] Canceling order: $orderId")

        _cancelOrderState.value = CancelOrderState.Loading

        viewModelScope.launch {
            try {
                val result = orderRepository.deleteOrder(orderId)
                println("DEBUG: [OrderDetailViewModel] Cancel result: ${result::class.simpleName}")

                when (result) {
                    is ApiResult.Success<*> -> {
                        println("DEBUG: [OrderDetailViewModel] Cancel successful")
                        _cancelOrderState.value = CancelOrderState.Success(orderId)
                        // Refresh order detail after cancellation
                        fetchOrderDetail(orderId)
                    }
                    is ApiResult.Failure -> {
                        println("DEBUG: [OrderDetailViewModel] Cancel failed: ${result.exception.message}")
                        _cancelOrderState.value = CancelOrderState.Error(
                            result.exception.message ?: "Hủy đơn hàng thất bại"
                        )
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: [OrderDetailViewModel] Exception in cancelOrder: ${e.message}")
                e.printStackTrace()
                _cancelOrderState.value = CancelOrderState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    fun resetCancelState() {
        _cancelOrderState.value = CancelOrderState.Idle
    }

    fun refresh(orderId: String) {
        println("DEBUG: [OrderDetailViewModel] Refresh order detail")
        fetchOrderDetail(orderId)
    }

    fun reset() {
        _orderDetailState.value = OrderDetailState.Idle
        _cancelOrderState.value = CancelOrderState.Idle
        _reviewState.value = ReviewState.Idle
        _currentOrder.value = null
        _hasReviewed.value = false
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val orderRepository = OrderRepository()
                val reviewRepository = ReviewRepository()
                OrderDetailViewModel(orderRepository, reviewRepository, context)
            }
        }
    }
}