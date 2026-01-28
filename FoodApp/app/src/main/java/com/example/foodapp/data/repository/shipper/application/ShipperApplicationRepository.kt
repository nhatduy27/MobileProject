package com.example.foodapp.data.repository.shipper.application

import android.util.Log
import com.example.foodapp.data.model.shipper.application.PaginatedShopsData
import com.example.foodapp.data.model.shipper.application.ShipperApplication
import com.example.foodapp.data.remote.shipper.ShipperApplicationApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File

interface ShipperApplicationRepository {
    suspend fun getShops(page: Int, limit: Int, search: String?): Result<PaginatedShopsData>
    suspend fun getMyApplications(): Result<List<ShipperApplication>>
    suspend fun applyShipper(
        shopId: String,
        vehicleType: String,
        vehicleNumber: String,
        idCardNumber: String,
        message: String?,
        idCardFrontFile: File,
        idCardBackFile: File,
        driverLicenseFile: File
    ): Result<ShipperApplication>
    suspend fun cancelApplication(id: String): Result<Unit>
}

class RealShipperApplicationRepository(
    private val apiService: ShipperApplicationApiService
) : ShipperApplicationRepository {

    override suspend fun getShops(page: Int, limit: Int, search: String?): Result<PaginatedShopsData> {
        return try {
            Log.d("ShipperAppRepo", "Calling getShops: page=$page, limit=$limit, search=$search")
            val response = apiService.getShops(page, limit, search)
            Log.d("ShipperAppRepo", "getShops response: code=${response.code()}, isSuccessful=${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("ShipperAppRepo", "getShops body: $body")
                
                val data = body?.data
                if (data != null) {
                    Log.d("ShipperAppRepo", "Got ${data.shops.size} shops")
                    Result.success(data)
                } else {
                    Log.w("ShipperAppRepo", "No data in response, returning empty")
                    Result.success(PaginatedShopsData())
                }
            } else {
                val errorMsg = parseErrorBody(response)
                Log.e("ShipperAppRepo", "getShops error: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("ShipperAppRepo", "getShops exception", e)
            Result.failure(e)
        }
    }

    override suspend fun getMyApplications(): Result<List<ShipperApplication>> {
        return try {
            Log.d("ShipperAppRepo", "Calling getMyApplications")
            val response = apiService.getMyApplications()
            Log.d("ShipperAppRepo", "getMyApplications response: code=${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data ?: emptyList()
                Log.d("ShipperAppRepo", "Got ${data.size} applications")
                Result.success(data)
            } else {
                Result.failure(Exception(parseErrorBody(response)))
            }
        } catch (e: Exception) {
            Log.e("ShipperAppRepo", "getMyApplications exception", e)
            Result.failure(e)
        }
    }

    override suspend fun applyShipper(
        shopId: String,
        vehicleType: String,
        vehicleNumber: String,
        idCardNumber: String,
        message: String?,
        idCardFrontFile: File,
        idCardBackFile: File,
        driverLicenseFile: File
    ): Result<ShipperApplication> {
        return try {
            val shopIdBody = shopId.toRequestBody("text/plain".toMediaTypeOrNull())
            val vehicleTypeBody = vehicleType.toRequestBody("text/plain".toMediaTypeOrNull())
            val vehicleNumberBody = vehicleNumber.toRequestBody("text/plain".toMediaTypeOrNull())
            val idCardNumberBody = idCardNumber.toRequestBody("text/plain".toMediaTypeOrNull())
            val messageBody = message?.toRequestBody("text/plain".toMediaTypeOrNull())

            val idCardFrontPart = createFilePart("idCardFront", idCardFrontFile)
            val idCardBackPart = createFilePart("idCardBack", idCardBackFile)
            val driverLicensePart = createFilePart("driverLicense", driverLicenseFile)

            val response = apiService.applyShipper(
                shopIdBody,
                vehicleTypeBody,
                vehicleNumberBody,
                idCardNumberBody,
                messageBody,
                idCardFrontPart,
                idCardBackPart,
                driverLicensePart
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Failed to submit application"))
                }
            } else {
                Result.failure(Exception(parseErrorBody(response)))
            }
        } catch (e: Exception) {
            Log.e("ShipperAppRepo", "Apply failed", e)
            Result.failure(e)
        }
    }

    override suspend fun cancelApplication(id: String): Result<Unit> {
        return try {
            val response = apiService.cancelApplication(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseErrorBody(response)))
            }
        } catch (e: Exception) {
            Log.e("ShipperAppRepo", "Cancel failed", e)
            Result.failure(e)
        }
    }

    private fun createFilePart(name: String, file: File): MultipartBody.Part {
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, file.name, requestBody)
    }

    private fun <T> parseErrorBody(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val json = JSONObject(errorBody)
                json.optString("message", "Error: ${response.code()}")
            } else {
                "Error: ${response.code()} ${response.message()}"
            }
        } catch (e: Exception) {
            "Error: ${response.code()} ${response.message()}"
        }
    }
}
