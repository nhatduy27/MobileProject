package com.example.foodapp.data.repository.client.products

import com.example.foodapp.data.remote.client.response.product.FavoriteResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.example.foodapp.data.remote.client.ProductApiService
import com.example.foodapp.data.remote.client.response.product.ProductFilterDto
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.remote.client.response.product.ApiResult
import com.example.foodapp.data.remote.client.response.product.FavoriteProductsApiResponse
import com.example.foodapp.data.remote.client.response.product.FavoriteQueryParams
import com.example.foodapp.data.remote.client.response.product.CheckFavoriteResponse
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

    // THÊM HÀM LẤY CHI TIẾT SẢN PHẨM
    suspend fun getProductDetail(productId: String): ApiResult<Product> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [ProductRepository] Getting product detail for ID: $productId")

                val apiResponse = productService.getProductDetail(productId)
                println("DEBUG: [ProductRepository] API Response success: ${apiResponse.success}")

                if (apiResponse.success && apiResponse.data != null) {
                    val product = apiResponse.data.toProduct()
                    println("DEBUG: [ProductRepository] Successfully got product: ${product.name}")
                    ApiResult.Success(product)
                } else {
                    val errorMessage = apiResponse.message ?: "Không tìm thấy sản phẩm"
                    println("DEBUG: [ProductRepository] Error: $errorMessage")
                    ApiResult.Failure(Exception(errorMessage))
                }
            } catch (e: IOException) {
                println("DEBUG: [ProductRepository] IOException: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi kết nối mạng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [ProductRepository] HttpException ${e.code()}: ${e.message}")
                val errorMsg = when (e.code()) {
                    404 -> "Không tìm thấy sản phẩm"
                    500 -> "Lỗi server"
                    else -> "Lỗi ${e.code()}: ${e.message}"
                }
                ApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [ProductRepository] Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi không xác định: ${e.message}")
                )
            }
        }
    }

    suspend fun addToFavorites(productId: String): ApiResult<FavoriteResponse> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [ProductRepository] Adding product to favorites: $productId")

                // Gọi API thêm vào favorites
                val apiResponse = productService.addToFavorites(productId)
                println("DEBUG: [ProductRepository] Add favorite API Response success: ${apiResponse.success}")

                if (apiResponse.success) {
                    println("DEBUG: [ProductRepository] Successfully added to favorites: ${apiResponse.message}")
                    ApiResult.Success(apiResponse)
                } else {
                    val errorMessage = apiResponse.message ?: "Không thể thêm vào yêu thích"
                    println("DEBUG: [ProductRepository] Error: $errorMessage")
                    ApiResult.Failure(Exception(errorMessage))
                }
            } catch (e: IOException) {
                println("DEBUG: [ProductRepository] IOException: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi kết nối mạng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [ProductRepository] HttpException ${e.code()}: ${e.message}")
                val errorMsg = when (e.code()) {
                    409 -> "Sản phẩm đã có trong danh sách yêu thích"
                    401 -> "Vui lòng đăng nhập để thêm vào yêu thích"
                    404 -> "Không tìm thấy sản phẩm"
                    500 -> "Lỗi server"
                    else -> "Lỗi ${e.code()}: ${e.message}"
                }
                ApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [ProductRepository] Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi không xác định: ${e.message}")
                )
            }
        }
    }


    suspend fun removeFromFavorites(productId: String): ApiResult<FavoriteResponse> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [ProductRepository] Removing product from favorites: $productId")

                // Gọi API xóa khỏi favorites
                val apiResponse = productService.deleteFavorites(productId)
                println("DEBUG: [ProductRepository] Remove favorite API Response success: ${apiResponse.success}")

                if (apiResponse.success) {
                    println("DEBUG: [ProductRepository] Successfully removed from favorites: ${apiResponse.message}")
                    ApiResult.Success(apiResponse)
                } else {
                    val errorMessage = apiResponse.message ?: "Không thể xóa khỏi yêu thích"
                    println("DEBUG: [ProductRepository] Error: $errorMessage")
                    ApiResult.Failure(Exception(errorMessage))
                }
            } catch (e: IOException) {
                println("DEBUG: [ProductRepository] IOException: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi kết nối mạng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [ProductRepository] HttpException ${e.code()}: ${e.message}")
                val errorMsg = when (e.code()) {
                    404 -> "Sản phẩm không có trong danh sách yêu thích"
                    401 -> "Vui lòng đăng nhập để thực hiện thao tác"
                    500 -> "Lỗi server"
                    else -> "Lỗi ${e.code()}: ${e.message}"
                }
                ApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [ProductRepository] Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi không xác định: ${e.message}")
                )
            }
        }
    }

    // ============== SỬA CÁC HÀM LẤY DANH SÁCH YÊU THÍCH ==============

    /**
     * Lấy danh sách sản phẩm yêu thích với phân trang
     * @param page Trang hiện tại (bắt đầu từ 1)
     * @param limit Số lượng sản phẩm mỗi trang
     */
    suspend fun getFavoriteProducts(
        page: Int = 1,
        limit: Int = 20
    ): ApiResult<FavoriteProductsApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [ProductRepository] Getting favorite products - page: $page, limit: $limit")

                // Gọi API lấy danh sách yêu thích
                val apiResponse = productService.getFavoriteProducts(page, limit)
                println("DEBUG: [ProductRepository] Get favorites API Response success: ${apiResponse.success}")

                if (apiResponse.isValid) {
                    val productCount = apiResponse.data?.data?.size ?: 0
                    println("DEBUG: [ProductRepository] Successfully got $productCount favorite products")
                    ApiResult.Success(apiResponse)
                } else {
                    // Nếu API trả về success=true nhưng không có data, vẫn trả về response
                    if (apiResponse.success) {
                        println("DEBUG: [ProductRepository] No favorite products found")
                        ApiResult.Success(apiResponse)
                    } else {
                        val errorMessage = apiResponse.message ?: "Không thể lấy danh sách yêu thích"
                        println("DEBUG: [ProductRepository] Error: $errorMessage")
                        ApiResult.Failure(Exception(errorMessage))
                    }
                }
            } catch (e: IOException) {
                println("DEBUG: [ProductRepository] IOException: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi kết nối mạng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [ProductRepository] HttpException ${e.code()}: ${e.message}")
                val errorMsg = when (e.code()) {
                    401 -> "Vui lòng đăng nhập để xem danh sách yêu thích"
                    403 -> "Không có quyền truy cập"
                    404 -> "Không tìm thấy danh sách yêu thích"
                    500 -> "Lỗi server"
                    else -> "Lỗi ${e.code()}: ${e.message}"
                }
                ApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [ProductRepository] Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi không xác định: ${e.message}")
                )
            }
        }
    }

    /**
     * Lấy danh sách sản phẩm yêu thích dưới dạng danh sách Product
     * @param params Đối tượng chứa thông tin phân trang
     */
    suspend fun getFavoriteProducts(
        params: FavoriteQueryParams
    ): ApiResult<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [ProductRepository] Getting favorite products with params: page=${params.page}, limit=${params.limit}")

                val apiResponse = productService.getFavoriteProducts(params.page, params.limit)

                if (apiResponse.isValid) {
                    // Chuyển đổi sang list Product
                    val products = if (apiResponse.data != null && apiResponse.data!!.data != null) {
                        apiResponse.data!!.data.map { it.toProduct() }
                    } else {
                        emptyList()
                    }
                    println("DEBUG: [ProductRepository] Successfully got ${products.size} favorite products")
                    ApiResult.Success(products)
                } else {
                    // Nếu API trả về success=true nhưng không có data, coi như empty list
                    if (apiResponse.success) {
                        println("DEBUG: [ProductRepository] No favorite products found (empty list)")
                        ApiResult.Success(emptyList())
                    } else {
                        val errorMessage = apiResponse.message ?: "Không thể lấy danh sách yêu thích"
                        println("DEBUG: [ProductRepository] Error: $errorMessage")
                        ApiResult.Failure(Exception(errorMessage))
                    }
                }
            } catch (e: IOException) {
                println("DEBUG: [ProductRepository] IOException: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi kết nối mạng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [ProductRepository] HttpException ${e.code()}: ${e.message}")
                val errorMsg = when (e.code()) {
                    401 -> "Vui lòng đăng nhập để xem danh sách yêu thích"
                    403 -> "Không có quyền truy cập"
                    404 -> "Không tìm thấy danh sách yêu thích"
                    500 -> "Lỗi server"
                    else -> "Lỗi ${e.code()}: ${e.message}"
                }
                ApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [ProductRepository] Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi không xác định: ${e.message}")
                )
            }
        }
    }

    /**
     * Lấy danh sách ID sản phẩm yêu thích
     * @return Danh sách ID sản phẩm yêu thích
     */
    suspend fun getFavoriteProductIds(): ApiResult<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                // Lấy trang đầu tiên với limit lớn để lấy tất cả ID
                val apiResponse = productService.getFavoriteProducts(page = 1, limit = 100)

                if (apiResponse.isValid) {
                    // Lấy productIds trực tiếp từ response
                    val productIds = if (apiResponse.data != null && apiResponse.data!!.data != null) {
                        apiResponse.data!!.data.map { it.productId }
                    } else {
                        emptyList()
                    }
                    println("DEBUG: [ProductRepository] Got ${productIds.size} favorite product IDs")
                    ApiResult.Success(productIds)
                } else {
                    // Nếu API trả về success=true nhưng không có data, trả về empty list
                    if (apiResponse.success) {
                        ApiResult.Success(emptyList())
                    } else {
                        ApiResult.Failure(Exception("Không thể lấy danh sách ID yêu thích"))
                    }
                }
            } catch (e: IOException) {
                println("DEBUG: [ProductRepository] IOException: ${e.message}")
                ApiResult.Failure(
                    Exception("Lỗi kết nối mạng: ${e.message}")
                )
            } catch (e: HttpException) {
                if (e.code() == 401 || e.code() == 403) {
                    // User chưa đăng nhập, trả về danh sách rỗng
                    ApiResult.Success(emptyList())
                } else {
                    ApiResult.Failure(Exception("Lỗi ${e.code()}: ${e.message}"))
                }
            } catch (e: Exception) {
                println("DEBUG: [ProductRepository] Exception: ${e.message}")
                ApiResult.Failure(
                    Exception("Lỗi không xác định: ${e.message}")
                )
            }
        }
    }

    /**
     * Kiểm tra sản phẩm có trong danh sách yêu thích không (Dùng danh sách local)
     * @param productId ID sản phẩm cần kiểm tra
     */
    suspend fun isProductFavorite(productId: String): ApiResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Lấy danh sách ID yêu thích
                val result = getFavoriteProductIds()

                when (result) {
                    is ApiResult.Success -> {
                        val isFavorite = result.data.contains(productId)
                        println("DEBUG: [ProductRepository] isProductFavorite: $productId = $isFavorite")
                        ApiResult.Success(isFavorite)
                    }
                    is ApiResult.Failure -> {
                        ApiResult.Failure(result.exception)
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: [ProductRepository] Exception in isProductFavorite: ${e.message}")
                ApiResult.Failure(
                    Exception("Lỗi kiểm tra yêu thích: ${e.message}")
                )
            }
        }
    }

    /**
     * Kiểm tra sản phẩm có trong danh sách yêu thích không (Dùng API check trực tiếp)
     * @param productId ID sản phẩm cần kiểm tra
     */
    suspend fun checkIsFavorite(productId: String): ApiResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [ProductRepository] Checking if product is favorite (API): $productId")

                // Gọi API check favorite
                val apiResponse = productService.checkIsFavorite(productId)

                if (apiResponse.success) {
                    // Sử dụng computed property isFavorited từ CheckFavoriteResponse
                    val isFavorited = apiResponse.isFavorited
                    println("DEBUG: [ProductRepository] API Check Result: Product $productId is favorite: $isFavorited")

                    ApiResult.Success(isFavorited)
                } else {
                    val errorMessage = apiResponse.message ?: "Không thể kiểm tra trạng thái yêu thích"
                    println("DEBUG: [ProductRepository] Error: $errorMessage")
                    ApiResult.Failure(Exception(errorMessage))
                }
            } catch (e: IOException) {
                println("DEBUG: [ProductRepository] IOException: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi kết nối mạng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [ProductRepository] HttpException ${e.code()}: ${e.message}")
                val errorMsg = when (e.code()) {
                    401 -> "Vui lòng đăng nhập để kiểm tra trạng thái yêu thích"
                    404 -> "Không tìm thấy sản phẩm"
                    500 -> "Lỗi server"
                    else -> "Lỗi ${e.code()}: ${e.message}"
                }
                ApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [ProductRepository] Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi không xác định: ${e.message}")
                )
            }
        }
    }

}