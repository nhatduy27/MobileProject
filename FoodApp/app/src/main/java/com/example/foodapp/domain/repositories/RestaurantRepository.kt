package com.example.foodapp.domain.repositories

import com.example.foodapp.domain.entities.Restaurant
import com.example.foodapp.domain.entities.MenuItem

interface RestaurantRepository {
    suspend fun getRestaurants(): List<Restaurant>
    suspend fun getRestaurantDetail(id: String): Restaurant
    suspend fun getMenuItems(restaurantId: String): List<MenuItem>
}
