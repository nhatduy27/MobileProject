package com.example.foodapp.pages.owner.foods

import com.example.foodapp.data.model.owner.product.Product

/**
 * UI State cho màn hình quản lý sản phẩm (FoodsScreen)
 *
 * Sử dụng Product model từ backend thay vì Food model cũ.
 */
data class FoodUiState(
    // Danh sách sản phẩm từ API
    val products: List<Product> = emptyList(),

    // Tổng số sản phẩm (từ API pagination)
    val totalProducts: Int = 0,

    // Bộ lọc category đang được chọn
    val selectedCategoryId: String? = null,
    val selectedCategoryName: String = "Tất cả",

    // Bộ lọc availability
    val filterAvailability: Boolean? = null,

    // Trạng thái loading
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,

    // Thông báo lỗi
    val errorMessage: String? = null,

    // Thông báo thành công
    val successMessage: String? = null,

    // Hiển thị dialog thêm/sửa
    val showAddDialog: Boolean = false,
    val editingProduct: Product? = null,

    // Query tìm kiếm
    val searchQuery: String = "",

    // Danh sách categories từ API (id -> name)
    val categories: List<CategoryItem> = listOf(CategoryItem(null, "Tất cả")),
    val categoriesLoading: Boolean = false,

    // Pagination
    val currentPage: Int = 1,
    val hasMore: Boolean = false,

    // Trạng thái các thao tác
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false
)

/**
 * Category item cho dropdown
 */
data class CategoryItem(
    val id: String?,
    val name: String
)

/**
 * Các thống kê đã tính toán
 */
data class ProductStats(
    val total: Int = 0,
    val available: Int = 0,
    val outOfStock: Int = 0
)
