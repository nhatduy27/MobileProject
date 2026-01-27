package com.example.foodapp.data.remote.client

import com.example.foodapp.data.remote.client.response.profile.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ProfileApiService {

    @GET("me")
    suspend fun getMe(): Response<ProfileResponse>

    @PUT("me")
    suspend fun updateMe(@Body request: UpdateProfileRequest): Response<UpdateProfileResponse>

    @POST("me/addresses")
    suspend fun createAddress(@Body request: CreateAddressRequest): Response<CreateAddressResponse>

    @GET("me/addresses")
    suspend fun getAddresses(): Response<GetAddressesResponse>

    @PUT("me/addresses/{id}")
    suspend fun updateAddress(
        @Path("id") addressId: String,
        @Body request: UpdateAddressRequest
    ): Response<UpdateAddressResponse>

    @DELETE("me/addresses/{id}")
    suspend fun deleteAddress(
        @Path("id") addressId: String
    ): Response<DeleteAddressResponse>

    @PUT("me/addresses/{id}/default")
    suspend fun setDefaultAddress(
        @Path("id") addressId: String
    ): Response<SetDefaultAddressResponse>

    @Multipart
    @POST("me/avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<UploadAvatarResponse>
}