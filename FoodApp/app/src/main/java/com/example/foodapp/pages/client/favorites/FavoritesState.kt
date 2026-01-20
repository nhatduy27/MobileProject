package com.example.foodapp.pages.client.favorites

import com.example.foodapp.data.model.shared.product.Product

sealed class FavoritesState {
    object Idle : FavoritesState()
    object Loading : FavoritesState()
    data class Success(val products: List<Product>) : FavoritesState()
    data class Error(val message: String) : FavoritesState()
}

sealed class RemoveFavoriteState {
    object Idle : RemoveFavoriteState()
    object Loading : RemoveFavoriteState()
    data class Success(val message: String) : RemoveFavoriteState()
    data class Error(val message: String) : RemoveFavoriteState()
}