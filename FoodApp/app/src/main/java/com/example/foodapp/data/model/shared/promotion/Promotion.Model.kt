package com.example.foodapp.data.model.shared

import java.util.Date

/**
 * Model đại diện cho chương trình khuyến mãi.
 * @param id ID duy nhất của khuyến mãi
 * @param code Mã khuyến mãi
 * @param name Tên chương trình khuyến mãi
 * @param description Mô tả chi tiết
 * @param discountType Loại giảm giá
 * @param discountValue Giá trị giảm giá
 * @param minOrderAmount Đơn hàng tối thiểu để áp dụng
 * @param maxDiscount Giảm giá tối đa (nếu có)
 * @param applicableFoodIds Danh sách món ăn áp dụng (null = tất cả)
 * @param applicableCategory Danh mục áp dụng (null = tất cả)
 * @param totalUses Số lượt sử dụng tối đa
 * @param usedCount Số lượt đã sử dụng
 * @param startDate Ngày bắt đầu
 * @param endDate Ngày kết thúc
 * @param ownerId ID người bán tạo khuyến mãi
 * @param isActive Còn hiệu lực không
 */
data class Promotion(
    val id: String = "",
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val discountType: DiscountType = DiscountType.PERCENTAGE,
    val discountValue: Double = 0.0,
    val minOrderAmount: Double = 0.0,
    val maxDiscount: Double? = null,
    val applicableFoodIds: List<String>? = null,
    //val applicableCategory: FoodCategory? = null,
    val totalUses: Int = 100,
    val usedCount: Int = 0,
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val ownerId: String = "",
    val isActive: Boolean = true
) {
    /**
     * Kiểm tra khuyến mãi còn hiệu lực không.
     */
    fun isValid(): Boolean {
        val now = Date()
        return isActive &&
                now.after(startDate) &&
                now.before(endDate) &&
                usedCount < totalUses
    }

    /**
     * Tính số tiền giảm giá.
     * @param orderAmount Tổng tiền đơn hàng
     */
    fun calculateDiscount(orderAmount: Double): Double {
        if (orderAmount < minOrderAmount) return 0.0

        return when (discountType) {
            DiscountType.PERCENTAGE -> {
                val discount = orderAmount * discountValue / 100
                maxDiscount?.let {
                    if (discount > it) it else discount
                } ?: discount
            }
            DiscountType.FIXED_AMOUNT -> discountValue
        }
    }
}

/**
 * Enum loại giảm giá.
 */
enum class DiscountType {
    PERCENTAGE,     // Giảm theo phần trăm
    FIXED_AMOUNT    // Giảm số tiền cố định
}