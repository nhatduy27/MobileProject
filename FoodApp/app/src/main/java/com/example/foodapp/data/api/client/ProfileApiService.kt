package com.example.foodapp.data.api.client

import androidx.compose.runtime.Updater
import com.example.foodapp.data.model.client.profile.*
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.GET

interface ProfileApiService {

    @GET("me")
    suspend fun getMe(): Response<ResponseBody>

    @PUT("me")
    suspend fun updateMe(@Body request: UpdateProfileRequest): Response<UpdateProfileResponse>

}