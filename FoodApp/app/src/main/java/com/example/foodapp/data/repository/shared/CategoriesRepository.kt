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

                // Log chi tiết để debug
                println("DEBUG: [CategoryRepository] Has data wrapper: ${response.data != null}")
                println("DEBUG: [CategoryRepository] Wrapper success: ${response.data?.success}")
                println("DEBUG: [CategoryRepository] Inner data count: ${response.data?.data?.size ?: 0}")

                if (response.success) {
                    // Lấy wrapper từ response
                    val wrapper = response.data

                    if (wrapper == null) {
                        println("DEBUG: [CategoryRepository] Wrapper is null")
                        return@withContext ApiResult.Failure(Exception("Không có dữ liệu trả về"))
                    }

                    // Kiểm tra success của wrapper (nếu có)
                    val isWrapperSuccess = wrapper.success ?: true

                    if (!isWrapperSuccess) {
                        val errorMessage = wrapper.message ?: "Failed to load categories"
                        println("DEBUG: [CategoryRepository] Inner wrapper error: $errorMessage")
                        return@withContext ApiResult.Failure(Exception(errorMessage))
                    }

                    // Lấy list categories từ wrapper.data
                    val categoryDtos: List<CategoryDto> = wrapper.data ?: emptyList()

                    println("DEBUG: [CategoryRepository] Raw DTOs count: ${categoryDtos.size}")

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
                    println("DEBUG: [CategoryRepository] Outer response error: $errorMessage")
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