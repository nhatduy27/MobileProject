package com.example.foodapp.data.repository.client.review

import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.response.review.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ReviewRepository {
    private val reviewApiService = ApiClient.reviewApiService

    /**
     * Tạo đánh giá cho đơn hàng
     */
    suspend fun createOrderReview(
        token: String,
        orderId: String,
        shopRating: Int,
        shopComment: String? = null,
        productReviews: List<ProductReviewRequest>
    ): ApiResult<OrderReviewApiModel> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateOrderReviewRequest(
                    orderId = orderId,
                    rating = shopRating,
                    comment = shopComment ?: "",
                    productReviews = productReviews
                )

                val response = reviewApiService.createOrderReview("Bearer $token", request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val reviewData = response.body()?.data
                    if (reviewData != null) {
                        ApiResult.Success(reviewData)
                    } else {
                        ApiResult.Failure(Exception("Không có dữ liệu đánh giá trả về"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Lỗi không xác định"
                    ApiResult.Failure(Exception("Tạo đánh giá thất bại: $errorBody"))
                }
            } catch (e: Exception) {
                ApiResult.Failure(Exception("Lỗi: ${e.message}"))
            }
        }
    }

    /**
     * Lấy danh sách đánh giá đơn hàng mà tôi đã đánh giá
     */
    suspend fun getMyOrderReviews(
        token: String,
        page: Int = 1,
        limit: Int = 20
    ): ApiResult<List<MyOrderReviewApiModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = reviewApiService.getMyOrderReviews("Bearer $token", page, limit)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.success == true) {
                        ApiResult.Success(responseBody.data)
                    } else {
                        ApiResult.Failure(Exception("API trả về success = false"))
                    }
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "Lỗi không xác định"

                    when (errorCode) {
                        401 -> ApiResult.Failure(Exception("Không có quyền truy cập"))
                        404 -> ApiResult.Success(emptyList()) // Không có review nào
                        else -> ApiResult.Failure(Exception("Lỗi $errorCode: $errorBody"))
                    }
                }
            } catch (e: HttpException) {
                ApiResult.Failure(Exception("Lỗi HTTP: ${e.code()} - ${e.message()}"))
            } catch (e: IOException) {
                ApiResult.Failure(Exception("Lỗi kết nối mạng: ${e.message}"))
            } catch (e: Exception) {
                ApiResult.Failure(Exception("Lỗi hệ thống: ${e.message}"))
            }
        }
    }


    /**
     * Lấy danh sách đánh giá của shop (cho owner shop)
     */
    suspend fun getShopOrderReviews(
        token: String,
        shopId: String,
        page: Int = 1,
        limit: Int = 20
    ): ApiResult<ShopOrderReviewsData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = reviewApiService.getShopOrderReviews(
                    token = "Bearer $token",
                    shopId = shopId,
                    page = page,
                    limit = limit
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.success == true && responseBody.data != null) {
                        // Trả về data chứ không phải responseBody
                        ApiResult.Success(responseBody.data)
                    } else {
                        ApiResult.Failure(Exception("Không có dữ liệu đánh giá"))
                    }
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "Lỗi không xác định"

                    when (errorCode) {
                        401 -> ApiResult.Failure(Exception("Không có quyền truy cập"))
                        403 -> ApiResult.Failure(Exception("Không có quyền xem đánh giá của shop này"))
                        404 -> {
                            // Trả về empty data khi shop không có review
                            ApiResult.Success(
                                ShopOrderReviewsData(
                                    reviews = emptyList(),
                                    total = 0,
                                    avgRating = 0.0
                                )
                            )
                        }
                        else -> ApiResult.Failure(Exception("Lỗi $errorCode: $errorBody"))
                    }
                }
            } catch (e: HttpException) {
                ApiResult.Failure(Exception("Lỗi HTTP: ${e.code()} - ${e.message()}"))
            } catch (e: IOException) {
                ApiResult.Failure(Exception("Lỗi kết nối mạng: ${e.message}"))
            } catch (e: Exception) {
                ApiResult.Failure(Exception("Lỗi hệ thống: ${e.message}"))
            }
        }
    }


}