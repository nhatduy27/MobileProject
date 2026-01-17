// data/repository/client/category/CategoryRepository.kt
package com.example.foodapp.data.repository.shared

import com.example.foodapp.data.remote.shared.response.ApiResult
import com.example.foodapp.data.model.shared.category.Category
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.shared.response.CategoryApiResponse
import com.example.foodapp.data.remote.shared.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class CategoryRepository() {

    private val categoryService = ApiClient.categoryApiService

    suspend fun getCategories(): ApiResult<List<Category>> {
        return try {
            withContext(Dispatchers.IO) {
                val response: CategoryApiResponse = categoryService.getCategories()

                println("DEBUG: [CategoryRepository] API Response: success=${response.success}")

                // Log chi tiết để debug - ĐÃ SỬA
                println("DEBUG: [CategoryRepository] Has data: ${response.data != null}")
                println("DEBUG: [CategoryRepository] Raw data count: ${response.data?.size ?: 0}")

                if (response.success) {
                    // Lấy list categories trực tiếp từ response.data
                    val categoryDtos: List<CategoryDto> = response.data ?: emptyList()

                    println("DEBUG: [CategoryRepository] Raw DTOs count: ${categoryDtos.size}")

                    // Log chi tiết raw data
                    categoryDtos.forEachIndexed { index, dto ->
                        println("DEBUG: [CategoryRepository] Raw DTO ${index + 1}: id=${dto.id}, name=${dto.name}, status=${dto.status}")
                    }

                    // Convert DTOs sang Models
                    val categories = mutableListOf<Category>()

                    for (dto: CategoryDto in categoryDtos) {
                        try {
                            val category = dto.toCategory()
                            categories.add(category)
                            println("DEBUG: [CategoryRepository] Converted: ${category.name} (id: ${category.id}, active: ${category.isActive})")
                        } catch (e: Exception) {
                            println("DEBUG: [CategoryRepository] Failed to convert dto ${dto.id}: ${e.message}")
                        }
                    }

                    // Lọc chỉ lấy categories active
                    val activeCategories = categories.filter { it.isActive }

                    println("DEBUG: [CategoryRepository] Active categories: ${activeCategories.size}/${categories.size}")

                    // Log tên các categories active
                    activeCategories.forEachIndexed { index, category ->
                        println("DEBUG: [CategoryRepository] Active category ${index + 1}: ${category.name}")
                    }

                    if (activeCategories.isNotEmpty()) {
                        ApiResult.Success(activeCategories)
                    } else {
                        println("DEBUG: [CategoryRepository] No active categories found")
                        ApiResult.Failure(Exception("Không có danh mục active nào"))
                    }
                } else {
                    val errorMessage = response.message ?: "Failed to load categories"
                    println("DEBUG: [CategoryRepository] API response error: $errorMessage")
                    ApiResult.Failure(Exception(errorMessage))
                }
            }
        } catch (e: HttpException) {
            println("DEBUG: [CategoryRepository] HttpException: ${e.code()} - ${e.message()}")
            ApiResult.Failure(Exception("Lỗi server: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            println("DEBUG: [CategoryRepository] IOException: ${e.message}")
            ApiResult.Failure(Exception("Lỗi kết nối mạng: ${e.message}"))
        } catch (e: Exception) {
            println("DEBUG: [CategoryRepository] Exception: ${e.message}")
            e.printStackTrace()
            ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
        }
    }
}