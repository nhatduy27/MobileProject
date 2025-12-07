package com.example.foodapp.data.repositories

import com.example.foodapp.domain.entities.Restaurant
import com.example.foodapp.domain.entities.MenuItem
import com.example.foodapp.domain.repositories.RestaurantRepository
import com.example.foodapp.data.remote.FirestoreRestaurantDataSource
import com.example.foodapp.data.mapper.RestaurantMapper
import com.example.foodapp.data.mapper.MenuItemMapper

class RestaurantRepositoryImpl(
    private val firestoreRestaurantDataSource: FirestoreRestaurantDataSource
) : RestaurantRepository {
    
    override suspend fun getRestaurants(): List<Restaurant> {
        return firestoreRestaurantDataSource.getRestaurants()
    }
    
    override suspend fun getRestaurantDetail(id: String): Restaurant {
        return firestoreRestaurantDataSource.getRestaurantDetail(id)
    }
    
    override suspend fun getMenuItems(restaurantId: String): List<MenuItem> {
        return firestoreRestaurantDataSource.getMenuItems(restaurantId)
    }
}
