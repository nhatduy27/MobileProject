package com.example.foodapp.data.remote.owner.response

import com.example.foodapp.data.model.owner.review.*
import com.google.gson.annotations.SerializedName

/**
 * Response wrapper for shop reviews list
 */
data class WrappedShopReviewsResponse(
    val success: Boolean,
    val data: ShopReviewsDto?,
    val message: String? = null,
    val timestamp: String? = null
)

/**
 * Response wrapper for review reply action
 */
data class WrappedReviewActionResponse(
    val success: Boolean,
    val message: String? = null,
    val timestamp: String? = null
)

/**
 * DTO for shop reviews data
 */
data class ShopReviewsDto(
    val reviews: List<ReviewDto>,
    val total: Int,
    val avgRating: Double
) {
    fun toShopReviewsData(): ShopReviewsData = ShopReviewsData(
        reviews = reviews.map { it.toReview() },
        total = total,
        avgRating = avgRating
    )
}

/**
 * DTO for Review from API
 */
data class ReviewDto(
    val id: String,
    val orderId: String,
    val customerId: String,
    val customerName: String,
    val shopId: String,
    val rating: Int,
    val comment: String?,
    val ownerReply: String?,
    val ownerRepliedAt: String?,
    val createdAt: String,
    val updatedAt: String?
) {
    fun toReview(): Review = Review(
        id = id,
        orderId = orderId,
        customerId = customerId,
        customerName = customerName,
        shopId = shopId,
        rating = rating,
        comment = comment,
        ownerReply = ownerReply,
        ownerRepliedAt = ownerRepliedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
