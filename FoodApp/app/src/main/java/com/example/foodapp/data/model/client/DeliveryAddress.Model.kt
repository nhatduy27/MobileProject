package com.example.foodapp.data.model.client

/**
 * Model đại diện cho địa chỉ giao hàng của người mua.
 * @param id ID địa chỉ
 * @param clientId ID người mua
 * @param name Tên người nhận
 * @param phone Số điện thoại người nhận
 * @param address Địa chỉ chi tiết
 * @param note Ghi chú (tầng, căn hộ, v.v.)
 * @param isDefault Có phải địa chỉ mặc định không
 * @param latitude Vĩ độ (cho map)
 * @param longitude Kinh độ (cho map)
 */
data class DeliveryAddress(
    val id: String = "",
    val clientId: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val note: String = "",
    val isDefault: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)