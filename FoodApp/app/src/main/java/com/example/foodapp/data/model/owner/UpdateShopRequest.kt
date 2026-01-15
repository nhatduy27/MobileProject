package com.example.foodapp.data.model.owner

import com.google.gson.annotations.SerializedName

/**
 * Request để cập nhật thông tin shop
 * Tất cả fields đều optional
 */
data class UpdateShopRequest(
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("openTime")
    val openTime: String? = null,
    
    @SerializedName("closeTime")
    val closeTime: String? = null,
    
    @SerializedName("shipFeePerOrder")
    val shipFeePerOrder: Int? = null,
    
    @SerializedName("minOrderAmount")
    val minOrderAmount: Int? = null
)
