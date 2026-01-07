package com.example.foodapp.data.repository.customer.base

import com.example.foodapp.data.model.client.CartItem
import kotlinx.coroutines.flow.Flow

/**
 * Interface repository cho việc quản lý giỏ hàng.
 * Xử lý thêm, sửa, xóa các món ăn trong giỏ hàng của người mua.
 */
interface ClientCartRepository {

    /**
     * Lấy toàn bộ danh sách các món ăn trong giỏ hàng.
     * @return Flow phát ra danh sách các CartItem trong giỏ
     */
    fun getCartItems(): Flow<List<CartItem>>

    /**
     * Thêm món ăn vào giỏ hàng.
     * Nếu món ăn đã có trong giỏ, tăng số lượng lên.
     * @param foodId ID của món ăn cần thêm
     * @param quantity Số lượng cần thêm (mặc định là 1)
     * @return Result cho biết thành công hay thất bại
     */
    suspend fun addToCart(foodId: String, quantity: Int = 1): Result<Unit>

    /**
     * Cập nhật số lượng của một món ăn trong giỏ hàng.
     * @param itemId ID của item trong giỏ hàng
     * @param quantity Số lượng mới
     * @return Result cho biết thành công hay thất bại
     */
    suspend fun updateCartItem(itemId: String, quantity: Int): Result<Unit>

    /**
     * Xóa một món ăn khỏi giỏ hàng.
     * @param itemId ID của item trong giỏ hàng cần xóa
     * @return Result cho biết thành công hay thất bại
     */
    suspend fun removeFromCart(itemId: String): Result<Unit>

    /**
     * Xóa toàn bộ giỏ hàng.
     * @return Result cho biết thành công hay thất bại
     */
    suspend fun clearCart(): Result<Unit>

    /**
     * Tính tổng giá trị hiện tại của giỏ hàng.
     * @return Flow phát ra tổng tiền cần thanh toán
     */
    fun getCartTotal(): Flow<Double>

    /**
     * Đếm tổng số lượng món ăn trong giỏ hàng.
     * @return Flow phát ra số lượng item trong giỏ
     */
    fun getCartItemCount(): Flow<Int>

    /**
     * Kiểm tra xem một món ăn đã có trong giỏ hàng chưa.
     * @param foodId ID của món ăn cần kiểm tra
     * @return Flow phát ra true nếu đã có trong giỏ, false nếu chưa
     */
    fun isFoodInCart(foodId: String): Flow<Boolean>
}