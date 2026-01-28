package com.example.foodapp.data.remote.shipper

import com.example.foodapp.data.model.shipper.application.ShopsApiResponse
import com.example.foodapp.data.model.shipper.application.ApplicationsApiResponse
import com.example.foodapp.data.model.shipper.application.ApplicationApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ShipperApplicationApiService {

    // Get all shops for browsing (public endpoint)
    @GET("shops")
    suspend fun getShops(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("search") search: String? = null
    ): Response<ShopsApiResponse>

    // Get my applications
    @GET("shipper-applications/me")
    suspend fun getMyApplications(): Response<ApplicationsApiResponse>

    // Apply to be shipper (multipart form data)
    @Multipart
    @POST("shipper-applications")
    suspend fun applyShipper(
        @Part("shopId") shopId: RequestBody,
        @Part("vehicleType") vehicleType: RequestBody,
        @Part("vehicleNumber") vehicleNumber: RequestBody,
        @Part("idCardNumber") idCardNumber: RequestBody,
        @Part("message") message: RequestBody?,
        @Part idCardFront: MultipartBody.Part,
        @Part idCardBack: MultipartBody.Part,
        @Part driverLicense: MultipartBody.Part
    ): Response<ApplicationApiResponse>

    // Cancel application
    @DELETE("shipper-applications/{id}")
    suspend fun cancelApplication(@Path("id") id: String): Response<Unit>
}
