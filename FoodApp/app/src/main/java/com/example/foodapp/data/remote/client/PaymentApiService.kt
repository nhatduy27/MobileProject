package com.example.foodapp.data.remote.client

import com.example.foodapp.data.remote.client.response.payment.*
import retrofit2.Response
import retrofit2.http.*

interface PaymentApiService {
    /**
     * Create payment for order
     * POST /api/orders/:orderId/payment
     *
     * Response: CreatePaymentResponse
     */
    @POST("orders/{orderId}/payment")
    suspend fun createPayment(
        @Header("Authorization") authHeader: String,
        @Path("orderId") orderId: String,
        @Body request: CreatePaymentRequest
    ): Response<CreatePaymentResponse>

    /**
     * Verify SEPAY payment status (polling endpoint)
     * POST /api/orders/:orderId/payment/verify
     *
     * Response: VerifyPaymentResponse
     */
    @POST("orders/{orderId}/payment/verify")
    suspend fun verifyPayment(
        @Header("Authorization") authHeader: String,
        @Path("orderId") orderId: String
    ): Response<VerifyPaymentResponse>



    @GET("orders/{orderId}/payment")
    suspend fun getPaymentByOrder(
        @Header("Authorization") authHeader: String,
        @Path("orderId") orderId: String
    ): Response<GetPaymentResponse>
}