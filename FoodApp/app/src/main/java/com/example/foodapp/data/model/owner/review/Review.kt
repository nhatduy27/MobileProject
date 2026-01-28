package com.example.foodapp.data.model.owner.review

import com.google.gson.annotations.SerializedName

/**
 * Review Entity - represents a customer review for an order
 */
data class Review(
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
)

/**
 * Request to reply to a review
 */
data class ReplyReviewRequest(
    val reply: String
)

/**
 * Shop reviews response with stats
 */
data class ShopReviewsData(
    val reviews: List<Review>,
    val total: Int,
    val avgRating: Double
)
