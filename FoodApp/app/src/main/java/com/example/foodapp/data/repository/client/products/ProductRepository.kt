// ProductRepository.kt - SỬA IMPORT
package com.example.foodapp.data.repository.product

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.example.foodapp.data.remote.client.ProductApiService
import com.example.foodapp.data.model.client.product.ProductFilterDto
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.model.client.product.ApiResult
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val productService: ProductApiService
) {

    suspend fun getProducts(filters: ProductFilterDto = ProductFilterDto()): ApiResult<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {

                val queryMap = filters.toQueryMap()

                val apiResponse = productService.getProducts(queryMap)

                if (apiResponse.success) {
                    val productListData = apiResponse.data
                    if (productListData != null) {
                        val products = productListData.products.map { it.toProduct() }

                        if (products.isNotEmpty()) {
                            products.take(3).forEachIndexed { index, product ->
                                println("Product $index: ${product.name} - ${product.price}")
                            }
                        }

                        // Trả về ApiResult.Success với kiểu cụ thể
                        ApiResult.Success(products)
                    } else {
                        ApiResult.Success(emptyList())
                    }
                } else {
                    ApiResult.Failure(
                        Exception(apiResponse.message ?: "API request failed")
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi kết nối mạng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [ProductRepository] HttpException: ${e.code()}: ${e.message}")
                ApiResult.Failure(
                    Exception("Lỗi server ${e.code()}: ${e.message}")
                )
            } catch (e: Exception) {
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi không xác định: ${e.message}")
                )
            }
        }
    }
}