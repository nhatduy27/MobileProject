package com.example.foodapp.data.repository.owner.products

import android.util.Log
import com.example.foodapp.data.model.owner.product.*
import com.example.foodapp.data.remote.owner.ProductApiService
import com.example.foodapp.data.repository.owner.base.OwnerProductRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Real implementation của OwnerProductRepository
 * Gọi API thực sự từ backend
 * 
 * Hỗ trợ nhiều ảnh thay vì chỉ 1 ảnh.
 */
class RealProductRepository(
    private val apiService: ProductApiService
) : OwnerProductRepository {

    companion object {
        private const val TAG = "RealProductRepository"
    }

    override suspend fun getProducts(
        categoryId: String?,
        isAvailable: Boolean?,
        page: Int,
        limit: Int
    ): Result<ProductsData> {
        return try {
            Log.d(TAG, "🔄 Fetching products: categoryId=$categoryId, isAvailable=$isAvailable, page=$page")

            val response = apiService.getMyProducts(
                categoryId = categoryId,
                isAvailable = isAvailable,
                page = page,
                limit = limit
            )

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                Log.d(TAG, "✅ Got ${data.products.size} products (total: ${data.total})")
                Result.success(data)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "❌ Error fetching products: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching products", e)
            Result.failure(e)
        }
    }

    override suspend fun getProductDetail(productId: String): Result<Product> {
        return try {
            Log.d(TAG, "🔄 Fetching product detail: $productId")

            val response = apiService.getProductDetail(productId)

            if (response.isSuccessful && response.body() != null) {
                val product = response.body()!!.data
                Log.d(TAG, "✅ Got product: ${product.name}")
                Result.success(product)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "❌ Error fetching product detail: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching product detail", e)
            Result.failure(e)
        }
    }

    override suspend fun createProduct(
        request: CreateProductRequest,
        imageFiles: List<File>
    ): Result<Product> {
        return try {
            Log.d(TAG, "🔄 Creating product: ${request.name} with ${imageFiles.size} images")

            val nameBody = request.name.toRequestBody("text/plain".toMediaType())
            val descBody = request.description.toRequestBody("text/plain".toMediaType())
            val priceBody = request.price.toString().toRequestBody("text/plain".toMediaType())
            val categoryBody = request.categoryId.toRequestBody("text/plain".toMediaType())
            val prepTimeBody = request.preparationTime.toString().toRequestBody("text/plain".toMediaType())

            val imageParts = imageFiles.mapIndexed { index, file -> 
                createImagePart(file, "images")
            }

            val response = apiService.createProduct(
                name = nameBody,
                description = descBody,
                price = priceBody,
                categoryId = categoryBody,
                preparationTime = prepTimeBody,
                images = imageParts
            )

            if (response.isSuccessful && response.body() != null) {
                val product = response.body()!!.data
                Log.d(TAG, "✅ Created product: ${product.name} (${product.id})")
                Result.success(product)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "❌ Error creating product: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception creating product", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(
        productId: String,
        request: UpdateProductRequest,
        imageFiles: List<File>?
    ): Result<String> {
        return try {
            Log.d(TAG, "🔄 Updating product: $productId with ${imageFiles?.size ?: 0} images")

            val nameBody = request.name?.toRequestBody("text/plain".toMediaType())
            val descBody = request.description?.toRequestBody("text/plain".toMediaType())
            val priceBody = request.price?.toString()?.toRequestBody("text/plain".toMediaType())
            val categoryBody = request.categoryId?.toRequestBody("text/plain".toMediaType())
            val prepTimeBody = request.preparationTime?.toString()?.toRequestBody("text/plain".toMediaType())

            // Use different API calls based on whether images are provided
            val response = if (imageFiles != null && imageFiles.isNotEmpty()) {
                val imageParts = imageFiles.map { file -> 
                    createImagePart(file, "images")
                }
                apiService.updateProductWithImages(
                    productId = productId,
                    name = nameBody,
                    description = descBody,
                    price = priceBody,
                    categoryId = categoryBody,
                    preparationTime = prepTimeBody,
                    images = imageParts
                )
            } else {
                apiService.updateProductWithoutImages(
                    productId = productId,
                    name = nameBody,
                    description = descBody,
                    price = priceBody,
                    categoryId = categoryBody,
                    preparationTime = prepTimeBody
                )
            }

            if (response.isSuccessful && response.body() != null) {
                val message = response.body()!!.message
                Log.d(TAG, "✅ Updated product: $message")
                Result.success(message)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "❌ Error updating product: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception updating product", e)
            Result.failure(e)
        }
    }

    override suspend fun toggleAvailability(
        productId: String,
        isAvailable: Boolean
    ): Result<String> {
        return try {
            Log.d(TAG, "🔄 Toggling availability: $productId -> $isAvailable")

            val response = apiService.toggleAvailability(
                productId = productId,
                request = ToggleAvailabilityRequest(isAvailable)
            )

            if (response.isSuccessful && response.body() != null) {
                val message = response.body()!!.message
                Log.d(TAG, "✅ Toggled availability: $message")
                Result.success(message)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "❌ Error toggling availability: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception toggling availability", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(productId: String): Result<String> {
        return try {
            Log.d(TAG, "🔄 Deleting product: $productId")

            val response = apiService.deleteProduct(productId)

            if (response.isSuccessful && response.body() != null) {
                val message = response.body()!!.message
                Log.d(TAG, "✅ Deleted product: $message")
                Result.success(message)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "❌ Error deleting product: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception deleting product", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadProductImages(
        productId: String,
        imageFiles: List<File>
    ): Result<List<String>> {
        return try {
            Log.d(TAG, "🔄 Uploading ${imageFiles.size} images for product: $productId")

            val imageParts = imageFiles.map { file -> 
                createImagePart(file, "images")
            }
            val response = apiService.uploadProductImages(productId, imageParts)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val imageUrls = body.data?.imageUrls ?: emptyList()
                Log.d(TAG, "✅ Uploaded ${imageUrls.size} images")
                Result.success(imageUrls)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "❌ Error uploading images: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception uploading images", e)
            Result.failure(e)
        }
    }

    /**
     * Helper function to create MultipartBody.Part from File
     * @param file File ảnh
     * @param fieldName Tên field trong multipart (images cho nhiều ảnh)
     */
    private fun createImagePart(file: File, fieldName: String = "images"): MultipartBody.Part {
        val mediaType = when {
            file.name.endsWith(".png", ignoreCase = true) -> "image/png"
            file.name.endsWith(".jpg", ignoreCase = true) -> "image/jpeg"
            file.name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            else -> "image/jpeg"
        }.toMediaType()

        val requestBody = file.asRequestBody(mediaType)
        return MultipartBody.Part.createFormData(fieldName, file.name, requestBody)
    }
}
