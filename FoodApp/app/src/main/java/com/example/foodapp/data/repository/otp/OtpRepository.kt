package com.example.foodapp.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.example.foodapp.data.api.ApiClient
import com.example.foodapp.data.model.shared.otp.*


class OtpRepository {
    private val apiService = ApiClient.otpApiService

    suspend fun sendOtp(email: String): ApiResult<SendOtpResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.sendOtp(SendOtpRequest(email))

                if (response.isSuccessful && response.body()?.success == true) {
                    ApiResult.Success(response.body()?.data!!)
                } else {
                    val errorMessage = response.body()?.message ?:
                    response.body()?.error ?:
                    "Failed to send OTP"
                    ApiResult.Failure(Exception(errorMessage))
                }
            }
        } catch (e: HttpException) {
            ApiResult.Failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            ApiResult.Failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            ApiResult.Failure(Exception("Unexpected error: ${e.message}"))
        }
    }

    suspend fun verifyOtp(email: String, otp: String, type : OTPType): ApiResult<VerifyOtpResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.verifyOtp(VerifyOtpRequest(email, otp, type))

                if (response.isSuccessful && response.body()?.success == true) {
                    ApiResult.Success(response.body()?.data!!)
                } else {
                    val errorMessage = response.body()?.message ?:
                    response.body()?.error ?:
                    "Failed to verify OTP"
                    ApiResult.Failure(Exception(errorMessage))
                }
            }
        } catch (e: HttpException) {
            ApiResult.Failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            ApiResult.Failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            ApiResult.Failure(Exception("Unexpected error: ${e.message}"))
        }
    }


    suspend fun sendOtpResetPassword(email: String): ApiResult<SendOtpResetPasswordResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.sendOtpResetPassword(SendOtpResetPasswordRequest(email))

                if (response.isSuccessful && response.body()?.success == true) {
                    ApiResult.Success(response.body()?.data!!)
                } else {
                    val errorMessage = response.body()?.message ?:
                    response.body()?.error ?:
                    "Failed to send OTP"
                    ApiResult.Failure(Exception(errorMessage))
                }
            }
        } catch (e: HttpException) {
            ApiResult.Failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            ApiResult.Failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            ApiResult.Failure(Exception("Unexpected error: ${e.message}"))
        }
    }


}