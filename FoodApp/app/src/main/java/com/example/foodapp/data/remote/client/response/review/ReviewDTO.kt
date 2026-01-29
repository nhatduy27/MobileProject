package com.example.foodapp.data.remote.client.response.review

import com.google.gson.annotations.SerializedName

/**
 * Product review item trong request
 */
data class ProductReviewRequest(
    @SerializedName("productId")
    val productId: String,

    @SerializedName("rating")
    val rating: Int,

    @SerializedName("comment")
    val comment: String? = null
)

/**
 * Request DTO cho API create order review
 * Format request:
 * POST /reviews
 * Body:
 * {
 *   "orderId": "order_xyz",
 *   "shopRating": 5,
 *   "shopComment": "Đồ ăn ngon",
 *   "shipperRating": 4,
 *   "shipperComment": "Giao hàng nhanh",
 *   "productReviews": [
 *     {
 *       "productId": "prod_789",
 *       "rating": 5,
 *       "comment": "Rất ngon!"
 *     }
 *   ]
 * }
 */
data class CreateOrderReviewRequest(
    @SerializedName("orderId")
    val orderId: String,

    @SerializedName("rating")
    val rating: Int,  // shop rating

    @SerializedName("comment")
    val comment: String? = null,

    @SerializedName("productReviews")
    val productReviews: List<ProductReviewRequest>
)

/**
 * Product review item trong response
 */

/**
 * Order review model từ API response
 */
data class OrderReviewApiModel @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("orderId")
    val orderId: String = "",

    @SerializedName("customerId")
    val customerId: String = "",

    @SerializedName("customerName")
    val customerName: String = "",

    @SerializedName("shopId")
    val shopId: String = "",

    @SerializedName("shopRating")
    val shopRating: Int = 0,

    @SerializedName("shopComment")
    val shopComment: String = "",

    @SerializedName("shipperRating")
    val shipperRating: Int? = null,

    @SerializedName("shipperComment")
    val shipperComment: String? = null,

    @SerializedName("productReviews")
    val productReviews: List<ProductReviewResponse> = emptyList(),

    @SerializedName("createdAt")
    val createdAt: String = ""
)

/**
 * Response model wrapper cho API create order review (thành công)
 */
data class CreateOrderReviewResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: OrderReviewApiModel? = null
)

/**
 * Extended Order review model cho response lấy danh sách reviews đã đánh giá
 * Có thể kết hợp hoặc tách riêng tùy backend response
 */
data class MyOrderReviewApiModel @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("orderId")
    val orderId: String = "",

    @SerializedName("shopId")
    val shopId: String = "",

    @SerializedName("shopName")
    val shopName: String = "",

    @SerializedName("shopRating")
    val shopRating: Int = 0,

    @SerializedName("shopComment")
    val shopComment: String = "",

    @SerializedName("shipperRating")
    val shipperRating: Int? = null,

    @SerializedName("shipperComment")
    val shipperComment: String? = null,

    @SerializedName("ownerReply")
    val ownerReply: String? = null,

    @SerializedName("ownerReplyAt")
    val ownerReplyAt: String? = null,

    @SerializedName("productReviews")
    val productReviews: List<ProductReviewResponse> = emptyList(),

    @SerializedName("createdAt")
    val createdAt: String = ""
)

/**
 * Response model wrapper cho API lấy danh sách order reviews đã đánh giá
 */
data class GetMyOrderReviewsResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: List<MyOrderReviewApiModel> = emptyList(),

    @SerializedName("timestamp")
    val timestamp: String = ""
)

/**
 * Order review model cho response lấy danh sách reviews của shop
 * SỬA: Chỉ giữ lại các field có trong response
 */
/**
 * Response model wrapper cho API lấy danh sách order reviews của shop
 */
data class GetShopOrderReviewsResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: ShopOrderReviewsData? = null  // Sửa thành object thay vì List
)

/**
 * Data wrapper cho reviews của shop
 */
data class ShopOrderReviewsData(
    @SerializedName("reviews")
    val reviews: List<ShopOrderReviewApiModel> = emptyList(),

    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("avgRating")
    val avgRating: Double = 0.0
)

/**
 * Order review model cho response lấy danh sách reviews của shop
 */
data class ShopOrderReviewApiModel @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("customerName")
    val customerName: String = "",

    @SerializedName("rating")
    val rating: Int = 0,

    @SerializedName("comment")
    val comment: String = "",

    @SerializedName("ownerReply")
    val ownerReply: String? = null,

    @SerializedName("createdAt")
    val createdAt: String = "",

    // Bỏ productReviews vì không có trong response mới
)

/**
 * Model cho product review trong response
 */
data class ProductReviewResponse(
    @SerializedName("productId")
    val productId: String = "",

    @SerializedName("productName")
    val productName: String = "",

    @SerializedName("rating")
    val rating: Int = 0,

    @SerializedName("comment")
    val comment: String = ""
)

/**
 * Response model wrapper cho API lấy danh sách order reviews của shop
 */

/**
 * Sealed class cho kết quả API
 */
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}

/**
 * Optional: Model cũ để backward compatibility nếu cần
 */
@Deprecated("Use CreateOrderReviewRequest instead", ReplaceWith("CreateOrderReviewRequest"))
data class CreateReviewRequest(
    @SerializedName("orderId")
    val orderId: String,

    @SerializedName("rating")
    val rating: Int,

    @SerializedName("comment")
    val comment: String? = null
)

@Deprecated("Use OrderReviewApiModel instead", ReplaceWith("OrderReviewApiModel"))
data class ReviewApiModel @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("orderId")
    val orderId: String = "",

    @SerializedName("customerId")
    val customerId: String = "",

    @SerializedName("customerName")
    val customerName: String = "",

    @SerializedName("shopId")
    val shopId: String = "",

    @SerializedName("rating")
    val rating: Int = 0,

    @SerializedName("comment")
    val comment: String = "",

    @SerializedName("createdAt")
    val createdAt: String = ""
)