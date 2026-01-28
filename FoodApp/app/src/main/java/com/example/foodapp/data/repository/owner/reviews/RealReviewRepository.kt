package com.example.foodapp.data.repository.owner.reviews

import com.example.foodapp.data.model.owner.review.*
import com.example.foodapp.data.remote.owner.ReviewApiService
import com.example.foodapp.data.repository.owner.base.OwnerReviewRepository
import retrofit2.Response

/**
 * Real implementation of OwnerReviewRepository using backend API
 */
class RealReviewRepository(
    private val apiService: ReviewApiService
) : OwnerReviewRepository {

    override suspend fun getShopReviews(shopId: String, page: Int, limit: Int): Result<ShopReviewsData> {
        return try {
            val response = apiService.getShopReviews(shopId, page, limit)
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success && wrapper.data != null) {
                    Result.success(wrapper.data.toShopReviewsData())
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to load reviews"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun replyToReview(reviewId: String, reply: String): Result<Unit> {
        return try {
            val response = apiService.replyToReview(reviewId, ReplyReviewRequest(reply))
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to reply to review"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> getErrorMessage(response: Response<T>): String {
        return try {
            response.errorBody()?.string() ?: "Unknown error occurred"
        } catch (e: Exception) {
            "Error: ${response.code()}"
        }
    }
}
