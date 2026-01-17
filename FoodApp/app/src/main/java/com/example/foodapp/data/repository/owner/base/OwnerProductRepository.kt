package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.product.*
import java.io.File

/**
 * Interface cho Owner Products Repository.
 * Định nghĩa các phương thức để quản lý sản phẩm của cửa hàng.
 */
interface OwnerProductRepository {

    /**
     * Lấy danh sách sản phẩm của cửa hàng
     * @param categoryId Lọc theo category (optional)
     * @param isAvailable Lọc theo trạng thái còn hàng (optional)
     * @param page Số trang (default 1)
     * @param limit Số lượng mỗi trang (default 50)
     */
    suspend fun getProducts(
        categoryId: String? = null,
        isAvailable: Boolean? = null,
        page: Int = 1,
        limit: Int = 50
    ): Result<ProductsData>

    /**
     * Lấy chi tiết sản phẩm
     * @param productId ID sản phẩm
     */
    suspend fun getProductDetail(productId: String): Result<Product>

    /**
     * Tạo sản phẩm mới
     * @param request Thông tin sản phẩm
     * @param imageFile File ảnh sản phẩm
     */
    suspend fun createProduct(
        request: CreateProductRequest,
        imageFile: File
    ): Result<Product>

    /**
     * Cập nhật sản phẩm
     * @param productId ID sản phẩm
     * @param request Thông tin cập nhật
     * @param imageFile File ảnh mới (optional)
     */
    suspend fun updateProduct(
        productId: String,
        request: UpdateProductRequest,
        imageFile: File? = null
    ): Result<String>

    /**
     * Toggle trạng thái còn hàng/hết hàng
     * @param productId ID sản phẩm
     * @param isAvailable Trạng thái mới
     */
    suspend fun toggleAvailability(
        productId: String,
        isAvailable: Boolean
    ): Result<String>

    /**
     * Xóa sản phẩm (soft delete)
     * @param productId ID sản phẩm
     */
    suspend fun deleteProduct(productId: String): Result<String>

    /**
     * Upload ảnh sản phẩm
     * @param productId ID sản phẩm
     * @param imageFile File ảnh
     */
    suspend fun uploadProductImage(
        productId: String,
        imageFile: File
    ): Result<String>
}
