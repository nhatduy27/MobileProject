package com.example.foodapp.data.remote.shared

import com.example.foodapp.data.remote.shared.response.CategoryApiResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface CategoryService {
    @GET("categories")
    suspend fun getCategories(): CategoryApiResponse
}