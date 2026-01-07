package com.example.foodapp.data.repository.client.cart


import com.example.foodapp.data.model.client.CartItem
import com.example.foodapp.data.repository.customer.base.ClientCartRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Mock implementation của ClientCartRepository cho mục đích testing và development.
 * Quản lý giỏ hàng với dữ liệu giả lập trong bộ nhớ.
 */
class MockClientCartRepository : ClientCartRepository {

    // State flow để quản lý danh sách item trong giỏ hàng
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    init {
        // Khởi tạo dữ liệu giỏ hàng mẫu khi repository được tạo
        initializeMockCart()
    }

    /**
     * Khởi tạo giỏ hàng mẫu.
     */
    private fun initializeMockCart() {
        _cartItems.value = listOf(
            CartItem(
                id = "cart_001",
                foodId = "1",
                foodName = "Phở Bò",
                foodImageUrl = "https://picsum.photos/id/492/300/200",
                price = 45000.0,
                quantity = 2,
                note = "Thêm nhiều hành",
                addedAt = System.currentTimeMillis() - 3600000 // 1 giờ trước
            ),
            CartItem(
                id = "cart_002",
                foodId = "9",
                foodName = "Cà Phê Sữa Đá",
                foodImageUrl = "https://picsum.photos/id/1060/300/200",
                price = 25000.0,
                quantity = 1,
                note = "Ít đường",
                addedAt = System.currentTimeMillis() - 1800000 // 30 phút trước
            ),
            CartItem(
                id = "cart_003",
                foodId = "14",
                foodName = "Khoai Tây Chiên",
                foodImageUrl = "https://picsum.photos/id/0/300/200",
                price = 30000.0,
                quantity = 1,
                note = "",
                addedAt = System.currentTimeMillis() - 900000 // 15 phút trước
            )
        )
    }

    /**
     * READ: Lấy tất cả item trong giỏ hàng.
     * Phương thức này trả về một StateFlow, cho phép UI lắng nghe sự thay đổi dữ liệu theo thời gian thực.
     * @return Một StateFlow chứa danh sách item trong giỏ hàng.
     */
    override fun getCartItems(): Flow<List<CartItem>> {
        return _cartItems.asStateFlow()
    }

    /**
     * Mô phỏng việc gọi API để lấy giỏ hàng chỉ một lần.
     * Phương thức này sử dụng 'flow' builder để tạo ra một cold flow.
     * Thêm 'delay' để giả lập độ trễ mạng.
     * @return Một Flow chỉ phát ra dữ liệu một lần.
     */
    fun fetchCartFromApi(): Flow<List<CartItem>> = flow {
        // Giả lập độ trễ mạng
        delay(500)
        // Phát ra giá trị hiện tại của danh sách
        emit(_cartItems.value)
    }

    /**
     * CREATE: Thêm món ăn vào giỏ hàng.
     * Nếu món ăn đã có trong giỏ, tăng số lượng lên.
     * @param foodId ID của món ăn cần thêm.
     * @param quantity Số lượng cần thêm.
     * @return Result cho biết thành công hay thất bại.
     */
    override suspend fun addToCart(foodId: String, quantity: Int): Result<Unit> {
        // Giả lập độ trễ network
        delay(300)

        val existingItem = _cartItems.value.find { it.foodId == foodId }

        if (existingItem != null) {
            // Nếu đã có, tăng số lượng
            _cartItems.update { currentList ->
                currentList.map { item ->
                    if (item.id == existingItem.id) {
                        item.copy(quantity = item.quantity + quantity)
                    } else {
                        item
                    }
                }
            }
        } else {
            // Nếu chưa có, thêm mới
            // Trong thực tế, cần lấy thông tin món ăn từ repository
            val foodName = when (foodId) {
                "1" -> "Phở Bò"
                "2" -> "Bánh Mì Thịt"
                "9" -> "Cà Phê Sữa Đá"
                "10" -> "Trà Sữa Trân Châu"
                else -> "Món ăn $foodId"
            }

            val foodImageUrl = when (foodId) {
                "1" -> "https://picsum.photos/id/492/300/200"
                "2" -> "https://picsum.photos/id/292/300/200"
                "9" -> "https://picsum.photos/id/1060/300/200"
                "10" -> "https://picsum.photos/id/61/300/200"
                else -> "https://picsum.photos/id/0/300/200"
            }

            val price = when (foodId) {
                "1" -> 45000.0
                "2" -> 25000.0
                "9" -> 25000.0
                "10" -> 35000.0
                else -> 30000.0
            }

            val newItem = CartItem(
                id = "cart_${UUID.randomUUID()}",
                foodId = foodId,
                foodName = foodName,
                foodImageUrl = foodImageUrl,
                price = price,
                quantity = quantity,
                note = "",
                addedAt = System.currentTimeMillis()
            )

            _cartItems.update { currentList ->
                currentList + newItem
            }
        }

        return Result.success(Unit)
    }

    /**
     * UPDATE: Cập nhật số lượng của một món ăn trong giỏ hàng.
     * @param itemId ID của item trong giỏ hàng.
     * @param quantity Số lượng mới.
     * @return Result cho biết thành công hay thất bại.
     */
    override suspend fun updateCartItem(itemId: String, quantity: Int): Result<Unit> {
        // Giả lập độ trễ network
        delay(200)

        if (quantity <= 0) {
            // Nếu số lượng <= 0, xóa item
            return removeFromCart(itemId)
        }

        _cartItems.update { currentList ->
            currentList.map { item ->
                if (item.id == itemId) {
                    item.copy(quantity = quantity)
                } else {
                    item
                }
            }
        }

        return Result.success(Unit)
    }

    /**
     * DELETE: Xóa một món ăn khỏi giỏ hàng.
     * @param itemId ID của item trong giỏ hàng cần xóa.
     * @return Result cho biết thành công hay thất bại.
     */
    override suspend fun removeFromCart(itemId: String): Result<Unit> {
        // Giả lập độ trễ network
        delay(200)

        _cartItems.update { currentList ->
            currentList.filterNot { it.id == itemId }
        }

        return Result.success(Unit)
    }

    /**
     * DELETE: Xóa toàn bộ giỏ hàng.
     * @return Result cho biết thành công hay thất bại.
     */
    override suspend fun clearCart(): Result<Unit> {
        // Giả lập độ trễ network
        delay(300)

        _cartItems.update { emptyList() }

        return Result.success(Unit)
    }

    /**
     * READ: Tính tổng giá trị hiện tại của giỏ hàng.
     * @return Flow phát ra tổng tiền cần thanh toán.
     */
    override fun getCartTotal(): Flow<Double> {
        return _cartItems.map { items ->
            items.sumOf { it.totalPrice() }
        }
    }

    /**
     * READ: Đếm tổng số lượng món ăn trong giỏ hàng.
     * @return Flow phát ra số lượng item trong giỏ.
     */
    override fun getCartItemCount(): Flow<Int> {
        return _cartItems.map { items ->
            items.sumOf { it.quantity }
        }
    }

    /**
     * READ: Kiểm tra xem một món ăn đã có trong giỏ hàng chưa.
     * @param foodId ID của món ăn cần kiểm tra.
     * @return Flow phát ra true nếu đã có trong giỏ, false nếu chưa.
     */
    override fun isFoodInCart(foodId: String): Flow<Boolean> {
        return _cartItems.map { items ->
            items.any { it.foodId == foodId }
        }
    }

    /**
     * UPDATE: Cập nhật ghi chú cho một item trong giỏ hàng.
     * @param itemId ID của item trong giỏ hàng.
     * @param note Ghi chú mới.
     */
    fun updateCartItemNote(itemId: String, note: String) {
        _cartItems.update { currentList ->
            currentList.map { item ->
                if (item.id == itemId) {
                    item.copy(note = note)
                } else {
                    item
                }
            }
        }
    }

    /**
     * READ: Tìm một item trong giỏ hàng theo ID.
     * @param itemId ID của item cần tìm.
     * @return Item tìm thấy hoặc null.
     */
    fun getCartItemById(itemId: String): CartItem? {
        return _cartItems.value.find { it.id == itemId }
    }
}
