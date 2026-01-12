package com.example.foodapp.data.api.client

import com.example.foodapp.data.model.client.profile.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ProfileApiService {

    @POST("me")
    suspend fun getMe(): Response<GetProfileResponse>

}