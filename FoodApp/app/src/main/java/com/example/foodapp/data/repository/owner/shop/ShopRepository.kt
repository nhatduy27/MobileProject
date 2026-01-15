package com.example.foodapp.data.repository.owner.shop

import android.content.Context
import android.net.Uri
import com.example.foodapp.data.model.owner.CreateShopRequest
import com.example.foodapp.data.model.owner.Shop
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.owner.ShopApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * Repository để quản lý thông tin shop
 */
class ShopRepository(private val context: Context) {
    
    private val apiService: ShopApiService = ApiClient.createService(ShopApiService::class.java)
    
    /**
     * Tạo shop mới (JSON - không có ảnh)
     */
    suspend fun createShop(request: CreateShopRequest): Result<Shop> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createShop(request)
                if (response.isSuccessful && response.body() != null) {
                    val shopResponse = response.body()!!
                    if (shopResponse.success) {
                        Result.success(shopResponse.data)
                    } else {
                        Result.failure(Exception("Tạo shop thất bại"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception(errorBody ?: "Lỗi không xác định"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Tạo shop mới với ảnh (Multipart)
     */
    suspend fun createShopWithImages(
        name: String,
        description: String,
        address: String,
        phone: String,
        openTime: String,
        closeTime: String,
        shipFeePerOrder: Int,
        minOrderAmount: Int,
        coverImageUri: Uri,
        logoUri: Uri
    ): Result<Shop> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert text fields to RequestBody
                val namePart = name.toRequestBody("text/plain".toMediaType())
                val descriptionPart = description.toRequestBody("text/plain".toMediaType())
                val addressPart = address.toRequestBody("text/plain".toMediaType())
                val phonePart = phone.toRequestBody("text/plain".toMediaType())
                val openTimePart = openTime.toRequestBody("text/plain".toMediaType())
                val closeTimePart = closeTime.toRequestBody("text/plain".toMediaType())
                val shipFeePart = shipFeePerOrder.toString().toRequestBody("text/plain".toMediaType())
                val minOrderPart = minOrderAmount.toString().toRequestBody("text/plain".toMediaType())
                
                // Convert URIs to MultipartBody.Part
                val coverImagePart = uriToMultipartPart(coverImageUri, "coverImage")
                val logoPart = uriToMultipartPart(logoUri, "logo")
                
                val response = apiService.createShopWithImages(
                    name = namePart,
                    description = descriptionPart,
                    address = addressPart,
                    phone = phonePart,
                    openTime = openTimePart,
                    closeTime = closeTimePart,
                    shipFeePerOrder = shipFeePart,
                    minOrderAmount = minOrderPart,
                    coverImage = coverImagePart,
                    logo = logoPart
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val shopResponse = response.body()!!
                    if (shopResponse.success) {
                        Result.success(shopResponse.data)
                    } else {
                        Result.failure(Exception("Tạo shop thất bại"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception(errorBody ?: "Lỗi không xác định"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Convert URI to MultipartBody.Part
     */
    private fun uriToMultipartPart(uri: Uri, partName: String): MultipartBody.Part {
        val contentResolver = context.contentResolver
        
        // Get MIME type from URI
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        
        // Determine file extension based on MIME type
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/jpg", "image/jpeg" -> "jpg"
            else -> "jpg"
        }
        
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw Exception("Không thể đọc file")
        
        // Create temp file with correct extension
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.$extension")
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        
        // Create request body with correct MIME type
        val requestBody = tempFile.asRequestBody(mimeType.toMediaType())
        return MultipartBody.Part.createFormData(partName, tempFile.name, requestBody)
    }
    
    /**
     * Lấy thông tin shop của owner hiện tại
     */
    suspend fun getMyShop(): Result<Shop> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyShop()
                if (response.isSuccessful && response.body() != null) {
                    val shopResponse = response.body()!!
                    if (shopResponse.success) {
                        Result.success(shopResponse.data)
                    } else {
                        Result.failure(Exception("Không thể lấy thông tin shop"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception(errorBody ?: "Không tìm thấy shop"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Cập nhật thông tin shop
     */
    suspend fun updateShop(request: CreateShopRequest): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateShop(request)
                if (response.isSuccessful) {
                    Result.success("Cập nhật shop thành công")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception(errorBody ?: "Cập nhật thất bại"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Cập nhật thông tin shop với ảnh (Multipart)
     * Tất cả fields đều optional
     */
    suspend fun updateShopWithImages(
        name: String? = null,
        description: String? = null,
        address: String? = null,
        phone: String? = null,
        openTime: String? = null,
        closeTime: String? = null,
        shipFeePerOrder: Int? = null,
        minOrderAmount: Int? = null,
        coverImageUri: Uri? = null,
        logoUri: Uri? = null
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert text fields to RequestBody (only if not null)
                val namePart = name?.toRequestBody("text/plain".toMediaType())
                val descriptionPart = description?.toRequestBody("text/plain".toMediaType())
                val addressPart = address?.toRequestBody("text/plain".toMediaType())
                val phonePart = phone?.toRequestBody("text/plain".toMediaType())
                val openTimePart = openTime?.toRequestBody("text/plain".toMediaType())
                val closeTimePart = closeTime?.toRequestBody("text/plain".toMediaType())
                val shipFeePart = shipFeePerOrder?.toString()?.toRequestBody("text/plain".toMediaType())
                val minOrderPart = minOrderAmount?.toString()?.toRequestBody("text/plain".toMediaType())
                
                // Convert URIs to MultipartBody.Part (only if not null)
                val coverImagePart = coverImageUri?.let { uriToMultipartPart(it, "coverImage") }
                val logoPart = logoUri?.let { uriToMultipartPart(it, "logo") }
                
                val response = apiService.updateShopWithImages(
                    name = namePart,
                    description = descriptionPart,
                    address = addressPart,
                    phone = phonePart,
                    openTime = openTimePart,
                    closeTime = closeTimePart,
                    shipFeePerOrder = shipFeePart,
                    minOrderAmount = minOrderPart,
                    coverImage = coverImagePart,
                    logo = logoPart
                )
                
                if (response.isSuccessful) {
                    Result.success("Cập nhật shop thành công")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception(errorBody ?: "Cập nhật thất bại"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
