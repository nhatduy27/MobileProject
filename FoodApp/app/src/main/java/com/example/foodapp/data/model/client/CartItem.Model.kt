package com.example.foodapp.data.model.client

/**
 * Model đại diện cho một món ăn trong giỏ hàng.
 * @param id ID của item trong giỏ hàng
 * @param foodId ID của món ăn
 * @param foodName Tên món ăn
 * @param foodImageUrl URL hình ảnh món ăn
 * @param price Giá tiền mỗi đơn vị
 * @param quantity Số lượng
 * @param note Ghi chú cho món ăn này (ít cay, không hành, v.v.)
 * @param addedAt Thời gian thêm vào giỏ
 */
data class CartItem(
    val id: String = "",
    val foodId: String = "",
    val foodName: String = "",
    val foodImageUrl: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val note: String = "",
    val addedAt: Long = System.currentTimeMillis()
) {
    /**
     * Tính tổng tiền cho item này.
     */
    fun totalPrice(): Double = price * quantity
}