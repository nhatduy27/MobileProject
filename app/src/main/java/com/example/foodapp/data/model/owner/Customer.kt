package com.example.foodapp.data.model.owner

/**
 * Đại diện cho thông tin của một khách hàng.
 *
 * @property id Mã định danh duy nhất cho mỗi khách hàng.
 * @property name Tên của khách hàng.
 * @property type Phân loại khách hàng (ví dụ: "VIP", "Thường xuyên", "Mới").
 * @property contact Thông tin liên hệ (SĐT, địa chỉ...).
 * @property ordersInfo Thông tin về số lượng đơn hàng và ngày tham gia.
 * @property revenueInfo Thông tin về tổng doanh thu từ khách hàng này.
 */
data class Customer(
    val id: String,
    val name: String,
    val type: String,
    val contact: String,
    val ordersInfo: String,
    val revenueInfo: String,
    val avatar: String
)
