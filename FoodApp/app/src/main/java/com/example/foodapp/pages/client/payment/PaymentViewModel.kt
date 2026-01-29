package com.example.foodapp.pages.client.payment

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.model.client.DeliveryAddress
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.remote.client.response.order.*
import com.example.foodapp.data.remote.client.response.payment.*
import com.example.foodapp.data.remote.client.response.voucher.VoucherApiModel
import com.example.foodapp.data.remote.client.response.voucher.ApiResult as VoucherApiResult
import com.example.foodapp.data.remote.client.response.payment.ApiResult as PaymentApiResult
import com.example.foodapp.data.remote.client.response.order.ApiResult as OrderApiResult
import com.example.foodapp.data.remote.client.response.voucher.ValidateVoucherResponse
import com.example.foodapp.data.repository.client.order.OrderRepository
import com.example.foodapp.data.repository.client.payment.PaymentRepository
import com.example.foodapp.data.repository.client.profile.ProfileRepository
import com.example.foodapp.data.repository.client.voucher.VoucherRepository
import com.example.foodapp.data.repository.firebase.AuthManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data class để đại diện cho một mặt hàng trong giỏ hàng
data class CartItem(val product: Product, val quantity: Int)

class PaymentViewModel(
    private val profileRepository: ProfileRepository,
    private val orderRepository: OrderRepository,
    private val voucherRepository: VoucherRepository,
    private val paymentRepository: PaymentRepository,
    private val context: Context
) : ViewModel() {

    private val authManager = AuthManager(context)

    // LiveData
    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _selectedPaymentMethod = MutableLiveData(0)
    val selectedPaymentMethod: LiveData<Int> = _selectedPaymentMethod

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _totalPrice = MutableLiveData(0.0)
    val totalPrice: LiveData<Double> = _totalPrice


    private val _discountAmount = MutableLiveData(0.0)
    val discountAmount: LiveData<Double> = _discountAmount

    private val _finalPrice = MutableLiveData(0.0)
    val finalPrice: LiveData<Double> = _finalPrice

    private val _addresses = MutableLiveData<List<DeliveryAddress>>(emptyList())
    val addresses: LiveData<List<DeliveryAddress>> = _addresses

    private val _selectedAddress = MutableLiveData<DeliveryAddress?>()
    val selectedAddress: LiveData<DeliveryAddress?> = _selectedAddress

    private val _addressError = MutableLiveData<String?>()
    val addressError: LiveData<String?> = _addressError

    private val _paymentResult = MutableLiveData<PaymentApiResult<PaymentData>?>()
    val paymentResult: LiveData<PaymentApiResult<PaymentData>?> = _paymentResult

    private val _paymentError = MutableLiveData<String?>()
    val paymentError: LiveData<String?> = _paymentError

    private val _isProcessingPayment = MutableLiveData(false)
    val isProcessingPayment: LiveData<Boolean> = _isProcessingPayment

    private val _createdOrder = MutableLiveData<OrderApiModel?>()
    val createdOrder: LiveData<OrderApiModel?> = _createdOrder

    private val _vouchers = MutableLiveData<List<VoucherApiModel>>(emptyList())
    val vouchers: LiveData<List<VoucherApiModel>> = _vouchers

    private val _selectedVoucher = MutableLiveData<VoucherApiModel?>()
    val selectedVoucher: LiveData<VoucherApiModel?> = _selectedVoucher

    private val _isLoadingVouchers = MutableLiveData(false)
    val isLoadingVouchers: LiveData<Boolean> = _isLoadingVouchers

    private val _voucherError = MutableLiveData<String?>()
    val voucherError: LiveData<String?> = _voucherError

    private val _voucherValidationResult = MutableLiveData<ValidateVoucherResponse?>()
    val voucherValidationResult: LiveData<ValidateVoucherResponse?> = _voucherValidationResult

    private val _isValidatingVoucher = MutableLiveData(false)
    val isValidatingVoucher: LiveData<Boolean> = _isValidatingVoucher

    private val _shopId = MutableLiveData<String?>()

    // LiveData để hiển thị thông tin chuyển khoản
    private val _showBankTransferInfo = MutableLiveData<BankTransferInfo?>(null)
    val showBankTransferInfo: LiveData<BankTransferInfo?> = _showBankTransferInfo

    // LiveData cho polling verification (ẩn)
    private val _isVerifyingPayment = MutableLiveData(false)
    val isVerifyingPayment: LiveData<Boolean> = _isVerifyingPayment

    // SỬA: PollingResult chỉ trả về OrderApiModel
    private val _pollingResult = MutableLiveData<OrderApiModel?>(null)
    val pollingResult: LiveData<OrderApiModel?> = _pollingResult

    private val _verificationResult = MutableLiveData<VerifyPaymentData?>(null)
    val verificationResult: LiveData<VerifyPaymentData?> = _verificationResult

    private var verificationJob: Job? = null
    private var currentOrderIdForVerification: String? = null

    // SỬA: shouldNavigateWithOrder là OrderApiModel
    private val _shouldNavigateWithOrder = MutableLiveData<OrderApiModel?>(null)
    val shouldNavigateWithOrder: LiveData<OrderApiModel?> = _shouldNavigateWithOrder

    private val _pollingError = MutableLiveData<String?>(null)
    val pollingError: LiveData<String?> = _pollingError

    data class BankTransferInfo(
        val qrCodeUrl: String,
        val accountNumber: String,
        val accountName: String,
        val bankCode: String,
        val amount: Int,
        val sepayContent: String,
        val orderId: String
    )

    fun selectPaymentMethod(method: Int) {
        _selectedPaymentMethod.value = method
        println("DEBUG: Selected payment method: $method")
    }

    fun createOrderAndPayment() {
        val accessToken = authManager.getCurrentToken()

        if (accessToken.isNullOrEmpty()) {
            _paymentError.value = "Chưa đăng nhập. Vui lòng đăng nhập lại"
            return
        }

        if (_selectedAddress.value == null) {
            _paymentError.value = "Vui lòng chọn địa chỉ giao hàng"
            return
        }

        if (_cartItems.value.isNullOrEmpty()) {
            _paymentError.value = "Giỏ hàng trống"
            return
        }

        val shopId = _cartItems.value?.firstOrNull()?.product?.shopId ?: ""
        if (shopId.isBlank()) {
            _paymentError.value = "Không tìm thấy thông tin cửa hàng"
            return
        }

        _isProcessingPayment.value = true
        _paymentError.value = null
        _paymentResult.value = null
        _pollingResult.value = null // Reset polling result
        _shouldNavigateWithOrder.value = null // Reset navigation

        viewModelScope.launch {
            try {
                println("DEBUG: Bắt đầu tạo order...")

                val orderRequest = CreateOrderRequest(
                    shopId = shopId,
                    deliveryAddress = DeliveryAddressRequest(
                        label = _selectedAddress.value?.label ?: "",
                        fullAddress = _selectedAddress.value?.fullAddress ?: "",
                        building = _selectedAddress.value?.building,
                        room = _selectedAddress.value?.room,
                        note = _selectedAddress.value?.note
                    ),
                    paymentMethod = if (_selectedPaymentMethod.value == 0) "COD" else "SEPAY",
                    voucherCode = _selectedVoucher.value?.code
                )

                println("DEBUG: Gửi request tạo order...")
                val orderResult = orderRepository.createOrder(orderRequest)

                when (orderResult) {
                    is OrderApiResult.Success<OrderApiModel> -> {
                        val order = orderResult.data
                        _createdOrder.value = order

                        val paymentMethod = if (_selectedPaymentMethod.value == 0) "COD" else "SEPAY"
                        println("DEBUG: Bắt đầu tạo payment cho orderId: ${order.id}, method: $paymentMethod")

                        val paymentResult = paymentRepository.createPayment(
                            accessToken = accessToken!!,
                            orderId = order.id,
                            method = paymentMethod
                        )

                        when (paymentResult) {
                            is PaymentApiResult.Success<PaymentData> -> {
                                val paymentData = paymentResult.data
                                println("DEBUG: Tạo payment thành công! Method: ${paymentData.method}")

                                _paymentResult.value = paymentResult

                                // Nếu là SEPAY, hiển thị QR code và bắt đầu polling ẨN
                                if (paymentData.method == "SEPAY") {
                                    val providerData = paymentData.providerData
                                    if (providerData != null) {
                                        val qrCodeUrl = providerData.qrCodeUrl
                                        val accountNumber = providerData.accountNumber
                                        val accountName = providerData.accountName
                                        val bankCode = providerData.bankCode
                                        val amount = providerData.amount
                                        val sepayContent = providerData.sepayContent

                                        if (qrCodeUrl != null && accountNumber != null &&
                                            accountName != null && bankCode != null &&
                                            amount != null && sepayContent != null) {

                                            _showBankTransferInfo.value = BankTransferInfo(
                                                qrCodeUrl = qrCodeUrl,
                                                accountNumber = accountNumber,
                                                accountName = accountName,
                                                bankCode = bankCode,
                                                amount = amount,
                                                sepayContent = sepayContent,
                                                orderId = order.id
                                            )

                                            // Bắt đầu polling ngầm (không hiển thị UI)
                                            startBackgroundPolling(order.id)
                                        } else {
                                            _paymentError.value = "Thiếu thông tin thanh toán QR code"
                                        }
                                    } else {
                                        _paymentError.value = "Không có thông tin thanh toán QR code"
                                    }
                                } else if (paymentData.method == "COD") {
                                    // COD: Ngay lập tức chuyển trang với order
                                    println("DEBUG: COD payment - Immediately navigate with order")
                                    _shouldNavigateWithOrder.value = order
                                }
                            }
                            is PaymentApiResult.Failure -> {
                                println("DEBUG: Tạo payment thất bại: ${paymentResult.exception.message}")
                                _paymentError.value = "Lỗi thanh toán: ${paymentResult.exception.message}"
                            }
                        }
                    }
                    is OrderApiResult.Failure -> {
                        println("DEBUG: Tạo order thất bại: ${orderResult.exception.message}")
                        _paymentError.value = "Lỗi tạo đơn hàng: ${orderResult.exception.message}"
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Exception trong createOrderAndPayment: ${e.message}")
                e.printStackTrace()
                _paymentError.value = "Lỗi hệ thống: ${e.message}"
            } finally {
                _isProcessingPayment.value = false
            }
        }
    }

    /**
     * Bắt đầu polling NGẦM - không hiển thị trạng thái trong UI
     */
    private fun startBackgroundPolling(orderId: String) {
        stopPaymentVerification()

        currentOrderIdForVerification = orderId
        _isVerifyingPayment.value = true

        verificationJob = viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 60
            var isPaymentVerified = false

            while (attempts < maxAttempts && _isVerifyingPayment.value == true && !isPaymentVerified) {
                try {
                    println("DEBUG: [Background Polling] Kiểm tra thanh toán, lần ${attempts + 1}")

                    val accessToken = authManager.getCurrentToken()
                    if (accessToken.isNullOrEmpty()) {
                        // Lỗi đăng nhập
                        _pollingError.value = "Chưa đăng nhập"
                        break
                    }

                    val result = paymentRepository.verifyPayment(
                        accessToken = accessToken,
                        orderId = orderId
                    )

                    when (result) {
                        is PaymentApiResult.Success<VerifyPaymentData> -> {
                            val verificationData = result.data
                            _verificationResult.value = verificationData

                            if (verificationData.matched) {
                                // Payment đã được xác nhận thành công
                                println("DEBUG: [Background Polling] Payment verified successfully!")

                                isPaymentVerified = true
                                _isVerifyingPayment.value = false

                                // SỬA: Lấy order từ createdOrder (đã lưu khi tạo order)
                                _createdOrder.value?.let { order ->
                                    _pollingResult.value = order
                                }

                                // Đóng dialog QR code sau 2 giây
                                delay(2000)
                                _showBankTransferInfo.value = null
                                break
                            } else {
                                // Chưa xác nhận -> tiếp tục polling
                                println("DEBUG: [Background Polling] Payment not yet confirmed, waiting...")
                            }
                        }
                        is PaymentApiResult.Failure -> {
                            println("DEBUG: [Background Polling] Verification error: ${result.exception.message}")
                            _pollingError.value = "Lỗi xác nhận thanh toán: ${result.exception.message}"
                        }
                    }
                } catch (e: Exception) {
                    println("DEBUG: [Background Polling] Exception: ${e.message}")
                    _pollingError.value = "Lỗi hệ thống: ${e.message}"
                }

                attempts++

                // Chờ 5 giây trước khi poll tiếp (trừ lần cuối)
                if (attempts < maxAttempts && !isPaymentVerified) {
                    delay(5000)
                }
            }

            if (attempts >= maxAttempts && !isPaymentVerified) {
                // Quá timeout
                _pollingError.value = "Đã quá thời gian chờ xác nhận thanh toán (5 phút)"
                println("DEBUG: [Background Polling] Timeout after 5 minutes")
            }

            _isVerifyingPayment.value = false
        }
    }

    /**
     * Dừng polling verification
     */
    fun stopPaymentVerification() {
        verificationJob?.cancel()
        verificationJob = null
        _isVerifyingPayment.value = false
        _pollingResult.value = null
        _pollingError.value = null
    }

    fun closeBankTransferDialog() {
        _showBankTransferInfo.value = null
        stopPaymentVerification()
    }

    fun resetNavigation() {
        _shouldNavigateWithOrder.value = null
        _pollingResult.value = null
    }

    // Hàm để clear polling error
    fun clearPollingError() {
        _pollingError.value = null
    }

    suspend fun validateSelectedVoucher(): Boolean {
        val voucher = _selectedVoucher.value ?: run {
            _voucherValidationResult.value = null
            calculateFinalPrice()
            return true
        }

        val shopId = _shopId.value ?: run {
            _voucherError.value = "Không tìm thấy thông tin cửa hàng"
            return false
        }

        // LẤY TOKEN
        val token = authManager.getCurrentToken()
        if (token.isNullOrEmpty()) {
            _voucherError.value = "Chưa đăng nhập. Vui lòng đăng nhập lại"
            return false
        }

        _isValidatingVoucher.value = true
        _voucherError.value = null

        return try {
            // THÊM TOKEN VÀO API CALL
            val result = voucherRepository.validateVoucher(
                token = token,
                voucherCode = voucher.code,
                shopId = shopId,
                subtotal = _totalPrice.value ?: 0.0,
                shipFee =  0.0
            )

            when (result) {
                is VoucherApiResult.Success<ValidateVoucherResponse> -> {
                    val validationData = result.data
                    _voucherValidationResult.value = validationData

                    if (validationData.isValid) {
                        _discountAmount.value = validationData.discountAmount
                        calculateFinalPrice()
                        true
                    } else {
                        _voucherError.value = "Voucher không hợp lệ hoặc đã hết hiệu lực"
                        _selectedVoucher.value = null
                        _discountAmount.value = 0.0
                        calculateFinalPrice()
                        false
                    }
                }
                is VoucherApiResult.Failure -> {
                    _voucherError.value = "Lỗi kiểm tra voucher: ${result.exception.message}"
                    false
                }
            }
        } catch (e: Exception) {
            _voucherError.value = "Lỗi khi kiểm tra voucher: ${e.message}"
            false
        } finally {
            _isValidatingVoucher.value = false
        }
    }

    fun fetchAddress() {
        if (_isLoading.value == true) return

        _isLoading.value = true
        _addressError.value = null

        viewModelScope.launch {
            try {
                val result = profileRepository.getAddresses()

                if (result is com.example.foodapp.data.remote.client.response.profile.ApiResult.Success) {
                    val deliveryAddresses = result.data.map { addressResponse ->
                        DeliveryAddress(
                            id = addressResponse.id ?: "",
                            label = addressResponse.label ?: "Địa chỉ",
                            fullAddress = addressResponse.fullAddress ?: "",
                            building = addressResponse.building,
                            room = addressResponse.room,
                            note = addressResponse.note,
                            isDefault = addressResponse.isDefault ?: false,
                            clientId = addressResponse.userId ?: "",
                            receiverName = "",
                            receiverPhone = ""
                        )
                    }

                    _addresses.value = deliveryAddresses

                    // Tự động chọn địa chỉ mặc định
                    val defaultAddress = deliveryAddresses.firstOrNull { it.isDefault }
                    if (defaultAddress != null) {
                        _selectedAddress.value = defaultAddress
                    } else if (deliveryAddresses.isNotEmpty()) {
                        _selectedAddress.value = deliveryAddresses.first()
                    }
                } else if (result is com.example.foodapp.data.remote.client.response.profile.ApiResult.Failure) {
                    _addressError.value = "Không thể tải địa chỉ: ${result.exception.message}"
                }
            } catch (e: Exception) {
                _addressError.value = "Lỗi khi tải địa chỉ: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchVouchers() {
        val shopId = _shopId.value
        if (shopId.isNullOrEmpty() || _isLoadingVouchers.value == true) return

        _isLoadingVouchers.value = true
        _voucherError.value = null

        viewModelScope.launch {
            try {
                val token = authManager.getCurrentToken()
                if (token.isNullOrEmpty()) {
                    _voucherError.value = "Chưa đăng nhập. Vui lòng đăng nhập lại"
                    _isLoadingVouchers.value = false
                    return@launch
                }

                // THÊM TOKEN VÀO API CALL
                val result = voucherRepository.getVouchers(token, shopId)

                when (result) {
                    is VoucherApiResult.Success<List<VoucherApiModel>> -> {
                        val applicableVouchers = result.data.filter { voucher ->
                            val isValid = voucher.isActive && !voucher.isDeleted
                            val meetsMinOrderAmount = (_totalPrice.value ?: 0.0) >= voucher.minOrderAmount
                            val hasUsageLeft = voucher.usageLimit == 0 ||
                                    (voucher.usageLimit > 0 && voucher.currentUsage < voucher.usageLimit)
                            isValid && meetsMinOrderAmount && hasUsageLeft
                        }

                        _vouchers.value = applicableVouchers

                        // Auto-select voucher
                        if (_selectedVoucher.value == null && applicableVouchers.isNotEmpty()) {
                            val bestVoucher = applicableVouchers.maxByOrNull { it.value } ?: applicableVouchers.first()
                            _selectedVoucher.value = bestVoucher
                            validateSelectedVoucher()
                        }
                    }
                    is VoucherApiResult.Failure -> {
                        _voucherError.value = "Không thể tải voucher: ${result.exception.message}"
                    }
                }
            } catch (e: Exception) {
                _voucherError.value = "Lỗi khi tải voucher: ${e.message}"
            } finally {
                _isLoadingVouchers.value = false
            }
        }
    }
    fun selectVoucher(voucher: VoucherApiModel?) {
        _selectedVoucher.value = voucher
        _voucherValidationResult.value = null

        if (voucher != null) {
            viewModelScope.launch {
                validateSelectedVoucher()
            }
        } else {
            _discountAmount.value = 0.0
            calculateFinalPrice()
        }
    }

    fun clearSelectedVoucher() {
        _selectedVoucher.value = null
        _voucherValidationResult.value = null
        _discountAmount.value = 0.0
        calculateFinalPrice()
    }

    private fun calculateDiscountAndFinalPrice() {
        val voucher = _selectedVoucher.value
        val total = _totalPrice.value ?: 0.0

        var discount = 0.0

        val validationResult = _voucherValidationResult.value
        if (validationResult != null && validationResult.isValid) {
            discount = validationResult.discountAmount
        } else if (voucher != null && voucher.isActive && !voucher.isDeleted) {
            when (voucher.type.uppercase()) {
                "PERCENTAGE" -> {
                    val discountPercent = voucher.value / 100.0
                    discount = total * discountPercent
                    voucher.maxDiscount?.let { maxDiscount ->
                        if (discount > maxDiscount) discount = maxDiscount
                    }
                }
                "FIXED_AMOUNT" -> {
                    discount = voucher.value
                }
                else -> discount = 0.0
            }

            if (discount > total) discount = total
        }

        _discountAmount.value = discount
        calculateFinalPrice()
    }

    private fun calculateFinalPrice() {
        val final = (_totalPrice.value ?: 0.0) + - (_discountAmount.value ?: 0.0)
        _finalPrice.value = if (final < 0) 0.0 else final
    }

    fun selectAddress(address: DeliveryAddress) {
        _selectedAddress.value = address
    }

    fun hasAddresses(): Boolean {
        return !_addresses.value.isNullOrEmpty()
    }

    fun initializeWithProductsAndQuantities(products: List<Product>, quantities: List<Int>) {
        if (products.isEmpty()) return

        val items = List(minOf(products.size, quantities.size)) { index ->
            CartItem(
                product = products[index],
                quantity = quantities[index]
            )
        }
        _cartItems.value = items
        calculateTotalPrice()

        val shopId = items.firstOrNull()?.product?.shopId
        if (shopId != null) {
            _shopId.value = shopId
            fetchVouchers()
        }

        fetchAddress()
    }

    fun increaseQuantity(productId: String) {
        val currentItems = _cartItems.value ?: return
        _cartItems.value = currentItems.map { cartItem ->
            if (cartItem.product.id == productId) {
                cartItem.copy(quantity = cartItem.quantity + 1)
            } else {
                cartItem
            }
        }
        calculateTotalPrice()

        if (_selectedVoucher.value != null) {
            viewModelScope.launch {
                validateSelectedVoucher()
            }
        }
    }

    fun decreaseQuantity(productId: String) {
        val currentItems = _cartItems.value ?: return
        _cartItems.value = currentItems.map { cartItem ->
            if (cartItem.product.id == productId && cartItem.quantity > 1) {
                cartItem.copy(quantity = cartItem.quantity - 1)
            } else {
                cartItem
            }
        }
        calculateTotalPrice()

        if (_selectedVoucher.value != null) {
            viewModelScope.launch {
                validateSelectedVoucher()
            }
        }
    }

    private fun calculateTotalPrice() {
        val total = (_cartItems.value ?: emptyList()).sumOf { cartItem ->
            val price = parsePrice(cartItem.product.price)
            price * cartItem.quantity
        }
        _totalPrice.value = total
        calculateDiscountAndFinalPrice()
    }

    private fun parsePrice(priceString: String): Double {
        return try {
            val sanitizedString = priceString
                .replace("đ", "")
                .replace(".", "")
                .replace(",", "")
                .trim()
            sanitizedString.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    fun getShopName(): String {
        return _cartItems.value?.firstOrNull()?.product?.shopName ?: "Không có thông tin shop"
    }

    fun clearPaymentError() {
        _paymentError.value = null
        _paymentResult.value = null
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val profileRepository = ProfileRepository()
                val orderRepository = OrderRepository()
                val voucherRepository = VoucherRepository()
                val paymentRepository = PaymentRepository()
                PaymentViewModel(
                    profileRepository = profileRepository,
                    orderRepository = orderRepository,
                    voucherRepository = voucherRepository,
                    paymentRepository = paymentRepository,
                    context = context
                )
            }
        }
    }
}