package com.example.foodapp.data.repository.client.cart

import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.response.cart.*
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class CartRepository {

    private val cartService = ApiClient.cartApiService
    private val gson = Gson()

    suspend fun addToCart(productId: String, quantity: Int = 1): CartApiResult<AddToCartResponse> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [CartRepository] Adding to cart - productId: $productId, quantity: $quantity")

                val request = AddToCartRequest(productId, quantity)
                val apiResponse = cartService.addToCart(request)

                println("DEBUG: [CartRepository] API Response success: ${apiResponse.success}")

                if (apiResponse.success) {
                    println("DEBUG: [CartRepository] Successfully added to cart")
                    CartApiResult.Success(apiResponse)
                } else {
                    val errorMessage = "Không thể thêm vào giỏ hàng"
                    println("DEBUG: [CartRepository] Error: $errorMessage")
                    CartApiResult.Failure(Exception(errorMessage))
                }
            } catch (e: IOException) {
                println("DEBUG: [CartRepository] IOException: ${e.message}")
                e.printStackTrace()
                CartApiResult.Failure(
                    Exception("Lỗi kết nối mạng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [CartRepository] HttpException ${e.code()}: ${e.message}")
                val errorMsg = when (e.code()) {
                    400 -> "Dữ liệu không hợp lệ"
                    401 -> "Vui lòng đăng nhập để thêm vào giỏ hàng"
                    404 -> "Không tìm thấy sản phẩm"
                    409 -> "Sản phẩm đã hết hàng"
                    500 -> "Lỗi server"
                    else -> "Lỗi ${e.code()}: ${e.message}"
                }
                CartApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [CartRepository] Exception: ${e.message}")
                e.printStackTrace()
                CartApiResult.Failure(
                    Exception("Lỗi không xác định: ${e.message}")
                )
            }
        }
    }

    suspend fun getCart(): CartApiResult<GetCartResponse> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [CartRepository] Getting cart data...")

                // Gọi API KHÔNG có tham số phân trang
                val apiResponse = cartService.getCart()

                println("DEBUG: [CartRepository] Get cart API Response success: ${apiResponse.success}")

                if (apiResponse.success) {
                    val cartData = apiResponse.data

                    // Tính toán thông tin
                    val totalItems = cartData.groups.flatMap { it.items }.sumOf { it.quantity }
                    val uniqueProducts = cartData.groups.flatMap { it.items }.size

                    // Log thông tin phân trang từ response
                    println("""
                    DEBUG: [CartRepository] Cart pagination info (from backend):
                    - Current Page: ${cartData.page}
                    - Items per page: ${cartData.limit}
                    - Total Groups: ${cartData.totalGroups}
                    - Total Pages: ${cartData.totalPages}
                    - Items in response: $totalItems items, $uniqueProducts unique products
                    - Shop groups in this page: ${cartData.groups.size}
                """.trimIndent())

                    // Kiểm tra phân trang logic
                    if (cartData.page > cartData.totalPages) {
                        println("DEBUG: [CartRepository] Warning: Current page (${cartData.page}) > total pages (${cartData.totalPages})")
                    }

                    // Kiểm tra nếu không có dữ liệu
                    if (cartData.groups.isEmpty()) {
                        println("DEBUG: [CartRepository] Cart is empty on page ${cartData.page}")
                    }

                    return@withContext CartApiResult.Success(apiResponse)
                } else {
                    val errorMessage = "Không thể lấy giỏ hàng: Response không thành công"
                    println("DEBUG: [CartRepository] Error: $errorMessage")
                    return@withContext CartApiResult.Failure(Exception(errorMessage))
                }
            } catch (e: IOException) {
                println("DEBUG: [CartRepository] IOException: ${e.message}")
                e.printStackTrace()
                return@withContext CartApiResult.Failure(
                    Exception("Lỗi kết nối mạng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [CartRepository] HttpException ${e.code()}: ${e.message}")

                // Cố gắng parse error body nếu có
                val errorResponse = try {
                    val errorBody = e.response()?.errorBody()?.string()
                    if (!errorBody.isNullOrEmpty()) {
                        val errorJson = gson.fromJson(errorBody, CartErrorResponse::class.java)
                        errorJson.message.takeIf { it.isNotEmpty() }
                    } else null
                } catch (ex: Exception) {
                    null
                }

                val errorMsg = when (e.code()) {
                    400 -> "Yêu cầu không hợp lệ"
                    401 -> "Vui lòng đăng nhập để xem giỏ hàng"
                    404 -> "Giỏ hàng trống"
                    500 -> "Lỗi server"
                    else -> errorResponse ?: "Lỗi ${e.code()}: ${e.message}"
                }
                return@withContext CartApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [CartRepository] Exception: ${e.message}")
                e.printStackTrace()
                return@withContext CartApiResult.Failure(
                    Exception("Lỗi không xác định: ${e.message}")
                )
            }
        }
    }

    // Hàm xóa 1 item khỏi giỏ hàng
    suspend fun removeItemFromCart(productId: String): CartApiResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [CartRepository] Removing item from cart - productId: $productId")

                // Gọi API để xóa item với productId trong path
                val response = cartService.removeCartItem(productId)

                println("DEBUG: [CartRepository] Remove item response code: ${response.code()}")

                if (response.isSuccessful) {
                    // 204 No Content là thành công (không có response body)
                    // Hoặc các status code 2xx khác
                    println("DEBUG: [CartRepository] Successfully removed item from cart")
                    CartApiResult.Success(Unit)
                } else {
                    // Response không thành công
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (!errorBody.isNullOrEmpty()) {
                        try {
                            // Thử parse JSON error response
                            val errorJson = gson.fromJson(errorBody, Map::class.java)
                            errorJson["message"]?.toString() ?: getDefaultRemoveItemErrorMessage(response.code())
                        } catch (e: Exception) {
                            getDefaultRemoveItemErrorMessage(response.code())
                        }
                    } else {
                        getDefaultRemoveItemErrorMessage(response.code())
                    }

                    println("DEBUG: [CartRepository] Remove item failed with status ${response.code()}: $errorMessage")

                    // Xử lý riêng cho các loại lỗi
                    when (response.code()) {
                        401 -> {
                            CartApiResult.Failure(
                                Exception("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại")
                            )
                        }
                        404 -> {
                            CartApiResult.Failure(
                                Exception("Không tìm thấy sản phẩm trong giỏ hàng")
                            )
                        }
                        else -> {
                            CartApiResult.Failure(Exception(errorMessage))
                        }
                    }
                }

            } catch (e: IOException) {
                println("DEBUG: [CartRepository] IOException while removing item: ${e.message}")
                e.printStackTrace()
                CartApiResult.Failure(
                    Exception("Lỗi kết nối mạng khi xóa sản phẩm: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [CartRepository] HttpException while removing item ${e.code()}: ${e.message}")
                val errorMsg = parseHttpExceptionForRemoveItem(e)
                CartApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [CartRepository] Exception while removing item: ${e.message}")
                e.printStackTrace()
                CartApiResult.Failure(
                    Exception("Lỗi không xác định khi xóa sản phẩm: ${e.message}")
                )
            }
        }
    }

    // Cập nhật hàm parseHttpExceptionForRemoveItem
    private fun parseHttpExceptionForRemoveItem(e: HttpException): String {
        return try {
            val responseBody = e.response()?.errorBody()?.string()
            val errorResponse = try {
                if (!responseBody.isNullOrEmpty()) {
                    // Parse generic JSON thay vì DeleteItemFromCartResponse
                    gson.fromJson(responseBody, Map::class.java)
                } else {
                    null
                }
            } catch (ex: Exception) {
                null
            }

            when (e.code()) {
                401 -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
                404 -> "Không tìm thấy sản phẩm trong giỏ hàng"
                403 -> "Bạn không có quyền xóa sản phẩm này"
                409 -> "Không thể xóa sản phẩm trong lúc đang xử lý đơn hàng"
                500 -> "Lỗi server khi xóa sản phẩm"
                else -> errorResponse?.get("message")?.toString() ?: "Lỗi ${e.code()} khi xóa sản phẩm: ${e.message}"
            }
        } catch (ex: Exception) {
            when (e.code()) {
                401 -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
                404 -> "Không tìm thấy sản phẩm trong giỏ hàng"
                else -> "Lỗi ${e.code()} khi xóa sản phẩm: ${e.message}"
            }
        }
    }

// Xóa hàm parseRemoveItemError và parseDeleteItemErrorResponse không cần thiết nữa
// Hoặc giữ lại nhưng cập nhật cho phù hợp

    // Nếu bạn muốn hàm helper để kiểm tra giỏ hàng trống
    suspend fun isCartEmpty(): Boolean {
        return when (val result = getCart()) {
            is CartApiResult.Success -> {
                result.data.data.groups.isEmpty() || result.data.data.groups.all { it.items.isEmpty() }
            }

            is CartApiResult.Failure -> true // Nếu lỗi thì coi như giỏ hàng trống
        }
    }

    // Hàm xóa toàn bộ giỏ hàng
    suspend fun clearCart(): CartApiResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [CartRepository] Clearing cart...")

                val response = cartService.clearCart()

                println("DEBUG: [CartRepository] Clear cart response code: ${response.code()}")

                when {
                    response.isSuccessful -> {
                        println("DEBUG: [CartRepository] Cart cleared successfully")
                        CartApiResult.Success(Unit)
                    }

                    else -> {
                        // Parse error response từ server
                        val errorResponse = parseClearCartError(response.errorBody()?.string(), response.code())
                        println("DEBUG: [CartRepository] Clear cart failed: $errorResponse")
                        CartApiResult.Failure(Exception(errorResponse))
                    }
                }
            } catch (e: IOException) {
                println("DEBUG: [CartRepository] IOException while clearing cart: ${e.message}")
                e.printStackTrace()
                CartApiResult.Failure(
                    Exception("Lỗi kết nối mạng khi xóa giỏ hàng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [CartRepository] HttpException while clearing cart ${e.code()}: ${e.message}")
                val errorMsg = parseHttpExceptionForClearCart(e)
                CartApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [CartRepository] Exception while clearing cart: ${e.message}")
                e.printStackTrace()
                CartApiResult.Failure(
                    Exception("Lỗi không xác định khi xóa giỏ hàng: ${e.message}")
                )
            }
        }
    }

    // Hàm cập nhật số lượng sản phẩm trong giỏ hàng
    suspend fun updateItemQuantity(productId: String, quantity: Int): CartApiResult<UpdateQuantityResponse> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [CartRepository] Updating item quantity - productId: $productId, quantity: $quantity")

                // Validate quantity
                if (quantity < 1 || quantity > 999) {
                    println("DEBUG: [CartRepository] Invalid quantity: $quantity")
                    return@withContext CartApiResult.Failure(
                        Exception("Số lượng phải từ 1 đến 999")
                    )
                }

                val request = UpdateQuantityRequest(quantity)
                val response = cartService.updateItemQuantity(productId, request)

                println("DEBUG: [CartRepository] Update quantity response code: ${response.code()}")

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        println("DEBUG: [CartRepository] Successfully updated item quantity")
                        CartApiResult.Success(apiResponse)
                    } else {
                        // Parse error from response body
                        val errorMessage = parseUpdateQuantityError(apiResponse, response.code())
                        println("DEBUG: [CartRepository] Update failed: $errorMessage")
                        CartApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    // Parse error from error body
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseUpdateQuantityErrorResponse(errorBody, response.code())
                    println("DEBUG: [CartRepository] Update failed with status ${response.code()}: $errorMessage")
                    CartApiResult.Failure(Exception(errorMessage))
                }
            } catch (e: IOException) {
                println("DEBUG: [CartRepository] IOException while updating quantity: ${e.message}")
                e.printStackTrace()
                CartApiResult.Failure(
                    Exception("Lỗi kết nối mạng khi cập nhật số lượng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [CartRepository] HttpException while updating quantity ${e.code()}: ${e.message}")
                val errorMsg = parseHttpExceptionForUpdateQuantity(e)
                CartApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [CartRepository] Exception while updating quantity: ${e.message}")
                e.printStackTrace()
                CartApiResult.Failure(
                    Exception("Lỗi không xác định khi cập nhật số lượng: ${e.message}")
                )
            }
        }
    }

    // ========== GET CART BY SHOP ==========
    suspend fun getCartByShop(shopId: String): CartApiResult<GetCartByShopResponse> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [CartRepository] Getting cart by shop - shopId: $shopId")

                val apiResponse = cartService.getCartByShop(shopId)

                println("DEBUG: [CartRepository] Get cart by shop response success: ${apiResponse.success}")

                if (apiResponse.success && apiResponse.data?.group != null) {
                    val shopGroup = apiResponse.data.group
                    val totalItems = shopGroup.items.sumOf { it.quantity }
                    val subtotal = shopGroup.subtotal

                    println("""
                    DEBUG: [CartRepository] Shop cart data retrieved:
                    - Shop: ${shopGroup.shopName} (${shopGroup.shopId})
                    - Status: ${if (shopGroup.isOpen) "Mở cửa" else "Đóng cửa"}
                    - Items: $totalItems sản phẩm
                    - Subtotal: $subtotal VND
            
                """.trimIndent())

                    CartApiResult.Success(apiResponse)
                } else {
                    // Nếu API trả về success = false nhưng vẫn có dữ liệu error trong response
                    val errorMessage = apiResponse.message ?: apiResponse.errorCode?.let {
                        when (it) {
                            "CART_401" -> "Vui lòng đăng nhập để xem giỏ hàng"
                            "CART_404" -> "Không tìm thấy shop hoặc giỏ hàng trống"
                            else -> "Lỗi $it"
                        }
                    } ?: "Không thể lấy giỏ hàng theo shop"

                    println("DEBUG: [CartRepository] Get cart by shop failed: $errorMessage")
                    CartApiResult.Failure(Exception(errorMessage))
                }
            } catch (e: IOException) {
                println("DEBUG: [CartRepository] IOException while getting cart by shop: ${e.message}")
                e.printStackTrace()
                CartApiResult.Failure(
                    Exception("Lỗi kết nối mạng khi lấy giỏ hàng theo shop: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [CartRepository] HttpException while getting cart by shop ${e.code()}: ${e.message}")
                val errorMsg = parseHttpExceptionForGetCartByShop(e)
                CartApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [CartRepository] Exception while getting cart by shop: ${e.message}")
                e.printStackTrace()
                CartApiResult.Failure(
                    Exception("Lỗi không xác định khi lấy giỏ hàng theo shop: ${e.message}")
                )
            }
        }
    }

    // Parse HttpException cho get cart by shop
    private fun parseHttpExceptionForGetCartByShop(e: HttpException): String {
        return try {
            val responseBody = e.response()?.errorBody()?.string()
            val errorResponse = try {
                if (!responseBody.isNullOrEmpty()) {
                    // Parse thành GetCartByShopResponse để lấy error details
                    gson.fromJson(responseBody, GetCartByShopResponse::class.java)
                } else {
                    null
                }
            } catch (ex: Exception) {
                null
            }

            // Ưu tiên lấy message từ response, nếu không có thì dùng default
            when {
                errorResponse?.message?.isNotEmpty() == true -> errorResponse.message!!
                errorResponse?.errorCode?.isNotEmpty() == true -> {
                    when (errorResponse.errorCode) {
                        "CART_401" -> "Vui lòng đăng nhập để xem giỏ hàng"
                        "CART_404" -> "Không tìm thấy shop hoặc giỏ hàng trống"
                        "CART_403" -> "Bạn không có quyền truy cập giỏ hàng này"
                        else -> "Lỗi ${errorResponse.errorCode}"
                    }
                }
                else -> {
                    // Dùng HTTP status code làm fallback
                    when (e.code()) {
                        400 -> "Dữ liệu không hợp lệ"
                        401 -> "Vui lòng đăng nhập để xem giỏ hàng"
                        403 -> "Bạn không có quyền truy cập giỏ hàng này"
                        404 -> "Không tìm thấy shop hoặc giỏ hàng trống"
                        500 -> "Lỗi server khi lấy giỏ hàng theo shop"
                        else -> "Lỗi ${e.code()} khi lấy giỏ hàng theo shop: ${e.message}"
                    }
                }
            }
        } catch (ex: Exception) {
            when (e.code()) {
                401 -> "Vui lòng đăng nhập để xem giỏ hàng"
                404 -> "Không tìm thấy shop hoặc giỏ hàng trống"
                else -> "Lỗi ${e.code()} khi lấy giỏ hàng theo shop: ${e.message}"
            }
        }
    }

    // Hàm helper kiểm tra shop có trong giỏ hàng không
    suspend fun hasShopInCart(shopId: String): Boolean {
        return when (val result = getCart()) {
            is CartApiResult.Success -> {
                result.data.data.groups.any { it.shopId == shopId }
            }
            is CartApiResult.Failure -> false
        }
    }

    // Hàm lấy shop group từ toàn bộ giỏ hàng
    suspend fun getShopGroupFromCart(shopId: String): CartGroup? {
        return when (val result = getCart()) {
            is CartApiResult.Success -> {
                result.data.data.groups.find { it.shopId == shopId }
            }
            is CartApiResult.Failure -> null
        }
    }


    // ========== DELETE SHOP ITEMS FROM CART ==========
    suspend fun deleteShopItems(shopId: String): CartApiResult<DeleteShopItemsData> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [CartRepository] Deleting all items from shop - shopId: $shopId")

                // Gọi API xóa toàn bộ items của shop
                val response = cartService.deleteShopItems(shopId)

                println("DEBUG: [CartRepository] Delete shop items response: success=${response.success}")

                if (response.success && response.data != null) {
                    // Thành công
                    val removedCount = response.data.removedCount
                    val remainingGroups = response.data.groups

                    println("""
                    DEBUG: [CartRepository] Successfully deleted shop items:
                    - Removed items count: $removedCount
                    - Remaining shop groups: ${remainingGroups.size}
                    - Remaining shops: ${remainingGroups.joinToString { it.shopName }}
                    """.trimIndent())

                    CartApiResult.Success(response.data)
                } else {
                    // Parse error từ response
                    val errorMessage = parseDeleteShopItemsError(response, shopId)
                    println("DEBUG: [CartRepository] Delete shop items failed: $errorMessage")
                    CartApiResult.Failure(Exception(errorMessage))
                }
            } catch (e: IOException) {
                println("DEBUG: [CartRepository] IOException while deleting shop items: ${e.message}")
                e.printStackTrace()
                CartApiResult.Failure(
                    Exception("Lỗi kết nối mạng khi xóa cửa hàng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [CartRepository] HttpException while deleting shop items ${e.code()}: ${e.message}")
                val errorMsg = parseHttpExceptionForDeleteShopItems(e, shopId)
                CartApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [CartRepository] Exception while deleting shop items: ${e.message}")
                e.printStackTrace()
                CartApiResult.Failure(
                    Exception("Lỗi không xác định khi xóa cửa hàng: ${e.message}")
                )
            }
        }
    }

    // Parse lỗi từ response khi xóa shop items
    private fun parseDeleteShopItemsError(
        response: DeleteShopItemsResponse,
        shopId: String
    ): String {
        return when {
            response.message?.isNotEmpty() == true -> response.message!!
            response.errorCode?.isNotEmpty() == true -> {
                when (response.errorCode) {
                    "CART_401" -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
                    "CART_404" -> "Không tìm thấy cửa hàng '$shopId' trong giỏ hàng"
                    "CART_403" -> "Bạn không có quyền xóa sản phẩm của cửa hàng này"
                    "CART_409" -> "Không thể xóa cửa hàng trong lúc đang xử lý đơn hàng"
                    else -> "Lỗi ${response.errorCode}"
                }
            }
            else -> "Không thể xóa cửa hàng khỏi giỏ hàng"
        }
    }

    // Parse HttpException cho delete shop items
    private fun parseHttpExceptionForDeleteShopItems(e: HttpException, shopId: String): String {
        return try {
            val responseBody = e.response()?.errorBody()?.string()
            val errorResponse = try {
                if (!responseBody.isNullOrEmpty()) {
                    gson.fromJson(responseBody, CartErrorResponse::class.java)
                } else {
                    null
                }
            } catch (ex: Exception) {
                null
            }

            when (e.code()) {
                401 -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
                404 -> "Không tìm thấy cửa hàng '$shopId' trong giỏ hàng"
                403 -> "Bạn không có quyền xóa sản phẩm của cửa hàng này"
                409 -> "Không thể xóa cửa hàng trong lúc đang xử lý đơn hàng"
                422 -> "Dữ liệu không hợp lệ"
                500 -> "Lỗi server khi xóa cửa hàng"
                else -> errorResponse?.message ?: "Lỗi ${e.code()} khi xóa cửa hàng: ${e.message}"
            }
        } catch (ex: Exception) {
            when (e.code()) {
                401 -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
                404 -> "Không tìm thấy cửa hàng '$shopId' trong giỏ hàng"
                else -> "Lỗi ${e.code()} khi xóa cửa hàng: ${e.message}"
            }
        }
    }

    // Parse lỗi từ response body cho update quantity
    private fun parseUpdateQuantityError(
        response: UpdateQuantityResponse?,
        statusCode: Int
    ): String {
        return when {
            response?.message?.isNotEmpty() == true -> response.message!!
            response?.errorCode?.isNotEmpty() == true -> {
                when (response.errorCode) {
                    "CART_400" -> "Dữ liệu không hợp lệ"
                    "CART_401" -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
                    "CART_404" -> "Không tìm thấy sản phẩm trong giỏ hàng"
                    "CART_409" -> "Không thể cập nhật số lượng lúc này"
                    else -> "Lỗi ${response.errorCode}"
                }
            }
            else -> getDefaultUpdateQuantityErrorMessage(statusCode)
        }
    }

    // Parse lỗi từ error body cho update quantity
    private fun parseUpdateQuantityErrorResponse(errorBody: String?, statusCode: Int): String {
        return try {
            if (!errorBody.isNullOrEmpty()) {
                val errorResponse = gson.fromJson(errorBody, CartErrorResponse::class.java)
                when {
                    errorResponse.message.isNotEmpty() -> errorResponse.message
                    errorResponse.errorCode.isNotEmpty() -> {
                        when (errorResponse.errorCode) {
                            "CART_400" -> "Dữ liệu không hợp lệ"
                            "CART_401" -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
                            "CART_404" -> "Không tìm thấy sản phẩm trong giỏ hàng"
                            "CART_409" -> "Không thể cập nhật số lượng lúc này"
                            else -> "Lỗi ${errorResponse.errorCode}"
                        }
                    }
                    else -> getDefaultUpdateQuantityErrorMessage(statusCode)
                }
            } else {
                getDefaultUpdateQuantityErrorMessage(statusCode)
            }
        } catch (e: JsonSyntaxException) {
            println("DEBUG: [CartRepository] Failed to parse update quantity error response: ${e.message}")
            getDefaultUpdateQuantityErrorMessage(statusCode)
        }
    }

    // Parse HttpException cho update quantity
    private fun parseHttpExceptionForUpdateQuantity(e: HttpException): String {
        return when (e.code()) {
            400 -> "Dữ liệu không hợp lệ. Số lượng phải từ 1 đến 999"
            401 -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
            404 -> "Không tìm thấy sản phẩm trong giỏ hàng"
            409 -> "Không thể cập nhật số lượng sản phẩm lúc này"
            422 -> "Số lượng không hợp lệ"
            500 -> "Lỗi server khi cập nhật số lượng"
            else -> "Lỗi ${e.code()} khi cập nhật số lượng: ${e.message}"
        }
    }

    // Hàm lấy thông báo lỗi mặc định cho update quantity
    private fun getDefaultUpdateQuantityErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> "Dữ liệu không hợp lệ"
            401 -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
            404 -> "Không tìm thấy sản phẩm trong giỏ hàng"
            409 -> "Không thể cập nhật số lượng lúc này"
            422 -> "Số lượng không hợp lệ"
            500 -> "Lỗi server"
            else -> "Không thể cập nhật số lượng (Lỗi $statusCode)"
        }
    }

    // Hàm parse lỗi từ error body cho clear cart
    private fun parseClearCartError(errorBody: String?, statusCode: Int): String {
        return try {
            if (!errorBody.isNullOrEmpty()) {
                val errorResponse = gson.fromJson(errorBody, ClearCartErrorResponse::class.java)
                when {
                    errorResponse.message.isNotEmpty() -> errorResponse.message
                    errorResponse.errorCode.isNotEmpty() -> "Lỗi ${errorResponse.errorCode}"
                    else -> "Không thể xóa giỏ hàng"
                }
            } else {
                getDefaultErrorMessage(statusCode)
            }
        } catch (e: JsonSyntaxException) {
            println("DEBUG: [CartRepository] Failed to parse error response: ${e.message}")
            getDefaultErrorMessage(statusCode)
        }
    }

    // Hàm parse error response riêng cho remove item
    private fun parseRemoveItemError(errorBody: String?, statusCode: Int): String {
        return try {
            if (!errorBody.isNullOrEmpty()) {
                // Parse thành DeleteItemFromCartResponse
                val errorResponse = gson.fromJson(errorBody, DeleteItemFromCartResponse::class.java)
                when {
                    errorResponse.message.isNotEmpty() -> errorResponse.message
                    errorResponse.errorCode.isNotEmpty() -> when (errorResponse.errorCode) {
                        "CART_401" -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
                        "CART_404" -> "Không tìm thấy sản phẩm trong giỏ hàng"
                        else -> "Lỗi ${errorResponse.errorCode}"
                    }
                    else -> getDefaultRemoveItemErrorMessage(statusCode)
                }
            } else {
                getDefaultRemoveItemErrorMessage(statusCode)
            }
        } catch (e: JsonSyntaxException) {
            println("DEBUG: [CartRepository] Failed to parse remove item error response: ${e.message}")
            getDefaultRemoveItemErrorMessage(statusCode)
        }
    }

    // Hàm parse chi tiết DeleteItemFromCartResponse
    private fun parseDeleteItemErrorResponse(errorBody: String?): String? {
        return try {
            if (!errorBody.isNullOrEmpty()) {
                val errorResponse = gson.fromJson(errorBody, DeleteItemFromCartResponse::class.java)
                if (errorResponse.errorCode == "CART_401") {
                    "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
                } else {
                    errorResponse.message.takeIf { it.isNotEmpty() }
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Hàm parse HttpException cho clear cart
    private fun parseHttpExceptionForClearCart(e: HttpException): String {
        return when (e.code()) {
            401 -> "Vui lòng đăng nhập để xóa giỏ hàng"
            403 -> "Bạn không có quyền xóa giỏ hàng này"
            404 -> "Không tìm thấy giỏ hàng để xóa"
            409 -> "Không thể xóa giỏ hàng trong lúc đang xử lý đơn hàng"
            500 -> "Lỗi server khi xóa giỏ hàng"
            else -> "Lỗi ${e.code()} khi xóa giỏ hàng: ${e.message}"
        }
    }

    // Hàm lấy thông báo lỗi mặc định dựa trên status code
    private fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            401 -> "Vui lòng đăng nhập để xóa giỏ hàng"
            403 -> "Bạn không có quyền xóa giỏ hàng này"
            404 -> "Không tìm thấy giỏ hàng"
            409 -> "Không thể xóa giỏ hàng lúc này"
            500 -> "Lỗi server"
            else -> "Không thể xóa giỏ hàng (Lỗi $statusCode)"
        }
    }

    // Hàm lấy thông báo lỗi mặc định cho remove item
    private fun getDefaultRemoveItemErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            401 -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
            403 -> "Bạn không có quyền xóa sản phẩm này"
            404 -> "Không tìm thấy sản phẩm trong giỏ hàng"
            409 -> "Không thể xóa sản phẩm lúc này"
            500 -> "Lỗi server"
            else -> "Không thể xóa sản phẩm (Lỗi $statusCode)"
        }
    }
}