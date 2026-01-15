package com.example.foodapp.data.remote.client

import com.example.foodapp.data.remote.client.response.profile.CreateAddressRequest
import com.example.foodapp.data.remote.client.response.profile.CreateAddressResponse
import com.example.foodapp.data.remote.client.response.profile.UpdateProfileRequest
import com.example.foodapp.data.remote.client.response.profile.UpdateProfileResponse
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.GET
import retrofit2.http.POST

interface ProfileApiService {

    @GET("me")
    suspend fun getMe(): Response<ResponseBody>

    @PUT("me")
    suspend fun updateMe(@Body request: UpdateProfileRequest): Response<UpdateProfileResponse>

    @POST("me/addresses")
    suspend fun createAddress(@Body request: CreateAddressRequest): Response<CreateAddressResponse>

}