package com.example.foodapp.pages.owner.foods

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.owner.product.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel cho màn hình quản lý sản phẩm.
 * Sử dụng Real Repository để gọi API backend.
 * 
 * Hỗ trợ nhiều ảnh thay vì chỉ 1 ảnh.
 */
class FoodsViewModel : ViewModel() {

    companion object {
        private const val TAG = "FoodsViewModel"
    }

    // Repository
    private val productRepository = RepositoryProvider.getProductRepository()
    private val categoryRepository = RepositoryProvider.getCategoryRepository()

    // UI State
    private val _uiState = MutableStateFlow(FoodUiState())
    val uiState: StateFlow<FoodUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        loadCategories()
    }

    // ==================== LOAD DATA ====================

    /**
     * Load danh sách sản phẩm từ API
     */
    fun loadProducts(refresh: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value

            _uiState.update {
                it.copy(
                    isLoading = !refresh,
                    isRefreshing = refresh,
                    errorMessage = null
                )
            }

            val result = productRepository.getProducts(
                categoryId = currentState.selectedCategoryId,
                isAvailable = currentState.filterAvailability,
                page = if (refresh) 1 else currentState.currentPage
            )

            result.onSuccess { data ->
                Log.d(TAG, "✅ Loaded ${data.products.size} products")
                _uiState.update {
                    it.copy(
                        products = data.products,
                        totalProducts = data.total,
                        currentPage = data.page,
                        hasMore = data.products.size < data.total,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to load products", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "Không thể tải danh sách sản phẩm: ${error.message}"
                    )
                }
            }
        }
    }

    /**
     * Refresh danh sách sản phẩm
     */
    fun refreshProducts() {
        loadProducts(refresh = true)
    }

    /**
     * Load danh sách categories từ API
     */
    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(categoriesLoading = true) }

            val result = categoryRepository.getCategories()

            result.onSuccess { categories ->
                val categoryItems = listOf(CategoryItem(null, "Tất cả")) +
                    categories.map { CategoryItem(it.id, it.name) }

                _uiState.update {
                    it.copy(
                        categories = categoryItems,
                        categoriesLoading = false
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to load categories", error)
                _uiState.update {
                    it.copy(
                        categories = listOf(CategoryItem(null, "Tất cả")),
                        categoriesLoading = false
                    )
                }
            }
        }
    }

    // ==================== FILTER & SEARCH ====================

    /**
     * Lọc theo category
     */
    fun onCategorySelected(categoryItem: CategoryItem) {
        _uiState.update {
            it.copy(
                selectedCategoryId = categoryItem.id,
                selectedCategoryName = categoryItem.name
            )
        }
        loadProducts(refresh = true)
    }

    /**
     * Lọc theo availability
     */
    fun onAvailabilityFilterChanged(isAvailable: Boolean?) {
        _uiState.update { it.copy(filterAvailability = isAvailable) }
        loadProducts(refresh = true)
    }

    /**
     * Tìm kiếm theo tên (local filter)
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Lấy danh sách sản phẩm đã lọc theo search query
     */
    fun getFilteredProducts(): List<Product> {
        val state = _uiState.value
        val query = state.searchQuery.trim()

        return if (query.isBlank()) {
            state.products
        } else {
            state.products.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
    }

    // ==================== STATISTICS ====================

    /**
     * Lấy thống kê sản phẩm
     */
    fun getStats(): ProductStats {
        val products = _uiState.value.products
        return ProductStats(
            total = products.size,
            available = products.count { it.isAvailable },
            outOfStock = products.count { !it.isAvailable }
        )
    }

    // ==================== CREATE/UPDATE/DELETE ====================

    /**
     * Tạo sản phẩm mới với nhiều ảnh
     * @param imageFiles List các file ảnh (ít nhất 1 ảnh)
     */
    fun createProduct(
        name: String,
        description: String,
        price: Double,
        categoryId: String,
        preparationTime: Int,
        imageFiles: List<File>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }

            val request = CreateProductRequest(
                name = name,
                description = description,
                price = price,
                categoryId = categoryId,
                preparationTime = preparationTime
            )

            val result = productRepository.createProduct(request, imageFiles)

            result.onSuccess { product ->
                Log.d(TAG, "✅ Created product: ${product.name}")
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        successMessage = "Đã thêm sản phẩm: ${product.name}"
                    )
                }
                loadProducts(refresh = true)
                onSuccess()
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to create product", error)
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        errorMessage = "Không thể tạo sản phẩm: ${error.message}"
                    )
                }
                onError(error.message ?: "Unknown error")
            }
        }
    }

    /**
     * Cập nhật sản phẩm với nhiều ảnh
     * @param imageFiles List các file ảnh mới (null = giữ ảnh cũ)
     */
    fun updateProduct(
        productId: String,
        name: String? = null,
        description: String? = null,
        price: Double? = null,
        categoryId: String? = null,
        preparationTime: Int? = null,
        imageFiles: List<File>? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, errorMessage = null) }

            val request = UpdateProductRequest(
                name = name,
                description = description,
                price = price,
                categoryId = categoryId,
                preparationTime = preparationTime
            )

            val result = productRepository.updateProduct(productId, request, imageFiles)

            result.onSuccess { message ->
                Log.d(TAG, "✅ Updated product: $message")
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        successMessage = message
                    )
                }
                loadProducts(refresh = true)
                onSuccess()
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to update product", error)
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        errorMessage = "Không thể cập nhật sản phẩm: ${error.message}"
                    )
                }
                onError(error.message ?: "Unknown error")
            }
        }
    }

    /**
     * Toggle trạng thái còn hàng/hết hàng
     */
    fun toggleAvailability(productId: String, currentAvailability: Boolean) {
        viewModelScope.launch {
            val result = productRepository.toggleAvailability(productId, !currentAvailability)

            result.onSuccess { message ->
                Log.d(TAG, "✅ Toggled availability: $message")
                // Update local state immediately for better UX
                _uiState.update { state ->
                    state.copy(
                        products = state.products.map {
                            if (it.id == productId) it.copy(isAvailable = !it.isAvailable)
                            else it
                        },
                        successMessage = message
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to toggle availability", error)
                _uiState.update {
                    it.copy(errorMessage = "Không thể cập nhật trạng thái: ${error.message}")
                }
            }
        }
    }

    /**
     * Xóa sản phẩm
     */
    fun deleteProduct(productId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }

            val result = productRepository.deleteProduct(productId)

            result.onSuccess { message ->
                Log.d(TAG, "✅ Deleted product: $message")
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        successMessage = message,
                        // Remove from local list immediately
                        products = it.products.filter { p -> p.id != productId }
                    )
                }
                onSuccess()
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to delete product", error)
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = "Không thể xóa sản phẩm: ${error.message}"
                    )
                }
            }
        }
    }

    // ==================== UI HELPERS ====================

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Clear success message
     */
    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    /**
     * Set editing product
     */
    fun setEditingProduct(product: Product?) {
        _uiState.update { it.copy(editingProduct = product) }
    }
}
