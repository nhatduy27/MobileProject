// com.example.foodapp.pages.client.favorites.FavoritesViewModel.kt
package com.example.foodapp.pages.client.favorites

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.response.product.ApiResult
import com.example.foodapp.data.remote.client.response.product.toProductList
import com.example.foodapp.data.repository.client.products.ProductRepository
import kotlinx.coroutines.launch


class FavoritesViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _favoritesState = MutableLiveData<FavoritesState>(FavoritesState.Idle)
    val favoritesState: LiveData<FavoritesState> = _favoritesState

    // State cho việc xóa yêu thích
    private val _removeFavoriteState = MutableLiveData<RemoveFavoriteState>(RemoveFavoriteState.Idle)
    val removeFavoriteState: LiveData<RemoveFavoriteState> = _removeFavoriteState

    // Current favorites data
    private val _currentFavorites = MutableLiveData<List<Product>>(emptyList())
    val currentFavorites: LiveData<List<Product>> = _currentFavorites

    // Map lưu trạng thái yêu thích của từng sản phẩm
    private val _favoriteStatusMap = MutableLiveData<Map<String, Boolean>>(emptyMap())
    val favoriteStatusMap: LiveData<Map<String, Boolean>> = _favoriteStatusMap

    // Load danh sách yêu thích ngay khi vào
    init {
        fetchFavorites()
    }

    // User refresh thủ công
    fun refreshFavorites() {
        fetchFavorites()
    }

    // Lấy danh sách yêu thích từ database
    fun fetchFavorites() {
        viewModelScope.launch {
            _favoritesState.value = FavoritesState.Loading

            when (val result = productRepository.getFavoriteProducts(1, 20)) {
                is ApiResult.Success -> {
                    val response = result.data
                    val products = response.toProductList()

                    _currentFavorites.value = products
                    _favoritesState.value = FavoritesState.Success(products)

                    // Cập nhật trạng thái yêu thích cho tất cả sản phẩm
                    updateFavoriteStatusForProducts(products)
                }
                is ApiResult.Failure -> {
                    _favoritesState.value = FavoritesState.Error(
                        result.exception.message ?: "Không thể tải danh sách yêu thích"
                    )
                    _currentFavorites.value = emptyList()
                }
            }
        }
    }

    // Cập nhật trạng thái yêu thích cho danh sách sản phẩm
    private fun updateFavoriteStatusForProducts(products: List<Product>) {
        viewModelScope.launch {
            val statusMap = mutableMapOf<String, Boolean>()

            products.forEach { product ->
                when (val result = productRepository.checkIsFavorite(product.id)) {
                    is ApiResult.Success -> {
                        statusMap[product.id] = result.data
                    }
                    is ApiResult.Failure -> {
                        // Nếu lỗi, mặc định là true vì sản phẩm đã trong danh sách yêu thích
                        statusMap[product.id] = true
                    }
                }
            }

            _favoriteStatusMap.value = statusMap
        }
    }

    // Kiểm tra sản phẩm có trong danh sách yêu thích không
    fun checkProductFavorite(productId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            when (val result = productRepository.checkIsFavorite(productId)) {
                is ApiResult.Success -> {
                    onResult(result.data)
                    // Cập nhật vào map
                    updateFavoriteStatus(productId, result.data)
                }
                is ApiResult.Failure -> {
                    // Nếu lỗi API, kiểm tra trong danh sách local
                    val isInFavorites = _currentFavorites.value?.any { it.id == productId } ?: false
                    onResult(isInFavorites)
                    updateFavoriteStatus(productId, isInFavorites)
                }
            }
        }
    }

    // Cập nhật trạng thái yêu thích của một sản phẩm
    private fun updateFavoriteStatus(productId: String, isFavorite: Boolean) {
        val currentMap = _favoriteStatusMap.value?.toMutableMap() ?: mutableMapOf()
        currentMap[productId] = isFavorite
        _favoriteStatusMap.value = currentMap
    }

    // Xóa sản phẩm khỏi yêu thích
    fun removeFromFavorites(productId: String) {
        viewModelScope.launch {
            _removeFavoriteState.value = RemoveFavoriteState.Loading

            when (val result = productRepository.removeFromFavorites(productId)) {
                is ApiResult.Success -> {
                    if (result.data.isSuccess) {
                        // Cập nhật danh sách local
                        val currentList = _currentFavorites.value ?: emptyList()
                        val updatedList = currentList.filter { it.id != productId }

                        _currentFavorites.value = updatedList
                        _favoritesState.value = FavoritesState.Success(updatedList)

                        // Cập nhật trạng thái yêu thích
                        updateFavoriteStatus(productId, false)

                        _removeFavoriteState.value = RemoveFavoriteState.Success(
                            result.data.message.takeIf { it.isNotBlank() } ?: "Đã xóa khỏi yêu thích"
                        )
                    } else {
                        _removeFavoriteState.value = RemoveFavoriteState.Error(
                            result.data.message.takeIf { it.isNotBlank() } ?: "Không thể xóa khỏi yêu thích"
                        )
                    }
                }
                is ApiResult.Failure -> {
                    val errorMessage = result.exception.message ?: "Không thể xóa khỏi yêu thích"
                    _removeFavoriteState.value = RemoveFavoriteState.Error(errorMessage)
                }
            }
        }
    }

    // Reset remove favorite state
    fun resetRemoveFavoriteState() {
        _removeFavoriteState.value = RemoveFavoriteState.Idle
    }

    // Factory cho ViewModel
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
                val apiService = ApiClient.productApiService
                val productRepository = ProductRepository()
                return FavoritesViewModel(productRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }

        companion object {
            @Volatile
            private var INSTANCE: Factory? = null
            fun getInstance(context: Context): Factory {
                return INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Factory(context.applicationContext).also {
                        INSTANCE = it
                    }
                }
            }
        }
    }
}