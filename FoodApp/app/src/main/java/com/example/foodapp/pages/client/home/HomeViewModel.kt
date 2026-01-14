package com.example.foodapp.pages.client.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.model.client.product.ProductFilterDto
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.model.client.product.ApiResult
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.example.foodapp.data.repository.product.ProductRepository
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

// ============== HOME VIEW MODEL ==============

class HomeViewModel(
    private val userRepository: UserFirebaseRepository,
    private val productRepository: ProductRepository
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
                println("DEBUG: [ViewModel] Is Success: ${result is ApiResult.Success<*>}")
                println("DEBUG: [ViewModel] Is Failure: ${result is ApiResult.Failure}")

                when (result) {
                    is ApiResult.Success<*> -> {
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
                    is ApiResult.Failure -> {
                        println("DEBUG: [ViewModel] Entered Failure branch: ${result.exception.message}")
                        _productState.value = ProductState.Error(
                            result.exception.message ?: "Lỗi không xác định"
                        )
                        _hasMore.value = false
                    }
                    // XÓA else branch - chỉ để lại 2 case trên
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
                    is ApiResult.Success<*> -> {
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
                    is ApiResult.Failure -> {
                        println("Load more failed: ${result.exception.message}")
                        _hasMore.value = false
                    }
                    // XÓA else branch
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

    fun searchProducts(query: String) {
        println("DEBUG: [ViewModel] Search products: $query")
        val currentFilters = _currentFilters.value ?: ProductFilterDto()
        val newFilters = currentFilters.copy(
            searchQuery = query.ifBlank { null },
            page = 1
        )
        getProducts(newFilters)
    }

    fun refresh() {
        println("DEBUG: [ViewModel] Refresh called")
        getProducts(_currentFilters.value ?: ProductFilterDto(), forceRefresh = true)
        fetchUserName()
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
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val userRepository = UserFirebaseRepository(context)
                val apiService = ApiClient.productApiService
                val productRepository = ProductRepository(apiService)
                HomeViewModel(userRepository, productRepository)
            }
        }
    }
}