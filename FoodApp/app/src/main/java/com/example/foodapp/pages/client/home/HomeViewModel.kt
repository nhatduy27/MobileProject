package com.example.foodapp.pages.client.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.model.shared.category.Category
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.response.product.ProductFilterDto
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.remote.shared.response.ApiResult as SharedApiResult // Alias để phân biệt
import com.example.foodapp.data.remote.client.response.product.ApiResult as ProductApiResult // Alias
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.example.foodapp.data.repository.client.products.ProductRepository
import com.example.foodapp.data.repository.shared.CategoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============== PRODUCT STATES ==============

sealed class ProductState {
    object Idle : ProductState()
    object Loading : ProductState()
    data class Success(val products: List<Product>) : ProductState()
    data class Error(val message: String) : ProductState()
    object Empty : ProductState()
}

sealed class UserNameState {
    object Idle : UserNameState()
    object Loading : UserNameState()
    data class Success(val userName: String) : UserNameState()
    data class Error(val message: String) : UserNameState()
    object Empty : UserNameState()
}

sealed class CategoryState {
    object Idle : CategoryState()
    object Loading : CategoryState()
    data class Success(val categories: List<Category>) : CategoryState()
    data class Error(val message: String) : CategoryState()
}

// ============== HOME VIEW MODEL ==============

class HomeViewModel(
    private val userRepository: UserFirebaseRepository,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _userNameState = MutableLiveData<UserNameState>(UserNameState.Idle)
    val userNameState: LiveData<UserNameState> = _userNameState

    private val _productState = MutableLiveData<ProductState>(ProductState.Idle)
    val productState: LiveData<ProductState> = _productState

    private val _products = MutableLiveData<List<Product>>(emptyList())
    val products: LiveData<List<Product>> = _products

    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int> = _currentPage

    private val _hasMore = MutableLiveData(true)
    val hasMore: LiveData<Boolean> = _hasMore

    private val _currentFilters = MutableLiveData<ProductFilterDto>(ProductFilterDto())
    val currentFilters: LiveData<ProductFilterDto> = _currentFilters

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    // Thêm search query và search job
    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _categoryState = MutableLiveData<CategoryState>(CategoryState.Idle)
    val categoryState: LiveData<CategoryState> = _categoryState

    private val _categories = MutableLiveData<List<Category>>(emptyList())
    val categories: LiveData<List<Category>> = _categories

    private var searchJob: Job? = null

    // ============== USER NAME FUNCTIONS ==============

    fun fetchUserName() {
        _userNameState.value = UserNameState.Loading
        userRepository.getCurrentUserName { name ->
            if (!name.isNullOrBlank()) {
                _userNameState.postValue(UserNameState.Success(name))
            } else {
                _userNameState.postValue(UserNameState.Error("Không tìm thấy người dùng"))
            }
        }
    }

    fun clearUserName() {
        _userNameState.value = UserNameState.Empty
    }

    // ============== PRODUCT FUNCTIONS ==============

    fun getProducts(
        filters: ProductFilterDto = ProductFilterDto(),
        forceRefresh: Boolean = false
    ) {
        if (forceRefresh) {
            _currentPage.value = 1
            _hasMore.value = true
        }

        _productState.value = ProductState.Loading
        _currentFilters.value = filters

        viewModelScope.launch {
            try {
                val result = productRepository.getProducts(filters)
                println("DEBUG: [ViewModel] Result type: ${result::class.simpleName}")
                println("DEBUG: [ViewModel] Is Success: ${result is ProductApiResult.Success<*>}")
                println("DEBUG: [ViewModel] Is Failure: ${result is ProductApiResult.Failure}")

                when (result) {
                    is ProductApiResult.Success<*> -> {
                        println("DEBUG: [ViewModel] Entered Success branch")
                        val products = (result.data as? List<Product>) ?: emptyList()
                        println("DEBUG: [ViewModel] Products count: ${products.size}")

                        _products.value = products

                        if (products.isNotEmpty()) {
                            _productState.value = ProductState.Success(products)
                            _hasMore.value = products.size >= filters.limit
                            println("DEBUG: [ViewModel] State set to Success")
                        } else {
                            _productState.value = ProductState.Empty
                            _hasMore.value = false
                            println("DEBUG: [ViewModel] State set to Empty")
                        }
                        _currentPage.value = 1
                    }
                    is ProductApiResult.Failure -> {
                        println("DEBUG: [ViewModel] Entered Failure branch: ${result.exception.message}")
                        _productState.value = ProductState.Error(
                            result.exception.message ?: "Lỗi không xác định"
                        )
                        _hasMore.value = false
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: [ViewModel] Exception in getProducts: ${e.message}")
                e.printStackTrace()
                _productState.value = ProductState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    fun loadMoreProducts() {
        val currentProducts = _products.value ?: emptyList()
        val currentPage = _currentPage.value ?: 1
        val filters = _currentFilters.value ?: ProductFilterDto()

        if (_isLoadingMore.value == true || _hasMore.value == false) return

        _isLoadingMore.value = true
        val nextPage = currentPage + 1

        viewModelScope.launch {
            try {
                val nextFilters = filters.copy(page = nextPage)
                val result = productRepository.getProducts(nextFilters)
                println("DEBUG: [ViewModel] Load more result type: ${result::class.simpleName}")

                when (result) {
                    is ProductApiResult.Success<*> -> {
                        val newProducts = (result.data as? List<Product>) ?: emptyList()
                        println("DEBUG: [ViewModel] Load more new products count: ${newProducts.size}")

                        if (newProducts.isNotEmpty()) {
                            val updatedList = currentProducts + newProducts
                            _products.value = updatedList
                            _currentPage.value = nextPage
                            _hasMore.value = newProducts.size >= filters.limit
                            _productState.value = ProductState.Success(updatedList)
                            println("DEBUG: [ViewModel] Load more successful")
                        } else {
                            _hasMore.value = false
                            println("DEBUG: [ViewModel] No more products to load")
                        }
                    }
                    is ProductApiResult.Failure -> {
                        println("Load more failed: ${result.exception.message}")
                        _hasMore.value = false
                    }
                }
            } catch (e: Exception) {
                println("Load more exception: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun filterByCategory(categoryId: String?) {
        println("DEBUG: [ViewModel] Filter by category: $categoryId")
        val currentFilters = _currentFilters.value ?: ProductFilterDto()
        val newFilters = currentFilters.copy(
            categoryId = categoryId,
            page = 1
        )
        getProducts(newFilters)
    }

    // Cải tiến hàm search với debounce
    fun searchProducts(query: String) {
        println("DEBUG: [ViewModel] Search products: $query")

        // Cập nhật search query
        _searchQuery.value = query

        // Hủy search job cũ nếu có
        searchJob?.cancel()

        // Tạo search job mới với debounce 300ms
        searchJob = viewModelScope.launch {
            delay(300) // Debounce để tránh gọi API liên tục khi gõ

            val currentFilters = _currentFilters.value ?: ProductFilterDto()
            val newFilters = currentFilters.copy(
                searchQuery = query.ifBlank { null },
                page = 1
            )
            getProducts(newFilters)
        }
    }

    // Thêm hàm clear search
    fun clearSearch() {
        _searchQuery.value = ""
        val currentFilters = _currentFilters.value ?: ProductFilterDto()
        val newFilters = currentFilters.copy(
            searchQuery = null,
            page = 1
        )
        getProducts(newFilters)
    }

    // ============== CATEGORY FUNCTIONS ==============

    fun fetchCategories() {
        _categoryState.value = CategoryState.Loading
        viewModelScope.launch {
            val result = categoryRepository.getCategories()
            when (result) {
                is SharedApiResult.Success<*> -> {
                    val categories = (result.data as? List<Category>) ?: emptyList()
                    _categories.value = categories
                    _categoryState.value = CategoryState.Success(categories)
                    println("DEBUG: [ViewModel] Fetched ${categories.size} categories")
                }
                is SharedApiResult.Failure -> {
                    _categoryState.value = CategoryState.Error(
                        result.exception.message ?: "Lỗi tải danh mục"
                    )
                    println("DEBUG: [ViewModel] Category fetch failed: ${result.exception.message}")
                }
            }
        }
    }

    // Cập nhật refresh function
    fun refresh() {
        println("DEBUG: [ViewModel] Refresh called")
        getProducts(_currentFilters.value ?: ProductFilterDto(), forceRefresh = true)
        fetchUserName()
        fetchCategories()
    }

    fun getProductById(productId: String, onResult: (Product?) -> Unit) {
        viewModelScope.launch {
            try {
                val product = _products.value?.find { it.id == productId }
                onResult(product)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    fun reset() {
        _products.value = emptyList()
        _productState.value = ProductState.Idle
        _currentPage.value = 1
        _hasMore.value = true
        _currentFilters.value = ProductFilterDto()
        _isLoadingMore.value = false
        _searchQuery.value = ""
        _categories.value = emptyList()
        _categoryState.value = CategoryState.Idle
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val userRepository = UserFirebaseRepository(context)
                val apiService = ApiClient.productApiService
                val productRepository = ProductRepository()
                val categoryRepository = CategoryRepository()
                HomeViewModel(userRepository, productRepository, categoryRepository)
            }
        }
    }
}