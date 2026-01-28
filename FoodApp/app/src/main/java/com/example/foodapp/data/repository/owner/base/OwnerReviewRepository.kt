package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.review.*

/**
 * Interface for Owner Review Repository.
 * Defines methods for review management through backend API.
 */
interface OwnerReviewRepository {
    
    /**
     * Get all reviews for the shop
     * @param shopId Shop ID
     * @param page Page number
     * @param limit Items per page
     */
    suspend fun getShopReviews(shopId: String, page: Int = 1, limit: Int = 20): Result<ShopReviewsData>
    
    /**
     * Reply to a review
     * @param reviewId Review ID
     * @param reply Reply content
     */
    suspend fun replyToReview(reviewId: String, reply: String): Result<Unit>
}
