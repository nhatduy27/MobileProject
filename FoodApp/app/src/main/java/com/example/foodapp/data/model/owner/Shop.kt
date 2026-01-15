package com.example.foodapp.data.model.owner

import com.google.gson.annotations.SerializedName

/**
 * Shop Entity - Thông tin cửa hàng
 */
data class Shop(
    @SerializedName("id")
    val id: String?,
    
    @SerializedName("ownerId")
    val ownerId: String?,
    
    @SerializedName("ownerName")
    val ownerName: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("address")
    val address: String?,
    
    @SerializedName("phone")
    val phone: String?,
    
    @SerializedName("coverImageUrl")
    val coverImageUrl: String? = null,
    
    @SerializedName("logoUrl")
    val logoUrl: String? = null,
    
    @SerializedName("openTime")
    val openTime: String?,
    
    @SerializedName("closeTime")
    val closeTime: String?,
    
    @SerializedName("shipFeePerOrder")
    val shipFeePerOrder: Int?,
    
    @SerializedName("minOrderAmount")
    val minOrderAmount: Int?,
    
    @SerializedName("isOpen")
    val isOpen: Boolean?,
    
    @SerializedName("status")
    val status: String?,
    
    @SerializedName("rating")
    val rating: Double?,
    
    @SerializedName("totalRatings")
    val totalRatings: Int?,
    
    @SerializedName("totalOrders")
    val totalOrders: Int?,
    
    @SerializedName("totalRevenue")
    val totalRevenue: Double?,
    
    @SerializedName("subscription")
    val subscription: Subscription,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class Subscription(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("startDate")
    val startDate: String,
    
    @SerializedName("trialEndDate")
    val trialEndDate: String?,
    
    @SerializedName("currentPeriodEnd")
    val currentPeriodEnd: String,
    
    @SerializedName("nextBillingDate")
    val nextBillingDate: String?,
    
    @SerializedName("autoRenew")
    val autoRenew: Boolean
)

/**
 * Request để tạo shop mới
 */
data class CreateShopRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("openTime")
    val openTime: String,
    
    @SerializedName("closeTime")
    val closeTime: String,
    
    @SerializedName("shipFeePerOrder")
    val shipFeePerOrder: Int,
    
    @SerializedName("minOrderAmount")
    val minOrderAmount: Int,
    
    @SerializedName("coverImageUrl")
    val coverImageUrl: String? = null,
    
    @SerializedName("logoUrl")
    val logoUrl: String? = null
)

/**
 * Response khi tạo shop thành công
 */
data class CreateShopResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: Shop
)
