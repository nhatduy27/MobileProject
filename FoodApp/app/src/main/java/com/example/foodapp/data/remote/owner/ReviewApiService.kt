package com.example.foodapp.data.remote.owner

import com.example.foodapp.data.model.owner.review.*
import com.example.foodapp.data.remote.owner.response.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service for Owner Review Management
 * 
 * Endpoints:
 * - GET /reviews/shop/{shopId} - Get shop reviews (public)
 * - POST /owner/reviews/{reviewId}/reply - Reply to a review
 */
interface ReviewApiService {

    /**
     * GET /reviews/shop/{shopId}
     * Get all reviews for the shop
     */
    @GET("reviews/shop/{shopId}")
    suspend fun getShopReviews(
        @Path("shopId") shopId: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<WrappedShopReviewsResponse>

    /**
     * POST /owner/reviews/{reviewId}/reply
     * Reply to a review
     */
    @POST("owner/reviews/{reviewId}/reply")
    suspend fun replyToReview(
        @Path("reviewId") reviewId: String,
        @Body request: ReplyReviewRequest
    ): Response<WrappedReviewActionResponse>
}
