package com.example.foodapp.pages.client.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.repository.client.cart.CartRepository
import com.example.foodapp.utils.CurrencyUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// ============== CART STATES ==============
sealed class CartState {
    object Idle : CartState()
    object Loading : CartState()
    data class Success(
        val cartData: com.example.foodapp.data.remote.client.response.cart.GetCartData
    ) : CartState()
    data class Error(val message: String) : CartState()
    object Empty : CartState()
}

// ============== CLEAR CART STATES ==============
sealed class ClearCartState {
    object Idle : ClearCartState()
    object Loading : ClearCartState()
    object Success : ClearCartState()
    data class Error(val message: String) : ClearCartState()
}

// ============== DELETE SHOP STATES ==============
sealed class DeleteShopState {
    object Idle : DeleteShopState()
    object Loading : DeleteShopState()
    data class Success(val message: String) : DeleteShopState()
    data class Error(val message: String) : DeleteShopState()
}

// ============== REMOVE ITEM STATES ==============
sealed class RemoveItemState {
    object Idle : RemoveItemState()
    object Loading : RemoveItemState()
    data class Success(val message: String) : RemoveItemState()
    data class Error(val message: String) : RemoveItemState()
}

// ============== UPDATE QUANTITY STATES ==============
sealed class UpdateQuantityState {
    object Idle : UpdateQuantityState()
    object Loading : UpdateQuantityState()
    data class Success(val message: String) : UpdateQuantityState()
    data class Error(val message: String) : UpdateQuantityState()
}

// ============== GET CART BY SHOP STATES ==============
sealed class CartByShopState {
    object Idle : CartByShopState()
    object Loading : CartByShopState()
    data class Success(
        val shopGroup: ShopGroup
    ) : CartByShopState()
    data class Error(val message: String) : CartByShopState()
}

// ============== CART UI DATA CLASSES ==============
data class CartItemUi(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int = 1,
    val imageUrl: String? = null,
    val shopId: String,
    val shopName: String,
    val addedAt: String,
    val updatedAt: String
) {
    val subtotal: Double
        get() = price * quantity

    val formattedPrice: String
        get() = CurrencyUtils.formatCurrency(price)

    val formattedSubtotal: String
        get() = CurrencyUtils.formatCurrency(subtotal)
}

// ============== SHOP GROUP DATA CLASS ==============
data class ShopGroup(
    val shopId: String,
    val shopName: String,
    val itemCount: Int,
    val totalItems: Int,
    val subtotal: Double,
    val shipFee: Double,
    val isOpen: Boolean,
    val items: List<CartItemUi>,
    val lastActivityAt: String? = null
)

// ============== SHOP FILTER OPTION DATA CLASS ==============
data class ShopFilterOption(
    val id: String = "",
    val name: String = "Tất cả cửa hàng",
    val itemCount: Int = 0,
    val totalItems: Int = 0
)

// ============== CART VIEW MODEL ==============
class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {

    // ============== BASE STATES ==============
    private val _cartState = MutableLiveData<CartState>(CartState.Idle)
    val cartState: LiveData<CartState> = _cartState

    private val _cartItems = MutableLiveData<List<CartItemUi>>(emptyList())
    val cartItems: LiveData<List<CartItemUi>> = _cartItems

    private val _shopGroups = MutableLiveData<List<ShopGroup>>(emptyList())
    val shopGroups: LiveData<List<ShopGroup>> = _shopGroups

    private val _totalAmount = MutableLiveData<Double>(0.0)
    val totalAmount: LiveData<Double> = _totalAmount

    private val _formattedTotalAmount = MutableLiveData<String>("0đ")
    val formattedTotalAmount: LiveData<String> = _formattedTotalAmount

    private val _totalShippingFee = MutableLiveData<Double>(0.0)
    val totalShippingFee: LiveData<Double> = _totalShippingFee

    private val _grandTotal = MutableLiveData<Double>(0.0)
    val grandTotal: LiveData<Double> = _grandTotal

    private val _clearCartState = MutableLiveData<ClearCartState>(ClearCartState.Idle)
    val clearCartState: LiveData<ClearCartState> = _clearCartState

    private val _deleteShopState = MutableLiveData<DeleteShopState>(DeleteShopState.Idle)
    val deleteShopState: LiveData<DeleteShopState> = _deleteShopState

    private val _showClearCartDialog = MutableLiveData<Boolean>(false)
    val showClearCartDialog: LiveData<Boolean> = _showClearCartDialog

    private val _showDeleteShopDialog = MutableLiveData<Boolean>(false)
    val showDeleteShopDialog: LiveData<Boolean> = _showDeleteShopDialog

    private val _selectedShopForDelete = MutableLiveData<ShopGroup?>(null)
    val selectedShopForDelete: LiveData<ShopGroup?> = _selectedShopForDelete

    private val _removeItemState = MutableLiveData<RemoveItemState>(RemoveItemState.Idle)
    val removeItemState: LiveData<RemoveItemState> = _removeItemState

    private val _removingItemId = MutableLiveData<String?>(null)
    val removingItemId: LiveData<String?> = _removingItemId

    private val _updateQuantityState = MutableLiveData<UpdateQuantityState>(UpdateQuantityState.Idle)
    val updateQuantityState: LiveData<UpdateQuantityState> = _updateQuantityState

    private val _updatingItemId = MutableLiveData<String?>(null)
    val updatingItemId: LiveData<String?> = _updatingItemId

    private val _pendingQuantityChanges = MutableLiveData<Map<String, Int>>(emptyMap())
    val pendingQuantityChanges: LiveData<Map<String, Int>> = _pendingQuantityChanges

    private val _currentPage = MutableLiveData<Int>(1)
    val currentPage: LiveData<Int> = _currentPage

    private val _totalPages = MutableLiveData<Int>(1)
    val totalPages: LiveData<Int> = _totalPages

    private val _totalGroups = MutableLiveData<Int>(0)
    val totalGroups: LiveData<Int> = _totalGroups

    // ============== FILTER STATES ==============
    private val _selectedShopFilter = MutableLiveData<ShopFilterOption>(ShopFilterOption())
    val selectedShopFilter: LiveData<ShopFilterOption> = _selectedShopFilter

    private val _shopFilterOptions = MutableLiveData<List<ShopFilterOption>>(emptyList())
    val shopFilterOptions: LiveData<List<ShopFilterOption>> = _shopFilterOptions

    private val _showFilterDropdown = MutableLiveData<Boolean>(false)
    val showFilterDropdown: LiveData<Boolean> = _showFilterDropdown

    private val _filteredCartItems = MutableLiveData<List<CartItemUi>>(emptyList())
    val filteredCartItems: LiveData<List<CartItemUi>> = _filteredCartItems

    private val _filteredShopGroups = MutableLiveData<List<ShopGroup>>(emptyList())
    val filteredShopGroups: LiveData<List<ShopGroup>> = _filteredShopGroups

    private val _filteredTotalAmount = MutableLiveData<Double>(0.0)
    val filteredTotalAmount: LiveData<Double> = _filteredTotalAmount

    private val _filteredTotalShippingFee = MutableLiveData<Double>(0.0)
    val filteredTotalShippingFee: LiveData<Double> = _filteredTotalShippingFee

    private val _filteredGrandTotal = MutableLiveData<Double>(0.0)
    val filteredGrandTotal: LiveData<Double> = _filteredGrandTotal

    private val _filteredFormattedTotalAmount = MutableLiveData<String>("0đ")
    val filteredFormattedTotalAmount: LiveData<String> = _filteredFormattedTotalAmount

    // ============== CART OPERATIONS ==============
    fun loadCart() {
        _cartState.value = CartState.Loading

        viewModelScope.launch {
            try {
                val result = cartRepository.getCart()

                when (result) {
                    is com.example.foodapp.data.remote.client.response.cart.CartApiResult.Success -> {
                        val response = result.data
                        if (response.success) {
                            val cartData = response.data

                            _currentPage.value = cartData.page
                            _totalPages.value = cartData.totalPages
                            _totalGroups.value = cartData.totalGroups

                            updateCartUI(cartData)
                            updateShopFilterOptions(cartData.groups)

                            _cartState.value = if (cartData.groups.isNotEmpty()) {
                                CartState.Success(cartData)
                            } else {
                                CartState.Empty
                            }
                        } else {
                            _cartState.value = CartState.Error("Không thể tải giỏ hàng")
                            resetCartData()
                        }
                    }
                    is com.example.foodapp.data.remote.client.response.cart.CartApiResult.Failure -> {
                        _cartState.value = CartState.Error(
                            result.exception.message ?: "Lỗi không xác định"
                        )
                        resetCartData()
                    }
                }
            } catch (e: Exception) {
                _cartState.value = CartState.Error("Lỗi: ${e.message ?: "Không xác định"}")
                resetCartData()
            }
        }
    }

    private fun updateCartUI(cartData: com.example.foodapp.data.remote.client.response.cart.GetCartData) {
        val cartItemsUi = mutableListOf<CartItemUi>()
        val shopGroupsList = mutableListOf<ShopGroup>()
        var totalAmount = 0.0
        var totalShippingFee = 0.0

        cartData.groups.forEach { group ->
            val shopGroup = ShopGroup(
                shopId = group.shopId,
                shopName = group.shopName,
                itemCount = group.items.size,
                totalItems = group.items.sumOf { it.quantity },
                subtotal = group.subtotal.toDouble(),
                shipFee = group.shipFee.toDouble(),
                isOpen = group.isOpen,
                items = group.items.map { item ->
                    CartItemUi(
                        id = item.productId,
                        name = item.productName,
                        price = item.price.toDouble(),
                        quantity = item.quantity,
                        imageUrl = item.productImage,
                        shopId = item.shopId,
                        shopName = group.shopName,
                        addedAt = item.addedAt,
                        updatedAt = item.updatedAt
                    )
                }
            )

            shopGroupsList.add(shopGroup)
            totalShippingFee += group.shipFee.toDouble()

            shopGroup.items.forEach { item ->
                cartItemsUi.add(item)
                totalAmount += item.subtotal
            }
        }

        _cartItems.value = cartItemsUi
        _shopGroups.value = shopGroupsList
        _totalAmount.value = totalAmount
        _totalShippingFee.value = totalShippingFee
        _grandTotal.value = totalAmount + totalShippingFee
        _formattedTotalAmount.value = CurrencyUtils.formatCurrency(totalAmount + totalShippingFee)
        _pendingQuantityChanges.value = emptyMap()

        // Áp dụng filter hiện tại
        applyShopFilter(_selectedShopFilter.value)
    }

    // ============== SHOP FILTER OPERATIONS ==============
    private fun updateShopFilterOptions(groups: List<com.example.foodapp.data.remote.client.response.cart.CartGroup>) {
        val options = mutableListOf<ShopFilterOption>()

        // Thêm option "Tất cả cửa hàng"
        options.add(ShopFilterOption(
            id = "",
            name = "Tất cả cửa hàng",
            itemCount = groups.size,
            totalItems = groups.sumOf { it.items.sumOf { item -> item.quantity } }
        ))

        // Thêm từng cửa hàng
        groups.forEach { group ->
            options.add(ShopFilterOption(
                id = group.shopId,
                name = group.shopName,
                itemCount = group.items.size,
                totalItems = group.items.sumOf { it.quantity }
            ))
        }

        _shopFilterOptions.value = options
    }

    fun setShopFilter(shopFilter: ShopFilterOption) {
        _selectedShopFilter.value = shopFilter
        applyShopFilter(shopFilter)
    }

    private fun applyShopFilter(shopFilter: ShopFilterOption) {
        val allShopGroups = _shopGroups.value ?: emptyList()
        val allCartItems = _cartItems.value ?: emptyList()

        if (shopFilter.id.isEmpty()) {
            // Hiển thị tất cả
            _filteredShopGroups.value = allShopGroups
            _filteredCartItems.value = allCartItems
            _filteredTotalAmount.value = _totalAmount.value ?: 0.0
            _filteredTotalShippingFee.value = _totalShippingFee.value ?: 0.0
            _filteredGrandTotal.value = _grandTotal.value ?: 0.0
            _filteredFormattedTotalAmount.value = _formattedTotalAmount.value ?: "0đ"
        } else {
            // Lọc theo shop
            val filteredGroups = allShopGroups.filter { it.shopId == shopFilter.id }
            val filteredItems = allCartItems.filter { it.shopId == shopFilter.id }

            var totalAmount = 0.0
            var totalShippingFee = 0.0

            filteredGroups.forEach { group ->
                totalShippingFee += group.shipFee
                totalAmount += group.subtotal
            }

            _filteredShopGroups.value = filteredGroups
            _filteredCartItems.value = filteredItems
            _filteredTotalAmount.value = totalAmount
            _filteredTotalShippingFee.value = totalShippingFee
            _filteredGrandTotal.value = totalAmount + totalShippingFee
            _filteredFormattedTotalAmount.value = CurrencyUtils.formatCurrency(totalAmount + totalShippingFee)
        }
    }

    fun toggleFilterDropdown() {
        _showFilterDropdown.value = !(_showFilterDropdown.value ?: false)
    }

    // ============== DELETE SHOP OPERATIONS ==============
    fun showDeleteShopDialog(shopGroup: ShopGroup) {
        _selectedShopForDelete.value = shopGroup
        _showDeleteShopDialog.value = true
    }

    fun hideDeleteShopDialog() {
        _showDeleteShopDialog.value = false
        _selectedShopForDelete.value = null
    }

    fun deleteShop() {
        val shopGroup = _selectedShopForDelete.value ?: return

        _deleteShopState.value = DeleteShopState.Loading
        _showDeleteShopDialog.value = false

        viewModelScope.launch {
            try {
                val result = cartRepository.deleteShopItems(shopGroup.shopId)

                when (result) {
                    is com.example.foodapp.data.remote.client.response.cart.CartApiResult.Success -> {
                        loadCart()
                        _deleteShopState.value = DeleteShopState.Success(
                            "Đã xóa ${shopGroup.itemCount} sản phẩm từ ${shopGroup.shopName}"
                        )

                        viewModelScope.launch {
                            delay(2000)
                            _deleteShopState.value = DeleteShopState.Idle
                        }
                    }
                    is com.example.foodapp.data.remote.client.response.cart.CartApiResult.Failure -> {
                        _deleteShopState.value = DeleteShopState.Error(
                            result.exception.message ?: "Không thể xóa cửa hàng"
                        )

                        viewModelScope.launch {
                            delay(3000)
                            _deleteShopState.value = DeleteShopState.Idle
                        }
                    }
                }
            } catch (e: Exception) {
                _deleteShopState.value = DeleteShopState.Error("Lỗi: ${e.message ?: "Không xác định"}")

                viewModelScope.launch {
                    delay(3000)
                    _deleteShopState.value = DeleteShopState.Idle
                }
            }
        }
    }

    // ============== QUANTITY CHANGE OPERATIONS ==============
    fun setPendingQuantityChange(itemId: String, newQuantity: Int) {
        if (newQuantity < 1 || newQuantity > 999) return

        val currentMap = _pendingQuantityChanges.value ?: emptyMap()
        val newMap = currentMap.toMutableMap()

        val currentItem = _cartItems.value?.find { it.id == itemId }
        currentItem?.let { item ->
            if (newQuantity == item.quantity) {
                newMap.remove(itemId)
            } else {
                newMap[itemId] = newQuantity
            }
        }

        _pendingQuantityChanges.value = newMap
    }

    fun saveQuantityChange(itemId: String) {
        val pendingQuantity = _pendingQuantityChanges.value?.get(itemId) ?: return
        val currentItem = _cartItems.value?.find { it.id == itemId } ?: return

        if (pendingQuantity == currentItem.quantity) {
            removePendingChange(itemId)
            return
        }

        _updateQuantityState.value = UpdateQuantityState.Loading
        _updatingItemId.value = itemId

        viewModelScope.launch {
            try {
                val result = cartRepository.updateItemQuantity(itemId, pendingQuantity)

                when (result) {
                    is com.example.foodapp.data.remote.client.response.cart.CartApiResult.Success -> {
                        val response = result.data
                        if (response.success) {
                            loadCart()
                            _updateQuantityState.value = UpdateQuantityState.Success("Đã cập nhật số lượng")
                            _updatingItemId.value = null

                            viewModelScope.launch {
                                delay(2000)
                                _updateQuantityState.value = UpdateQuantityState.Idle
                            }
                        } else {
                            val errorMessage = response.message ?: "Không thể cập nhật số lượng"
                            _updateQuantityState.value = UpdateQuantityState.Error(errorMessage)
                            _updatingItemId.value = null

                            viewModelScope.launch {
                                delay(3000)
                                _updateQuantityState.value = UpdateQuantityState.Idle
                            }
                        }
                    }
                    is com.example.foodapp.data.remote.client.response.cart.CartApiResult.Failure -> {
                        _updatingItemId.value = null
                        val errorMessage = result.exception.message ?: "Không thể cập nhật số lượng"
                        _updateQuantityState.value = UpdateQuantityState.Error(errorMessage)

                        viewModelScope.launch {
                            delay(3000)
                            _updateQuantityState.value = UpdateQuantityState.Idle
                        }
                    }
                }
            } catch (e: Exception) {
                _updatingItemId.value = null
                _updateQuantityState.value = UpdateQuantityState.Error("Lỗi: ${e.message ?: "Không xác định"}")

                viewModelScope.launch {
                    delay(3000)
                    _updateQuantityState.value = UpdateQuantityState.Idle
                }
            }
        }
    }

    fun saveAllQuantityChanges() {
        val pendingChanges = _pendingQuantityChanges.value ?: emptyMap()
        if (pendingChanges.isEmpty()) return

        _updateQuantityState.value = UpdateQuantityState.Loading

        viewModelScope.launch {
            try {
                var hasError = false
                var errorMessage = ""

                for ((itemId, newQuantity) in pendingChanges) {
                    try {
                        val result = cartRepository.updateItemQuantity(itemId, newQuantity)

                        when (result) {
                            is com.example.foodapp.data.remote.client.response.cart.CartApiResult.Success -> {
                                if (!result.data.success) {
                                    hasError = true
                                    errorMessage = result.data.message ?: "Không thể cập nhật số lượng"
                                }
                            }
                            is com.example.foodapp.data.remote.client.response.cart.CartApiResult.Failure -> {
                                hasError = true
                                errorMessage = result.exception.message ?: "Không thể cập nhật số lượng"
                            }
                        }
                    } catch (e: Exception) {
                        hasError = true
                        errorMessage = "Lỗi: ${e.message ?: "Không xác định"}"
                    }
                }

                if (hasError) {
                    _updateQuantityState.value = UpdateQuantityState.Error(errorMessage)
                } else {
                    loadCart()
                    _updateQuantityState.value = UpdateQuantityState.Success("Đã lưu tất cả thay đổi")
                }

                viewModelScope.launch {
                    delay(3000)
                    _updateQuantityState.value = UpdateQuantityState.Idle
                }

            } catch (e: Exception) {
                _updateQuantityState.value = UpdateQuantityState.Error("Lỗi: ${e.message ?: "Không xác định"}")

                viewModelScope.launch {
                    delay(3000)
                    _updateQuantityState.value = UpdateQuantityState.Idle
                }
            }
        }
    }

    fun removePendingChange(itemId: String) {
        val currentMap = _pendingQuantityChanges.value ?: emptyMap()
        val newMap = currentMap.toMutableMap()
        newMap.remove(itemId)
        _pendingQuantityChanges.value = newMap
    }

    fun clearAllPendingChanges() {
        _pendingQuantityChanges.value = emptyMap()
    }

    // ============== REMOVE ITEM ==============
    fun removeItem(itemId: String) {
        removePendingChange(itemId)

        _removeItemState.value = RemoveItemState.Loading
        _removingItemId.value = itemId

        viewModelScope.launch {
            try {
                val result = cartRepository.removeItemFromCart(itemId)

                when (result) {
                    is com.example.foodapp.data.remote.client.response.cart.CartApiResult.Success -> {
                        loadCart()
                        _removeItemState.value = RemoveItemState.Success("Đã xóa sản phẩm khỏi giỏ hàng")
                        _removingItemId.value = null

                        viewModelScope.launch {
                            delay(2000)
                            _removeItemState.value = RemoveItemState.Idle
                        }
                    }
                    is com.example.foodapp.data.remote.client.response.cart.CartApiResult.Failure -> {
                        _removingItemId.value = null
                        val errorMessage = result.exception.message ?: "Không thể xóa sản phẩm"
                        _removeItemState.value = RemoveItemState.Error(errorMessage)

                        viewModelScope.launch {
                            delay(3000)
                            _removeItemState.value = RemoveItemState.Idle
                        }
                    }
                }
            } catch (e: Exception) {
                _removingItemId.value = null
                _removeItemState.value = RemoveItemState.Error("Lỗi: ${e.message ?: "Không xác định"}")

                viewModelScope.launch {
                    delay(3000)
                    _removeItemState.value = RemoveItemState.Idle
                }
            }
        }
    }

    private fun resetCartData() {
        _cartItems.value = emptyList()
        _shopGroups.value = emptyList()
        _totalAmount.value = 0.0
        _totalShippingFee.value = 0.0
        _grandTotal.value = 0.0
        _formattedTotalAmount.value = "0đ"
        _pendingQuantityChanges.value = emptyMap()
        _currentPage.value = 1
        _totalPages.value = 1
        _totalGroups.value = 0
        _selectedShopFilter.value = ShopFilterOption()
        _shopFilterOptions.value = emptyList()
        _showFilterDropdown.value = false
        _filteredCartItems.value = emptyList()
        _filteredShopGroups.value = emptyList()
        _filteredTotalAmount.value = 0.0
        _filteredTotalShippingFee.value = 0.0
        _filteredGrandTotal.value = 0.0
        _filteredFormattedTotalAmount.value = "0đ"
    }

    // ============== CLEAR CART OPERATIONS ==============
    fun showClearCartDialog() {
        _showClearCartDialog.value = true
    }

    fun hideClearCartDialog() {
        _showClearCartDialog.value = false
    }

    fun clearCart() {
        _clearCartState.value = ClearCartState.Loading
        _showClearCartDialog.value = false

        viewModelScope.launch {
            try {
                val result = cartRepository.clearCart()

                when (result) {
                    is com.example.foodapp.data.remote.client.response.cart.CartApiResult.Success -> {
                        resetCartData()
                        _cartState.value = CartState.Empty
                        _clearCartState.value = ClearCartState.Success

                        viewModelScope.launch {
                            delay(2000)
                            _clearCartState.value = ClearCartState.Idle
                        }
                    }
                    is com.example.foodapp.data.remote.client.response.cart.CartApiResult.Failure -> {
                        _clearCartState.value = ClearCartState.Error(
                            result.exception.message ?: "Không thể xóa giỏ hàng"
                        )

                        viewModelScope.launch {
                            delay(3000)
                            _clearCartState.value = ClearCartState.Idle
                        }
                    }
                }
            } catch (e: Exception) {
                _clearCartState.value = ClearCartState.Error("Lỗi: ${e.message ?: "Không xác định"}")

                viewModelScope.launch {
                    delay(3000)
                    _clearCartState.value = ClearCartState.Idle
                }
            }
        }
    }

    fun resetClearCartState() {
        _clearCartState.value = ClearCartState.Idle
    }

    companion object {
        fun factory(context: android.content.Context) = viewModelFactory {
            initializer {
                val repository = CartRepository()
                CartViewModel(repository)
            }
        }
    }
}