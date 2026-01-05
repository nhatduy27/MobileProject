package com.example.foodapp.data.api

import com.example.foodapp.data.model.OTP.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface OtpApiService {
    @POST("otp/send")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<ApiResponse<SendOtpResponse>>

    @POST("otp/verify")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiResponse<VerifyOtpResponse>>

    @GET("otp/check-verification")
    suspend fun checkVerification(@Query("email") email: String): Response<ApiResponse<VerificationStatusResponse>>
}

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:3000/api/" // Cho Android emulator

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val otpApiService: OtpApiService by lazy {
        retrofit.create(OtpApiService::class.java)
    }
}