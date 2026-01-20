package com.example.foodapp.data.remote.api

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.example.foodapp.data.remote.shared.AuthApiService
import com.example.foodapp.data.remote.shared.OtpApiService
import com.example.foodapp.data.remote.client.ProfileApiService
import com.example.foodapp.data.remote.client.ProductApiService
import com.example.foodapp.data.remote.client.CartApiService
import  com.example.foodapp.data.remote.shared.CategoryService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        Log.d("ApiClient", "✅ Đã khởi tạo với context")
    }

    private fun getToken(): String? {
        return try {
            val context = appContext ?: throw IllegalStateException("Context chưa được init")

            val sharedPref = context.getSharedPreferences("auth", MODE_PRIVATE)

            // Debug: In tất cả keys trong auth
            val allEntries = sharedPref.all
            Log.d("ApiClient", "🔍 All entries in 'auth' SharedPreferences:")
            allEntries.forEach { (key, value) ->
                Log.d("ApiClient", "   $key = ${value.toString().take(20)}...")
            }

            // Tìm token
            val token = sharedPref.getString("firebase_id_token", null)

            if (token == null) {
                Log.w("ApiClient", "⚠ Không tìm thấy token với key 'firebase_id_token'")
            } else {
                Log.d("ApiClient", "✅ Found token: ${token.take(10)}...")
            }

            token
        } catch (e: Exception) {
            Log.e("ApiClient", "❌ Lỗi khi lấy token: ${e.message}")
            null
        }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")

                // Lấy token
                val token = getToken()

                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                    Log.d("ApiClient", "Đã thêm Authorization header")
                } else {
                    Log.w("ApiClient", "Không có token để thêm vào header")
                }

                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }

    val otpApiService: OtpApiService by lazy { retrofit.create(OtpApiService::class.java) }
    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val profileApiService: ProfileApiService by lazy { retrofit.create(ProfileApiService::class.java) }
    val productApiService: ProductApiService by lazy { retrofit.create(ProductApiService::class.java) }

    val categoryApiService: CategoryService by lazy {
        retrofit.create(CategoryService::class.java)
    }

    val cartApiService: CartApiService by lazy { retrofit.create(CartApiService::class.java) }
}