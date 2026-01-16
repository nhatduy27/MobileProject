package com.example.foodapp.pages.client.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.shared.product.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(0) // 0: COD, 1: MoMo
    val selectedPaymentMethod: StateFlow<Int> = _selectedPaymentMethod.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    // Hàm khởi tạo với Product đã có sẵn
    fun initializeWithProduct(product: Product) {
        _product.value = product
        _quantity.value = 1 // Luôn bắt đầu từ 1
        calculateTotalPrice()
    }

    fun increaseQuantity() {
        _quantity.value += 1
        calculateTotalPrice()
    }

    fun decreaseQuantity() {
        if (_quantity.value > 1) {
            _quantity.value -= 1
            calculateTotalPrice()
        }
    }

    fun selectPaymentMethod(method: Int) {
        _selectedPaymentMethod.value = method
    }

    fun placeOrder(onSuccess: () -> Unit) {
        if (_isLoading.value) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                // TODO: Gọi API đặt hàng
                kotlinx.coroutines.delay(2000) // Simulate API call
                onSuccess()
            } catch (e: Exception) {
                // TODO: Xử lý lỗi
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateTotalPrice() {
        val product = _product.value ?: return
        val quantity = _quantity.value

        // Sử dụng priceValue thay vì price
        _totalPrice.value = product.priceValue * quantity
    }

    companion object {
        // Factory cho ViewModel
        fun getFactory(): androidx.lifecycle.ViewModelProvider.Factory {
            return object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
                        return PaymentViewModel() as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}