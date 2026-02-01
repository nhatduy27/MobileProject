package com.example.foodapp.pages.client.ordersuccess

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodapp.data.remote.client.response.order.OrderApiModel
import com.example.foodapp.data.remote.client.response.order.OrderItemApiModel

class OrderSuccessViewModel : ViewModel() {

    private val _order = MutableLiveData<OrderApiModel?>()
    val order: LiveData<OrderApiModel?> = _order

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun initializeWithOrder(order: OrderApiModel) {
        _order.value = order
    }

    fun getOrderId(): String {
        return _order.value?.id ?: ""
    }

    fun getOrderNumber(): String {
        return _order.value?.orderNumber ?: ""
    }

    fun getOrderTotal(): String {
        val total = _order.value?.total ?: 0.0
        return "${String.format("%,.0f", total).replace(",", ".")}đ"
    }

    fun getPaymentMethod(): String {
        return when (_order.value?.paymentMethod?.uppercase()) {
            "COD" -> "Thanh toán khi nhận hàng (COD)"
            "SEPAY" -> "Chuyển khoản ngân hàng"
            else -> "Không xác định"
        }
    }

    fun getOrderStatus(): String {
        return when (_order.value?.status) {
            "PENDING" -> "Đang chờ xác nhận"
            "CONFIRMED" -> "Đã xác nhận"
            "PREPARING" -> "Đang chuẩn bị"
            "READY" -> "Sẵn sàng giao"
            "SHIPPING" -> "Đang giao hàng"
            "DELIVERED" -> "Đã giao"
            "CANCELLED" -> "Đã hủy"
            else -> "Không xác định"
        }
    }


    fun clearError() {
        _error.value = null
    }

    private fun formatDateTime(dateTime: String): String {
        return try {
            // Simple parsing for display
            if (dateTime.contains("T")) {
                val datePart = dateTime.substring(0, 10).replace("-", "/")
                val timePart = dateTime.substring(11, 16)
                "$datePart $timePart"
            } else {
                dateTime
            }
        } catch (e: Exception) {
            dateTime
        }
    }
}