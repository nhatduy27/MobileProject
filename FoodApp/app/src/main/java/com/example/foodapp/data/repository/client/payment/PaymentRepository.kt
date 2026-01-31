package com.example.foodapp.data.repository.client.payment

import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.PaymentApiService
import com.example.foodapp.data.remote.client.response.payment.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class PaymentRepository() {

    private val paymentApiService: PaymentApiService = ApiClient.paymentApiService

    /**
     * Tạo thanh toán cho đơn hàng
     * POST /api/orders/:orderId/payment
     *
     * @param accessToken Access token của khách hàng
     * @param orderId ID của đơn hàng cần thanh toán
     * @param method Phương thức thanh toán (COD, SEPAY, etc.)
     */
    suspend fun createPayment(
        accessToken: String,
        orderId: String,
        method: String
    ): ApiResult<PaymentData> {
        return try {
            withContext(Dispatchers.IO) {
                val authHeader = "Bearer $accessToken"
                val request = CreatePaymentRequest(method = method)

                val response = paymentApiService.createPayment(
                    authHeader = authHeader,
                    orderId = orderId,
                    request = request
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        ApiResult.Success(body.data.payment)
                    } else {
                        ApiResult.Failure(
                            Exception(
                                body?.let {
                                    "Response success=${it.success}, data is null"
                                } ?: "Response body is null"
                            )
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody.isNullOrEmpty()) {
                        "HTTP ${response.code()}: ${response.message()}"
                    } else {
                        "HTTP ${response.code()}: $errorBody"
                    }
                    ApiResult.Failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

    /**
     * Xác minh trạng thái thanh toán SEPAY (polling endpoint)
     * POST /api/orders/:orderId/payment/verify
     *
     * @param accessToken Access token của khách hàng
     * @param orderId ID của đơn hàng cần xác minh
     * @return ApiResult<VerifyPaymentData> chứa thông tin xác minh
     */
    suspend fun verifyPayment(
        accessToken: String,
        orderId: String
    ): ApiResult<VerifyPaymentData> {
        return try {
            withContext(Dispatchers.IO) {
                val authHeader = "Bearer $accessToken"

                val response = paymentApiService.verifyPayment(
                    authHeader = authHeader,
                    orderId = orderId
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        ApiResult.Success(body.data)
                    } else {
                        val errorMsg = when {
                            body == null -> "Verify payment response body is null"
                            !body.success -> "Verify payment API returned success=false"
                            body.data == null -> "Verify payment data is null"
                            else -> "Unknown error"
                        }
                        ApiResult.Failure(Exception(errorMsg))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody.isNullOrEmpty()) {
                        "HTTP ${response.code()}: ${response.message()}"
                    } else {
                        "HTTP ${response.code()}: $errorBody"
                    }
                    ApiResult.Failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }



    /**
     * Lấy thông tin thanh toán theo order ID
     * GET /api/orders/:orderId/payment
     *
     * @param accessToken Access token của khách hàng
     * @param orderId ID của đơn hàng
     * @return ApiResult<GetPaymentData> chứa thông tin thanh toán
     */
    suspend fun getPaymentByOrder(
        accessToken: String,
        orderId: String
    ): ApiResult<GetPaymentData> {
        return try {
            withContext(Dispatchers.IO) {
                val authHeader = "Bearer $accessToken"

                val response = paymentApiService.getPaymentByOrder(
                    authHeader = authHeader,
                    orderId = orderId
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        ApiResult.Success(body.data)
                    } else {
                        val errorMsg = when {
                            body == null -> "Get payment response body is null"
                            !body.success -> "Get payment API returned success=false"
                            body.data == null -> "Get payment data is null"
                            else -> "Unknown error"
                        }
                        ApiResult.Failure(Exception(errorMsg))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody.isNullOrEmpty()) {
                        "HTTP ${response.code()}: ${response.message()}"
                    } else {
                        "HTTP ${response.code()}: $errorBody"
                    }
                    ApiResult.Failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }
}