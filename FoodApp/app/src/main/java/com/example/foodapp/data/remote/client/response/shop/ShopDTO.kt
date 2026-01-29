package com.example.foodapp.data.remote.client.response.shop

import com.google.gson.annotations.SerializedName

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}

/**
 * Request DTO cho API get all shops
 * Format request:
 * GET /shops
 * Query parameters:
 * - page: Số trang (mặc định: 1)
 * - limit: Số lượng mỗi trang (mặc định: 20)
 * - status: Trạng thái shop (OPEN/CLOSED)
 * - search: Từ khóa tìm kiếm
 */
data class GetAllShopsRequest @JvmOverloads constructor(
    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("limit")
    val limit: Int = 20,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("search")
    val search: String? = null
)

/**
 * Shop model từ API response
 */
data class ShopApiModel @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("address")
    val address: String = "",

    @SerializedName("rating")
    val rating: Double = 0.0,

    @SerializedName("totalRatings")
    val totalRatings: Int = 0,

    @SerializedName("isOpen")
    val isOpen: Boolean = false,

    @SerializedName("openTime")
    val openTime: String = "",

    @SerializedName("closeTime")
    val closeTime: String = "",

    @SerializedName("shipFeePerOrder")
    val shipFeePerOrder: Int = 0,

    @SerializedName("minOrderAmount")
    val minOrderAmount: Int = 0
)

/**
 * Data model cho shops response
 */
data class ShopsData(
    @SerializedName("shops")
    val shops: List<ShopApiModel> = emptyList(),

    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("limit")
    val limit: Int = 20
)

/**
 * Response model wrapper cho API get all shops
 */
data class GetAllShopsResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: ShopsData? = null
)

/**
 * Request DTO cho API get shop details
 * Format request:
 * GET /shops/{id}
 * Path parameter:
 * - id: ID của shop
 */
data class GetShopDetailRequest(
    val id: String
)

/**
 * Chi tiết shop model từ API response
 */
/**
 * Chi tiết shop model từ API response - SỬA LẠI
 */
data class ShopDetailApiModel(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("address")
    val address: String = "",

    @SerializedName("phone")
    val phone: String = "",

    @SerializedName("coverImageUrl")
    val coverImageUrl: String = "",

    @SerializedName("logoUrl")
    val logoUrl: String = "",

    @SerializedName("rating")
    val rating: Double = 0.0,

    @SerializedName("totalRatings")
    val totalRatings: Int = 0, // JSON: 10 (Int)

    @SerializedName("isOpen")
    val isOpen: Boolean = false, // JSON: true

    @SerializedName("openTime")
    val openTime: String = "", // JSON: "07:00"

    @SerializedName("closeTime")
    val closeTime: String = "", // JSON: "21:00"

    @SerializedName("shipFeePerOrder")
    val shipFeePerOrder: Int = 0, // JSON: 5000 (Int)

    @SerializedName("minOrderAmount")
    val minOrderAmount: Int = 0,

    @SerializedName("totalOrders")
    val totalOrders: Int = 0,

    @SerializedName("ownerId")
    val ownerId: String = "",

    @SerializedName("ownerName")
    val ownerName: String = "",


)

/**
 * Response model wrapper cho API get shop details
 */
data class GetShopDetailResponse(
    @SerializedName("success")
    val success: Boolean = false, // JSON: true

    @SerializedName("data")
    val data: ShopDetailApiModel? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null // JSON: "2026-01-29T10:21:45.369Z"
)

/**
 * Error response model cho API get shop details
 */
data class ShopErrorResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("code")
    val code: String = "",

    @SerializedName("message")
    val message: String = "",

    @SerializedName("statusCode")
    val statusCode: Int = 0
)